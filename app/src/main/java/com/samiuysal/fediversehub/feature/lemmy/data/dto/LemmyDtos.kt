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
data class LemmyPostViewDto(
    val post: LemmyPostDto,
    val creator: LemmyPersonDto,
    val community: LemmyCommunityDto,
    val counts: LemmyPostAggregatesDto,
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
)

@Serializable
data class LemmyCommunityDto(
    val id: Int? = null,
    val name: String,
    @SerialName("title") val title: String? = null,
    @SerialName("actor_id") val actorId: String? = null,
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
