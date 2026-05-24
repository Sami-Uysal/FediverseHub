package com.samiuysal.fediversehub.feature.lemmy.data.remote

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyApiMapper
import com.samiuysal.fediversehub.feature.lemmy.mapper.apiValue

class LemmyPostPagingSource(
    private val instanceUrl: String,
    private val accessToken: String?,
    private val sort: LemmySortType,
    private val feedType: LemmyFeedType,
    private val communityName: String? = null,
    private val lemmyApi: LemmyApi,
) : PagingSource<Int, LemmyPost>() {
    override fun getRefreshKey(state: PagingState<Int, LemmyPost>): Int? =
        state.anchorPosition?.let { anchor ->
            state.closestPageToPosition(anchor)?.prevKey?.plus(1)
                ?: state.closestPageToPosition(anchor)?.nextKey?.minus(1)
        }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, LemmyPost> =
        try {
            val page = params.key ?: LemmyPostPage.FIRST_PAGE
            val limit = params.loadSize.coerceIn(1, LemmyPostPage.DEFAULT_LIMIT)
            val response = lemmyApi.getPosts(
                instanceUrl = instanceUrl,
                accessToken = accessToken,
                page = page,
                limit = limit,
                sort = sort.apiValue,
                feedType = feedType.apiValue,
                communityName = communityName,
            )
            val posts = response.posts.map(LemmyApiMapper::postViewToDomain)
            LoadResult.Page(
                data = posts,
                prevKey = if (page == LemmyPostPage.FIRST_PAGE) null else page - 1,
                nextKey = if (posts.size < limit) null else page + 1,
            )
        } catch (throwable: Throwable) {
            LoadResult.Error(AppErrorException(NetworkErrorMapper.map(throwable)))
        }
}
