package com.samiuysal.fediversehub.feature.mastodon.domain

data class MastodonProfile(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val headerUrl: String?,
    val note: String,
    val followersCount: Int,
    val followingCount: Int,
    val statusesCount: Int,
    val fields: List<MastodonProfileField>,
)

data class MastodonProfileField(
    val name: String,
    val value: String,
)

enum class MastodonProfileTimelineFilter {
    POSTS,
    REPLIES,
    MEDIA,
}
