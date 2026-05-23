package com.samiuysal.fediversehub.feature.mastodon.data.mock

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonNotification

class MockMastodonNotificationsPagingSource(
    private val notifications: List<MastodonNotification>,
) : PagingSource<Int, MastodonNotification>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, MastodonNotification> {
        val page = params.key ?: 0
        val start = page * params.loadSize
        val data = notifications.drop(start).take(params.loadSize)
        return LoadResult.Page(
            data = data,
            prevKey = if (page == 0) null else page - 1,
            nextKey = if (data.isEmpty()) null else page + 1,
        )
    }

    override fun getRefreshKey(state: PagingState<Int, MastodonNotification>): Int? = null
}
