package com.samiuysal.fediversehub.feature.lemmy.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommentActionType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostActionType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LemmyPostDetailViewModel @Inject constructor(
    private val lemmyRepository: LemmyRepository,
    private val accountStore: AccountStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val postId: String = checkNotNull(savedStateHandle[AppDestination.POST_ID_ARGUMENT])

    private val _uiState = MutableStateFlow<LemmyPostDetailUiState>(LemmyPostDetailUiState.Loading)
    val uiState: StateFlow<LemmyPostDetailUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<LemmyPostDetailEffect>()
    val effects: SharedFlow<LemmyPostDetailEffect> = _effects.asSharedFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun retryComments() {
        viewModelScope.launch {
            val current = _uiState.value as? LemmyPostDetailUiState.Success ?: return@launch
            _uiState.value = current.copy(isCommentsLoading = true, commentsErrorMessage = null)
            loadComments(account = lemmyAccount(), postId = current.post.id)
        }
    }

    fun toggleComment(commentId: String) {
        _uiState.update { state ->
            val success = state as? LemmyPostDetailUiState.Success ?: return@update state
            val nextCollapsedIds = if (commentId in success.collapsedCommentIds) {
                success.collapsedCommentIds - commentId
            } else {
                success.collapsedCommentIds + commentId
            }
            success.copy(collapsedCommentIds = nextCollapsedIds)
        }
    }

    fun onPostAction(action: LemmyPostActionType) {
        val current = _uiState.value as? LemmyPostDetailUiState.Success ?: return
        val before = current.post
        val optimistic = before.optimistic(action).copy(loadingAction = action)
        _uiState.value = current.copy(post = optimistic)
        viewModelScope.launch {
            val account = lemmyAccount()
            val result = when (action) {
                LemmyPostActionType.UPVOTE -> lemmyRepository.votePost(
                    account = account,
                    postId = before.id,
                    score = if (before.isUpvoted) 0 else 1,
                )
                LemmyPostActionType.DOWNVOTE -> lemmyRepository.votePost(
                    account = account,
                    postId = before.id,
                    score = if (before.isDownvoted) 0 else -1,
                )
                LemmyPostActionType.SAVE -> lemmyRepository.savePost(
                    account = account,
                    postId = before.id,
                    saved = !before.isSaved,
                )
            }
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { state ->
                        (state as? LemmyPostDetailUiState.Success)
                            ?.copy(post = LemmyPostMapper.domainToUi(result.data).copy(loadingAction = null))
                            ?: state
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        (state as? LemmyPostDetailUiState.Success)
                            ?.copy(post = before.copy(loadingAction = null))
                            ?: state
                    }
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(LemmyPostDetailEffect.NavigateToLogin)
                    }
                }
            }
        }
    }

    fun onCommentAction(comment: CommentUiModel, action: LemmyCommentActionType) {
        val current = _uiState.value as? LemmyPostDetailUiState.Success ?: return
        val before = current.comments.firstOrNull { it.id == comment.id } ?: comment
        val optimistic = before.optimistic(action).copy(loadingAction = action)
        _uiState.value = current.replaceComment(optimistic)
        viewModelScope.launch {
            val result = lemmyRepository.voteComment(
                account = lemmyAccount(),
                commentId = before.id,
                score = when (action) {
                    LemmyCommentActionType.UPVOTE -> if (before.isUpvoted) 0 else 1
                    LemmyCommentActionType.DOWNVOTE -> if (before.isDownvoted) 0 else -1
                },
            )
            when (result) {
                is AppResult.Success -> {
                    _uiState.update { state ->
                        (state as? LemmyPostDetailUiState.Success)
                            ?.replaceComment(LemmyPostMapper.commentToUi(result.data).copy(loadingAction = null))
                            ?: state
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        (state as? LemmyPostDetailUiState.Success)
                            ?.replaceComment(before.copy(loadingAction = null))
                            ?: state
                    }
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(LemmyPostDetailEffect.NavigateToLogin)
                    }
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = LemmyPostDetailUiState.Loading
            val account = lemmyAccount()
            val postDeferred = async { lemmyRepository.getPost(account, postId) }
            val commentsDeferred = async { lemmyRepository.getComments(account, postId) }
            when (val postResult = postDeferred.await()) {
                is AppResult.Success -> {
                    val commentsResult = commentsDeferred.await()
                    _uiState.value = LemmyPostDetailUiState.Success(
                        post = LemmyPostMapper.domainToUi(postResult.data),
                        comments = (commentsResult as? AppResult.Success)
                            ?.data
                            ?.map(LemmyPostMapper::commentToUi)
                            .orEmpty(),
                        commentsErrorMessage = (commentsResult as? AppResult.Failure)?.error?.lemmyMessage(),
                    )
                }
                is AppResult.Failure -> {
                    commentsDeferred.cancel()
                    _uiState.value = LemmyPostDetailUiState.Error(postResult.error.lemmyMessage())
                    if (postResult.error is AppError.Unauthorized) {
                        _effects.emit(LemmyPostDetailEffect.NavigateToLogin)
                    }
                }
            }
        }
    }

    private suspend fun loadComments(account: Account, postId: String) {
        when (val result = lemmyRepository.getComments(account, postId)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    (state as? LemmyPostDetailUiState.Success)
                        ?.copy(
                            comments = result.data.map(LemmyPostMapper::commentToUi),
                            isCommentsLoading = false,
                            commentsErrorMessage = null,
                        )
                        ?: state
                }
            }
            is AppResult.Failure -> {
                _uiState.update { state ->
                    (state as? LemmyPostDetailUiState.Success)
                        ?.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = result.error.lemmyMessage(),
                        )
                        ?: state
                }
            }
        }
    }

    private suspend fun lemmyAccount(): Account {
        val accounts = accountStore.accounts.first()
        val activeId = accountStore.activeAccountIds.first()[PlatformType.LEMMY]
        return accounts.firstOrNull {
            it.platform == PlatformType.LEMMY &&
                it.id == activeId &&
                !it.accessToken.isNullOrBlank()
        } ?: accounts.firstOrNull {
            it.platform == PlatformType.LEMMY && !it.accessToken.isNullOrBlank()
        } ?: Account(
            id = "lemmy-detail-preview",
            platform = PlatformType.LEMMY,
            instanceUrl = "lemmy.world",
            username = "preview",
            displayName = "Lemmy",
            avatarUrl = null,
            accessToken = null,
        )
    }
}

sealed interface LemmyPostDetailEffect {
    data object NavigateToLogin : LemmyPostDetailEffect
}

private fun AppError.lemmyMessage(): String = when (this) {
    AppError.Network -> "Ağ hatası. Bağlantıyı kontrol et."
    AppError.RateLimited -> "Çok hızlı istek atıldı. Biraz bekle."
    AppError.Unauthorized -> "Oturum süresi doldu. Lemmy hesabına tekrar giriş yap."
    is AppError.Server -> "Sunucu hatası $code. Biraz sonra tekrar dene."
    is AppError.Unknown -> message ?: "Lemmy içeriği yüklenemedi."
}

private fun LemmyPostDetailUiState.Success.replaceComment(
    comment: CommentUiModel,
): LemmyPostDetailUiState.Success =
    copy(comments = comments.map { if (it.id == comment.id) comment else it })

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

private fun CommentUiModel.optimistic(action: LemmyCommentActionType): CommentUiModel =
    when (action) {
        LemmyCommentActionType.UPVOTE -> {
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
        LemmyCommentActionType.DOWNVOTE -> {
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
    }
