package com.samiuysal.fediversehub.feature.mastodon.data.mock

import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import javax.inject.Inject

class MockMastodonRepository @Inject constructor() : MastodonRepository {
    override suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage,
    ): AppResult<List<MastodonPost>> = AppResult.Success(MockMastodonData.homeTimeline)
}
