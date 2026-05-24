package com.samiuysal.fediversehub.feature.mastodon.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostActionType
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonReplyComposeState
import com.samiuysal.fediversehub.feature.mastodon.canSend
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MastodonPostDetailViewModel @Inject constructor(
    private val mastodonRepository: MastodonRepository,
    private val accountStore: AccountStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val postId: String = checkNotNull(savedStateHandle[AppDestination.POST_ID_ARGUMENT])

    private val _uiState = MutableStateFlow<MastodonPostDetailUiState>(MastodonPostDetailUiState.Loading)
    val uiState: StateFlow<MastodonPostDetailUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<MastodonPostDetailEffect>()
    val effects: SharedFlow<MastodonPostDetailEffect> = _effects.asSharedFlow()
    private val _replyComposeState = MutableStateFlow<MastodonReplyComposeState?>(null)
    val replyComposeState: StateFlow<MastodonReplyComposeState?> = _replyComposeState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun onPostAction(
        post: MastodonPostUiModel,
        action: MastodonPostActionType,
    ) {
        if (action == MastodonPostActionType.REPLY) {
            openReplyCompose(post)
            return
        }
        val current = _uiState.value as? MastodonPostDetailUiState.Success ?: return
        val before = current.find(post.detailId) ?: post
        val optimistic = before.optimistic(action).copy(loadingAction = action)
        _uiState.update { current.replace(optimistic) }
        viewModelScope.launch {
            val account = mastodonAccount()
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
                    _uiState.update {
                        (it as? MastodonPostDetailUiState.Success)
                            ?.replace(optimistic.withActionStateFrom(confirmed).copy(loadingAction = null))
                            ?: it
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        (it as? MastodonPostDetailUiState.Success)
                            ?.replace(before.copy(loadingAction = null))
                            ?: it
                    }
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(MastodonPostDetailEffect.NavigateToMastodonLogin)
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
        val current = _uiState.value as? MastodonPostDetailUiState.Success ?: return
        _replyComposeState.value = state.copy(isSending = true, errorMessage = null)
        viewModelScope.launch {
            val account = mastodonAccount()
            when (
                val result = mastodonRepository.replyToPost(
                    account = account,
                    postId = state.parent.detailId,
                    text = state.text.trim(),
                    visibility = state.parent.visibility.ifBlank { "public" },
                )
            ) {
                is AppResult.Success -> {
                    val reply = MastodonTimelineMapper.domainToUi(result.data).copy(
                        replyContext = "Replying to ${state.parent.username}",
                        showThreadLine = true,
                    )
                    val updatedParent = state.parent.copy(
                        replies = (state.parent.replies + 1).coerceAtLeast(0),
                        loadingAction = null,
                    )
                    _uiState.update {
                        (it as? MastodonPostDetailUiState.Success)
                            ?.replace(updatedParent)
                            ?.let { success -> success.copy(descendants = success.descendants + reply) }
                            ?: it
                    }
                    _replyComposeState.value = null
                }
                is AppResult.Failure -> {
                    _replyComposeState.value = state.copy(
                        isSending = false,
                        errorMessage = result.error.replyErrorMessage(),
                    )
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(MastodonPostDetailEffect.NavigateToMastodonLogin)
                    }
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { MastodonPostDetailUiState.Loading }
            val account = mastodonAccount()
            when (val result = mastodonRepository.getPostDetail(account = account, postId = postId)) {
                is AppResult.Success -> {
                    val detail = result.data
                    _uiState.update {
                        MastodonPostDetailUiState.Success(
                            ancestors = detail.ancestors.map(MastodonTimelineMapper::domainToUi),
                            post = MastodonTimelineMapper.domainToUi(detail.post),
                            descendants = detail.descendants.map(MastodonTimelineMapper::domainToUi),
                        )
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { MastodonPostDetailUiState.Error(result.error.loadErrorMessage()) }
                }
            }
        }
    }

    private suspend fun mastodonAccount(): Account =
        accountStore.accounts.first().firstOrNull { it.platform == PlatformType.MASTODON }
            ?: Account(
                id = "mastodon-detail-preview",
                platform = PlatformType.MASTODON,
                instanceUrl = "mastodon.social",
                username = "preview",
                displayName = "Mastodon",
                avatarUrl = null,
                accessToken = null,
            )

    private fun MastodonPostDetailUiState.Success.find(postId: String): MastodonPostUiModel? =
        (ancestors + post + descendants).firstOrNull { it.id == postId || it.detailId == postId }

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
        is AppError.Unknown -> "Reply could not be sent. Try again."
    }

    private fun AppError.loadErrorMessage(): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> "Post could not be loaded. Try again."
    }

    private fun MastodonPostDetailUiState.Success.replace(
        updated: MastodonPostUiModel,
    ): MastodonPostDetailUiState.Success = copy(
        ancestors = ancestors.map { if (it.id == updated.id || it.detailId == updated.detailId) updated else it },
        post = if (post.id == updated.id || post.detailId == updated.detailId) updated else post,
        descendants = descendants.map { if (it.id == updated.id || it.detailId == updated.detailId) updated else it },
    )

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

sealed interface MastodonPostDetailEffect {
    data object NavigateToMastodonLogin : MastodonPostDetailEffect
}
