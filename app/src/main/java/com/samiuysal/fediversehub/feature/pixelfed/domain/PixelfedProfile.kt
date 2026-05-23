package com.samiuysal.fediversehub.feature.pixelfed.domain

data class PixelfedProfile(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val headerUrl: String?,
    val note: String,
    val followersCount: Int,
    val followingCount: Int,
    val statusesCount: Int,
)
