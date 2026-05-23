package com.samiuysal.fediversehub.feature.mastodon.domain

import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow

interface MastodonRepository {
    fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>>

    fun getNotificationsPagingData(
        account: Account,
    ): Flow<PagingData<MastodonNotification>>

    fun getAccountStatusesPagingData(
        account: Account,
        accountId: String,
        filter: MastodonProfileTimelineFilter,
    ): Flow<PagingData<MastodonPost>>

    suspend fun getOwnProfile(
        account: Account,
    ): AppResult<MastodonProfile>

    suspend fun search(
        account: Account,
        query: String,
        category: MastodonSearchCategory,
    ): AppResult<MastodonSearchResult>

    suspend fun getTrendingStatuses(account: Account): AppResult<List<MastodonPost>>
    suspend fun getTrendingTags(account: Account): AppResult<List<MastodonHashtag>>
    suspend fun getTrendingLinks(account: Account): AppResult<List<MastodonTrendLink>>

    suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage = MastodonTimelinePage(),
    ): AppResult<List<MastodonPost>>

    suspend fun getPostDetail(
        account: Account,
        postId: String,
    ): AppResult<MastodonPostDetail>

    suspend fun setFavourite(
        account: Account,
        postId: String,
        favourite: Boolean,
    ): AppResult<MastodonPost>

    suspend fun setBoosted(
        account: Account,
        postId: String,
        boosted: Boolean,
    ): AppResult<MastodonPost>

    suspend fun setBookmarked(
        account: Account,
        postId: String,
        bookmarked: Boolean,
    ): AppResult<MastodonPost>

    suspend fun replyToPost(
        account: Account,
        postId: String,
        text: String,
        visibility: String = "public",
    ): AppResult<MastodonPost>

    suspend fun createPost(
        account: Account,
        text: String,
        visibility: String = "public",
        spoilerText: String? = null,
    ): AppResult<MastodonPost>
}
