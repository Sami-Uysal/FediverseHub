package com.samiuysal.fediversehub.feature.mastodon

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
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
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppPostAction
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.motion.AppMotion
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account

@Composable
fun MastodonHomeScreen(
    account: Account?,
    posts: LazyPagingItems<MastodonPostUiModel>,
    modifier: Modifier = Modifier,
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
        AppTopBar(
            title = account?.displayName ?: "Mastodon",
            subtitle = "@${account?.username.orEmpty()} · ${account?.instanceUrl.orEmpty()}",
            actions = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                ) {
                    AssistChip(
                        onClick = posts::refresh,
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

        when {
            isRefreshing -> MastodonTimelineSkeleton()
            refreshError != null -> AppErrorState(
                message = refreshError?.error?.message ?: "Timeline could not be loaded.",
                onRetry = posts::retry,
            )
            isEmpty -> EmptyState(
                title = "No posts yet",
                message = "Your Mastodon timeline will appear here after refresh.",
            )
            else -> MastodonTimelineList(posts = posts)
        }
    }
}

@Composable
private fun MastodonTimelineList(
    posts: LazyPagingItems<MastodonPostUiModel>,
) {
    LazyColumn(
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(
            start = AppSpacing.lg,
            top = AppSpacing.sm,
            end = AppSpacing.lg,
            bottom = AppSpacing.xl,
        ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
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
                MastodonTimelinePost(post = post)
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
                    message = appendError.error.message ?: "More posts could not be loaded.",
                    onRetry = posts::retry,
                    modifier = Modifier.height(220.dp),
                )
            }
        }
    }
}

@Composable
private fun MastodonTimelinePost(post: MastodonPostUiModel) {
    val actions = remember(post.replies, post.boosts, post.favourites) {
        listOf(
            AppPostAction(
                icon = Icons.Outlined.ChatBubbleOutline,
                count = post.replies.compactMetric(),
                contentDescription = "Replies",
            ),
            AppPostAction(
                icon = Icons.Outlined.Repeat,
                count = post.boosts.compactMetric(),
                contentDescription = "Boosts",
            ),
            AppPostAction(
                icon = Icons.Outlined.FavoriteBorder,
                count = post.favourites.compactMetric(),
                contentDescription = "Favourites",
            ),
        )
    }

    AppPostCard(
        displayName = post.displayName,
        username = post.username,
        timeAgo = post.timeAgo,
        avatarUrl = post.avatarUrl,
        content = post.content,
        mediaUrl = post.mediaUrl,
        actions = actions,
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
        items(4) {
            MastodonPostSkeleton()
        }
    }
}

@Composable
private fun MastodonPostSkeleton() {
    val transition = rememberInfiniteTransition(label = "mastodon_skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.38f,
        targetValue = 0.78f,
        animationSpec = infiniteRepeatable(
            animation = tween(AppMotion.slow),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "mastodon_skeleton_alpha",
    )
    val skeletonColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
