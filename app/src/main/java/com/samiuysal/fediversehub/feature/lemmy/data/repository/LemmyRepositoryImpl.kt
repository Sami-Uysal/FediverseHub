package com.samiuysal.fediversehub.feature.lemmy.data.repository

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
import com.samiuysal.fediversehub.feature.lemmy.data.remote.LemmyApi
import com.samiuysal.fediversehub.feature.lemmy.data.remote.LemmyPostPagingSource
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyCommunity
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyProfile
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotification
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchCategory
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchResult
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyApiMapper
import com.samiuysal.fediversehub.feature.lemmy.mapper.apiValue
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class LemmyRepositoryImpl @Inject constructor(
    private val lemmyApi: LemmyApi,
    private val feedLoadPolicy: FeedLoadPolicy,
    private val memoryCache: FediverseMemoryCache,
) : LemmyRepository {
    override fun getPostsPagingData(
        account: Account,
        sort: LemmySortType,
        feedType: LemmyFeedType,
        communityName: String?,
    ): Flow<PagingData<LemmyPost>> =
        Pager(
            config = PagingConfig(
                pageSize = feedLoadPolicy.pageSize(PlatformType.LEMMY, FeedSurface.HOME),
                initialLoadSize = feedLoadPolicy.pageSize(PlatformType.LEMMY, FeedSurface.HOME),
                prefetchDistance = feedLoadPolicy.pageSize(PlatformType.LEMMY, FeedSurface.HOME) / 2,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                LemmyPostPagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken,
                    sort = sort,
                    feedType = feedType,
                    communityName = communityName,
                    lemmyApi = lemmyApi,
                )
            },
        ).flow

    override suspend fun getPosts(
        account: Account,
        page: LemmyPostPage,
    ): AppResult<List<LemmyPost>> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey(
                        type = "posts",
                        id = "${page.feedType.apiValue}:${page.sort.apiValue}:${page.page}:${page.limit}",
                    ),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val response = lemmyApi.getPosts(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        page = page.page,
                        limit = page.limit,
                        sort = page.sort.apiValue,
                        feedType = page.feedType.apiValue,
                        communityName = null,
                    )
                    withContext(Dispatchers.Default) {
                        response.posts.map(LemmyApiMapper::postViewToDomain).distinctBy { it.id }
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getPost(account: Account, postId: String): AppResult<LemmyPost> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("post", postId)) {
                    val response = lemmyApi.getPost(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        postId = postId.toInt(),
                    )
                    withContext(Dispatchers.Default) {
                        response.postView.let(LemmyApiMapper::postViewToDomain)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getComments(account: Account, postId: String): AppResult<List<LemmyComment>> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("comments", postId),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val response = lemmyApi.getComments(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        postId = postId.toInt(),
                        limit = COMMENTS_LIMIT,
                    )
                    withContext(Dispatchers.Default) {
                        response.comments.map(LemmyApiMapper::commentViewToDomain)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getOwnProfile(account: Account): AppResult<LemmyProfile> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("own-profile")) {
                    val sitePerson = lemmyApi.getSite(
                        instanceUrl = account.instanceUrl,
                        accessToken = token,
                    ).myUser?.localUserView?.person
                    val username = sitePerson?.name?.takeIf(String::isNotBlank)
                        ?: account.username.substringBefore("@").takeIf(String::isNotBlank)
                        ?: account.username
                    val response = lemmyApi.getUser(
                        instanceUrl = account.instanceUrl,
                        accessToken = token,
                        username = username,
                        page = 1,
                        limit = PROFILE_LIMIT,
                        sort = LemmySortType.NEW.apiValue,
                        savedOnly = false,
                    )
                    val savedResponse = lemmyApi.getUser(
                        instanceUrl = account.instanceUrl,
                        accessToken = token,
                        username = username,
                        page = 1,
                        limit = PROFILE_LIMIT,
                        sort = LemmySortType.NEW.apiValue,
                        savedOnly = true,
                    )
                    withContext(Dispatchers.Default) {
                        val profile = LemmyApiMapper.userToProfile(response, username)
                        val savedProfile = LemmyApiMapper.userToProfile(savedResponse, username, savedOnly = true)
                        profile.copy(
                            savedPosts = savedProfile.posts.map { it.copy(saved = true) },
                            savedComments = savedProfile.comments,
                        )
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getProfile(account: Account, username: String): AppResult<LemmyProfile> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("profile", username.lowercase()),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val response = lemmyApi.getUser(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        username = username,
                        page = 1,
                        limit = PROFILE_LIMIT,
                        sort = LemmySortType.NEW.apiValue,
                        savedOnly = false,
                    )
                    withContext(Dispatchers.Default) {
                        LemmyApiMapper.userToProfile(response, username)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun search(
        account: Account,
        query: String,
        category: LemmySearchCategory,
    ): AppResult<LemmySearchResult> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("search", "${category.apiType}:${query.lowercase()}"),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val response = lemmyApi.search(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        query = query,
                        type = category.apiType,
                        page = 1,
                        limit = 20,
                        sort = LemmySortType.TOP.apiValue,
                    )
                    withContext(Dispatchers.Default) {
                        LemmyApiMapper.searchToDomain(response)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getReplies(account: Account): AppResult<List<LemmyNotification>> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("notifications", "replies"),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val response = lemmyApi.getReplies(
                        instanceUrl = account.instanceUrl,
                        accessToken = token,
                        unreadOnly = false,
                        page = 1,
                        limit = NOTIFICATION_LIMIT,
                    )
                    withContext(Dispatchers.Default) {
                        response.replies.map(LemmyApiMapper::replyToNotification)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getMentions(account: Account): AppResult<List<LemmyNotification>> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey("notifications", "mentions"),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val response = lemmyApi.getMentions(
                        instanceUrl = account.instanceUrl,
                        accessToken = token,
                        unreadOnly = false,
                        page = 1,
                        limit = NOTIFICATION_LIMIT,
                    )
                    withContext(Dispatchers.Default) {
                        response.mentions.map(LemmyApiMapper::mentionToNotification)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun createComment(
        account: Account,
        postId: String,
        parentId: String?,
        content: String,
    ): AppResult<LemmyComment> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            val response = lemmyApi.createComment(
                instanceUrl = account.instanceUrl,
                accessToken = token,
                postId = postId.toInt(),
                parentId = parentId?.toIntOrNull(),
                content = content,
            )
            memoryCache.invalidate(account.cacheKey("comments", postId))
            AppResult.Success(
                withContext(Dispatchers.Default) {
                    LemmyApiMapper.commentViewToDomain(response.commentView)
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun votePost(
        account: Account,
        postId: String,
        score: Int,
    ): AppResult<LemmyPost> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            val response = lemmyApi.votePost(
                instanceUrl = account.instanceUrl,
                accessToken = token,
                postId = postId.toInt(),
                score = score.coerceIn(-1, 1),
            )
            val post = withContext(Dispatchers.Default) {
                LemmyApiMapper.postViewToDomain(response.postView)
            }
            memoryCache.put(account.cacheKey("post", postId), post)
            AppResult.Success(post)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun savePost(
        account: Account,
        postId: String,
        saved: Boolean,
    ): AppResult<LemmyPost> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            val response = lemmyApi.savePost(
                instanceUrl = account.instanceUrl,
                accessToken = token,
                postId = postId.toInt(),
                saved = saved,
            )
            val post = withContext(Dispatchers.Default) {
                LemmyApiMapper.postViewToDomain(response.postView)
            }
            memoryCache.put(account.cacheKey("post", postId), post)
            AppResult.Success(post)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun createPost(
        account: Account,
        communityId: String,
        title: String,
        body: String?,
        url: String?,
    ): AppResult<LemmyPost> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            val response = lemmyApi.createPost(
                instanceUrl = account.instanceUrl,
                accessToken = token,
                communityId = communityId.toInt(),
                title = title,
                body = body,
                url = url,
            )
            AppResult.Success(
                withContext(Dispatchers.Default) {
                    LemmyApiMapper.postViewToDomain(response.postView)
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun voteComment(
        account: Account,
        commentId: String,
        score: Int,
    ): AppResult<LemmyComment> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            val response = lemmyApi.voteComment(
                instanceUrl = account.instanceUrl,
                accessToken = token,
                commentId = commentId.toInt(),
                score = score.coerceIn(-1, 1),
            )
            AppResult.Success(
                withContext(Dispatchers.Default) {
                    LemmyApiMapper.commentViewToDomain(response.commentView)
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    override suspend fun getCommunity(
        account: Account,
        communityName: String,
    ): AppResult<LemmyCommunity> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(account.cacheKey("community", communityName.lowercase())) {
                    val response = lemmyApi.getCommunity(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        communityName = communityName,
                    )
                    withContext(Dispatchers.Default) {
                        response.communityView.let(LemmyApiMapper::communityViewToDomain)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getCommunities(
        account: Account,
        page: LemmyPostPage,
    ): AppResult<List<LemmyCommunity>> =
        try {
            AppResult.Success(
                memoryCache.getOrPut(
                    key = account.cacheKey(
                        type = "communities",
                        id = "${page.feedType.apiValue}:${page.sort.apiValue}:${page.page}:${page.limit}",
                    ),
                    ttlMillis = FediverseMemoryCache.SHORT_TTL_MILLIS,
                ) {
                    val response = lemmyApi.getCommunities(
                        instanceUrl = account.instanceUrl,
                        accessToken = account.accessToken,
                        page = page.page,
                        limit = page.limit,
                        sort = page.sort.apiValue,
                        feedType = page.feedType.apiValue,
                    )
                    withContext(Dispatchers.Default) {
                        response.communities.map(LemmyApiMapper::communityViewToDomain)
                    }
                },
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun followCommunity(
        account: Account,
        communityId: String,
        follow: Boolean,
    ): AppResult<LemmyCommunity> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            val response = lemmyApi.followCommunity(
                instanceUrl = account.instanceUrl,
                accessToken = token,
                communityId = communityId.toInt(),
                follow = follow,
            )
            val community = withContext(Dispatchers.Default) {
                LemmyApiMapper.communityViewToDomain(response.communityView)
            }
            memoryCache.put(account.cacheKey("community", community.name.lowercase()), community)
            AppResult.Success(community)
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    private companion object {
        const val COMMENTS_LIMIT = 200
        const val PROFILE_LIMIT = 30
        const val NOTIFICATION_LIMIT = 40
    }

    private fun Account.cacheKey(type: String, id: String = "self"): String =
        "lemmy:${instanceUrl.normalizedInstance()}:${cacheOwnerKey()}:$type:$id"

    private fun Account.cacheOwnerKey(): String =
        if (accessToken.isNullOrBlank()) "public" else id

    private fun String.normalizedInstance(): String =
        removePrefix("https://")
            .removePrefix("http://")
            .trimEnd('/')
            .lowercase()
}
