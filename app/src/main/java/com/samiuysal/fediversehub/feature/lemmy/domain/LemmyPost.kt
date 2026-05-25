package com.samiuysal.fediversehub.feature.lemmy.domain

data class LemmyPost(
    val id: String,
    val title: String,
    val communityId: String?,
    val communityName: String,
    val communityActorId: String?,
    val domain: String?,
    val authorName: String,
    val publishedAt: String?,
    val score: Int,
    val commentCount: Int,
    val previewText: String,
    val comments: List<LemmyComment>,
    val url: String? = null,
    val thumbnailUrl: String? = null,
    val myVote: Int? = null,
    val saved: Boolean = false,
)

data class LemmyComment(
    val id: String,
    val postId: String,
    val postTitle: String? = null,
    val parentId: String?,
    val authorName: String,
    val content: String,
    val depth: Int,
    val isCollapsed: Boolean,
    val score: Int = 0,
    val myVote: Int? = null,
)

data class LemmyCommunity(
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
    val subscribed: Boolean,
)

data class LemmyProfile(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val bannerUrl: String?,
    val bio: String,
    val postCount: Int,
    val commentCount: Int,
    val posts: List<LemmyPost>,
    val comments: List<LemmyComment>,
    val savedPosts: List<LemmyPost> = emptyList(),
    val savedComments: List<LemmyComment> = emptyList(),
)

enum class LemmySortType {
    HOT,
    ACTIVE,
    NEW,
    TOP,
}

enum class LemmyFeedType {
    SUBSCRIBED,
    LOCAL,
    ALL,
}
