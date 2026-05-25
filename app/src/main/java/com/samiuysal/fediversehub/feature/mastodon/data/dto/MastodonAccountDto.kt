package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonAccountDto(
    val id: String,
    val username: String,
    val acct: String,
    @SerialName("display_name") val displayName: String = "",
    val note: String = "",
    val avatar: String? = null,
    @SerialName("avatar_static") val avatarStatic: String? = null,
    val header: String? = null,
    @SerialName("header_static") val headerStatic: String? = null,
    val url: String? = null,
    @SerialName("followers_count") val followersCount: Int = 0,
    @SerialName("following_count") val followingCount: Int = 0,
    @SerialName("statuses_count") val statusesCount: Int = 0,
    val fields: List<MastodonAccountFieldDto> = emptyList(),
)

@Serializable
data class MastodonAccountFieldDto(
    val name: String,
    val value: String,
)

@Serializable
data class MastodonRelationshipDto(
    val id: String,
    val following: Boolean = false,
    @SerialName("followed_by") val followedBy: Boolean = false,
    val blocking: Boolean = false,
    val muting: Boolean = false,
    val requested: Boolean = false,
)
