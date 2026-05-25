package com.samiuysal.fediversehub.feature.notifications

import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotification
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotificationType
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedNotification
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedNotificationType

object PlatformNotificationMappers {
    fun pixelfedToUi(notification: PixelfedNotification): PixelfedNotificationUiModel =
        PixelfedNotificationUiModel(
            id = notification.id,
            type = notification.type,
            actorAccountId = notification.actorAccountId,
            title = notification.type.title(notification.actorDisplayName),
            actor = notification.actorUsername,
            avatarUrl = notification.actorAvatarUrl,
            postId = notification.postId,
            preview = notification.postPreview,
            time = notification.createdAt ?: "now",
        )

    fun lemmyToUi(notification: LemmyNotification): LemmyNotificationUiModel =
        LemmyNotificationUiModel(
            id = notification.id,
            type = notification.type,
            title = when (notification.type) {
                LemmyNotificationType.REPLY -> "Yanıt: ${notification.postTitle}"
                LemmyNotificationType.MENTION -> "Mention: ${notification.postTitle}"
            },
            actor = notification.actorName,
            community = notification.communityName,
            postId = notification.postId,
            preview = notification.text,
            score = notification.score,
            time = notification.createdAt ?: "now",
            read = notification.read,
        )

    private fun PixelfedNotificationType.title(actor: String): String =
        when (this) {
            PixelfedNotificationType.FAVOURITE -> "$actor gönderini beğendi"
            PixelfedNotificationType.COMMENT -> "$actor yorum yaptı"
            PixelfedNotificationType.MENTION -> "$actor senden bahsetti"
            PixelfedNotificationType.FOLLOW -> "$actor takip etti"
            PixelfedNotificationType.STATUS -> "$actor yeni gönderi paylaştı"
            PixelfedNotificationType.UNKNOWN -> "$actor bildirim gönderdi"
        }
}
