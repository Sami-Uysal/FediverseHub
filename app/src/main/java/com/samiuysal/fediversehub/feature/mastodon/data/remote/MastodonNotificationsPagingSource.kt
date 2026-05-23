package com.samiuysal.fediversehub.feature.mastodon.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonNotification
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonNotificationMapper

class MastodonNotificationsPagingSource(
    private val instanceUrl: String,
    private val accessToken: String,
    private val mastodonApi: MastodonApi,
) : PagingSource<String, MastodonNotification>() {
    override suspend fun load(
        params: LoadParams<String>,
    ): LoadResult<String, MastodonNotification> {
        if (accessToken.isBlank()) {
            return LoadResult.Error(AppErrorException(AppError.Unauthorized))
        }
        return try {
            val notifications = mastodonApi.getNotifications(
                instanceUrl = instanceUrl,
                accessToken = accessToken,
                maxId = params.key,
                limit = params.loadSize.coerceIn(10, 40),
            )
            LoadResult.Page(
                data = notifications.map(MastodonNotificationMapper::dtoToDomain),
                prevKey = null,
                nextKey = notifications.lastOrNull()?.id,
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(AppErrorException(NetworkErrorMapper.map(throwable)))
        }
    }

    override fun getRefreshKey(
        state: PagingState<String, MastodonNotification>,
    ): String? = null
}
