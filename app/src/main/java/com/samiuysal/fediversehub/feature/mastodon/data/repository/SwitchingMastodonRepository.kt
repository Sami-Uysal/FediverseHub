package com.samiuysal.fediversehub.feature.mastodon.data.repository

import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.di.MockMastodonRepositoryBinding
import com.samiuysal.fediversehub.di.RealMastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfile
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchCategory
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchResult
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class SwitchingMastodonRepository @Inject constructor(
    @param:RealMastodonRepository private val realRepository: MastodonRepository,
    @param:MockMastodonRepositoryBinding private val mockRepository: MastodonRepository,
) : MastodonRepository {
    override fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>> =
        account.repository().getHomeTimelinePagingData(account)

    override fun getNotificationsPagingData(
        account: Account,
    ) = account.repository().getNotificationsPagingData(account)

    override fun getAccountStatusesPagingData(
        account: Account,
        accountId: String,
        filter: MastodonProfileTimelineFilter,
    ) = account.repository().getAccountStatusesPagingData(account, accountId, filter)

    override suspend fun getOwnProfile(
        account: Account,
    ): AppResult<MastodonProfile> =
        account.repository().getOwnProfile(account)

    override suspend fun search(
        account: Account,
        query: String,
        category: MastodonSearchCategory,
    ): AppResult<MastodonSearchResult> =
        account.repository().search(account, query, category)

    override suspend fun getTrendingStatuses(account: Account) =
        account.repository().getTrendingStatuses(account)

    override suspend fun getTrendingTags(account: Account) =
        account.repository().getTrendingTags(account)

    override suspend fun getTrendingLinks(account: Account) =
        account.repository().getTrendingLinks(account)

    override suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage,
    ): AppResult<List<MastodonPost>> =
        account.repository().getHomeTimeline(account, page)

    override suspend fun getPostDetail(
        account: Account,
        postId: String,
    ) = account.repository().getPostDetail(account, postId)

    override suspend fun setFavourite(
        account: Account,
        postId: String,
        favourite: Boolean,
    ): AppResult<MastodonPost> =
        account.repository().setFavourite(account, postId, favourite)

    override suspend fun setBoosted(
        account: Account,
        postId: String,
        boosted: Boolean,
    ): AppResult<MastodonPost> =
        account.repository().setBoosted(account, postId, boosted)

    override suspend fun setBookmarked(
        account: Account,
        postId: String,
        bookmarked: Boolean,
    ): AppResult<MastodonPost> =
        account.repository().setBookmarked(account, postId, bookmarked)

    override suspend fun replyToPost(
        account: Account,
        postId: String,
        text: String,
        visibility: String,
    ): AppResult<MastodonPost> =
        account.repository().replyToPost(account, postId, text, visibility)

    override suspend fun createPost(
        account: Account,
        text: String,
        visibility: String,
        spoilerText: String?,
    ): AppResult<MastodonPost> =
        account.repository().createPost(account, text, visibility, spoilerText)

    private fun Account.repository(): MastodonRepository =
        if (accessToken.isNullOrBlank()) mockRepository else realRepository
}
