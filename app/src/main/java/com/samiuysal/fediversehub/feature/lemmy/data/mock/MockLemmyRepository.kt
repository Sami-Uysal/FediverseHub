package com.samiuysal.fediversehub.feature.lemmy.data.mock

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MockLemmyRepository @Inject constructor() : LemmyRepository {
    override fun getPostsPagingData(
        account: Account,
        sort: LemmySortType,
        feedType: LemmyFeedType,
    ): Flow<PagingData<LemmyPost>> = Pager(
        config = PagingConfig(
            pageSize = LemmyPostPage.DEFAULT_LIMIT,
            initialLoadSize = LemmyPostPage.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = { MockLemmyPagingSource(MockLemmyData.posts.sortedFor(sort)) },
    ).flow

    override suspend fun getPosts(
        account: Account,
        page: LemmyPostPage,
    ): AppResult<List<LemmyPost>> = AppResult.Success(MockLemmyData.posts.sortedFor(page.sort))

    override suspend fun getPost(account: Account, postId: String): AppResult<LemmyPost> =
        MockLemmyData.posts.firstOrNull { it.id == postId }?.let { AppResult.Success(it) }
            ?: AppResult.Failure(com.samiuysal.fediversehub.core.common.error.AppError.Unknown("Post not found."))

    override suspend fun getComments(
        account: Account,
        postId: String,
    ): AppResult<List<com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment>> =
        AppResult.Success(MockLemmyData.posts.firstOrNull { it.id == postId }?.comments.orEmpty())

    private fun List<LemmyPost>.sortedFor(sort: LemmySortType): List<LemmyPost> = when (sort) {
        LemmySortType.HOT -> sortedByDescending { it.score + it.commentCount }
        LemmySortType.ACTIVE -> sortedByDescending { it.commentCount }
        LemmySortType.NEW -> asReversed()
        LemmySortType.TOP -> sortedByDescending { it.score }
    }

}
