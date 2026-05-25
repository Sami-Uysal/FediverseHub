package com.samiuysal.fediversehub.feature.pixelfed.domain

import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import kotlinx.coroutines.flow.Flow

interface PixelfedRepository {
    fun getHomeFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>>
    fun getProfileMediaPagingData(account: Account): Flow<PagingData<PixelfedPost>>
    fun getProfileMediaPagingData(account: Account, accountId: String): Flow<PagingData<PixelfedPost>>
    fun getExploreFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>>

    suspend fun getOwnProfile(account: Account): AppResult<PixelfedProfile>
    suspend fun getProfile(account: Account, accountId: String): AppResult<PixelfedProfile>
    suspend fun getRelationship(account: Account, accountId: String): AppResult<PixelfedRelationship>
    suspend fun setFollowing(account: Account, accountId: String, following: Boolean): AppResult<PixelfedRelationship>
    suspend fun search(
        account: Account,
        query: String,
        category: PixelfedSearchCategory,
    ): AppResult<PixelfedSearchResult>
    suspend fun getNotifications(account: Account): AppResult<List<PixelfedNotification>>
    suspend fun getPost(account: Account, postId: String): AppResult<PixelfedPost>
    suspend fun setLiked(account: Account, postId: String, liked: Boolean): AppResult<PixelfedPost>
    suspend fun getComments(account: Account, postId: String): AppResult<List<PixelfedComment>>
    suspend fun postComment(account: Account, postId: String, text: String): AppResult<PixelfedComment>
}
