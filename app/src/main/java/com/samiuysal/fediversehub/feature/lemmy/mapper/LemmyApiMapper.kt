package com.samiuysal.fediversehub.feature.lemmy.mapper

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentViewDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunityViewDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyMentionViewDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPersonViewDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostViewDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyReplyViewDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmySearchResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyUserResponseDto
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyCommunity
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotification
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotificationType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyProfile
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchResult
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchUser
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType

object LemmyApiMapper {
    fun postViewToDomain(view: LemmyPostViewDto): LemmyPost =
        LemmyPost(
            id = view.post.id.toString(),
            title = view.post.name,
            communityId = view.community.id?.toString(),
            communityName = view.community.name,
            communityActorId = view.community.actorId,
            domain = view.post.url?.hostLabel() ?: "self",
            authorName = view.creator.displayName?.takeIf(String::isNotBlank) ?: view.creator.name,
            publishedAt = view.post.published,
            score = view.counts.score,
            commentCount = view.counts.comments,
            previewText = htmlToPlainText(view.post.body.orEmpty()),
            comments = emptyList(),
            url = view.post.url,
            thumbnailUrl = view.post.thumbnailUrl,
            myVote = view.myVote,
            saved = view.saved,
        )

    fun commentViewToDomain(view: LemmyCommentViewDto): LemmyComment =
        LemmyComment(
            id = view.comment.id.toString(),
            postId = view.comment.postId.toString(),
            parentId = view.comment.path.parentId(),
            authorName = view.creator.displayName?.takeIf(String::isNotBlank) ?: view.creator.name,
            content = htmlToPlainText(view.comment.content),
            depth = view.comment.path.depth(),
            isCollapsed = view.comment.deleted || view.comment.removed,
            score = view.counts.score,
            myVote = view.myVote,
        )

    fun searchToDomain(response: LemmySearchResponseDto): LemmySearchResult =
        LemmySearchResult(
            posts = response.posts.map(::postViewToDomain).distinctBy { it.id },
            communities = response.communities.map(::communityViewToDomain).distinctBy { it.id },
            users = response.users.map(::personViewToSearchUser).distinctBy { it.id.ifBlank { it.name } },
        )

    fun replyToNotification(view: LemmyReplyViewDto): LemmyNotification =
        LemmyNotification(
            id = view.commentReply.id.toString(),
            type = LemmyNotificationType.REPLY,
            postId = view.comment.postId.toString(),
            postTitle = view.post.name,
            communityName = view.community.name,
            actorName = view.creator.displayName?.takeIf(String::isNotBlank) ?: view.creator.name,
            text = htmlToPlainText(view.comment.content),
            score = view.counts.score,
            createdAt = view.comment.published,
            read = view.commentReply.read,
        )

    fun mentionToNotification(view: LemmyMentionViewDto): LemmyNotification =
        LemmyNotification(
            id = view.personMention.id.toString(),
            type = LemmyNotificationType.MENTION,
            postId = view.comment.postId.toString(),
            postTitle = view.post.name,
            communityName = view.community.name,
            actorName = view.creator.displayName?.takeIf(String::isNotBlank) ?: view.creator.name,
            text = htmlToPlainText(view.comment.content),
            score = view.counts.score,
            createdAt = view.comment.published,
            read = view.personMention.read,
        )

    fun communityViewToDomain(view: LemmyCommunityViewDto): LemmyCommunity =
        LemmyCommunity(
            id = view.community.id?.toString().orEmpty(),
            name = view.community.name,
            title = view.community.title?.takeIf(String::isNotBlank) ?: view.community.name,
            actorId = view.community.actorId,
            description = htmlToPlainText(view.community.description.orEmpty()),
            iconUrl = view.community.iconUrl,
            bannerUrl = view.community.bannerUrl,
            subscribers = view.counts.subscribers,
            posts = view.counts.posts,
            comments = view.counts.comments,
            subscribed = view.subscribed.equals("Subscribed", ignoreCase = true),
        )

    fun userToProfile(
        response: LemmyUserResponseDto,
        fallbackName: String,
        savedOnly: Boolean = false,
    ): LemmyProfile {
        val personView = response.personView
        val person = personView?.person
        val name = person?.name?.takeIf(String::isNotBlank) ?: fallbackName
        val displayName = person?.displayName?.takeIf(String::isNotBlank) ?: name
        return LemmyProfile(
            id = person?.id?.toString().orEmpty(),
            name = name,
            displayName = displayName,
            avatarUrl = person?.avatarUrl,
            bannerUrl = person?.bannerUrl,
            bio = htmlToPlainText(person?.bio.orEmpty()),
            postCount = personView?.counts?.postCount ?: response.posts.size,
            commentCount = personView?.counts?.commentCount ?: response.comments.size,
            posts = response.posts.map(::postViewToDomain),
            comments = response.comments.map(::commentViewToDomain),
            savedPosts = if (savedOnly) response.posts.map(::postViewToDomain) else response.posts.filter { it.saved }.map(::postViewToDomain),
            savedComments = if (savedOnly) response.comments.map(::commentViewToDomain) else emptyList(),
        )
    }

    private fun personViewToSearchUser(view: LemmyPersonViewDto): LemmySearchUser {
        val name = view.person.name
        return LemmySearchUser(
            id = view.person.id?.toString().orEmpty(),
            name = name,
            displayName = view.person.displayName?.takeIf(String::isNotBlank) ?: name,
            avatarUrl = view.person.avatarUrl,
            bio = htmlToPlainText(view.person.bio.orEmpty()),
        )
    }

    private fun String?.depth(): Int {
        val parts = this?.split(".").orEmpty().filter { it.isNotBlank() }
        return (parts.size - 2).coerceAtLeast(0)
    }

    private fun String?.parentId(): String? {
        val parts = this?.split(".").orEmpty().filter { it.isNotBlank() }
        return parts.dropLast(1).lastOrNull()?.takeUnless { it == "0" }
    }

    private fun String.hostLabel(): String =
        removePrefix("https://")
            .removePrefix("http://")
            .substringBefore("/")
            .removePrefix("www.")
            .takeIf(String::isNotBlank)
            ?: "link"

    private fun htmlToPlainText(value: String): String =
        HtmlCompat.fromHtml(value, HtmlCompat.FROM_HTML_MODE_COMPACT)
            .toString()
            .trim()
}

val LemmySortType.apiValue: String
    get() = when (this) {
        LemmySortType.ACTIVE -> "Active"
        LemmySortType.HOT -> "Hot"
        LemmySortType.NEW -> "New"
        LemmySortType.TOP -> "TopAll"
    }

val LemmyFeedType.apiValue: String
    get() = when (this) {
        LemmyFeedType.SUBSCRIBED -> "Subscribed"
        LemmyFeedType.LOCAL -> "Local"
        LemmyFeedType.ALL -> "All"
    }
