package com.samiuysal.fediversehub.feature.pixelfed.mapper

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonNotificationDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonSearchDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedComment
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedHashtag
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedMedia
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedNotification
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedNotificationType
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedPost
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedProfile
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchAccount
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchResult
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

object PixelfedMapper {
    fun statusToDomain(dto: MastodonStatusDto): PixelfedPost {
        val status = dto.reblog ?: dto
        return PixelfedPost(
            id = status.id,
            authorAccountId = status.account.id,
            authorDisplayName = htmlToPlainText(status.account.displayName.ifBlank { status.account.username }),
            authorUsername = status.account.acct.ifBlank { status.account.username },
            authorAvatarUrl = status.account.avatarStatic ?: status.account.avatar,
            createdAt = status.createdAt,
            caption = htmlToPlainText(status.content),
            media = status.mediaAttachments.map {
                PixelfedMedia(
                    id = it.id,
                    previewUrl = it.previewUrl ?: it.url,
                    fullUrl = it.url ?: it.previewUrl,
                    description = it.description,
                )
            },
            likeCount = status.favouritesCount,
            commentCount = status.repliesCount,
            isLiked = status.favourited,
        )
    }

    fun accountToProfile(dto: MastodonAccountDto): PixelfedProfile =
        PixelfedProfile(
            id = dto.id,
            displayName = htmlToPlainText(dto.displayName.ifBlank { dto.username }),
            username = "@${dto.acct.ifBlank { dto.username }}",
            avatarUrl = dto.avatarStatic ?: dto.avatar,
            headerUrl = dto.headerStatic ?: dto.header,
            note = htmlToPlainText(dto.note),
            followersCount = dto.followersCount,
            followingCount = dto.followingCount,
            statusesCount = dto.statusesCount,
        )

    fun searchToDomain(dto: MastodonSearchDto): PixelfedSearchResult =
        PixelfedSearchResult(
            posts = dto.statuses.map(::statusToDomain),
            accounts = dto.accounts.map(::accountToSearchDomain),
            hashtags = dto.hashtags.map { PixelfedHashtag(name = it.name) },
        )

    fun notificationToDomain(dto: MastodonNotificationDto): PixelfedNotification =
        PixelfedNotification(
            id = dto.id,
            type = dto.type.toPixelfedNotificationType(),
            actorAccountId = dto.account.id,
            actorDisplayName = htmlToPlainText(dto.account.displayName.ifBlank { dto.account.username }),
            actorUsername = "@${dto.account.acct.ifBlank { dto.account.username }}",
            actorAvatarUrl = dto.account.avatarStatic ?: dto.account.avatar,
            postId = dto.status?.id,
            postPreview = dto.status?.content?.let(::htmlToPlainText),
            createdAt = dto.createdAt,
        )

    fun postToUi(domain: PixelfedPost): PixelfedPostUiModel =
        PixelfedPostUiModel(
            id = domain.id,
            authorAccountId = domain.authorAccountId,
            displayName = domain.authorDisplayName,
            username = "@${domain.authorUsername}",
            avatarUrl = domain.authorAvatarUrl,
            imageUrl = domain.media.firstNotNullOfOrNull { it.previewUrl ?: it.fullUrl }.orEmpty(),
            fullImageUrls = domain.media.mapNotNull { it.fullUrl ?: it.previewUrl },
            altFlags = domain.media.map { !it.description.isNullOrBlank() },
            caption = domain.caption,
            likes = domain.likeCount,
            comments = domain.commentCount,
            timeAgo = domain.createdAt.toRelativeTimeLabel(),
            isLiked = domain.isLiked,
        )

    fun statusToComment(dto: MastodonStatusDto): PixelfedComment =
        PixelfedComment(
            id = dto.id,
            displayName = htmlToPlainText(dto.account.displayName.ifBlank { dto.account.username }),
            username = "@${dto.account.acct.ifBlank { dto.account.username }}",
            avatarUrl = dto.account.avatarStatic ?: dto.account.avatar,
            text = htmlToPlainText(dto.content),
            timeAgo = dto.createdAt.toRelativeTimeLabel(),
        )

    private fun accountToSearchDomain(dto: MastodonAccountDto): PixelfedSearchAccount =
        PixelfedSearchAccount(
            id = dto.id,
            displayName = htmlToPlainText(dto.displayName.ifBlank { dto.username }),
            username = "@${dto.acct.ifBlank { dto.username }}",
            avatarUrl = dto.avatarStatic ?: dto.avatar,
            note = htmlToPlainText(dto.note),
        )

    private fun String.toPixelfedNotificationType(): PixelfedNotificationType =
        when (lowercase()) {
            "favourite" -> PixelfedNotificationType.FAVOURITE
            "comment" -> PixelfedNotificationType.COMMENT
            "mention" -> PixelfedNotificationType.MENTION
            "follow" -> PixelfedNotificationType.FOLLOW
            "status" -> PixelfedNotificationType.STATUS
            else -> PixelfedNotificationType.UNKNOWN
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
