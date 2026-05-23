package com.samiuysal.fediversehub.feature.mastodon.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper

class MastodonAccountStatusesPagingSource(
    private val instanceUrl: String,
    private val accessToken: String,
    private val accountId: String,
    private val filter: MastodonProfileTimelineFilter,
    private val mastodonApi: MastodonApi,
) : PagingSource<String, MastodonPost>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, MastodonPost> {
        if (accessToken.isBlank()) {
            return LoadResult.Error(AppErrorException(AppError.Unauthorized))
        }
        return try {
            val posts = mastodonApi.getAccountStatuses(
                instanceUrl = instanceUrl,
                accessToken = accessToken,
                accountId = accountId,
                maxId = params.key,
                limit = params.loadSize.coerceIn(10, 40),
                excludeReplies = filter == MastodonProfileTimelineFilter.POSTS,
                onlyMedia = filter == MastodonProfileTimelineFilter.MEDIA,
            )
            LoadResult.Page(
                data = posts.map(MastodonTimelineMapper::dtoToDomain),
                prevKey = null,
                nextKey = posts.lastOrNull()?.id,
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(AppErrorException(NetworkErrorMapper.map(throwable)))
        }
    }

    override fun getRefreshKey(state: PagingState<String, MastodonPost>): String? = null
}
