package com.samiuysal.fediversehub.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostActionType
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonReplyComposeState
import com.samiuysal.fediversehub.feature.mastodon.canSend
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mastodonRepository: MastodonRepository,
    private val lemmyRepository: LemmyRepository,
    private val accountStore: AccountStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MockFediverseData.homeState)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()
    private val _mastodonActionOverrides = MutableStateFlow<Map<String, MastodonPostUiModel>>(emptyMap())
    val mastodonActionOverrides: StateFlow<Map<String, MastodonPostUiModel>> =
        _mastodonActionOverrides.asStateFlow()
    private val _replyComposeState = MutableStateFlow<MastodonReplyComposeState?>(null)
    val replyComposeState: StateFlow<MastodonReplyComposeState?> = _replyComposeState.asStateFlow()

    val mastodonTimeline: Flow<PagingData<MastodonPostUiModel>> =
        uiState
            .map { state ->
                state.accounts.firstOrNull { it.platform == PlatformType.MASTODON }
                    ?: fallbackAccount(PlatformType.MASTODON)
            }
            .distinctUntilChanged()
            .flatMapLatest { account ->
                mastodonRepository
                    .getHomeTimelinePagingData(account = account)
                    .map { pagingData ->
                        pagingData.map(MastodonTimelineMapper::domainToUi)
                    }
                }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    val lemmyPosts: Flow<PagingData<LemmyPostUiModel>> =
        lemmyRepository
            .getPostsPagingData(
                account = _uiState.value.accounts.first { it.platform == PlatformType.LEMMY },
                sort = LemmySortType.HOT,
            )
            .map { pagingData ->
                pagingData.map(LemmyPostMapper::domainToUi)
            }
            .cachedIn(viewModelScope)

    init {
        accountStore.accounts
            .onEach { storedAccounts ->
                _uiState.update { state ->
                    state.copy(accounts = mergeAccounts(storedAccounts))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.PlatformSelected -> selectPlatform(event.platform)
        }
    }

    fun onMastodonAction(
        post: MastodonPostUiModel,
        action: MastodonPostActionType,
    ) {
        if (action == MastodonPostActionType.REPLY) {
            openReplyCompose(post)
            return
        }
        val account = _uiState.value.accounts.firstOrNull { it.platform == PlatformType.MASTODON }
            ?: return
        val before = _mastodonActionOverrides.value[post.detailId] ?: post
        val optimistic = before.optimistic(action).copy(loadingAction = action)
        setMastodonOverride(optimistic)
        viewModelScope.launch {
            val result = when (action) {
                MastodonPostActionType.REPLY -> return@launch
                MastodonPostActionType.BOOST -> mastodonRepository.setBoosted(
                    account = account,
                    postId = post.detailId,
                    boosted = optimistic.isBoosted,
                )
                MastodonPostActionType.FAVOURITE -> mastodonRepository.setFavourite(
                    account = account,
                    postId = post.detailId,
                    favourite = optimistic.isFavourited,
                )
                MastodonPostActionType.BOOKMARK -> mastodonRepository.setBookmarked(
                    account = account,
                    postId = post.detailId,
                    bookmarked = optimistic.isBookmarked,
                )
            }
            when (result) {
                is AppResult.Success -> {
                    val confirmed = MastodonTimelineMapper.domainToUi(result.data)
                    setMastodonOverride(optimistic.withActionStateFrom(confirmed).copy(loadingAction = null))
                }
                is AppResult.Failure -> {
                    setMastodonOverride(before.copy(loadingAction = null))
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToMastodonLogin)
                    }
                }
            }
        }
    }

    fun onReplyTextChanged(text: String) {
        _replyComposeState.update { state ->
            state?.copy(text = text, errorMessage = null)
        }
    }

    fun dismissReplyCompose() {
        if (_replyComposeState.value?.isSending == true) return
        _replyComposeState.value = null
    }

    fun submitReply() {
        val state = _replyComposeState.value ?: return
        if (!state.canSend) {
            _replyComposeState.value = state.copy(errorMessage = "Reply needs more than a mention.")
            return
        }
        val account = _uiState.value.accounts.firstOrNull { it.platform == PlatformType.MASTODON }
            ?: return
        _replyComposeState.value = state.copy(isSending = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = mastodonRepository.replyToPost(
                    account = account,
                    postId = state.parent.detailId,
                    text = state.text.trim(),
                    visibility = state.parent.visibility.ifBlank { "public" },
                )
            ) {
                is AppResult.Success -> {
                    val currentParent = _mastodonActionOverrides.value[state.parent.detailId] ?: state.parent
                    setMastodonOverride(
                        currentParent.copy(
                            replies = (currentParent.replies + 1).coerceAtLeast(0),
                            loadingAction = null,
                        ),
                    )
                    _replyComposeState.value = null
                }
                is AppResult.Failure -> {
                    _replyComposeState.value = state.copy(
                        isSending = false,
                        errorMessage = result.error.replyErrorMessage(),
                    )
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToMastodonLogin)
                    }
                }
            }
        }
    }

    private fun selectPlatform(platform: PlatformType) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }

    private fun mergeAccounts(storedAccounts: List<Account>): List<Account> {
        val fallbackAccounts = MockFediverseData.homeState.accounts
            .filterNot { fallback ->
                storedAccounts.any { it.platform == fallback.platform }
            }
        return storedAccounts + fallbackAccounts
    }

    private fun fallbackAccount(platform: PlatformType): Account =
        MockFediverseData.homeState.accounts.first { it.platform == platform }

    private fun setMastodonOverride(post: MastodonPostUiModel) {
        _mastodonActionOverrides.update { overrides ->
            overrides + (post.id to post) + (post.detailId to post)
        }
    }

    private fun openReplyCompose(post: MastodonPostUiModel) {
        val mention = post.username.takeIf { it.startsWith("@") } ?: "@${post.username}"
        _replyComposeState.value = MastodonReplyComposeState(
            parent = post,
            text = "$mention ",
        )
    }

    private fun AppError.replyErrorMessage(): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again to reply."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> message ?: "Reply could not be sent. Try again."
    }

    private fun MastodonPostUiModel.optimistic(action: MastodonPostActionType): MastodonPostUiModel =
        when (action) {
            MastodonPostActionType.REPLY -> this
            MastodonPostActionType.BOOST -> copy(
                isBoosted = !isBoosted,
                boosts = (boosts + if (!isBoosted) 1 else -1).coerceAtLeast(0),
            )
            MastodonPostActionType.FAVOURITE -> copy(
                isFavourited = !isFavourited,
                favourites = (favourites + if (!isFavourited) 1 else -1).coerceAtLeast(0),
            )
            MastodonPostActionType.BOOKMARK -> copy(isBookmarked = !isBookmarked)
        }

    private fun MastodonPostUiModel.withActionStateFrom(
        confirmed: MastodonPostUiModel,
    ): MastodonPostUiModel = copy(
        replies = confirmed.replies,
        boosts = confirmed.boosts,
        favourites = confirmed.favourites,
        isBoosted = confirmed.isBoosted,
        isFavourited = confirmed.isFavourited,
        isBookmarked = confirmed.isBookmarked,
    )
}

sealed interface HomeEffect {
    data object NavigateToMastodonLogin : HomeEffect
}
