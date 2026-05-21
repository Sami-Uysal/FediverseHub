package com.samiuysal.fediversehub.feature.mastodon.domain

data class MastodonTimelinePage(
    val maxId: String? = null,
    val sinceId: String? = null,
    val minId: String? = null,
    val limit: Int = DEFAULT_LIMIT,
) {
    companion object {
        const val DEFAULT_LIMIT = 20
    }
}
