package com.samiuysal.fediversehub.feature.mastodon.data.mock

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPostDetail
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MockMastodonRepository @Inject constructor() : MastodonRepository {
    override fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>> = Pager(
        config = PagingConfig(
            pageSize = MastodonTimelinePage.DEFAULT_LIMIT,
            initialLoadSize = MastodonTimelinePage.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { MockMastodonPagingSource() },
    ).flow

    override suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage,
    ): AppResult<List<MastodonPost>> = AppResult.Success(MockMastodonData.homeTimeline)

    override suspend fun getPostDetail(
        account: Account,
        postId: String,
    ): AppResult<MastodonPostDetail> {
        val posts = MockMastodonData.homeTimeline
        val post = posts.firstOrNull { it.id == postId } ?: posts.first()
        return AppResult.Success(
            MastodonPostDetail(
                post = post,
                ancestors = posts.takeWhile { it.id != post.id }.takeLast(1),
                descendants = posts.dropWhile { it.id != post.id }.drop(1),
            ),
        )
    }
}
