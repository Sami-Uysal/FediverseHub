package com.samiuysal.fediversehub.feature.lemmy.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LemmyLoginRequestDto(
    @SerialName("username_or_email") val usernameOrEmail: String,
    val password: String,
    @SerialName("totp_2fa_token") val totp2faToken: String? = null,
)

@Serializable
data class LemmyLoginResponseDto(
    val jwt: String? = null,
)

@Serializable
data class LemmySiteResponseDto(
    @SerialName("my_user") val myUser: LemmyMyUserDto? = null,
)

@Serializable
data class LemmyMyUserDto(
    @SerialName("local_user_view") val localUserView: LemmyLocalUserViewDto? = null,
)

@Serializable
data class LemmyLocalUserViewDto(
    val person: LemmyPersonDto? = null,
)

@Serializable
data class LemmyPostsResponseDto(
    val posts: List<LemmyPostViewDto> = emptyList(),
)

@Serializable
data class LemmyPostResponseDto(
    @SerialName("post_view") val postView: LemmyPostViewDto,
)

@Serializable
data class LemmyCommentsResponseDto(
    val comments: List<LemmyCommentViewDto> = emptyList(),
)

@Serializable
data class LemmySearchResponseDto(
    val posts: List<LemmyPostViewDto> = emptyList(),
    val communities: List<LemmyCommunityViewDto> = emptyList(),
    val users: List<LemmyPersonViewDto> = emptyList(),
)

@Serializable
data class LemmyRepliesResponseDto(
    val replies: List<LemmyReplyViewDto> = emptyList(),
)

@Serializable
data class LemmyMentionsResponseDto(
    val mentions: List<LemmyMentionViewDto> = emptyList(),
)

@Serializable
data class LemmyCreateCommentRequestDto(
    @SerialName("post_id") val postId: Int,
    @SerialName("parent_id") val parentId: Int? = null,
    val content: String,
    val auth: String? = null,
)

@Serializable
data class LemmyCreateCommentResponseDto(
    @SerialName("comment_view") val commentView: LemmyCommentViewDto,
)

@Serializable
data class LemmyCommunitiesResponseDto(
    val communities: List<LemmyCommunityViewDto> = emptyList(),
)

@Serializable
data class LemmyCommunityResponseDto(
    @SerialName("community_view") val communityView: LemmyCommunityViewDto,
)

@Serializable
data class LemmyPostActionRequestDto(
    @SerialName("post_id") val postId: Int,
    val score: Int? = null,
    val save: Boolean? = null,
    val auth: String? = null,
)

@Serializable
data class LemmyPostActionResponseDto(
    @SerialName("post_view") val postView: LemmyPostViewDto,
)

@Serializable
data class LemmyCreatePostRequestDto(
    @SerialName("community_id") val communityId: Int,
    val name: String,
    val url: String? = null,
    val body: String? = null,
    val auth: String? = null,
)

@Serializable
data class LemmyCreatePostResponseDto(
    @SerialName("post_view") val postView: LemmyPostViewDto,
)

@Serializable
data class LemmyCommentActionRequestDto(
    @SerialName("comment_id") val commentId: Int,
    val score: Int,
    val auth: String? = null,
)

@Serializable
data class LemmyCommentActionResponseDto(
    @SerialName("comment_view") val commentView: LemmyCommentViewDto,
)

@Serializable
data class LemmyCommunityFollowRequestDto(
    @SerialName("community_id") val communityId: Int,
    val follow: Boolean,
    val auth: String? = null,
)

@Serializable
data class LemmyUserResponseDto(
    @SerialName("person_view") val personView: LemmyPersonViewDto? = null,
    val posts: List<LemmyPostViewDto> = emptyList(),
    val comments: List<LemmyCommentViewDto> = emptyList(),
)

@Serializable
data class LemmyReplyViewDto(
    @SerialName("comment_reply") val commentReply: LemmyReplyMarkerDto,
    val comment: LemmyCommentDto,
    val creator: LemmyPersonDto,
    val post: LemmyPostDto,
    val community: LemmyCommunityDto,
    val counts: LemmyCommentAggregatesDto = LemmyCommentAggregatesDto(),
    @SerialName("my_vote") val myVote: Int? = null,
)

@Serializable
data class LemmyMentionViewDto(
    @SerialName("person_mention") val personMention: LemmyReplyMarkerDto,
    val comment: LemmyCommentDto,
    val creator: LemmyPersonDto,
    val post: LemmyPostDto,
    val community: LemmyCommunityDto,
    val counts: LemmyCommentAggregatesDto = LemmyCommentAggregatesDto(),
    @SerialName("my_vote") val myVote: Int? = null,
)

@Serializable
data class LemmyReplyMarkerDto(
    val id: Int,
    val read: Boolean = false,
)

@Serializable
data class LemmyPersonViewDto(
    val person: LemmyPersonDto,
    val counts: LemmyPersonAggregatesDto = LemmyPersonAggregatesDto(),
)

@Serializable
data class LemmyPostViewDto(
    val post: LemmyPostDto,
    val creator: LemmyPersonDto,
    val community: LemmyCommunityDto,
    val counts: LemmyPostAggregatesDto,
    @SerialName("my_vote") val myVote: Int? = null,
    val saved: Boolean = false,
)

@Serializable
data class LemmyPostDto(
    val id: Int,
    val name: String,
    val url: String? = null,
    val body: String? = null,
    @SerialName("thumbnail_url") val thumbnailUrl: String? = null,
    val published: String? = null,
)

@Serializable
data class LemmyPersonDto(
    val id: Int? = null,
    val name: String,
    @SerialName("display_name") val displayName: String? = null,
    @SerialName("avatar") val avatarUrl: String? = null,
    @SerialName("banner") val bannerUrl: String? = null,
    val bio: String? = null,
)

@Serializable
data class LemmyPersonAggregatesDto(
    @SerialName("post_count") val postCount: Int = 0,
    @SerialName("comment_count") val commentCount: Int = 0,
)

@Serializable
data class LemmyCommunityDto(
    val id: Int? = null,
    val name: String,
    @SerialName("title") val title: String? = null,
    @SerialName("actor_id") val actorId: String? = null,
    val description: String? = null,
    @SerialName("icon") val iconUrl: String? = null,
    @SerialName("banner") val bannerUrl: String? = null,
)

@Serializable
data class LemmyPostAggregatesDto(
    val score: Int = 0,
    val comments: Int = 0,
)

@Serializable
data class LemmyCommentViewDto(
    val comment: LemmyCommentDto,
    val creator: LemmyPersonDto,
    val counts: LemmyCommentAggregatesDto,
    @SerialName("my_vote") val myVote: Int? = null,
)

@Serializable
data class LemmyCommentDto(
    val id: Int,
    @SerialName("post_id") val postId: Int,
    val content: String,
    val path: String? = null,
    val published: String? = null,
    val deleted: Boolean = false,
    val removed: Boolean = false,
)

@Serializable
data class LemmyCommentAggregatesDto(
    val score: Int = 0,
)

@Serializable
data class LemmyCommunityViewDto(
    val community: LemmyCommunityDto,
    val counts: LemmyCommunityAggregatesDto,
    val subscribed: String? = null,
)

@Serializable
data class LemmyCommunityAggregatesDto(
    val subscribers: Int = 0,
    val posts: Int = 0,
    val comments: Int = 0,
)
