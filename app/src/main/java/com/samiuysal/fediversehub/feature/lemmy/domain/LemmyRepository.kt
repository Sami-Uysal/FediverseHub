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

    suspend fun getOwnProfile(account: Account): AppResult<LemmyProfile>

    suspend fun getProfile(account: Account, username: String): AppResult<LemmyProfile>

    suspend fun search(
        account: Account,
        query: String,
        category: LemmySearchCategory,
    ): AppResult<LemmySearchResult>

    suspend fun getReplies(account: Account): AppResult<List<LemmyNotification>>

    suspend fun getMentions(account: Account): AppResult<List<LemmyNotification>>

    suspend fun createComment(
        account: Account,
        postId: String,
        parentId: String?,
        content: String,
    ): AppResult<LemmyComment>

    suspend fun votePost(account: Account, postId: String, score: Int): AppResult<LemmyPost>

    suspend fun savePost(account: Account, postId: String, saved: Boolean): AppResult<LemmyPost>

    suspend fun createPost(
        account: Account,
        communityId: String,
        title: String,
        body: String?,
        url: String?,
    ): AppResult<LemmyPost>

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
