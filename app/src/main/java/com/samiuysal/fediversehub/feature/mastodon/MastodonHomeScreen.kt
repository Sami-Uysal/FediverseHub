package com.samiuysal.fediversehub.feature.mastodon

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppMediaItem
import com.samiuysal.fediversehub.core.designsystem.component.AppPostAction
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.AppCompactLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.MockFediverseData

@Composable
fun MastodonHomeScreen(
    account: Account?,
    posts: LazyPagingItems<MastodonPostUiModel>,
    actionOverrides: Map<String, MastodonPostUiModel> = emptyMap(),
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    onPostClick: (String) -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit = { _, _, _ -> },
    onPostAction: (MastodonPostUiModel, MastodonPostActionType) -> Unit = { _, _ -> },
) {
    val isInitialLoading by remember(posts) {
        derivedStateOf {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0
        }
    }
    val refreshError by remember(posts) {
        derivedStateOf { posts.loadState.refresh as? LoadState.Error }
    }
    val isEmpty by remember(posts) {
        derivedStateOf {
            posts.loadState.refresh is LoadState.NotLoading && posts.itemCount == 0
        }
    }

    Column(modifier = modifier) {
        if (showTopBar) {
            MastodonTopBar(
                account = account,
                onRefresh = posts::refresh,
            )
        }

        when {
            isInitialLoading -> MastodonTimelineSkeleton()
            refreshError != null && posts.itemCount == 0 -> AppErrorState(
                message = refreshError?.error.timelineMessage(),
                onRetry = posts::retry,
            )
            isEmpty -> EmptyState(
                title = "No posts yet",
                message = "Your Mastodon timeline will appear here after refresh.",
            )
            else -> MastodonTimelineList(
                posts = posts,
                actionOverrides = actionOverrides,
                onPostClick = onPostClick,
                onAuthorClick = onAuthorClick,
                onMediaClick = onMediaClick,
                onPostAction = onPostAction,
            )
        }
    }
}

@Composable
fun MastodonHomeScreenContent(
    account: Account?,
    posts: List<MastodonPostUiModel>,
    actionOverrides: Map<String, MastodonPostUiModel> = emptyMap(),
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = {},
    showTopBar: Boolean = true,
    onPostClick: (String) -> Unit = {},
    onAuthorClick: (String) -> Unit = {},
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit = { _, _, _ -> },
    onPostAction: (MastodonPostUiModel, MastodonPostActionType) -> Unit = { _, _ -> },
) {
    Column(modifier = modifier) {
        if (showTopBar) {
            MastodonTopBar(
                account = account,
                onRefresh = onRefresh,
            )
        }
        when {
            isLoading -> MastodonTimelineSkeleton()
            errorMessage != null -> AppErrorState(
                message = errorMessage,
                onRetry = onRetry,
            )
            posts.isEmpty() -> EmptyState(
                title = "No posts yet",
                message = "Your Mastodon timeline will appear here after refresh.",
            )
            else -> MastodonTimelineList(
                posts = posts,
                actionOverrides = actionOverrides,
                onPostClick = onPostClick,
                onAuthorClick = onAuthorClick,
                onMediaClick = onMediaClick,
                onPostAction = onPostAction,
            )
        }
    }
}

@Composable
private fun MastodonTopBar(
    account: Account?,
    onRefresh: () -> Unit,
) {
    AppTopBar(
        title = account?.displayName ?: "Mastodon",
        subtitle = "@${account?.username.orEmpty()} · ${account?.instanceUrl.orEmpty()}",
        actions = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                AssistChip(
                    onClick = onRefresh,
                    label = { Text("Live") },
                )
                AppAvatar(
                    imageUrl = account?.avatarUrl,
                    name = account?.displayName ?: "M",
                    size = 36.dp,
                )
            }
        },
    )
}

@Composable
private fun MastodonTimelineList(
    posts: List<MastodonPostUiModel>,
    actionOverrides: Map<String, MastodonPostUiModel>,
    onPostClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit,
    onPostAction: (MastodonPostUiModel, MastodonPostActionType) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 0.dp,
            top = 0.dp,
            end = 0.dp,
            bottom = AppSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(
            items = posts,
            key = { it.id },
            contentType = { "mastodon-post" },
        ) { post ->
            val renderedPost = post.withActionOverride(actionOverrides)
            MastodonTimelinePost(
                post = renderedPost,
                onClick = { onPostClick(renderedPost.detailId) },
                onAuthorClick = { onAuthorClick(renderedPost.authorAccountId) },
                onMediaClick = onMediaClick,
                onPostAction = onPostAction,
            )
        }
    }
}

@Composable
private fun MastodonTimelineList(
    posts: LazyPagingItems<MastodonPostUiModel>,
    actionOverrides: Map<String, MastodonPostUiModel>,
    onPostClick: (String) -> Unit,
    onAuthorClick: (String) -> Unit,
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit,
    onPostAction: (MastodonPostUiModel, MastodonPostActionType) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = 0.dp,
            top = 0.dp,
            end = 0.dp,
            bottom = AppSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(
            count = posts.itemCount,
            key = posts.itemKey { it.id },
            contentType = posts.itemContentType { "mastodon-post" },
        ) { index ->
            val post = posts[index]
            if (post == null) {
                MastodonPostSkeleton()
            } else {
                val renderedPost = post.withActionOverride(actionOverrides)
                MastodonTimelinePost(
                    post = renderedPost,
                    onClick = { onPostClick(renderedPost.detailId) },
                    onAuthorClick = { onAuthorClick(renderedPost.authorAccountId) },
                    onMediaClick = onMediaClick,
                    onPostAction = onPostAction,
                )
            }
        }

        if (posts.loadState.append is LoadState.Loading) {
            item(key = "mastodon-append-loading") {
                MastodonPostSkeleton()
            }
        }

        val appendError = posts.loadState.append as? LoadState.Error
        if (appendError != null) {
            item(key = "mastodon-append-error") {
                AppErrorState(
                    message = appendError.error.timelineMessage(),
                    onRetry = posts::retry,
                    modifier = Modifier.height(220.dp),
                )
            }
        }
    }
}

@Composable
private fun MastodonTimelinePost(
    post: MastodonPostUiModel,
    onClick: () -> Unit = {},
    onAuthorClick: () -> Unit = {},
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit = { _, _, _ -> },
    onPostAction: (MastodonPostUiModel, MastodonPostActionType) -> Unit = { _, _ -> },
) {
    val actions = remember(
        post.replies,
        post.boosts,
        post.favourites,
        post.isBoosted,
        post.isFavourited,
        post.isBookmarked,
        post.loadingAction,
    ) {
        listOf(
            AppPostAction(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = post.replies.compactMetric(),
                contentDescription = "Replies",
                isLoading = post.loadingAction == MastodonPostActionType.REPLY,
                onClick = { onPostAction(post, MastodonPostActionType.REPLY) },
            ),
            AppPostAction(
                icon = Icons.Outlined.Repeat,
                count = post.boosts.compactMetric(),
                contentDescription = "Boosts",
                isHighlighted = post.isBoosted,
                isLoading = post.loadingAction == MastodonPostActionType.BOOST,
                onClick = { onPostAction(post, MastodonPostActionType.BOOST) },
            ),
            AppPostAction(
                icon = if (post.isFavourited) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                count = post.favourites.compactMetric(),
                contentDescription = "Favourites",
                isHighlighted = post.isFavourited,
                isLoading = post.loadingAction == MastodonPostActionType.FAVOURITE,
                onClick = { onPostAction(post, MastodonPostActionType.FAVOURITE) },
            ),
            AppPostAction(
                icon = if (post.isBookmarked) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                count = "",
                contentDescription = "Bookmark",
                isHighlighted = post.isBookmarked,
                isLoading = post.loadingAction == MastodonPostActionType.BOOKMARK,
                onClick = { onPostAction(post, MastodonPostActionType.BOOKMARK) },
            ),
        )
    }
    val linkPreview = remember(post.linkPreview) {
        post.linkPreview?.let {
            AppLinkPreview(
                domain = it.domain,
                title = it.title,
                description = it.description,
                thumbnailUrl = it.thumbnailUrl,
            )
        }
    }
    val mediaItems = remember(post.media) {
        post.media.map {
            AppMediaItem(
                previewUrl = it.previewUrl,
                fullUrl = it.fullUrl,
                altText = it.altText,
            )
        }
    }
    val mediaNavigationItems = remember(post.media) {
        post.media.mapNotNull { media ->
            val url = media.fullUrl ?: media.previewUrl
            url?.let { it to !media.altText.isNullOrBlank() }
        }
    }
    val mediaUrls = remember(mediaNavigationItems) { mediaNavigationItems.map { it.first } }
    val mediaHasAlt = remember(mediaNavigationItems) { mediaNavigationItems.map { it.second } }

    AppPostCard(
        displayName = post.displayName,
        username = post.username,
        timeAgo = post.timeAgo,
        avatarUrl = post.avatarUrl,
        content = post.content,
        mediaUrl = post.mediaUrl,
        mediaItems = mediaItems,
        hasAltText = post.hasAltText,
        boostedByDisplayName = post.boostedByDisplayName,
        boostedByAvatarUrl = post.boostedByAvatarUrl,
        replyContext = post.replyContext,
        showThreadLine = post.showThreadLine,
        linkPreview = linkPreview,
        actions = actions,
        onClick = onClick,
        onAuthorClick = onAuthorClick,
        onMediaClick = { index ->
            if (mediaUrls.isNotEmpty()) {
                onMediaClick(mediaUrls, mediaHasAlt, index.coerceIn(mediaUrls.indices))
            }
        },
    )
}

@Composable
private fun MastodonTimelineSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        userScrollEnabled = false,
    ) {
        items(2) {
            MastodonPostSkeleton()
        }
    }
}

@Composable
private fun MastodonPostSkeleton() {
    val skeletonColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            SkeletonBlock(
                color = skeletonColor,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape),
            )
            Column(modifier = Modifier.weight(1f)) {
                SkeletonBlock(
                    color = skeletonColor,
                    modifier = Modifier
                        .fillMaxWidth(0.62f)
                        .height(18.dp),
                )
                Spacer(Modifier.height(AppSpacing.sm))
                SkeletonBlock(
                    color = skeletonColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(14.dp),
                )
                Spacer(Modifier.height(AppSpacing.xs))
                SkeletonBlock(
                    color = skeletonColor,
                    modifier = Modifier
                        .fillMaxWidth(0.74f)
                        .height(14.dp),
                )
                Spacer(Modifier.height(AppSpacing.md))
                SkeletonBlock(
                    color = skeletonColor,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(92.dp),
                )
            }
        }
    }
}

@Composable
private fun SkeletonBlock(
    color: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(AppRadius.sm))
            .background(color),
    )
}

private fun Int.compactMetric(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}

private fun Throwable?.timelineMessage(): String =
    (this as? AppErrorException)?.message ?: "Timeline could not be loaded. Pull to refresh or try again."

private fun MastodonPostUiModel.withActionOverride(
    overrides: Map<String, MastodonPostUiModel>,
): MastodonPostUiModel = overrides[detailId] ?: overrides[id] ?: this

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonHomeScreenPreview() {
    FediverseHubTheme {
        MastodonHomeScreenContent(
            account = MockFediverseData.homeState.accounts.first { it.platform == PlatformType.MASTODON },
            posts = MockFediverseData.homeState.mastodonPosts,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonBoostedPostPreview() {
    FediverseHubTheme {
        MastodonTimelinePost(
            post = MockFediverseData.homeState.mastodonPosts.first { it.boostedByDisplayName != null },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonThreadReplyPreview() {
    FediverseHubTheme {
        MastodonTimelinePost(
            post = MockFediverseData.homeState.mastodonPosts.first { it.showThreadLine },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LinkPreviewPreview() {
    FediverseHubTheme {
        AppCompactLinkPreview(
            linkPreview = AppLinkPreview(
                domain = "blog.changs.co.uk",
                title = "Python 3.15: features that didn't make the headlines",
                description = "It's that time of the year again.",
                thumbnailUrl = null,
            ),
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
