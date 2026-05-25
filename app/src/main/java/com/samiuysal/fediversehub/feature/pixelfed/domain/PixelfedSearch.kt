package com.samiuysal.fediversehub.feature.pixelfed.domain

data class PixelfedSearchResult(
    val posts: List<PixelfedPost> = emptyList(),
    val accounts: List<PixelfedSearchAccount> = emptyList(),
    val hashtags: List<PixelfedHashtag> = emptyList(),
)

data class PixelfedSearchAccount(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val note: String,
)

data class PixelfedHashtag(
    val name: String,
)

enum class PixelfedSearchCategory(val apiType: String) {
    POSTS("statuses"),
    ACCOUNTS("accounts"),
    HASHTAGS("hashtags"),
}

