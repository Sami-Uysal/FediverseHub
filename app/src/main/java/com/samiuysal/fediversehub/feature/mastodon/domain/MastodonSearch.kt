package com.samiuysal.fediversehub.feature.mastodon.domain

data class MastodonSearchResult(
    val posts: List<MastodonPost> = emptyList(),
    val accounts: List<MastodonSearchAccount> = emptyList(),
    val hashtags: List<MastodonHashtag> = emptyList(),
)

data class MastodonSearchAccount(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val note: String,
)

data class MastodonHashtag(
    val name: String,
    val url: String?,
)

enum class MastodonSearchCategory(val apiType: String) {
    POSTS("statuses"),
    ACCOUNTS("accounts"),
    HASHTAGS("hashtags"),
}
