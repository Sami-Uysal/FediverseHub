package com.samiuysal.fediversehub.feature.mastodon.domain

import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account

interface MastodonRepository {
    suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage = MastodonTimelinePage(),
    ): AppResult<List<MastodonPost>>
}
