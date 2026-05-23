package com.samiuysal.fediversehub.feature.pixelfed.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.network.NetworkErrorMapper
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedApi
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedFeedPagingSource
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedProfileMediaPagingSource
import com.samiuysal.fediversehub.feature.pixelfed.data.remote.PixelfedPublicFeedPagingSource
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedComment
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedPost
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedProfile
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class PixelfedRepositoryImpl @Inject constructor(
    private val pixelfedApi: PixelfedApi,
) : PixelfedRepository {
    override fun getHomeFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>> =
        Pager(
            config = PagingConfig(
                pageSize = HOME_PAGE_SIZE,
                initialLoadSize = HOME_INITIAL_LOAD_SIZE,
                prefetchDistance = HOME_INITIAL_LOAD_SIZE,
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
        Pager(
            config = PagingConfig(
                pageSize = PROFILE_PAGE_SIZE,
                initialLoadSize = PROFILE_PAGE_SIZE,
                enablePlaceholders = false,
            ),
            pagingSourceFactory = {
                PixelfedProfileMediaPagingSource(
                    instanceUrl = account.instanceUrl,
                    accessToken = account.accessToken.orEmpty(),
                    accountId = account.pixelfedRemoteId(),
                    pixelfedApi = pixelfedApi,
                )
            },
        ).flow

    override fun getExploreFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>> =
        Pager(
            config = PagingConfig(
                pageSize = EXPLORE_PAGE_SIZE,
                initialLoadSize = EXPLORE_PAGE_SIZE,
                prefetchDistance = EXPLORE_PAGE_SIZE / 2,
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
            val profile = pixelfedApi.verifyCredentials(account.instanceUrl, accessToken)
            AppResult.Success(PixelfedMapper.accountToProfile(profile))
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
            val status = pixelfedApi.getStatus(account.instanceUrl, accessToken, postId)
            AppResult.Success(PixelfedMapper.statusToDomain(status))
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
            AppResult.Success(PixelfedMapper.statusToDomain(status))
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
            val context = pixelfedApi.getStatusContext(account.instanceUrl, accessToken, postId)
            AppResult.Success(context.descendants.map(PixelfedMapper::statusToComment))
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
            AppResult.Success(PixelfedMapper.statusToComment(status))
        } catch (throwable: Throwable) {
            AppResult.Failure(NetworkErrorMapper.map(throwable))
        }
    }

    private fun Account.pixelfedRemoteId(): String =
        id.substringAfterLast("-")

    private companion object {
        const val HOME_PAGE_SIZE = 8
        const val HOME_INITIAL_LOAD_SIZE = 8
        const val PROFILE_PAGE_SIZE = 24
        const val EXPLORE_PAGE_SIZE = 15
    }
}
