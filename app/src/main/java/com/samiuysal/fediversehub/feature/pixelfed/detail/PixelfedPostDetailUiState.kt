package com.samiuysal.fediversehub.feature.pixelfed.detail

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedComment

sealed interface PixelfedPostDetailUiState {
    data object Loading : PixelfedPostDetailUiState

    @Immutable
    data class Success(
        val post: PixelfedPostUiModel,
        val comments: List<PixelfedComment> = emptyList(),
        val isCommentsLoading: Boolean = false,
        val commentsErrorMessage: String? = null,
    ) : PixelfedPostDetailUiState

    data class Error(val message: String) : PixelfedPostDetailUiState
}
