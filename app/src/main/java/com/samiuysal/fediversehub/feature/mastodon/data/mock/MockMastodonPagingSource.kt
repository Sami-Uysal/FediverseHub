package com.samiuysal.fediversehub.feature.mastodon.data.mock

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost

class MockMastodonPagingSource(
    private val posts: List<MastodonPost> = MockMastodonData.homeTimeline,
) : PagingSource<Int, MastodonPost>() {
    override fun getRefreshKey(state: PagingState<Int, MastodonPost>): Int? {
        val anchorPosition = state.anchorPosition ?: return null
        val anchorPage = state.closestPageToPosition(anchorPosition) ?: return null
        return anchorPage.prevKey?.plus(1) ?: anchorPage.nextKey?.minus(1)
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MastodonPost> {
        val page = params.key ?: FIRST_PAGE
        val fromIndex = (page - FIRST_PAGE) * params.loadSize
        if (fromIndex >= posts.size) {
            return LoadResult.Page(
                data = emptyList(),
                prevKey = if (page == FIRST_PAGE) null else page - 1,
                nextKey = null,
            )
        }

        val toIndex = minOf(fromIndex + params.loadSize, posts.size)
        return LoadResult.Page(
            data = posts.subList(fromIndex, toIndex),
            prevKey = if (page == FIRST_PAGE) null else page - 1,
            nextKey = if (toIndex < posts.size) page + 1 else null,
        )
    }

    private companion object {
        const val FIRST_PAGE = 1
    }
}
