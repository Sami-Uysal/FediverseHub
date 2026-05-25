package com.samiuysal.fediversehub.feature.lemmy.profile

import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel

sealed interface LemmyProfileUiState {
    data object NoAccount : LemmyProfileUiState
    data object Loading : LemmyProfileUiState
    data class Error(val message: String) : LemmyProfileUiState
    data class Success(
        val profile: LemmyProfileUiModel,
        val selectedTab: LemmyProfileTab = LemmyProfileTab.POSTS,
    ) : LemmyProfileUiState
}

data class LemmyProfileUiModel(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val bio: String,
    val postCount: Int,
    val commentCount: Int,
    val posts: List<LemmyPostUiModel>,
    val comments: List<CommentUiModel>,
    val savedPosts: List<LemmyPostUiModel>,
    val savedComments: List<CommentUiModel>,
)

enum class LemmyProfileTab {
    POSTS,
    COMMENTS,
    SAVED,
}
