package com.samiuysal.fediversehub.feature.lemmy.data.mock

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyCommunity
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyProfile
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotification
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotificationType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchCategory
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchResult
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchUser
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class MockLemmyRepository @Inject constructor() : LemmyRepository {
    override fun getPostsPagingData(
        account: Account,
        sort: LemmySortType,
        feedType: LemmyFeedType,
        communityName: String?,
    ): Flow<PagingData<LemmyPost>> = Pager(
        config = PagingConfig(
            pageSize = LemmyPostPage.DEFAULT_LIMIT,
            initialLoadSize = LemmyPostPage.DEFAULT_LIMIT,
            enablePlaceholders = false,
        ),
        pagingSourceFactory = {
            MockLemmyPagingSource(
                MockLemmyData.posts
                    .filter { communityName == null || it.communityName == communityName }
                    .sortedFor(sort),
            )
        },
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
    ): AppResult<List<LemmyComment>> =
        AppResult.Success(MockLemmyData.posts.firstOrNull { it.id == postId }?.comments.orEmpty())

    override suspend fun getOwnProfile(account: Account): AppResult<LemmyProfile> =
        AppResult.Success(
            LemmyProfile(
                id = account.id,
                name = account.username,
                displayName = account.displayName ?: account.username,
                avatarUrl = account.avatarUrl,
                bannerUrl = null,
                bio = "Mock Lemmy profile",
                postCount = MockLemmyData.posts.size,
                commentCount = MockLemmyData.posts.sumOf { it.comments.size },
                posts = MockLemmyData.posts,
                comments = MockLemmyData.posts.flatMap { it.comments },
                savedPosts = MockLemmyData.posts.take(2).map { it.copy(saved = true) },
                savedComments = MockLemmyData.posts.flatMap { post ->
                    post.comments.map { it.copy(postTitle = post.title) }
                }.take(2),
            ),
        )

    override suspend fun getProfile(account: Account, username: String): AppResult<LemmyProfile> =
        getOwnProfile(account).let { result ->
            when (result) {
                is AppResult.Success -> AppResult.Success(
                    result.data.copy(
                        id = username,
                        name = username,
                        displayName = username,
                        bio = "Mock Lemmy user",
                        savedPosts = emptyList(),
                        savedComments = emptyList(),
                    ),
                )
                is AppResult.Failure -> result
            }
        }

    override suspend fun search(
        account: Account,
        query: String,
        category: LemmySearchCategory,
    ): AppResult<LemmySearchResult> {
        val posts = MockLemmyData.posts.filter {
            it.title.contains(query, ignoreCase = true) ||
                it.communityName.contains(query, ignoreCase = true)
        }.ifEmpty { MockLemmyData.posts.take(2) }
        return AppResult.Success(
            LemmySearchResult(
                posts = posts.takeIf { category == LemmySearchCategory.POSTS }.orEmpty(),
                communities = posts.map { MockLemmyData.communityFor(it.communityName) }
                    .distinctBy { it.name }
                    .takeIf { category == LemmySearchCategory.COMMUNITIES }
                    .orEmpty(),
                users = listOf(
                    LemmySearchUser("mock-user", "cache-first", "Cache First", null, "Mock Lemmy user"),
                ).takeIf { category == LemmySearchCategory.USERS }.orEmpty(),
            ),
        )
    }

    override suspend fun getReplies(account: Account): AppResult<List<LemmyNotification>> =
        AppResult.Success(
            MockLemmyData.posts.flatMap { post ->
                post.comments.take(1).map {
                    LemmyNotification(
                        id = "reply-${it.id}",
                        type = LemmyNotificationType.REPLY,
                        postId = post.id,
                        postTitle = post.title,
                        communityName = post.communityName,
                        actorName = it.authorName,
                        text = it.content,
                        score = it.score,
                        createdAt = post.publishedAt,
                        read = false,
                    )
                }
            },
        )

    override suspend fun getMentions(account: Account): AppResult<List<LemmyNotification>> =
        AppResult.Success(
            getReplies(account).let { (it as AppResult.Success).data.map { n -> n.copy(type = LemmyNotificationType.MENTION) } },
        )

    override suspend fun createComment(
        account: Account,
        postId: String,
        parentId: String?,
        content: String,
    ): AppResult<LemmyComment> =
        AppResult.Success(
            LemmyComment(
                id = "mock-comment-${content.hashCode()}",
                postId = postId,
                parentId = parentId,
                authorName = account.displayName ?: account.username,
                content = content,
                depth = if (parentId == null) 0 else 1,
                isCollapsed = false,
                score = 1,
                myVote = 1,
            ),
        )

    override suspend fun votePost(account: Account, postId: String, score: Int): AppResult<LemmyPost> =
        getPost(account, postId)

    override suspend fun savePost(account: Account, postId: String, saved: Boolean): AppResult<LemmyPost> =
        getPost(account, postId)

    override suspend fun createPost(
        account: Account,
        communityId: String,
        title: String,
        body: String?,
        url: String?,
    ): AppResult<LemmyPost> =
        AppResult.Success(
            MockLemmyData.posts.first().copy(
                id = "mock-post-${title.hashCode()}",
                title = title,
                previewText = body.orEmpty(),
                url = url,
                authorName = account.displayName ?: account.username,
                commentCount = 0,
                score = 1,
            ),
        )

    override suspend fun voteComment(account: Account, commentId: String, score: Int): AppResult<LemmyComment> =
        AppResult.Success(
            MockLemmyData.posts.flatMap { it.comments }.firstOrNull { it.id == commentId }
                ?: MockLemmyData.posts.first().comments.first(),
        )

    override suspend fun getCommunity(account: Account, communityName: String): AppResult<LemmyCommunity> =
        AppResult.Success(MockLemmyData.communityFor(communityName))

    override suspend fun getCommunities(
        account: Account,
        page: LemmyPostPage,
    ): AppResult<List<LemmyCommunity>> =
        AppResult.Success(MockLemmyData.posts.map { MockLemmyData.communityFor(it.communityName) }.distinctBy { it.name })

    override suspend fun followCommunity(
        account: Account,
        communityId: String,
        follow: Boolean,
    ): AppResult<LemmyCommunity> =
        AppResult.Success(MockLemmyData.communityFor("fediverse").copy(subscribed = follow))

    private fun List<LemmyPost>.sortedFor(sort: LemmySortType): List<LemmyPost> = when (sort) {
        LemmySortType.HOT -> sortedByDescending { it.score + it.commentCount }
        LemmySortType.ACTIVE -> sortedByDescending { it.commentCount }
        LemmySortType.NEW -> asReversed()
        LemmySortType.TOP -> sortedByDescending { it.score }
    }

}
