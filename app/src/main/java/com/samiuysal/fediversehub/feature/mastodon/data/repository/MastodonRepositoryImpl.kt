package com.samiuysal.fediversehub.feature.mastodon.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonApi
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonTimelinePagingSource
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MastodonRepositoryImpl @Inject constructor(
    private val mastodonApi: MastodonApi,
) : MastodonRepository {
    override fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>> = Pager(
        config = PagingConfig(
            pageSize = MastodonTimelinePage.DEFAULT_LIMIT,
            initialLoadSize = MastodonTimelinePage.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            MastodonTimelinePagingSource(
                mastodonApi = mastodonApi,
                account = account,
            )
        },
    ).flow

    override suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage,
    ): AppResult<List<MastodonPost>> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val posts = mastodonApi.getHomeTimeline(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                page = page,
            ).map(MastodonTimelineMapper::dtoToDomain)
            AppResult.Success(posts)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }
}
