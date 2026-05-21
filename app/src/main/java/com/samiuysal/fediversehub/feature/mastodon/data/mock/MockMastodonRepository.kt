package com.samiuysal.fediversehub.feature.mastodon.data.mock

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
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
}
