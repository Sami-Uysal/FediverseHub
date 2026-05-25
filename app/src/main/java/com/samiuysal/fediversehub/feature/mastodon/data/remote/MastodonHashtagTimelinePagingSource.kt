package com.samiuysal.fediversehub.feature.mastodon.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper

class MastodonHashtagTimelinePagingSource(
    private val instanceUrl: String,
    private val accessToken: String,
    private val hashtag: String,
    private val limit: Int,
    private val mastodonApi: MastodonApi,
) : PagingSource<String, MastodonPost>() {
    override suspend fun load(params: LoadParams<String>): LoadResult<String, MastodonPost> =
        try {
            val statuses = mastodonApi.getHashtagTimeline(
                instanceUrl = instanceUrl,
                accessToken = accessToken,
                hashtag = hashtag,
                maxId = params.key,
                limit = limit,
            )
            val posts = statuses.map(MastodonTimelineMapper::dtoToDomain)
            LoadResult.Page(
                data = posts,
                prevKey = null,
                nextKey = statuses.lastOrNull()?.id,
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }

    override fun getRefreshKey(state: PagingState<String, MastodonPost>): String? = null
}
