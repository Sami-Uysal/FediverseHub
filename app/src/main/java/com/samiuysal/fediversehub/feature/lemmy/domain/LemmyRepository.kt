package com.samiuysal.fediversehub.feature.lemmy.domain

import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import kotlinx.coroutines.flow.Flow

interface LemmyRepository {
    fun getPostsPagingData(
        account: Account,
        sort: LemmySortType = LemmySortType.HOT,
        feedType: LemmyFeedType = LemmyFeedType.ALL,
        communityName: String? = null,
    ): Flow<PagingData<LemmyPost>>

    suspend fun getPosts(
        account: Account,
        page: LemmyPostPage = LemmyPostPage(),
    ): AppResult<List<LemmyPost>>

    suspend fun getPost(account: Account, postId: String): AppResult<LemmyPost>

    suspend fun getComments(account: Account, postId: String): AppResult<List<LemmyComment>>

    suspend fun votePost(account: Account, postId: String, score: Int): AppResult<LemmyPost>

    suspend fun savePost(account: Account, postId: String, saved: Boolean): AppResult<LemmyPost>

    suspend fun voteComment(account: Account, commentId: String, score: Int): AppResult<LemmyComment>

    suspend fun getCommunity(account: Account, communityName: String): AppResult<LemmyCommunity>

    suspend fun getCommunities(
        account: Account,
        page: LemmyPostPage = LemmyPostPage(sort = LemmySortType.TOP, feedType = LemmyFeedType.ALL),
    ): AppResult<List<LemmyCommunity>>

    suspend fun followCommunity(
        account: Account,
        communityId: String,
        follow: Boolean,
    ): AppResult<LemmyCommunity>
}
