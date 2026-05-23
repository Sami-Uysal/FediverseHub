package com.samiuysal.fediversehub.feature.mastodon.domain

data class MastodonNotification(
    val id: String,
    val type: MastodonNotificationType,
    val createdAt: String?,
    val accountDisplayName: String,
    val accountUsername: String,
    val accountAvatarUrl: String?,
    val status: MastodonPost?,
)

enum class MastodonNotificationType {
    FAVOURITE,
    REBLOG,
    MENTION,
    FOLLOW,
    STATUS,
    UPDATE,
    POLL,
    UNKNOWN,
}
