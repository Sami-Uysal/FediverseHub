package com.samiuysal.fediversehub.feature.lemmy

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.MockFediverseData

@Composable
fun LemmyHomeScreen(
    account: Account?,
    posts: LazyPagingItems<LemmyPostUiModel>,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
) {
    val isRefreshing by remember(posts) {
        derivedStateOf { posts.loadState.refresh is LoadState.Loading }
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
            LemmyTopBar(account = account, onRefresh = posts::refresh)
        }
        SortSelector(
            selectedSort = "Hot",
            onSortSelected = {},
        )

        when {
            isRefreshing -> LemmyFeedSkeleton()
            refreshError != null -> AppErrorState(
                message = refreshError?.error?.message ?: "Lemmy posts could not be loaded.",
                onRetry = posts::retry,
            )
            isEmpty -> EmptyState(
                title = "No Lemmy posts",
                message = "Community posts will appear here after refresh.",
            )
            else -> LemmyPostList(posts = posts)
        }
    }
}

@Composable
fun LemmyHomeScreenContent(
    account: Account?,
    posts: List<LemmyPostUiModel>,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null,
    onRetry: () -> Unit = {},
    onRefresh: () -> Unit = {},
    showTopBar: Boolean = true,
) {
    Column(modifier = modifier) {
        if (showTopBar) {
            LemmyTopBar(account = account, onRefresh = onRefresh)
        }
        SortSelector(
            selectedSort = "Hot",
            onSortSelected = {},
        )

        when {
            isLoading -> LemmyFeedSkeleton()
            errorMessage != null -> AppErrorState(
                message = errorMessage,
                onRetry = onRetry,
            )
            posts.isEmpty() -> EmptyState(
                title = "No Lemmy posts",
                message = "Community posts will appear here after refresh.",
            )
            else -> LemmyPostList(posts = posts)
        }
    }
}

@Composable
private fun LemmyTopBar(
    account: Account?,
    onRefresh: () -> Unit,
) {
    AppTopBar(
        title = "All communities",
        subtitle = "${account?.instanceUrl.orEmpty()} · front page",
        actions = {
            AssistChip(
                onClick = onRefresh,
                label = { Text("Hot") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Sort,
                        contentDescription = null,
                    )
                },
            )
        },
    )
}

@Composable
private fun SortSelector(
    selectedSort: String,
    onSortSelected: (String) -> Unit,
) {
    val sorts = remember { listOf("Hot", "Active", "New", "Top") }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        sorts.forEach { sort ->
            FilterChip(
                selected = selectedSort == sort,
                onClick = { onSortSelected(sort) },
                label = { Text(sort) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
    }
}

@Composable
private fun LemmyPostList(posts: List<LemmyPostUiModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = AppSpacing.lg,
            top = 0.dp,
            end = AppSpacing.lg,
            bottom = AppSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(
            items = posts,
            key = { it.id },
            contentType = { "lemmy-post" },
        ) { post ->
            LemmyPostCard(post = post)
        }
    }
}

@Composable
private fun LemmyPostList(posts: LazyPagingItems<LemmyPostUiModel>) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = AppSpacing.lg,
            top = 0.dp,
            end = AppSpacing.lg,
            bottom = AppSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(0.dp),
    ) {
        items(
            count = posts.itemCount,
            key = posts.itemKey { it.id },
            contentType = posts.itemContentType { "lemmy-post" },
        ) { index ->
            val post = posts[index]
            if (post == null) {
                LemmyPostSkeleton()
            } else {
                LemmyPostCard(post = post)
            }
        }

        if (posts.loadState.append is LoadState.Loading) {
            item(key = "lemmy-append-loading") {
                LemmyPostSkeleton()
            }
        }

        val appendError = posts.loadState.append as? LoadState.Error
        if (appendError != null) {
            item(key = "lemmy-append-error") {
                AppErrorState(
                    message = appendError.error.message ?: "More Lemmy posts could not be loaded.",
                    onRetry = posts::retry,
                    modifier = Modifier.height(220.dp),
                )
            }
        }
    }
}

@Composable
private fun LemmyPostCard(post: LemmyPostUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                ScorePillar(score = post.score)
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CommunityChip(community = post.community)
                        Spacer(Modifier.width(AppSpacing.sm))
                        Text(
                            text = post.domain ?: "self",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = {}) {
                            Icon(
                                imageVector = Icons.Outlined.MoreHoriz,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Text(
                        text = post.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "${post.author} · ${post.timeAgo}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = post.previewText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            LemmyMetaRow(
                comments = post.comments,
                score = post.score,
            )

            if (post.nestedComments.isNotEmpty()) {
                NestedCommentPreview(comments = post.nestedComments.take(3))
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
        )
    }
}

@Composable
private fun CommunityChip(community: String) {
    Surface(
        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
        contentColor = MaterialTheme.colorScheme.primary,
        shape = RoundedCornerShape(AppRadius.full),
    ) {
        Text(
            text = "c/$community",
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun ScorePillar(score: Int) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(AppRadius.full))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f))
            .padding(horizontal = AppSpacing.xxs, vertical = AppSpacing.xs),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
    ) {
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowUp,
            contentDescription = "Upvote",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = score.compactMetric(),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Downvote",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LemmyMetaRow(
    comments: Int,
    score: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ModeComment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${comments.compactMetric()} comments",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = "${score.compactMetric()} points",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Icon(
                imageVector = Icons.Outlined.BookmarkBorder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
            )
            Text(
                text = "Save",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun NestedCommentPreview(comments: List<CommentUiModel>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.38f),
                shape = RoundedCornerShape(AppRadius.md),
            )
            .padding(AppSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        comments.forEach { comment ->
            Row {
                if (comment.depth > 0) {
                    Spacer(Modifier.width((comment.depth * 14).dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(36.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.54f),
                                shape = RoundedCornerShape(AppRadius.full),
                            ),
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                }
                Column {
                    Text(
                        text = comment.author,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        text = if (comment.isCollapsed) "Collapsed thread" else comment.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (comment.isCollapsed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color.Unspecified
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LemmyFeedSkeleton() {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        userScrollEnabled = false,
    ) {
        items(2) {
            LemmyPostSkeleton()
        }
    }
}

@Composable
private fun LemmyPostSkeleton() {
    val transition = rememberInfiniteTransition(label = "lemmySkeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.36f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 940),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "lemmySkeletonAlpha",
    )
    val color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

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
                color = color,
                modifier = Modifier
                    .size(width = 44.dp, height = 92.dp)
                    .clip(RoundedCornerShape(AppRadius.full)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                SkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth(0.38f)
                        .height(24.dp),
                )
                SkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(18.dp),
                )
                SkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .height(18.dp),
                )
                SkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth(0.58f)
                        .height(16.dp),
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LemmyHomeScreenPreview() {
    FediverseHubTheme {
        LemmyHomeScreenContent(
            account = MockFediverseData.homeState.accounts.first { it.platform == PlatformType.LEMMY },
            posts = MockFediverseData.homeState.lemmyPosts,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LemmyPostCardPreview() {
    FediverseHubTheme {
        LemmyPostCard(post = MockFediverseData.homeState.lemmyPosts.first())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LemmyCommentPreview() {
    FediverseHubTheme {
        NestedCommentPreview(
            comments = MockFediverseData.homeState.lemmyPosts.first().nestedComments.take(3),
        )
    }
}
