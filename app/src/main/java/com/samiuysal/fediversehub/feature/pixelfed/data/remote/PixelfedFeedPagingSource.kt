package com.samiuysal.fediversehub.feature.pixelfed.data.remote

import android.util.Log
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.BuildConfig
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedPost
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper

class PixelfedFeedPagingSource(
    private val instanceUrl: String,
    private val accessToken: String,
    private val pixelfedApi: PixelfedApi,
) : PagingSource<String, PixelfedPost>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, PixelfedPost> {
        if (accessToken.isBlank()) {
            return LoadResult.Error(AppErrorException(AppError.Unauthorized))
        }
        return try {
            val statuses = pixelfedApi.getHomeFeed(
                instanceUrl = instanceUrl,
                accessToken = accessToken,
                maxId = params.key,
                limit = params.loadSize.coerceIn(6, 20),
            )
            val posts = statuses.map(PixelfedMapper::statusToDomain)
                .filter { it.media.isNotEmpty() }
            debugLog("Home feed page loaded: instance=$instanceUrl, raw=${statuses.size}, media=${posts.size}")
            LoadResult.Page(
                data = posts,
                prevKey = null,
                nextKey = statuses.lastOrNull()?.id,
            )
        } catch (throwable: Throwable) {
            debugLog("Home feed failed: instance=$instanceUrl, error=${throwable::class.simpleName}")
            LoadResult.Error(AppErrorException(NetworkErrorMapper.map(throwable)))
        }
    }

    override fun getRefreshKey(state: PagingState<String, PixelfedPost>): String? = null

    private fun debugLog(message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("PixelfedFeed", message)
        }
    }
}
