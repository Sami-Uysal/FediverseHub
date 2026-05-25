package com.samiuysal.fediversehub.feature.mastodon.mapper

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonNotificationDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonNotification
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonNotificationType
import com.samiuysal.fediversehub.feature.mastodon.notifications.MastodonNotificationUiModel
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

object MastodonNotificationMapper {
    fun dtoToDomain(dto: MastodonNotificationDto): MastodonNotification =
        MastodonNotification(
            id = dto.id,
            type = dto.type.toNotificationType(),
            createdAt = dto.createdAt,
            accountId = dto.account.id,
            accountDisplayName = htmlToPlainText(
                dto.account.displayName.ifBlank { dto.account.username },
            ),
            accountUsername = dto.account.acct.ifBlank { dto.account.username },
            accountAvatarUrl = dto.account.avatarStatic ?: dto.account.avatar,
            status = dto.status?.let(MastodonTimelineMapper::dtoToDomain),
        )

    fun domainToUi(domain: MastodonNotification): MastodonNotificationUiModel =
        MastodonNotificationUiModel(
            id = domain.id,
            type = domain.type,
            actorAccountId = domain.accountId,
            actorDisplayName = domain.accountDisplayName,
            actorUsername = "@${domain.accountUsername}",
            actorAvatarUrl = domain.accountAvatarUrl,
            timeAgo = domain.createdAt.toRelativeTimeLabel(),
            actionText = domain.actionText(),
            status = domain.status?.let(MastodonTimelineMapper::domainToUi),
        )

    private fun String.toNotificationType(): MastodonNotificationType =
        when (lowercase()) {
            "favourite" -> MastodonNotificationType.FAVOURITE
            "reblog" -> MastodonNotificationType.REBLOG
            "mention" -> MastodonNotificationType.MENTION
            "follow", "follow_request" -> MastodonNotificationType.FOLLOW
            "status" -> MastodonNotificationType.STATUS
            "update" -> MastodonNotificationType.UPDATE
            "poll" -> MastodonNotificationType.POLL
            else -> MastodonNotificationType.UNKNOWN
        }

    private fun MastodonNotification.actionText(): String =
        when (type) {
            MastodonNotificationType.FAVOURITE -> "$accountDisplayName favourited your post"
            MastodonNotificationType.REBLOG -> "$accountDisplayName boosted your post"
            MastodonNotificationType.MENTION -> "$accountDisplayName mentioned you"
            MastodonNotificationType.FOLLOW -> "$accountDisplayName followed you"
            MastodonNotificationType.STATUS -> "$accountDisplayName posted"
            MastodonNotificationType.UPDATE -> "$accountDisplayName edited a post"
            MastodonNotificationType.POLL -> "A poll from $accountDisplayName ended"
            MastodonNotificationType.UNKNOWN -> "$accountDisplayName sent a notification"
        }

    private fun htmlToPlainText(value: String): String =
        HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .trim()

    private fun String?.toRelativeTimeLabel(): String {
        if (this == null) return "now"
        return try {
            val duration = Duration.between(OffsetDateTime.parse(this), OffsetDateTime.now())
            when {
                duration.toMinutes() < 1 -> "now"
                duration.toHours() < 1 -> "${duration.toMinutes()}m"
                duration.toDays() < 1 -> "${duration.toHours()}h"
                duration.toDays() < 7 -> "${duration.toDays()}d"
                else -> "${duration.toDays() / 7}w"
            }
        } catch (_: DateTimeParseException) {
            this
        }
    }
}
