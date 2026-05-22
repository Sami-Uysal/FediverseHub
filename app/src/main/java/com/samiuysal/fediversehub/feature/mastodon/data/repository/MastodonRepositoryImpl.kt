package com.samiuysal.fediversehub.feature.mastodon.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.database.AppDatabase
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonCacheMapper
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonTimelineDao
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonApi
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonTimelineRemoteMediator
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPostDetail
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalPagingApi::class)
class MastodonRepositoryImpl @Inject constructor(
    private val mastodonApi: MastodonApi,
    private val database: AppDatabase,
    private val mastodonTimelineDao: MastodonTimelineDao,
) : MastodonRepository {
    override fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>> = Pager(
        config = PagingConfig(
            pageSize = MastodonTimelinePage.DEFAULT_LIMIT,
            initialLoadSize = MastodonTimelinePage.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        remoteMediator = MastodonTimelineRemoteMediator(
            account = account,
            mastodonApi = mastodonApi,
            database = database,
        ),
        pagingSourceFactory = {
            mastodonTimelineDao.homeTimelinePagingSource(
                accountId = account.id,
            )
        },
    ).flow.map { pagingData ->
        pagingData.map(MastodonCacheMapper::entityToDomain)
    }

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

    override suspend fun getPostDetail(
        account: Account,
        postId: String,
    ): AppResult<MastodonPostDetail> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val post = mastodonApi.getStatus(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                statusId = postId,
            )
            val context = mastodonApi.getStatusContext(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                statusId = postId,
            )
            AppResult.Success(
                MastodonPostDetail(
                    post = MastodonTimelineMapper.dtoToDomain(post),
                    ancestors = context.ancestors.map(MastodonTimelineMapper::dtoToDomain),
                    descendants = context.descendants.map(MastodonTimelineMapper::dtoToDomain),
                ),
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }
}
