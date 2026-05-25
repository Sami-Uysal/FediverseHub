package com.samiuysal.fediversehub.feature.pixelfed.domain

data class PixelfedNotification(
    val id: String,
    val type: PixelfedNotificationType,
    val actorAccountId: String,
    val actorDisplayName: String,
    val actorUsername: String,
    val actorAvatarUrl: String?,
    val postId: String?,
    val postPreview: String?,
    val createdAt: String?,
)

enum class PixelfedNotificationType {
    FAVOURITE,
    COMMENT,
    MENTION,
    FOLLOW,
    STATUS,
    UNKNOWN,
}
