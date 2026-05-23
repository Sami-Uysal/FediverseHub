package com.samiuysal.fediversehub.feature.pixelfed.data.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedPost
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper

class PixelfedPublicFeedPagingSource(
    private val instanceUrl: String,
    private val accessToken: String?,
    private val pixelfedApi: PixelfedApi,
) : PagingSource<String, PixelfedPost>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, PixelfedPost> =
        try {
            val statuses = pixelfedApi.getPublicFeed(
                instanceUrl = instanceUrl,
                accessToken = accessToken,
                maxId = params.key,
                limit = params.loadSize.coerceIn(9, 24),
                local = true,
            )
            val posts = statuses.map(PixelfedMapper::statusToDomain)
                .filter { it.media.isNotEmpty() }
            Log.d(TAG, "Explore page loaded: instance=$instanceUrl, raw=${statuses.size}, media=${posts.size}")
            LoadResult.Page(
                data = posts,
                prevKey = null,
                nextKey = statuses.lastOrNull()?.id,
            )
        } catch (throwable: Throwable) {
            Log.d(TAG, "Explore page failed: instance=$instanceUrl, error=${throwable::class.simpleName}")
            LoadResult.Error(AppErrorException(NetworkErrorMapper.map(throwable)))
        }

    override fun getRefreshKey(state: PagingState<String, PixelfedPost>): String? = null

    private companion object {
        const val TAG = "PixelfedExplore"
    }
}
