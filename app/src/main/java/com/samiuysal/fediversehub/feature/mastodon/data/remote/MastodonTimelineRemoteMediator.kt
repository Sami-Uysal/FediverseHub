package com.samiuysal.fediversehub.feature.mastodon.data.remote

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.database.AppDatabase
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonCacheMapper
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonPostWithMedia
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonRemoteKeyEntity
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import kotlin.coroutines.cancellation.CancellationException

@OptIn(ExperimentalPagingApi::class)
class MastodonTimelineRemoteMediator(
    private val account: Account,
    private val mastodonApi: MastodonApi,
    private val database: AppDatabase,
) : RemoteMediator<Int, MastodonPostWithMedia>() {
    private val dao = database.mastodonTimelineDao()

    override suspend fun initialize(): InitializeAction {
        val refreshedAt = dao.remoteKey(account.id)?.refreshedAt ?: return InitializeAction.LAUNCH_INITIAL_REFRESH
        val cacheAge = System.currentTimeMillis() - refreshedAt
        return if (cacheAge < CACHE_TTL_MILLIS) {
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, MastodonPostWithMedia>,
    ): MediatorResult {
        val accessToken = account.accessToken
            ?: return MediatorResult.Error(AppErrorException(AppError.Unauthorized))

        return try {
            val maxId = when (loadType) {
                LoadType.REFRESH -> null
                LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
                LoadType.APPEND -> {
                    val key = dao.remoteKey(account.id)
                    key?.nextMaxId ?: return MediatorResult.Success(endOfPaginationReached = true)
                }
            }
            val statuses = mastodonApi.getHomeTimeline(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                page = MastodonTimelinePage(
                    maxId = maxId,
                    limit = state.config.pageSize.coerceAtMost(MastodonTimelinePage.DEFAULT_LIMIT),
                ),
            )
            val cachedAt = System.currentTimeMillis()

            database.withTransaction {
                val startPosition = if (loadType == LoadType.REFRESH) {
                    dao.clearRemoteKey(account.id)
                    dao.clearTimeline(account.id)
                    0L
                } else {
                    dao.maxTimelinePosition(account.id) + 1L
                }
                dao.upsertPosts(
                    posts = statuses.mapIndexed { index, status ->
                        MastodonCacheMapper.dtoToPostEntity(
                            dto = status,
                            account = account,
                            timelinePosition = startPosition + index,
                            cachedAt = cachedAt,
                        )
                    },
                )
                dao.upsertMedia(
                    media = statuses.flatMap { status ->
                        MastodonCacheMapper.dtoToMediaEntities(
                            dto = status,
                            accountId = account.id,
                        )
                    },
                )
                dao.upsertRemoteKey(
                    MastodonRemoteKeyEntity(
                        accountId = account.id,
                        nextMaxId = statuses.lastOrNull()?.id,
                        refreshedAt = cachedAt,
                    ),
                )
            }

            MediatorResult.Success(endOfPaginationReached = statuses.isEmpty())
        } catch (throwable: Throwable) {
            if (throwable is CancellationException) throw throwable
            MediatorResult.Error(AppErrorException(NetworkErrorMapper.map(throwable)))
        }
    }

    private companion object {
        const val CACHE_TTL_MILLIS = 2 * 60 * 1000L
    }
}
