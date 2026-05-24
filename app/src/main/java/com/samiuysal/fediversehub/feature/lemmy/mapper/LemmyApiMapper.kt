package com.samiuysal.fediversehub.feature.lemmy.mapper

import androidx.core.text.HtmlCompat
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentViewDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostViewDto
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType

object LemmyApiMapper {
    fun postViewToDomain(view: LemmyPostViewDto): LemmyPost =
        LemmyPost(
            id = view.post.id.toString(),
            title = view.post.name,
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
        )

    fun commentViewToDomain(view: LemmyCommentViewDto): LemmyComment =
        LemmyComment(
            id = view.comment.id.toString(),
            parentId = view.comment.path.parentId(),
            authorName = view.creator.displayName?.takeIf(String::isNotBlank) ?: view.creator.name,
            content = htmlToPlainText(view.comment.content),
            depth = view.comment.path.depth(),
            isCollapsed = view.comment.deleted || view.comment.removed,
            score = view.counts.score,
        )

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
        LemmySortType.TOP -> "Top"
    }

val LemmyFeedType.apiValue: String
    get() = when (this) {
        LemmyFeedType.SUBSCRIBED -> "Subscribed"
        LemmyFeedType.LOCAL -> "Local"
        LemmyFeedType.ALL -> "All"
    }
