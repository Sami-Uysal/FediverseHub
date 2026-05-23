package com.samiuysal.fediversehub.feature.pixelfed.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedPost
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper

class PixelfedProfileMediaPagingSource(
    private val instanceUrl: String,
    private val accessToken: String,
    private val accountId: String,
    private val pixelfedApi: PixelfedApi,
) : PagingSource<String, PixelfedPost>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, PixelfedPost> {
        if (accessToken.isBlank()) {
            return LoadResult.Error(AppErrorException(AppError.Unauthorized))
        }
        return try {
            val posts = pixelfedApi.getAccountStatuses(
                instanceUrl = instanceUrl,
                accessToken = accessToken,
                accountId = accountId,
                maxId = params.key,
                limit = params.loadSize.coerceIn(12, 45),
                onlyMedia = true,
            ).map(PixelfedMapper::statusToDomain)
                .filter { it.media.isNotEmpty() }
            LoadResult.Page(
                data = posts,
                prevKey = null,
                nextKey = posts.lastOrNull()?.id,
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(AppErrorException(NetworkErrorMapper.map(throwable)))
        }
    }

    override fun getRefreshKey(state: PagingState<String, PixelfedPost>): String? = null
}
