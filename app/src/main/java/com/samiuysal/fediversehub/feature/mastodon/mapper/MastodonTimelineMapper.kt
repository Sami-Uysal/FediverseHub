package com.samiuysal.fediversehub.feature.mastodon.mapper

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonLinkPreviewUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonMediaUiModel
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonMediaAttachmentDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonPreviewCardDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonLinkPreview
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaAttachment
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaType
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import java.time.Duration
import java.time.OffsetDateTime
import java.time.format.DateTimeParseException

object MastodonTimelineMapper {
    fun dtoToDomain(dto: MastodonStatusDto): MastodonPost {
        val status = dto.reblog ?: dto
        val displayName = status.account.displayName.ifBlank { status.account.username }
        val boostedByDisplayName = dto.reblog?.let {
            dto.account.displayName.ifBlank { dto.account.username }
        }
        return MastodonPost(
            id = dto.id,
            detailId = status.id,
            authorAccountId = status.account.id,
            authorDisplayName = htmlToPlainText(displayName),
            authorUsername = status.account.acct.ifBlank { status.account.username },
            authorAvatarUrl = status.account.avatarStatic ?: status.account.avatar,
            createdAt = status.createdAt,
            contentText = htmlToPlainText(status.content),
            mediaAttachments = status.mediaAttachments.map(::mediaDtoToDomain),
            boostedByDisplayName = boostedByDisplayName?.let(::htmlToPlainText),
            boostedByAvatarUrl = dto.reblog?.let { dto.account.avatarStatic ?: dto.account.avatar },
            inReplyToAccountId = status.inReplyToAccountId,
            linkPreview = status.card?.toDomain(),
            replyCount = status.repliesCount,
            reblogCount = status.reblogsCount,
            favouriteCount = status.favouritesCount,
            isReblogged = status.reblogged,
            isFavourited = status.favourited,
            isBookmarked = status.bookmarked,
            visibility = status.visibility,
            url = status.url ?: status.uri,
        )
    }

    fun domainToUi(domain: MastodonPost): MastodonPostUiModel = MastodonPostUiModel(
        id = domain.id,
        detailId = domain.detailId,
        authorAccountId = domain.authorAccountId,
        displayName = domain.authorDisplayName,
        username = "@${domain.authorUsername}",
        avatarUrl = domain.authorAvatarUrl,
        timeAgo = domain.createdAt.toRelativeTimeLabel(),
        content = domain.contentText,
        mediaUrl = domain.mediaAttachments.firstNotNullOfOrNull { it.previewUrl ?: it.url },
        media = domain.mediaAttachments.map { it.toUi() },
        hasAltText = domain.mediaAttachments.any { !it.description.isNullOrBlank() },
        boostedByDisplayName = domain.boostedByDisplayName,
        boostedByAvatarUrl = domain.boostedByAvatarUrl,
        replyContext = domain.inReplyToAccountId?.let { "Replying in thread" },
        showThreadLine = domain.inReplyToAccountId != null,
        linkPreview = domain.linkPreview?.toUi(),
        replies = domain.replyCount,
        boosts = domain.reblogCount,
        favourites = domain.favouriteCount,
        isBoosted = domain.isReblogged,
        isFavourited = domain.isFavourited,
        isBookmarked = domain.isBookmarked,
        visibility = domain.visibility,
    )

    private fun MastodonMediaAttachment.toUi(): MastodonMediaUiModel =
        MastodonMediaUiModel(
            id = id,
            previewUrl = previewUrl ?: url,
            fullUrl = url ?: previewUrl,
            altText = description,
        )

    private fun mediaDtoToDomain(dto: MastodonMediaAttachmentDto): MastodonMediaAttachment =
        MastodonMediaAttachment(
            id = dto.id,
            type = dto.type.toMediaType(),
            url = dto.url,
            previewUrl = dto.previewUrl,
            description = dto.description,
        )

    private fun MastodonPreviewCardDto.toDomain(): MastodonLinkPreview? {
        if (title.isBlank()) return null
        return MastodonLinkPreview(
            domain = providerName?.takeIf { it.isNotBlank() } ?: url?.toDomainLabel().orEmpty(),
            title = htmlToPlainText(title),
            description = description.takeIf { it.isNotBlank() }?.let(::htmlToPlainText),
            thumbnailUrl = image,
        )
    }

    private fun MastodonLinkPreview.toUi(): MastodonLinkPreviewUiModel =
        MastodonLinkPreviewUiModel(
            domain = domain,
            title = title,
            description = description,
            thumbnailUrl = thumbnailUrl,
        )

    private fun String.toDomainLabel(): String =
        removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")

    private fun String.toMediaType(): MastodonMediaType = when (lowercase()) {
        "image" -> MastodonMediaType.IMAGE
        "gifv" -> MastodonMediaType.GIFV
        "video" -> MastodonMediaType.VIDEO
        "audio" -> MastodonMediaType.AUDIO
        else -> MastodonMediaType.UNKNOWN
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
