package com.samiuysal.fediversehub.feature.mastodon.data.mock

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPostDetail
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfile
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileField
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MockMastodonRepository @Inject constructor() : MastodonRepository {
    override fun getHomeTimelinePagingData(
        account: Account,
    ): Flow<PagingData<MastodonPost>> = Pager(
        config = PagingConfig(
            pageSize = MastodonTimelinePage.DEFAULT_LIMIT,
            initialLoadSize = MastodonTimelinePage.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { MockMastodonPagingSource() },
    ).flow

    override fun getNotificationsPagingData(
        account: Account,
    ) = Pager(
        config = PagingConfig(
            pageSize = 30,
            initialLoadSize = 30,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            MockMastodonNotificationsPagingSource(MockMastodonData.notifications)
        },
    ).flow

    override fun getAccountStatusesPagingData(
        account: Account,
        accountId: String,
        filter: MastodonProfileTimelineFilter,
    ): Flow<PagingData<MastodonPost>> = Pager(
        config = PagingConfig(
            pageSize = MastodonTimelinePage.DEFAULT_LIMIT,
            initialLoadSize = MastodonTimelinePage.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { MockMastodonPagingSource() },
    ).flow

    override suspend fun getOwnProfile(
        account: Account,
    ): AppResult<MastodonProfile> = AppResult.Success(
        MastodonProfile(
            id = account.id.substringAfterLast("-"),
            displayName = account.displayName ?: account.username,
            username = "@${account.username}",
            avatarUrl = account.avatarUrl,
            headerUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=1200&h=420&fit=crop",
            note = "Building a calm, fast Fediverse client for Android.",
            followersCount = 1240,
            followingCount = 312,
            statusesCount = 284,
            fields = listOf(
                MastodonProfileField("Website", "fediversehub.local"),
                MastodonProfileField("Stack", "Kotlin, Compose, Ktor"),
            ),
        ),
    )

    override suspend fun getHomeTimeline(
        account: Account,
        page: MastodonTimelinePage,
    ): AppResult<List<MastodonPost>> = AppResult.Success(MockMastodonData.homeTimeline)

    override suspend fun getPostDetail(
        account: Account,
        postId: String,
    ): AppResult<MastodonPostDetail> {
        val posts = MockMastodonData.homeTimeline
        val post = posts.firstOrNull { it.id == postId } ?: posts.first()
        return AppResult.Success(
            MastodonPostDetail(
                post = post,
                ancestors = posts.takeWhile { it.id != post.id }.takeLast(1),
                descendants = posts.dropWhile { it.id != post.id }.drop(1),
            ),
        )
    }

    override suspend fun setFavourite(
        account: Account,
        postId: String,
        favourite: Boolean,
    ): AppResult<MastodonPost> = AppResult.Success(
        post(postId).copy(
            isFavourited = favourite,
            favouriteCount = (post(postId).favouriteCount + if (favourite) 1 else -1).coerceAtLeast(0),
        ),
    )

    override suspend fun setBoosted(
        account: Account,
        postId: String,
        boosted: Boolean,
    ): AppResult<MastodonPost> = AppResult.Success(
        post(postId).copy(
            isReblogged = boosted,
            reblogCount = (post(postId).reblogCount + if (boosted) 1 else -1).coerceAtLeast(0),
        ),
    )

    override suspend fun setBookmarked(
        account: Account,
        postId: String,
        bookmarked: Boolean,
    ): AppResult<MastodonPost> = AppResult.Success(post(postId).copy(isBookmarked = bookmarked))

    override suspend fun replyToPost(
        account: Account,
        postId: String,
        text: String,
        visibility: String,
    ): AppResult<MastodonPost> = AppResult.Success(
        post(postId).copy(
            id = "mock-reply-$postId",
            detailId = "mock-reply-$postId",
            contentText = text,
            inReplyToAccountId = postId,
            visibility = visibility,
        ),
    )

    override suspend fun createPost(
        account: Account,
        text: String,
        visibility: String,
        spoilerText: String?,
    ): AppResult<MastodonPost> = AppResult.Success(
        MockMastodonData.homeTimeline.first().copy(
            id = "mock-new-post-${text.hashCode()}",
            detailId = "mock-new-post-${text.hashCode()}",
            authorDisplayName = account.displayName ?: account.username,
            authorUsername = account.username,
            authorAvatarUrl = account.avatarUrl,
            contentText = text,
            mediaAttachments = emptyList(),
            boostedByDisplayName = null,
            boostedByAvatarUrl = null,
            inReplyToAccountId = null,
            linkPreview = null,
            replyCount = 0,
            reblogCount = 0,
            favouriteCount = 0,
            isReblogged = false,
            isFavourited = false,
            isBookmarked = false,
            visibility = visibility,
        ),
    )

    private fun post(postId: String): MastodonPost =
        MockMastodonData.homeTimeline.firstOrNull { it.id == postId || it.detailId == postId }
            ?: MockMastodonData.homeTimeline.first()
}
