package com.samiuysal.fediversehub.feature.mastodon.data.repository

import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.map
import com.samiuysal.fediversehub.core.cache.FediverseMemoryCache
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.database.AppDatabase
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.core.performance.FeedLoadPolicy
import com.samiuysal.fediversehub.core.performance.FeedSurface
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonCacheMapper
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonTimelineDao
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonAccountStatusesPagingSource
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonApi
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonHashtagTimelinePagingSource
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonNotificationsPagingSource
import com.samiuysal.fediversehub.feature.mastodon.data.remote.MastodonTimelineRemoteMediator
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonNotification
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPostDetail
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfile
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRelationship
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchCategory
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchResult
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonHashtag
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTrendLink
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonProfileMapper
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonSearchMapper
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

@OptIn(ExperimentalPagingApi::class)
class MastodonRepositoryImpl @Inject constructor(
    private val mastodonApi: MastodonApi,
    private val database: AppDatabase,
    private val mastodonTimelineDao: MastodonTimelineDao,
    private val feedLoadPolicy: FeedLoadPolicy,
    private val memoryCache: FediverseMemoryCache,
) : MastodonRepository {
    override fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>> = Pager(
        config = PagingConfig(
            pageSize = feedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.HOME),
            initialLoadSize = feedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.HOME),
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

    override fun getNotificationsPagingData(
        account: Account,
    ): Flow<PagingData<MastodonNotification>> {
        val accessToken = account.accessToken.orEmpty()
        return Pager(
            config = PagingConfig(
            pageSize = feedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.HOME),
            initialLoadSize = feedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.HOME),
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                MastodonNotificationsPagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = accessToken,
                    mastodonApi = mastodonApi,
                )
            },
        ).flow
    }

    override fun getAccountStatusesPagingData(
        account: Account,
        accountId: String,
        filter: MastodonProfileTimelineFilter,
    ): Flow<PagingData<MastodonPost>> {
        val accessToken = account.accessToken.orEmpty()
        return Pager(
            config = PagingConfig(
                pageSize = feedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.PROFILE),
                initialLoadSize = feedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.PROFILE),
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                MastodonAccountStatusesPagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = accessToken,
                    accountId = accountId,
                    filter = filter,
                    mastodonApi = mastodonApi,
                )
            },
        ).flow
    }

    override fun getHashtagTimelinePagingData(
        account: Account,
        hashtag: String,
    ): Flow<PagingData<MastodonPost>> {
        val accessToken = account.accessToken.orEmpty()
        val pageSize = feedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.EXPLORE)
        return Pager(
            config = PagingConfig(
                pageSize = pageSize,
                initialLoadSize = pageSize,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                MastodonHashtagTimelinePagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = accessToken,
                    hashtag = hashtag,
                    limit = pageSize,
                    mastodonApi = mastodonApi,
                )
            },
        ).flow
    }

    override suspend fun getOwnProfile(
        account: Account,
    ): AppResult<MastodonProfile> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("own-profile")) {
                    val profile = mastodonApi.verifyCredentials(
                        instanceUrl = account.instanceUrl,
                        accessToken = accessToken,
                    )
                    withContext(Dispatchers.Default) {
                        MastodonProfileMapper.dtoToDomain(profile)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getProfile(
        account: Account,
        accountId: String,
    ): AppResult<MastodonProfile> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("profile", accountId)) {
                    val profile = mastodonApi.getAccount(
                        instanceUrl = account.instanceUrl,
                        accessToken = accessToken,
                        accountId = accountId,
                    )
                    withContext(Dispatchers.Default) {
                        MastodonProfileMapper.dtoToDomain(profile)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getRelationship(
        account: Account,
        accountId: String,
    ): AppResult<MastodonRelationship> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("relationship", accountId),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    mastodonApi.getRelationships(
                        instanceUrl = account.instanceUrl,
                        accessToken = accessToken,
                        accountIds = listOf(accountId),
                    ).firstOrNull()?.let {
                        MastodonRelationship(
                            accountId = it.id,
                            following = it.following,
                            requested = it.requested,
                        )
                    } ?: MastodonRelationship(accountId = accountId, following = false, requested = false)
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun setFollowing(
        account: Account,
        accountId: String,
        following: Boolean,
    ): AppResult<MastodonRelationship> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val dto = if (following) {
                mastodonApi.followAccount(account.instanceUrl, accessToken, accountId)
            } else {
                mastodonApi.unfollowAccount(account.instanceUrl, accessToken, accountId)
            }
            val relationship = MastodonRelationship(
                accountId = dto.id,
                following = dto.following,
                requested = dto.requested,
            )
            memoryCache.put(account.cacheKey("relationship", accountId), relationship)
            AppResult.Success(relationship)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun search(
        account: Account,
        query: String,
        category: MastodonSearchCategory,
    ): AppResult<MastodonSearchResult> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val result = mastodonApi.search(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                query = query,
                type = category.apiType,
            )
            AppResult.Success(MastodonSearchMapper.dtoToDomain(result))
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getTrendingStatuses(account: Account): AppResult<List<MastodonPost>> {
        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("trends-statuses")) {
                    val statuses = mastodonApi.getTrendingStatuses(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                    )
                    withContext(Dispatchers.Default) {
                        statuses.map(MastodonTimelineMapper::dtoToDomain)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getTrendingTags(account: Account): AppResult<List<MastodonHashtag>> {
        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("trends-tags")) {
                    mastodonApi.getTrendingTags(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                    ).map { MastodonHashtag(name = it.name, url = it.url) }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getTrendingLinks(account: Account): AppResult<List<MastodonTrendLink>> {
        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("trends-links")) {
                    mastodonApi.getTrendingLinks(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                    ).map {
                        MastodonTrendLink(
                            url = it.url.orEmpty(),
                            title = it.title,
                            description = it.description,
                            imageUrl = it.image,
                            providerName = it.providerName,
                        )
                    }.filter { it.url.isNotBlank() }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
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
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("post-detail", postId)) {
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
                    withContext(Dispatchers.Default) {
                        MastodonPostDetail(
                            post = MastodonTimelineMapper.dtoToDomain(post),
                            ancestors = context.ancestors.map(MastodonTimelineMapper::dtoToDomain),
                            descendants = context.descendants.map(MastodonTimelineMapper::dtoToDomain),
                        )
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun setFavourite(
        account: Account,
        postId: String,
        favourite: Boolean,
    ): AppResult<MastodonPost> = runStatusAction(account, postId) { token ->
        if (favourite) {
            mastodonApi.favouriteStatus(account.instanceUrl, token, postId)
        } else {
            mastodonApi.unfavouriteStatus(account.instanceUrl, token, postId)
        }
    }

    override suspend fun setBoosted(
        account: Account,
        postId: String,
        boosted: Boolean,
    ): AppResult<MastodonPost> = runStatusAction(account, postId) { token ->
        if (boosted) {
            mastodonApi.boostStatus(account.instanceUrl, token, postId)
        } else {
            mastodonApi.unboostStatus(account.instanceUrl, token, postId)
        }
    }

    override suspend fun setBookmarked(
        account: Account,
        postId: String,
        bookmarked: Boolean,
    ): AppResult<MastodonPost> = runStatusAction(account, postId) { token ->
        if (bookmarked) {
            mastodonApi.bookmarkStatus(account.instanceUrl, token, postId)
        } else {
            mastodonApi.unbookmarkStatus(account.instanceUrl, token, postId)
        }
    }

    override suspend fun replyToPost(
        account: Account,
        postId: String,
        text: String,
        visibility: String,
    ): AppResult<MastodonPost> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val dto = mastodonApi.replyToStatus(account.instanceUrl, accessToken, postId, text, visibility)
            mastodonTimelineDao.incrementReplyCount(accountId = account.id, statusId = postId)
            AppResult.Success(MastodonTimelineMapper.dtoToDomain(dto))
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun createPost(
        account: Account,
        text: String,
        visibility: String,
        spoilerText: String?,
    ): AppResult<MastodonPost> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val dto = mastodonApi.createStatus(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                text = text,
                visibility = visibility,
                spoilerText = spoilerText,
            )
            val now = System.currentTimeMillis()
            val position = mastodonTimelineDao.minTimelinePosition(account.id) - 1
            mastodonTimelineDao.upsertPosts(
                listOf(
                    MastodonCacheMapper.dtoToPostEntity(
                        dto = dto,
                        account = account,
                        timelinePosition = position,
                        cachedAt = now,
                    ),
                ),
            )
            mastodonTimelineDao.upsertMedia(
                MastodonCacheMapper.dtoToMediaEntities(dto = dto, accountId = account.id),
            )
            AppResult.Success(MastodonTimelineMapper.dtoToDomain(dto))
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    private suspend fun runStatusAction(
        account: Account,
        postId: String,
        action: suspend (String) -> com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto,
    ): AppResult<MastodonPost> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val dto = action(accessToken)
            val post = MastodonTimelineMapper.dtoToDomain(dto)
            memoryCache.put(account.cacheKey("post", postId), post)
            mastodonTimelineDao.updateStatusActions(
                accountId = account.id,
                statusId = postId,
                replyCount = post.replyCount,
                reblogCount = post.reblogCount,
                favouriteCount = post.favouriteCount,
                isReblogged = post.isReblogged,
                isFavourited = post.isFavourited,
                isBookmarked = post.isBookmarked,
            )
            AppResult.Success(post)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    private fun Account.cacheKey(type: String, id: String = "self"): String =
        "mastodon:${instanceUrl.normalizedInstance()}:${cacheOwnerKey()}:$type:$id"

    private fun Account.cacheOwnerKey(): String =
        if (accessToken.isNullOrBlank()) "public" else id

    private fun String.normalizedInstance(): String =
        removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
            .lowercase()
}
