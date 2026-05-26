package com.samiuysal.fediversehub.feature.pixelfed.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.cache.FediverseMemoryCache
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.core.performance.FeedLoadPolicy
import com.samiuysal.fediversehub.core.performance.FeedSurface
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedApi
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedFeedPagingSource
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedProfileMediaPagingSource
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedPublicFeedPagingSource
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedComment
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedNotification
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedPost
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedProfile
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRelationship
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchCategory
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchResult
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class PixelfedRepositoryImpl @Inject constructor(
    private val pixelfedApi: PixelfedApi,
    private val feedLoadPolicy: FeedLoadPolicy,
    private val memoryCache: FediverseMemoryCache,
) : PixelfedRepository {
    override fun getHomeFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>> =
        Pager(
            config = PagingConfig(
                pageSize = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.HOME),
                initialLoadSize = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.HOME),
                prefetchDistance = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.HOME),
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                PixelfedFeedPagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken.orEmpty(),
                    pixelfedApi = pixelfedApi,
                )
            },
        ).flow

    override fun getProfileMediaPagingData(account: Account): Flow<PagingData<PixelfedPost>> =
        getProfileMediaPagingData(account = account, accountId = account.pixelfedRemoteId())

    override fun getProfileMediaPagingData(
        account: Account,
        accountId: String,
    ): Flow<PagingData<PixelfedPost>> =
        Pager(
            config = PagingConfig(
                pageSize = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.PROFILE),
                initialLoadSize = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.PROFILE),
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                PixelfedProfileMediaPagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken.orEmpty(),
                    accountId = accountId,
                    pixelfedApi = pixelfedApi,
                )
            },
        ).flow

    override fun getExploreFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>> =
        Pager(
            config = PagingConfig(
                pageSize = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.EXPLORE),
                initialLoadSize = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.EXPLORE),
                prefetchDistance = feedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.EXPLORE) / 2,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                PixelfedPublicFeedPagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken,
                    pixelfedApi = pixelfedApi,
                )
            },
        ).flow

    override suspend fun getOwnProfile(account: Account): AppResult<PixelfedProfile> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("own-profile")) {
                    val profile = pixelfedApi.verifyCredentials(account.instanceUrl, accessToken)
                    withContext(Dispatchers.Default) {
                        PixelfedMapper.accountToProfile(profile)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getProfile(account: Account, accountId: String): AppResult<PixelfedProfile> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("profile", accountId),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val profile = pixelfedApi.getAccount(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        accountId = accountId,
                    )
                    withContext(Dispatchers.Default) {
                        PixelfedMapper.accountToProfile(profile)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getRelationship(account: Account, accountId: String): AppResult<PixelfedRelationship> {
        val accessToken = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("relationship", accountId),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    pixelfedApi.getRelationships(
                        instanceUrl = account.instanceUrl,
                        accessToken = accessToken,
                        accountIds = listOf(accountId),
                    ).firstOrNull()?.let {
                        PixelfedRelationship(
                            accountId = it.id,
                            following = it.following,
                            requested = it.requested,
                        )
                    } ?: PixelfedRelationship(accountId = accountId, following = false, requested = false)
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
    ): AppResult<PixelfedRelationship> {
        val accessToken = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            val dto = if (following) {
                pixelfedApi.followAccount(account.instanceUrl, accessToken, accountId)
            } else {
                pixelfedApi.unfollowAccount(account.instanceUrl, accessToken, accountId)
            }
            val relationship = PixelfedRelationship(
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
        category: PixelfedSearchCategory,
    ): AppResult<PixelfedSearchResult> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("search", "${category.apiType}:${query.lowercase()}"),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val result = pixelfedApi.search(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        query = query,
                        type = category.apiType,
                        limit = 20,
                    )
                    withContext(Dispatchers.Default) {
                        PixelfedMapper.searchToDomain(result)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getNotifications(account: Account): AppResult<List<PixelfedNotification>> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("notifications"),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val notifications = pixelfedApi.getNotifications(
                        instanceUrl = account.instanceUrl,
                        accessToken = accessToken,
                    )
                    withContext(Dispatchers.Default) {
                        notifications.map(PixelfedMapper::notificationToDomain)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getPost(
        account: Account,
        postId: String,
    ): AppResult<PixelfedPost> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("post", postId)) {
                    val status = pixelfedApi.getStatus(account.instanceUrl, accessToken, postId)
                    withContext(Dispatchers.Default) {
                        PixelfedMapper.statusToDomain(status)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun createPost(account: Account, text: String): AppResult<PixelfedPost> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val status = pixelfedApi.createStatus(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                text = text,
            )
            val post = withContext(Dispatchers.Default) {
                PixelfedMapper.statusToDomain(status)
            }
            memoryCache.put(account.cacheKey("post", post.id), post)
            AppResult.Success(post)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun setLiked(
        account: Account,
        postId: String,
        liked: Boolean,
    ): AppResult<PixelfedPost> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val status = if (liked) {
                pixelfedApi.favouriteStatus(account.instanceUrl, accessToken, postId)
            } else {
                pixelfedApi.unfavouriteStatus(account.instanceUrl, accessToken, postId)
            }
            val post = withContext(Dispatchers.Default) {
                PixelfedMapper.statusToDomain(status)
            }
            memoryCache.put(account.cacheKey("post", postId), post)
            AppResult.Success(post)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getComments(
        account: Account,
        postId: String,
    ): AppResult<List<PixelfedComment>> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("comments", postId),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val context = pixelfedApi.getStatusContext(account.instanceUrl, accessToken, postId)
                    withContext(Dispatchers.Default) {
                        context.descendants.map(PixelfedMapper::statusToComment)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun postComment(
        account: Account,
        postId: String,
        text: String,
    ): AppResult<PixelfedComment> {
        val accessToken = account.accessToken
            ?: return AppResult.Failure(AppError.Unauthorized)

        return try {
            val status = pixelfedApi.postComment(
                instanceUrl = account.instanceUrl,
                accessToken = accessToken,
                statusId = postId,
                text = text,
            )
            memoryCache.invalidate(account.cacheKey("comments", postId))
            memoryCache.invalidate(account.cacheKey("post", postId))
            AppResult.Success(
                withContext(Dispatchers.Default) {
                    PixelfedMapper.statusToComment(status)
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    private fun Account.pixelfedRemoteId(): String =
        id.substringAfterLast("-")

    private fun Account.cacheKey(type: String, id: String = "self"): String =
        "pixelfed:${instanceUrl.normalizedInstance()}:${cacheOwnerKey()}:$type:$id"

    private fun Account.cacheOwnerKey(): String =
        if (accessToken.isNullOrBlank()) "public" else id

    private fun String.normalizedInstance(): String =
        removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
            .lowercase()
}
