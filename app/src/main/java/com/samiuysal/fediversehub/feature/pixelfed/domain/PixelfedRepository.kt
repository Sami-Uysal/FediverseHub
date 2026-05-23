package com.samiuysal.fediversehub.feature.pixelfed.domain

import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import kotlinx.coroutines.flow.Flow

interface PixelfedRepository {
    fun getHomeFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>>
    fun getProfileMediaPagingData(account: Account): Flow<PagingData<PixelfedPost>>
    fun getExploreFeedPagingData(account: Account): Flow<PagingData<PixelfedPost>>

    suspend fun getOwnProfile(account: Account): AppResult<PixelfedProfile>
    suspend fun getPost(account: Account, postId: String): AppResult<PixelfedPost>
    suspend fun setLiked(account: Account, postId: String, liked: Boolean): AppResult<PixelfedPost>
    suspend fun getComments(account: Account, postId: String): AppResult<List<PixelfedComment>>
    suspend fun postComment(account: Account, postId: String, text: String): AppResult<PixelfedComment>
}
