package com.samiuysal.fediversehub.feature.lemmy.domain

data class LemmyPostPage(
    val page: Int = FIRST_PAGE,
    val limit: Int = DEFAULT_LIMIT,
    val sort: LemmySortType = LemmySortType.HOT,
    val feedType: LemmyFeedType = LemmyFeedType.ALL,
) {
    companion object {
        const val FIRST_PAGE = 1
        const val DEFAULT_LIMIT = 20
    }
}
