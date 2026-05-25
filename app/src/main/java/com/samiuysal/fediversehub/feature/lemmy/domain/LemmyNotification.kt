package com.samiuysal.fediversehub.feature.lemmy.domain

data class LemmyNotification(
    val id: String,
    val type: LemmyNotificationType,
    val postId: String,
    val postTitle: String,
    val communityName: String,
    val actorName: String,
    val text: String,
    val score: Int,
    val createdAt: String?,
    val read: Boolean,
)

enum class LemmyNotificationType {
    REPLY,
    MENTION,
}

