package com.samiuysal.fediversehub.feature.mastodon.data.repository

import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.di.MockMastodonRepositoryBinding
import com.samiuysal.fediversehub.di.RealMastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SwitchingMastodonRepository @Inject constructor(
    @param:RealMastodonRepository private val realRepository: MastodonRepository,
    @param:MockMastodonRepositoryBinding private val mockRepository: MastodonRepository,
) : MastodonRepository {
    override fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>> =
        account.repository().getHomeTimelinePagingData(account)

    override suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage,
    ): AppResult<List<MastodonPost>> =
        account.repository().getHomeTimeline(account, page)

    private fun Account.repository(): MastodonRepository =
        if (accessToken.isNullOrBlank()) mockRepository else realRepository
}
