package com.samiuysal.fediversehub.feature.mastodon.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper

class MastodonTimelinePagingSource(
    private val mastodonApi: MastodonApi,
    private val account: Account,
) : PagingSource<String, MastodonPost>() {
    override fun getRefreshKey(state: PagingState<String, MastodonPost>): String? = null

    override suspend fun load(params: LoadParams<String>): LoadResult<String, MastodonPost> {
        val token = account.accessToken
            ?: return LoadResult.Error(IllegalStateException("Mastodon account needs login."))

        return try {
            val statuses = mastodonApi.getHomeTimeline(
                instanceUrl = account.instanceUrl,
                accessToken = token,
                page = MastodonTimelinePage(
                    maxId = params.key,
                    limit = params.loadSize.coerceAtMost(MastodonTimelinePage.DEFAULT_LIMIT),
                ),
            )
            val posts = statuses.map(MastodonTimelineMapper::dtoToDomain)
            LoadResult.Page(
                data = posts,
                prevKey = null,
                nextKey = posts.lastOrNull()?.id?.takeIf { posts.isNotEmpty() },
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(throwable)
        }
    }
}
