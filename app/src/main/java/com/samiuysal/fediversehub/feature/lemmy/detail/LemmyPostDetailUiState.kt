package com.samiuysal.fediversehub.feature.lemmy.detail

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel

sealed interface LemmyPostDetailUiState {
    data object Loading : LemmyPostDetailUiState

    data class Success(
        val post: LemmyPostUiModel,
        val comments: List<CommentUiModel>,
        val collapsedCommentIds: Set<String> = emptySet(),
        val commentsErrorMessage: String? = null,
        val isCommentsLoading: Boolean = false,
    ) : LemmyPostDetailUiState

    data class Error(val message: String) : LemmyPostDetailUiState
}

@Immutable
data class LemmyVisibleComment(
    val comment: CommentUiModel,
    val hasChildren: Boolean,
    val isCollapsed: Boolean,
)
