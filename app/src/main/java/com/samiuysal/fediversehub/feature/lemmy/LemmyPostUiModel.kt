package com.samiuysal.fediversehub.feature.lemmy

import androidx.compose.runtime.Immutable

@Immutable
data class LemmyPostUiModel(
    val id: String,
    val title: String,
    val communityId: String?,
    val community: String,
    val domain: String?,
    val author: String,
    val timeAgo: String,
    val score: Int,
    val comments: Int,
    val previewText: String,
    val nestedComments: List<CommentUiModel>,
    val url: String? = null,
    val thumbnailUrl: String? = null,
    val isUpvoted: Boolean = false,
    val isDownvoted: Boolean = false,
    val isSaved: Boolean = false,
    val loadingAction: LemmyPostActionType? = null,
)

@Immutable
data class CommentUiModel(
    val id: String,
    val parentId: String?,
    val author: String,
    val text: String,
    val depth: Int,
    val isCollapsed: Boolean,
    val score: Int = 0,
    val isUpvoted: Boolean = false,
    val isDownvoted: Boolean = false,
    val loadingAction: LemmyCommentActionType? = null,
)

@Immutable
data class LemmyCommunityUiModel(
    val id: String,
    val name: String,
    val title: String,
    val actorId: String?,
    val description: String,
    val iconUrl: String?,
    val bannerUrl: String?,
    val subscribers: Int,
    val posts: Int,
    val comments: Int,
    val isSubscribed: Boolean,
    val isFollowLoading: Boolean = false,
)

enum class LemmyPostActionType {
    UPVOTE,
    DOWNVOTE,
    SAVE,
}

enum class LemmyCommentActionType {
    UPVOTE,
    DOWNVOTE,
}
