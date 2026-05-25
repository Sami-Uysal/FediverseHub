package com.samiuysal.fediversehub.feature.mastodon.notifications

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonNotificationType

@Immutable
data class MastodonNotificationUiModel(
    val id: String,
    val type: MastodonNotificationType,
    val actorAccountId: String,
    val actorDisplayName: String,
    val actorUsername: String,
    val actorAvatarUrl: String?,
    val timeAgo: String,
    val actionText: String,
    val status: MastodonPostUiModel?,
)
