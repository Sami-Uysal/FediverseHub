package com.samiuysal.fediversehub.feature.mastodon.data.remote

import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage

interface MastodonApi {
    suspend fun getHomeTimeline(
        instanceUrl: String,
        accessToken: String,
        page: MastodonTimelinePage = MastodonTimelinePage(),
    ): List<MastodonStatusDto>
}
