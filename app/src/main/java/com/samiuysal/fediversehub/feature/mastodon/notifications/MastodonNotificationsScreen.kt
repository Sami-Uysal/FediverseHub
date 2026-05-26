package com.samiuysal.fediversehub.feature.mastodon.notifications

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Reply
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.HowToReg
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Poll
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.PlatformSwitcher
import com.samiuysal.fediversehub.feature.mastodon.data.mock.MockMastodonData
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonNotificationType
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonNotificationMapper

@Composable
fun MastodonNotificationsScreen(
    selectedPlatform: PlatformType,
    notifications: LazyPagingItems<MastodonNotificationUiModel>,
    onPlatformSelected: (PlatformType) -> Unit,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isInitialLoading by remember(notifications) {
        derivedStateOf {
            notifications.loadState.refresh is LoadState.Loading && notifications.itemCount == 0
        }
    }
    val refreshError by remember(notifications) {
        derivedStateOf { notifications.loadState.refresh as? LoadState.Error }
    }
    val isEmpty by remember(notifications) {
        derivedStateOf {
            notifications.loadState.refresh is LoadState.NotLoading && notifications.itemCount == 0
        }
    }

    Column(modifier = modifier.fillMaxSize()) {
        MastodonNotificationsTopBar(
            selectedPlatform = selectedPlatform,
            onPlatformSelected = onPlatformSelected,
        )
        when {
            isInitialLoading -> AppLoading(
                message = "Loading notifications...",
                modifier = Modifier.weight(1f),
            )
            refreshError != null && notifications.itemCount == 0 -> AppErrorState(
                message = refreshError?.error.notificationMessage(),
                onRetry = notifications::retry,
                modifier = Modifier.weight(1f),
            )
            isEmpty -> EmptyState(
                title = "No notifications yet",
                message = "Favourites, boosts, follows and mentions will appear here.",
                modifier = Modifier.weight(1f),
            )
            else -> MastodonNotificationsList(
                notifications = notifications,
                onPostSelected = onPostSelected,
                onProfileSelected = onProfileSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun MastodonNotificationsContent(
    notifications: List<MastodonNotificationUiModel>,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MastodonNotificationsTopBar(
            selectedPlatform = PlatformType.MASTODON,
            onPlatformSelected = {},
        )
        if (notifications.isEmpty()) {
            EmptyState(
                title = "No notifications yet",
                message = "Favourites, boosts, follows and mentions will appear here.",
                modifier = Modifier.weight(1f),
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = AppSpacing.xl),
            ) {
                items(
                    count = notifications.size,
                    key = { index -> notifications[index].id },
                    contentType = { "mastodon-notification" },
                ) { index ->
                    MastodonNotificationRow(
                        notification = notifications[index],
                        onPostSelected = onPostSelected,
                        onProfileSelected = onProfileSelected,
                    )
                }
            }
        }
    }
}

@Composable
private fun MastodonNotificationsTopBar(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                PlatformSwitcher(
                    selectedPlatform = selectedPlatform,
                    onPlatformSelected = onPlatformSelected,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Notifications,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Bildirimler",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MastodonNotificationsList(
    notifications: LazyPagingItems<MastodonNotificationUiModel>,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isRefreshing = notifications.loadState.refresh is LoadState.Loading && notifications.itemCount > 0
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = notifications::refresh,
        modifier = modifier,
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = AppSpacing.xl),
        ) {
            items(
                count = notifications.itemCount,
                key = notifications.itemKey { it.id },
                contentType = notifications.itemContentType { "mastodon-notification" },
            ) { index ->
                val notification = notifications[index]
                if (notification != null) {
                    MastodonNotificationRow(
                        notification = notification,
                        onPostSelected = onPostSelected,
                        onProfileSelected = onProfileSelected,
                    )
                }
            }

            if (notifications.loadState.append is LoadState.Loading) {
                item(key = "notifications-append-loading") {
                    AppLoading(
                        message = "Loading more...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                    )
                }
            }

            val appendError = notifications.loadState.append as? LoadState.Error
            if (appendError != null) {
                item(key = "notifications-append-error") {
                    AppErrorState(
                        message = appendError.error.notificationMessage(),
                        onRetry = notifications::retry,
                        modifier = Modifier.height(180.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun MastodonNotificationRow(
    notification: MastodonNotificationUiModel,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
) {
    val targetPostId = notification.status?.detailId
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                if (targetPostId != null) {
                    onPostSelected(targetPostId)
                } else {
                    onProfileSelected(notification.actorAccountId)
                }
            },
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
                NotificationTypeIcon(type = notification.type)
                AppAvatar(
                    imageUrl = notification.actorAvatarUrl,
                    name = notification.actorDisplayName,
                    size = 42.dp,
                    modifier = Modifier.clickable { onProfileSelected(notification.actorAccountId) },
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = notification.actionText,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = notification.timeAgo,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = notification.actorUsername,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    when (notification.type) {
                        MastodonNotificationType.MENTION,
                        MastodonNotificationType.STATUS,
                        MastodonNotificationType.UPDATE,
                        MastodonNotificationType.POLL,
                        -> notification.status?.let { StatusPreview(it) }
                        MastodonNotificationType.FOLLOW -> FollowProfileHint(
                            username = notification.actorUsername,
                        )
                        else -> notification.status?.let { CompactActionStatusPreview(it) }
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.52f))
        }
    }
}

@Composable
private fun NotificationTypeIcon(type: MastodonNotificationType) {
    val icon = type.icon()
    val tint = when (type) {
        MastodonNotificationType.FAVOURITE -> MaterialTheme.colorScheme.error
        MastodonNotificationType.REBLOG -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun StatusPreview(status: com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.46f),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = status.displayName,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = status.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CompactActionStatusPreview(
    status: com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(
            modifier = Modifier
                .size(3.dp)
                .clip(CircleShape),
        )
        Text(
            text = status.content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun FollowProfileHint(username: String) {
    Text(
        text = "Open profile $username",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.primary,
    )
}

private fun MastodonNotificationType.icon(): ImageVector =
    when (this) {
        MastodonNotificationType.FAVOURITE -> Icons.Outlined.Favorite
        MastodonNotificationType.REBLOG -> Icons.Outlined.Repeat
        MastodonNotificationType.MENTION -> Icons.AutoMirrored.Outlined.Reply
        MastodonNotificationType.FOLLOW -> Icons.Outlined.HowToReg
        MastodonNotificationType.STATUS -> Icons.Outlined.Campaign
        MastodonNotificationType.UPDATE -> Icons.Outlined.Edit
        MastodonNotificationType.POLL -> Icons.Outlined.Poll
        MastodonNotificationType.UNKNOWN -> Icons.Outlined.Notifications
    }

private fun Throwable?.notificationMessage(): String =
    (this as? AppErrorException)?.message ?: "Notifications could not be loaded. Pull to refresh or try again."

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonNotificationsScreenPreview() {
    FediverseHubTheme {
        MastodonNotificationsContent(
            notifications = MockMastodonData.notifications.map(MastodonNotificationMapper::domainToUi),
            onPostSelected = {},
            onProfileSelected = {},
        )
    }
}
