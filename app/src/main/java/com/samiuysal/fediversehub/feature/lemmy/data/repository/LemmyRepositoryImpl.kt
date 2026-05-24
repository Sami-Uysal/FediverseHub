package com.samiuysal.fediversehub.feature.lemmy.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.lemmy.data.remote.LemmyApi
import com.samiuysal.fediversehub.feature.lemmy.data.remote.LemmyPostPagingSource
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyCommunity
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyApiMapper
import com.samiuysal.fediversehub.feature.lemmy.mapper.apiValue
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class LemmyRepositoryImpl @Inject constructor(
    private val lemmyApi: LemmyApi,
) : LemmyRepository {
    override fun getPostsPagingData(
        account: Account,
        sort: LemmySortType,
        feedType: LemmyFeedType,
        communityName: String?,
    ): Flow<PagingData<LemmyPost>> =
        Pager(
            config = PagingConfig(
                pageSize = LemmyPostPage.DEFAULT_LIMIT,
                initialLoadSize = LemmyPostPage.DEFAULT_LIMIT,
                prefetchDistance = LemmyPostPage.DEFAULT_LIMIT / 2,
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
                lemmyApi.getPosts(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken,
                    page = page.page,
                    limit = page.limit,
                    sort = page.sort.apiValue,
                    feedType = page.feedType.apiValue,
                    communityName = null,
                ).posts.map(LemmyApiMapper::postViewToDomain),
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getPost(account: Account, postId: String): AppResult<LemmyPost> =
        try {
            AppResult.Success(
                lemmyApi.getPost(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken,
                    postId = postId.toInt(),
                ).postView.let(LemmyApiMapper::postViewToDomain),
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun getComments(account: Account, postId: String): AppResult<List<LemmyComment>> =
        try {
            AppResult.Success(
                lemmyApi.getComments(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken,
                    postId = postId.toInt(),
                    limit = COMMENTS_LIMIT,
                ).comments.map(LemmyApiMapper::commentViewToDomain),
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }

    override suspend fun votePost(
        account: Account,
        postId: String,
        score: Int,
    ): AppResult<LemmyPost> {
        val token = account.accessToken ?: return AppResult.Failure(AppError.Unauthorized)
        return try {
            AppResult.Success(
                lemmyApi.votePost(
                    instanceUrl = account.instanceUrl,
                    accessToken = token,
                    postId = postId.toInt(),
                    score = score.coerceIn(-1, 1),
                ).postView.let(LemmyApiMapper::postViewToDomain),
            )
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
            AppResult.Success(
                lemmyApi.savePost(
                    instanceUrl = account.instanceUrl,
                    accessToken = token,
                    postId = postId.toInt(),
                    saved = saved,
                ).postView.let(LemmyApiMapper::postViewToDomain),
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
            AppResult.Success(
                lemmyApi.voteComment(
                    instanceUrl = account.instanceUrl,
                    accessToken = token,
                    commentId = commentId.toInt(),
                    score = score.coerceIn(-1, 1),
                ).commentView.let(LemmyApiMapper::commentViewToDomain),
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
                lemmyApi.getCommunity(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken,
                    communityName = communityName,
                ).communityView.let(LemmyApiMapper::communityViewToDomain),
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
                lemmyApi.getCommunities(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken,
                    page = page.page,
                    limit = page.limit,
                    sort = page.sort.apiValue,
                    feedType = page.feedType.apiValue,
                ).communities.map(LemmyApiMapper::communityViewToDomain),
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
            AppResult.Success(
                lemmyApi.followCommunity(
                    instanceUrl = account.instanceUrl,
                    accessToken = token,
                    communityId = communityId.toInt(),
                    follow = follow,
                ).communityView.let(LemmyApiMapper::communityViewToDomain),
            )
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    private companion object {
        const val COMMENTS_LIMIT = 200
    }
}
