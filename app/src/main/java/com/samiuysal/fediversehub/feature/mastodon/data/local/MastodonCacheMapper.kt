package com.samiuysal.fediversehub.feature.mastodon.data.local

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonMediaAttachmentDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonPreviewCardDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonLinkPreview
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaAttachment
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaType
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost

object MastodonCacheMapper {
    fun dtoToPostEntity(
        dto: MastodonStatusDto,
        account: Account,
        timelinePosition: Long,
        cachedAt: Long,
    ): MastodonPostEntity {
        val status = dto.reblog ?: dto
        val displayName = status.account.displayName.ifBlank { status.account.username }
        val boostedByDisplayName = dto.reblog?.let {
            dto.account.displayName.ifBlank { dto.account.username }
        }
        val card = status.card
        return MastodonPostEntity(
            localId = dto.localId(account.id),
            accountId = account.id,
            instanceUrl = account.instanceUrl,
            remoteId = dto.id,
            statusRemoteId = status.id,
            uri = status.uri,
            url = status.url,
            createdAt = status.createdAt,
            authorRemoteId = status.account.id,
            authorDisplayName = htmlToPlainText(displayName),
            authorUsername = status.account.acct.ifBlank { status.account.username },
            authorAvatarUrl = status.account.avatarStatic ?: status.account.avatar,
            contentText = htmlToPlainText(status.content),
            boostedByDisplayName = boostedByDisplayName?.let(::htmlToPlainText),
            boostedByAvatarUrl = dto.reblog?.let { dto.account.avatarStatic ?: dto.account.avatar },
            inReplyToAccountId = status.inReplyToAccountId,
            linkDomain = card?.domainLabel(),
            linkTitle = card?.title?.takeIf { it.isNotBlank() }?.let(::htmlToPlainText),
            linkDescription = card?.description?.takeIf { it.isNotBlank() }?.let(::htmlToPlainText),
            linkThumbnailUrl = card?.image,
            replyCount = status.repliesCount,
            reblogCount = status.reblogsCount,
            favouriteCount = status.favouritesCount,
            isReblogged = status.reblogged,
            isFavourited = status.favourited,
            isBookmarked = status.bookmarked,
            visibility = status.visibility,
            timelinePosition = timelinePosition,
            cachedAt = cachedAt,
        )
    }

    fun dtoToMediaEntities(
        dto: MastodonStatusDto,
        accountId: String,
    ): List<MastodonMediaEntity> {
        val status = dto.reblog ?: dto
        val postLocalId = dto.localId(accountId)
        return status.mediaAttachments.mapIndexed { index, media ->
            media.toEntity(
                accountId = accountId,
                postLocalId = postLocalId,
                sortOrder = index,
            )
        }
    }

    fun entityToDomain(value: MastodonPostWithMedia): MastodonPost {
        val post = value.post
        return MastodonPost(
            id = post.remoteId,
            detailId = post.statusRemoteId,
            authorDisplayName = post.authorDisplayName,
            authorUsername = post.authorUsername,
            authorAvatarUrl = post.authorAvatarUrl,
            createdAt = post.createdAt,
            contentText = post.contentText,
            mediaAttachments = value.media
                .sortedBy(MastodonMediaEntity::sortOrder)
                .map(MastodonCacheMapper::mediaEntityToDomain),
            boostedByDisplayName = post.boostedByDisplayName,
            boostedByAvatarUrl = post.boostedByAvatarUrl,
            inReplyToAccountId = post.inReplyToAccountId,
            linkPreview = post.linkTitle?.let {
                MastodonLinkPreview(
                    domain = post.linkDomain.orEmpty(),
                    title = it,
                    description = post.linkDescription,
                    thumbnailUrl = post.linkThumbnailUrl,
                )
            },
            replyCount = post.replyCount,
            reblogCount = post.reblogCount,
            favouriteCount = post.favouriteCount,
            isReblogged = post.isReblogged,
            isFavourited = post.isFavourited,
            isBookmarked = post.isBookmarked,
            visibility = post.visibility,
            url = post.url ?: post.uri,
        )
    }

    private fun MastodonMediaAttachmentDto.toEntity(
        accountId: String,
        postLocalId: String,
        sortOrder: Int,
    ): MastodonMediaEntity = MastodonMediaEntity(
        localId = "$postLocalId|$id",
        postLocalId = postLocalId,
        accountId = accountId,
        remoteId = id,
        type = type,
        url = url,
        previewUrl = previewUrl,
        description = description,
        sortOrder = sortOrder,
    )

    private fun mediaEntityToDomain(entity: MastodonMediaEntity): MastodonMediaAttachment =
        MastodonMediaAttachment(
            id = entity.remoteId,
            type = entity.type.toMediaType(),
            url = entity.url,
            previewUrl = entity.previewUrl,
            description = entity.description,
        )

    private fun MastodonPreviewCardDto.domainLabel(): String =
        providerName?.takeIf { it.isNotBlank() }
            ?: url?.removePrefix("https://")
                ?.removePrefix("http://")
                ?.substringBefore("/")
                .orEmpty()

    private fun MastodonStatusDto.localId(accountId: String): String = "$accountId|$id"

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
}
