package com.samiuysal.fediversehub.feature.lemmy

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.MockFediverseData
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemmyHomeScreen(
    account: Account?,
    posts: LazyPagingItems<LemmyPostUiModel>,
    selectedSort: LemmySortType,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    onSortSelected: (LemmySortType) -> Unit,
    onPostClick: (String) -> Unit,
) {
    val isInitialLoading by remember(posts) {
        derivedStateOf {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0
        }
    }
    val isRefreshing by remember(posts) {
        derivedStateOf {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount > 0
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

    Column(modifier = modifier.fillMaxSize()) {
        if (showTopBar) {
            LemmyTopBar(account = account, onRefresh = posts::refresh)
        }
        LemmyHomeControls(
            selectedSort = selectedSort,
            onSortSelected = onSortSelected,
            onRefresh = posts::refresh,
        )

        when {
            isInitialLoading -> LemmyFeedSkeleton(modifier = Modifier.weight(1f))
            refreshError != null && posts.itemCount == 0 -> AppErrorState(
                message = refreshError?.error?.message ?: "Lemmy feed yüklenemedi.",
                onRetry = posts::retry,
                modifier = Modifier.weight(1f),
            )
            isEmpty -> EmptyState(
                title = "Abonelik yok",
                message = "Henüz community aboneliğin yok. Keşfet’ten community bulabilirsin.",
                modifier = Modifier.weight(1f),
            )
            else -> PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = posts::refresh,
                modifier = Modifier.weight(1f),
            ) {
                LemmyPostList(posts = posts, onPostClick = onPostClick)
            }
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
    selectedSort: LemmySortType = LemmySortType.HOT,
    onSortSelected: (LemmySortType) -> Unit = {},
    onPostClick: (String) -> Unit = {},
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (showTopBar) {
            LemmyTopBar(account = account, onRefresh = onRefresh)
        }
        LemmyHomeControls(
            selectedSort = selectedSort,
            onSortSelected = onSortSelected,
            onRefresh = onRefresh,
        )

        when {
            isLoading -> LemmyFeedSkeleton(modifier = Modifier.weight(1f))
            errorMessage != null -> AppErrorState(
                message = errorMessage,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )
            posts.isEmpty() -> EmptyState(
                title = "Abonelik yok",
                message = "Henüz community aboneliğin yok. Keşfet’ten community bulabilirsin.",
                modifier = Modifier.weight(1f),
            )
            else -> LemmyPostList(
                posts = posts,
                onPostClick = onPostClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LemmyTopBar(
    account: Account?,
    onRefresh: () -> Unit,
) {
    AppTopBar(
        title = "Lemmy",
        subtitle = "${account?.instanceUrl.orEmpty()} · home",
        actions = {
            AssistChip(
                onClick = onRefresh,
                label = { Text("Yenile") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = null,
                    )
                },
            )
        },
    )
}

@Composable
private fun LemmyHomeControls(
    selectedSort: LemmySortType,
    onSortSelected: (LemmySortType) -> Unit,
    onRefresh: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        lemmySortTypes.forEach { sort ->
            FilterChip(
                selected = selectedSort == sort,
                onClick = { onSortSelected(sort) },
                label = { Text(sort.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f),
                    selectedLabelColor = MaterialTheme.colorScheme.primary,
                ),
            )
        }
        AssistChip(
            onClick = onRefresh,
            label = { Text("Yenile") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = null,
                )
            },
        )
    }
}

@Composable
private fun LemmyPostList(
    posts: List<LemmyPostUiModel>,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
            LemmyPostCard(post = post, onClick = { onPostClick(post.id) })
        }
    }
}

@Composable
private fun LemmyPostList(
    posts: LazyPagingItems<LemmyPostUiModel>,
    onPostClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
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
                LemmyPostCard(post = post, onClick = { onPostClick(post.id) })
            }
        }

        if (posts.loadState.append is LoadState.Loading) {
            item(key = "lemmy-append-loading", contentType = "lemmy-loading") {
                LemmyPostSkeleton()
            }
        }

        val appendError = posts.loadState.append as? LoadState.Error
        if (appendError != null) {
            item(key = "lemmy-append-error", contentType = "lemmy-error") {
                AppErrorState(
                    message = appendError.error.message ?: "Daha fazla Lemmy gönderisi yüklenemedi.",
                    onRetry = posts::retry,
                    modifier = Modifier.height(220.dp),
                )
            }
        }
    }
}

@Composable
private fun LemmyPostCard(
    post: LemmyPostUiModel,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.Top,
            ) {
                ScorePill(score = post.score)
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
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    if (post.previewText.isNotBlank()) {
                        Text(
                            text = post.previewText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    LemmyLinkPreview(post = post)
                    LemmyMetaRow(comments = post.comments, score = post.score)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
        }
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
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ScorePill(score: Int) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(AppRadius.full),
    ) {
        Column(
            modifier = Modifier
                .width(48.dp)
                .padding(vertical = AppSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = score.compactMetric(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = "puan",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun LemmyLinkPreview(post: LemmyPostUiModel) {
    val thumbnailUrl = post.thumbnailUrl
    val url = post.url
    if (thumbnailUrl.isNullOrBlank() && url.isNullOrBlank()) return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = RoundedCornerShape(AppRadius.md),
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (!thumbnailUrl.isNullOrBlank()) {
                val context = LocalContext.current
                val request = remember(thumbnailUrl) {
                    ImageRequest.Builder(context)
                        .data(thumbnailUrl)
                        .size(360, 220)
                        .precision(Precision.INEXACT)
                        .crossfade(false)
                        .build()
                }
                AsyncImage(
                    model = request,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 96.dp, height = 64.dp)
                        .clip(RoundedCornerShape(AppRadius.sm)),
                )
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = post.domain ?: "link",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (!url.isNullOrBlank()) {
                    Text(
                        text = url,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LemmyMetaRow(
    comments: Int,
    score: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
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
                text = "${comments.compactMetric()} yorum",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = "${score.compactMetric()} puan",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun LemmyFeedSkeleton(modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
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
        initialValue = 0.28f,
        targetValue = 0.56f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1_100),
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
                    .size(width = 48.dp, height = 54.dp)
                    .clip(RoundedCornerShape(AppRadius.full)),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                SkeletonBlock(
                    color = color,
                    modifier = Modifier
                        .fillMaxWidth(0.42f)
                        .height(22.dp),
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
                        .fillMaxWidth(0.62f)
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

private val lemmySortTypes = listOf(
    LemmySortType.ACTIVE,
    LemmySortType.HOT,
    LemmySortType.NEW,
    LemmySortType.TOP,
)

private val LemmySortType.label: String
    get() = when (this) {
        LemmySortType.ACTIVE -> "Active"
        LemmySortType.HOT -> "Hot"
        LemmySortType.NEW -> "New"
        LemmySortType.TOP -> "Top"
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
        LemmyPostCard(
            post = MockFediverseData.homeState.lemmyPosts.first(),
            onClick = {},
        )
    }
}
