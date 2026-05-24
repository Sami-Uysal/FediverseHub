package com.samiuysal.fediversehub.feature.lemmy.domain

data class LemmyPost(
    val id: String,
    val title: String,
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
)

data class LemmyComment(
    val id: String,
    val parentId: String?,
    val authorName: String,
    val content: String,
    val depth: Int,
    val isCollapsed: Boolean,
    val score: Int = 0,
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
