package com.samiuysal.fediversehub.feature.lemmy.mapper

import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyCommunity
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost

object LemmyPostMapper {
    fun domainToUi(domain: LemmyPost): LemmyPostUiModel = LemmyPostUiModel(
        id = domain.id,
        title = domain.title,
        communityId = domain.communityId,
        communityActorId = domain.communityActorId,
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
        isUpvoted = domain.myVote == 1,
        isDownvoted = domain.myVote == -1,
        isSaved = domain.saved,
    )

    fun commentToUi(comment: LemmyComment): CommentUiModel = CommentUiModel(
        id = comment.id,
        postId = comment.postId,
        postTitle = comment.postTitle,
        parentId = comment.parentId,
        author = comment.authorName,
        text = comment.content,
        depth = comment.depth,
        isCollapsed = comment.isCollapsed,
        score = comment.score,
        isUpvoted = comment.myVote == 1,
        isDownvoted = comment.myVote == -1,
    )

    fun communityToUi(community: LemmyCommunity): LemmyCommunityUiModel = LemmyCommunityUiModel(
        id = community.id,
        name = community.name,
        title = community.title,
        actorId = community.actorId,
        description = community.description,
        iconUrl = community.iconUrl,
        bannerUrl = community.bannerUrl,
        subscribers = community.subscribers,
        posts = community.posts,
        comments = community.comments,
        isSubscribed = community.subscribed,
    )
}
