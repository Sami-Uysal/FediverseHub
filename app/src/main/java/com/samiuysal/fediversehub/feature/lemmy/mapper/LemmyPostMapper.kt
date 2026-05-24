package com.samiuysal.fediversehub.feature.lemmy.mapper

import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost

object LemmyPostMapper {
    fun domainToUi(domain: LemmyPost): LemmyPostUiModel = LemmyPostUiModel(
        id = domain.id,
        title = domain.title,
        community = domain.communityName,
        domain = domain.domain,
        author = "u/${domain.authorName}",
        timeAgo = domain.publishedAt ?: "now",
        score = domain.score,
        comments = domain.commentCount,
        previewText = domain.previewText,
        nestedComments = domain.comments.map(::commentToUi),
        url = domain.url,
        thumbnailUrl = domain.thumbnailUrl,
    )

    fun commentToUi(comment: LemmyComment): CommentUiModel = CommentUiModel(
        id = comment.id,
        parentId = comment.parentId,
        author = comment.authorName,
        text = comment.content,
        depth = comment.depth,
        isCollapsed = comment.isCollapsed,
        score = comment.score,
    )
}
