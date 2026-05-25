package com.samiuysal.fediversehub.feature.lemmy.domain

data class LemmySearchResult(
    val posts: List<LemmyPost> = emptyList(),
    val communities: List<LemmyCommunity> = emptyList(),
    val users: List<LemmySearchUser> = emptyList(),
)

data class LemmySearchUser(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String,
)

enum class LemmySearchCategory(val apiType: String) {
    POSTS("Posts"),
    COMMUNITIES("Communities"),
    USERS("Users"),
}

