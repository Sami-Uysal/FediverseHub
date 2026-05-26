package com.samiuysal.fediversehub.feature.notifications

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.PlatformSwitcher
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyNotificationType
import com.samiuysal.fediversehub.feature.mastodon.notifications.MastodonNotificationsRoute
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedNotificationType

@Composable
fun PlatformNotificationsRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
    onPixelfedPostSelected: (String) -> Unit = {},
    onLemmyPostSelected: (String) -> Unit = {},
) {
    when (selectedPlatform) {
        PlatformType.MASTODON -> MastodonNotificationsRoute(
            selectedPlatform = selectedPlatform,
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            onPlatformSelected = onPlatformSelected,
            onPostSelected = onPostSelected,
            onProfileSelected = onProfileSelected,
        )
        PlatformType.PIXELFED -> PixelfedNotificationsRoute(
            selectedPlatform = selectedPlatform,
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            onPlatformSelected = onPlatformSelected,
            onPostSelected = onPixelfedPostSelected,
            onProfileSelected = onProfileSelected,
        )
        PlatformType.LEMMY -> LemmyNotificationsRoute(
            selectedPlatform = selectedPlatform,
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            onPlatformSelected = onPlatformSelected,
            onPostSelected = onLemmyPostSelected,
        )
    }
}

@Composable
private fun PixelfedNotificationsRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onPostSelected: (String) -> Unit,
    onProfileSelected: (String) -> Unit,
    viewModel: PixelfedNotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }
    NotificationContainer(
        selectedPlatform = selectedPlatform,
        contentPadding = contentPadding,
        onPlatformSelected = onPlatformSelected,
    ) {
        when (val uiState = state) {
            PlatformNotificationUiState.NoAccount -> EmptyState(
                title = "Pixelfed hesabı gerekli",
                message = "Bildirimler için Pixelfed hesabı bağla.",
                modifier = Modifier.fillMaxSize(),
            )
            PlatformNotificationUiState.Loading -> AppLoading(
                modifier = Modifier.fillMaxSize(),
                message = "Bildirimler yükleniyor...",
            )
            is PlatformNotificationUiState.Error -> AppErrorState(uiState.message, viewModel::retry, Modifier.fillMaxSize())
            is PlatformNotificationUiState.Success -> NotificationList(
                items = uiState.items,
                emptyTitle = "Bildirim yok",
                emptyMessage = "Pixelfed bildirimlerin burada görünecek.",
                key = { it.id },
                contentType = { "pixelfed-notification" },
            ) { item ->
                PixelfedNotificationRow(
                    item = item,
                    onClick = {
                        item.postId?.let(onPostSelected) ?: onProfileSelected(item.actorAccountId)
                    },
                )
            }
        }
    }
}

@Composable
private fun LemmyNotificationsRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onPostSelected: (String) -> Unit,
    viewModel: LemmyNotificationsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab by viewModel.tabState.collectAsStateWithLifecycle()
    LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }
    NotificationContainer(
        selectedPlatform = selectedPlatform,
        contentPadding = contentPadding,
        onPlatformSelected = onPlatformSelected,
    ) {
        LemmyNotificationTabs(selectedTab = selectedTab, onSelected = viewModel::selectTab)
        when (val uiState = state) {
            PlatformNotificationUiState.NoAccount -> EmptyState(
                title = "Lemmy hesabı gerekli",
                message = "Bildirimler için Lemmy hesabı bağla.",
                modifier = Modifier.fillMaxSize(),
            )
            PlatformNotificationUiState.Loading -> AppLoading(
                modifier = Modifier.fillMaxSize(),
                message = "Bildirimler yükleniyor...",
            )
            is PlatformNotificationUiState.Error -> AppErrorState(uiState.message, viewModel::retry, Modifier.fillMaxSize())
            is PlatformNotificationUiState.Success -> NotificationList(
                items = uiState.items,
                emptyTitle = "Bildirim yok",
                emptyMessage = "Lemmy yanıtların ve mention'ların burada görünecek.",
                key = { it.id },
                contentType = { "lemmy-notification" },
            ) { item ->
                LemmyNotificationRow(item = item, onClick = { onPostSelected(item.postId) })
            }
        }
    }
}

@Composable
private fun NotificationContainer(
    selectedPlatform: PlatformType,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        NotificationTopBar(
            selectedPlatform = selectedPlatform,
            onPlatformSelected = onPlatformSelected,
        )
        content()
    }
}

@Composable
private fun NotificationTopBar(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
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

@Composable
private fun LemmyNotificationTabs(
    selectedTab: LemmyNotificationTab,
    onSelected: (LemmyNotificationTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        LemmyNotificationTab.entries.forEach { tab ->
            FilterChip(
                selected = selectedTab == tab,
                onClick = { onSelected(tab) },
                label = { Text(if (tab == LemmyNotificationTab.REPLIES) "Replies" else "Mentions") },
            )
        }
    }
}

@Composable
private fun <T> NotificationList(
    items: List<T>,
    emptyTitle: String,
    emptyMessage: String,
    key: (T) -> String,
    contentType: (T) -> String,
    row: @Composable (T) -> Unit,
) {
    if (items.isEmpty()) {
        EmptyState(title = emptyTitle, message = emptyMessage, modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        items(items = items, key = key, contentType = contentType) { item ->
            row(item)
        }
    }
}

@Composable
private fun PixelfedNotificationRow(
    item: PixelfedNotificationUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(
            imageUrl = item.avatarUrl,
            name = item.title,
            modifier = Modifier.clickable { onClick() },
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .clickable { onClick() },
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(item.actor, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            item.preview?.takeIf(String::isNotBlank)?.let {
                Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
        Icon(
            imageVector = when (item.type) {
                PixelfedNotificationType.FAVOURITE -> Icons.Outlined.Favorite
                PixelfedNotificationType.FOLLOW -> Icons.Outlined.PersonAdd
                else -> Icons.Outlined.Notifications
            },
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}

@Composable
private fun LemmyNotificationRow(
    item: LemmyNotificationUiModel,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(item.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        Text("${item.actor} · c/${item.community}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(item.preview, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Text("${item.score} puan · ${if (item.type == LemmyNotificationType.REPLY) "reply" else "mention"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}
