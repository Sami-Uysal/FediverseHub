package com.samiuysal.fediversehub.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import android.util.Log
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostActionType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostActionType
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonNewPostComposeState
import com.samiuysal.fediversehub.feature.mastodon.MastodonReplyComposeState
import com.samiuysal.fediversehub.feature.mastodon.MastodonVisibility
import com.samiuysal.fediversehub.feature.mastodon.canSend
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedCommentsState
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
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
    private val pixelfedRepository: PixelfedRepository,
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
    private val _newPostComposeState = MutableStateFlow<MastodonNewPostComposeState?>(null)
    val newPostComposeState: StateFlow<MastodonNewPostComposeState?> = _newPostComposeState.asStateFlow()
    private val _pixelfedActionOverrides = MutableStateFlow<Map<String, PixelfedPostUiModel>>(emptyMap())
    val pixelfedActionOverrides: StateFlow<Map<String, PixelfedPostUiModel>> =
        _pixelfedActionOverrides.asStateFlow()
    private val _pixelfedCommentsState = MutableStateFlow<PixelfedCommentsState?>(null)
    val pixelfedCommentsState: StateFlow<PixelfedCommentsState?> =
        _pixelfedCommentsState.asStateFlow()
    private val _lemmySort = MutableStateFlow(LemmySortType.HOT)
    val lemmySort: StateFlow<LemmySortType> = _lemmySort.asStateFlow()
    private val _lemmyActionOverrides = MutableStateFlow<Map<String, LemmyPostUiModel>>(emptyMap())
    val lemmyActionOverrides: StateFlow<Map<String, LemmyPostUiModel>> =
        _lemmyActionOverrides.asStateFlow()

    val mastodonTimeline: Flow<PagingData<MastodonPostUiModel>> =
        uiState
            .map { state ->
                state.activeAccount(PlatformType.MASTODON)
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
        combine(uiState, _lemmySort) { state, sort ->
            state.activeAccount(PlatformType.LEMMY)
                ?.takeIf { !it.accessToken.isNullOrBlank() } to sort
        }
            .distinctUntilChanged()
            .flatMapLatest { (account, sort) ->
                if (account == null) {
                    flowOf(PagingData.empty())
                } else {
                    lemmyRepository
                        .getPostsPagingData(
                            account = account,
                            sort = sort,
                            feedType = LemmyFeedType.SUBSCRIBED,
                        )
                        .map { pagingData -> pagingData.map(LemmyPostMapper::domainToUi) }
                }
            }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    val pixelfedFeed: Flow<PagingData<PixelfedPostUiModel>> =
        uiState
            .map { state ->
                state.activeAccount(PlatformType.PIXELFED)
                    ?.takeIf { !it.accessToken.isNullOrBlank() }
            }
            .distinctUntilChanged()
            .flatMapLatest { account ->
                if (account == null) {
                    Log.d(TAG, "Pixelfed home skipped: no active token account")
                    flowOf(PagingData.empty())
                } else {
                    Log.d(
                        TAG,
                        "Pixelfed home loading: account=${account.id}, instance=${account.instanceUrl}, token=true",
                    )
                    pixelfedRepository
                        .getHomeFeedPagingData(account)
                        .map { pagingData -> pagingData.map(PixelfedMapper::postToUi) }
                }
            }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    init {
        combine(accountStore.accounts, accountStore.activeAccountIds) { storedAccounts, activeAccountIds ->
            storedAccounts to activeAccountIds
        }
            .onEach { (storedAccounts, activeAccountIds) ->
                _uiState.update { state ->
                    state.copy(
                        accounts = mergeAccounts(storedAccounts),
                        activeAccountIds = activeAccountIds,
                    )
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
        val account = _uiState.value.activeAccount(PlatformType.MASTODON)
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
        val account = _uiState.value.activeAccount(PlatformType.MASTODON)
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

    fun openNewPostCompose() {
        if (_uiState.value.selectedPlatform != PlatformType.MASTODON) return
        _newPostComposeState.value = MastodonNewPostComposeState()
    }

    fun onNewPostTextChanged(text: String) {
        _newPostComposeState.update { state ->
            state?.copy(text = text, errorMessage = null)
        }
    }

    fun onNewPostVisibilityChanged(visibility: MastodonVisibility) {
        _newPostComposeState.update { state ->
            state?.copy(visibility = visibility, errorMessage = null)
        }
    }

    fun onNewPostContentWarningEnabledChanged(enabled: Boolean) {
        _newPostComposeState.update { state ->
            state?.copy(isContentWarningEnabled = enabled, errorMessage = null)
        }
    }

    fun onNewPostContentWarningChanged(contentWarning: String) {
        _newPostComposeState.update { state ->
            state?.copy(contentWarning = contentWarning, errorMessage = null)
        }
    }

    fun dismissNewPostCompose() {
        if (_newPostComposeState.value?.isSending == true) return
        _newPostComposeState.value = null
    }

    fun submitNewPost() {
        val state = _newPostComposeState.value ?: return
        if (!state.canSend) {
            _newPostComposeState.value = state.copy(errorMessage = "Post cannot be empty.")
            return
        }
        val account = _uiState.value.activeAccount(PlatformType.MASTODON)
            ?: return
        _newPostComposeState.value = state.copy(isSending = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = mastodonRepository.createPost(
                    account = account,
                    text = state.text.trim(),
                    visibility = state.visibility.apiValue,
                    spoilerText = state.contentWarning.takeIf {
                        state.isContentWarningEnabled && it.isNotBlank()
                    },
                )
            ) {
                is AppResult.Success -> {
                    _newPostComposeState.value = null
                }
                is AppResult.Failure -> {
                    _newPostComposeState.value = state.copy(
                        isSending = false,
                        errorMessage = result.error.postErrorMessage(),
                    )
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToMastodonLogin)
                    }
                }
            }
        }
    }

    fun selectPlatform(platform: PlatformType) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }

    fun selectAccount(account: Account?) {
        if (account == null) return
        _uiState.update {
            it.copy(activeAccountIds = it.activeAccountIds + (account.platform to account.id))
        }
    }

    fun selectLemmySort(sort: LemmySortType) {
        _lemmySort.value = sort
    }

    fun onLemmyAction(post: LemmyPostUiModel, action: LemmyPostActionType) {
        val account = _uiState.value.activeAccount(PlatformType.LEMMY) ?: return
        val before = _lemmyActionOverrides.value[post.id] ?: post
        val optimistic = before.optimistic(action).copy(loadingAction = action)
        setLemmyOverride(optimistic)
        viewModelScope.launch {
            val result = when (action) {
                LemmyPostActionType.UPVOTE -> lemmyRepository.votePost(
                    account = account,
                    postId = post.id,
                    score = if (before.isUpvoted) 0 else 1,
                )
                LemmyPostActionType.DOWNVOTE -> lemmyRepository.votePost(
                    account = account,
                    postId = post.id,
                    score = if (before.isDownvoted) 0 else -1,
                )
                LemmyPostActionType.SAVE -> lemmyRepository.savePost(
                    account = account,
                    postId = post.id,
                    saved = !before.isSaved,
                )
            }
            when (result) {
                is AppResult.Success -> {
                    setLemmyOverride(LemmyPostMapper.domainToUi(result.data).copy(loadingAction = null))
                }
                is AppResult.Failure -> {
                    setLemmyOverride(before.copy(loadingAction = null))
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToMastodonLogin)
                    }
                }
            }
        }
    }

    fun onPixelfedLike(post: PixelfedPostUiModel) {
        val account = _uiState.value.activeAccount(PlatformType.PIXELFED)
            ?: return
        val before = _pixelfedActionOverrides.value[post.id] ?: post
        val optimistic = before.copy(
            isLiked = !before.isLiked,
            likes = (before.likes + if (!before.isLiked) 1 else -1).coerceAtLeast(0),
            isLoadingLike = true,
        )
        setPixelfedOverride(optimistic)
        viewModelScope.launch {
            when (
                val result = pixelfedRepository.setLiked(
                    account = account,
                    postId = post.id,
                    liked = optimistic.isLiked,
                )
            ) {
                is AppResult.Success -> {
                    setPixelfedOverride(
                        PixelfedMapper.postToUi(result.data).copy(isLoadingLike = false),
                    )
                }
                is AppResult.Failure -> {
                    setPixelfedOverride(before.copy(isLoadingLike = false))
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToMastodonLogin)
                    }
                }
            }
        }
    }

    fun openPixelfedComments(post: PixelfedPostUiModel) {
        val account = _uiState.value.activeAccount(PlatformType.PIXELFED)
            ?: return
        _pixelfedCommentsState.value = PixelfedCommentsState(postId = post.id, isLoading = true)
        viewModelScope.launch {
            when (val result = pixelfedRepository.getComments(account, post.id)) {
                is AppResult.Success -> {
                    _pixelfedCommentsState.value = PixelfedCommentsState(
                        postId = post.id,
                        comments = result.data,
                    )
                }
                is AppResult.Failure -> {
                    _pixelfedCommentsState.value = PixelfedCommentsState(
                        postId = post.id,
                        errorMessage = result.error.userMessage("Comments failed."),
                    )
                }
            }
        }
    }

    fun dismissPixelfedComments() {
        _pixelfedCommentsState.value = null
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

    private fun HomeUiState.activeAccount(platform: PlatformType): Account? {
        val platformAccounts = accounts.filter { it.platform == platform }
        return platformAccounts.firstOrNull { it.id == activeAccountIds[platform] }
            ?: platformAccounts.firstOrNull()
    }

    private fun setMastodonOverride(post: MastodonPostUiModel) {
        _mastodonActionOverrides.update { overrides ->
            overrides + (post.id to post) + (post.detailId to post)
        }
    }

    private fun setPixelfedOverride(post: PixelfedPostUiModel) {
        _pixelfedActionOverrides.update { overrides ->
            overrides + (post.id to post)
        }
    }

    private fun setLemmyOverride(post: LemmyPostUiModel) {
        _lemmyActionOverrides.update { overrides ->
            overrides + (post.id to post)
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

    private fun AppError.postErrorMessage(): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again to post."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> message ?: "Post could not be sent. Try again."
    }

    private fun AppError.userMessage(fallback: String): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> message ?: fallback
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

    private fun LemmyPostUiModel.optimistic(action: LemmyPostActionType): LemmyPostUiModel =
        when (action) {
            LemmyPostActionType.UPVOTE -> {
                val nextUpvoted = !isUpvoted
                copy(
                    isUpvoted = nextUpvoted,
                    isDownvoted = false,
                    score = score + when {
                        nextUpvoted && isDownvoted -> 2
                        nextUpvoted -> 1
                        else -> -1
                    },
                )
            }
            LemmyPostActionType.DOWNVOTE -> {
                val nextDownvoted = !isDownvoted
                copy(
                    isDownvoted = nextDownvoted,
                    isUpvoted = false,
                    score = score + when {
                        nextDownvoted && isUpvoted -> -2
                        nextDownvoted -> -1
                        else -> 1
                    },
                )
            }
            LemmyPostActionType.SAVE -> copy(isSaved = !isSaved)
        }

    private companion object {
        const val TAG = "HomeViewModel"
    }
}

sealed interface HomeEffect {
    data object NavigateToMastodonLogin : HomeEffect
}
