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
