package com.samiuysal.fediversehub.feature.mastodon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppCard
import com.samiuysal.fediversehub.core.designsystem.component.AppIconButton
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account

@Composable
fun MastodonHomeScreen(
    account: Account?,
    posts: List<MastodonPostUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppTopBar(
            title = account?.displayName ?: "Mastodon",
            subtitle = "@${account?.username.orEmpty()} · ${account?.instanceUrl.orEmpty()}",
            actions = {
                AppAvatar(
                    imageUrl = account?.avatarUrl,
                    name = account?.displayName ?: "M",
                    size = 36.dp,
                )
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            items(
                items = posts,
                key = { it.id },
            ) { post ->
                MastodonPostCard(post = post)
            }
        }
    }
}

@Composable
private fun MastodonPostCard(post: MastodonPostUiModel) {
    AppCard(contentPadding = PaddingValues(AppSpacing.lg)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            AppAvatar(
                imageUrl = post.avatarUrl,
                name = post.displayName,
            )
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = post.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "  ${post.username} · ${post.timeAgo}",
                        modifier = Modifier.weight(1f),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                    )
                    Icon(
                        imageVector = Icons.Outlined.MoreHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(AppSpacing.sm))
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (post.mediaUrl != null) {
                    Spacer(Modifier.height(AppSpacing.md))
                    AsyncImage(
                        model = post.mediaUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.75f)
                            .clip(RoundedCornerShape(AppRadius.md)),
                        contentScale = ContentScale.Crop,
                    )
                }
                Spacer(Modifier.height(AppSpacing.md))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    ActionMetric(Icons.Outlined.ChatBubbleOutline, post.replies.toString(), "Replies")
                    ActionMetric(Icons.Outlined.Repeat, post.boosts.toString(), "Boosts")
                    ActionMetric(Icons.Outlined.FavoriteBorder, post.favourites.toString(), "Favourites")
                }
            }
        }
    }
}

@Composable
private fun ActionMetric(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    contentDescription: String,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        AppIconButton(
            icon = icon,
            contentDescription = contentDescription,
            onClick = {},
            modifier = Modifier.size(36.dp),
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
