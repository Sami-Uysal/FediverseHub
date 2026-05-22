package com.samiuysal.fediversehub.feature.mastodon.domain

import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MastodonRepository {
    fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>>

    suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage = MastodonTimelinePage(),
    ): AppResult<List<MastodonPost>>

    suspend fun getPostDetail(
        account: Account,
        postId: String,
    ): AppResult<MastodonPostDetail>
}
