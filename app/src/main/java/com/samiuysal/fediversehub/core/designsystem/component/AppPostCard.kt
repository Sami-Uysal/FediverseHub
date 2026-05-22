package com.samiuysal.fediversehub.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing

@Composable
fun AppPostCard(
    displayName: String,
    username: String,
    timeAgo: String,
    avatarUrl: String?,
    content: String,
    mediaUrl: String?,
    actions: List<AppPostAction>,
    modifier: Modifier = Modifier,
    onMoreClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = AppSpacing.lg,
                        vertical = AppSpacing.md,
                    ),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                AppAvatar(
                    imageUrl = avatarUrl,
                    name = displayName,
                    size = 42.dp,
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = displayName,
                            modifier = Modifier.weight(0.44f, fill = false),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "  $username · $timeAgo",
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = onMoreClick) {
                            Icon(
                                imageVector = Icons.Outlined.MoreHoriz,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (mediaUrl != null) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        AppMediaPreview(
                            mediaUrl = mediaUrl,
                            contentDescription = content,
                        )
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    AppPostActionsRow(
                        actions = actions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppPostCardPreview() {
    FediverseHubTheme {
        AppPostCard(
            displayName = "Nora Dev",
            username = "@nora@hachyderm.io",
            timeAgo = "8m",
            avatarUrl = null,
            content = "Compose feed performance starts with stable UI models, explicit LazyColumn keys and a component layer that keeps the timeline boring in the best way.",
            mediaUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=720&h=420&fit=crop",
            actions = listOf(
                AppPostAction(Icons.Outlined.ChatBubbleOutline, "12", "Replies"),
                AppPostAction(Icons.Outlined.Repeat, "44", "Boosts"),
                AppPostAction(Icons.Outlined.FavoriteBorder, "130", "Favourites"),
            ),
        )
    }
}
