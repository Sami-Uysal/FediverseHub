package com.samiuysal.fediversehub.feature.pixelfed.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.error.userMessage
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper
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
class PixelfedPostDetailViewModel @Inject constructor(
    private val pixelfedRepository: PixelfedRepository,
    private val accountStore: AccountStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val postId: String = checkNotNull(savedStateHandle[AppDestination.POST_ID_ARGUMENT])

    private val _uiState = MutableStateFlow<PixelfedPostDetailUiState>(PixelfedPostDetailUiState.Loading)
    val uiState: StateFlow<PixelfedPostDetailUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<PixelfedPostDetailEffect>()
    val effects: SharedFlow<PixelfedPostDetailEffect> = _effects.asSharedFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun retryComments() {
        viewModelScope.launch {
            val current = _uiState.value as? PixelfedPostDetailUiState.Success ?: return@launch
            _uiState.value = current.copy(isCommentsLoading = true, commentsErrorMessage = null)
            loadComments(account = pixelfedAccount(), postId = current.post.id)
        }
    }

    fun onLikeClick() {
        val current = _uiState.value as? PixelfedPostDetailUiState.Success ?: return
        val before = current.post
        val optimistic = before.copy(
            isLiked = !before.isLiked,
            likes = (before.likes + if (!before.isLiked) 1 else -1).coerceAtLeast(0),
            isLoadingLike = true,
        )
        _uiState.value = current.copy(post = optimistic)
        viewModelScope.launch {
            when (
                val result = pixelfedRepository.setLiked(
                    account = pixelfedAccount(),
                    postId = before.id,
                    liked = optimistic.isLiked,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { state ->
                        (state as? PixelfedPostDetailUiState.Success)
                            ?.copy(post = PixelfedMapper.postToUi(result.data).copy(isLoadingLike = false))
                            ?: state
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { state ->
                        (state as? PixelfedPostDetailUiState.Success)
                            ?.copy(post = before.copy(isLoadingLike = false))
                            ?: state
                    }
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(PixelfedPostDetailEffect.NavigateToLogin)
                    }
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = PixelfedPostDetailUiState.Loading
            val account = pixelfedAccount()
            val postDeferred = async { pixelfedRepository.getPost(account, postId) }
            val commentsDeferred = async { pixelfedRepository.getComments(account, postId) }
            when (val postResult = postDeferred.await()) {
                is AppResult.Success -> {
                    val commentsResult = commentsDeferred.await()
                    _uiState.value = PixelfedPostDetailUiState.Success(
                        post = PixelfedMapper.postToUi(postResult.data),
                        comments = (commentsResult as? AppResult.Success)?.data.orEmpty(),
                        commentsErrorMessage = (commentsResult as? AppResult.Failure)?.error?.userMessage(),
                    )
                }
                is AppResult.Failure -> {
                    commentsDeferred.cancel()
                    _uiState.value = PixelfedPostDetailUiState.Error(postResult.error.userMessage())
                    if (postResult.error is AppError.Unauthorized) {
                        _effects.emit(PixelfedPostDetailEffect.NavigateToLogin)
                    }
                }
            }
        }
    }

    private suspend fun loadComments(account: Account, postId: String) {
        when (val result = pixelfedRepository.getComments(account, postId)) {
            is AppResult.Success -> {
                _uiState.update { state ->
                    (state as? PixelfedPostDetailUiState.Success)
                        ?.copy(
                            comments = result.data,
                            isCommentsLoading = false,
                            commentsErrorMessage = null,
                        )
                        ?: state
                }
            }
            is AppResult.Failure -> {
                _uiState.update { state ->
                    (state as? PixelfedPostDetailUiState.Success)
                        ?.copy(
                            isCommentsLoading = false,
                            commentsErrorMessage = result.error.userMessage(),
                        )
                        ?: state
                }
            }
        }
    }

    private suspend fun pixelfedAccount(): Account {
        val accounts = accountStore.accounts.first()
        val activeId = accountStore.activeAccountIds.first()[PlatformType.PIXELFED]
        return accounts.firstOrNull {
            it.platform == PlatformType.PIXELFED &&
                it.id == activeId &&
                !it.accessToken.isNullOrBlank()
        } ?: accounts.firstOrNull {
            it.platform == PlatformType.PIXELFED && !it.accessToken.isNullOrBlank()
        } ?: Account(
            id = "pixelfed-detail-preview",
            platform = PlatformType.PIXELFED,
            instanceUrl = "pixelfed.social",
            username = "preview",
            displayName = "Pixelfed",
            avatarUrl = null,
            accessToken = null,
        )
    }
}

sealed interface PixelfedPostDetailEffect {
    data object NavigateToLogin : PixelfedPostDetailEffect
}
