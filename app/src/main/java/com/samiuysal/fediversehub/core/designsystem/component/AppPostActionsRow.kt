package com.samiuysal.fediversehub.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing

@Immutable
data class AppPostAction(
    val icon: ImageVector,
    val count: String,
    val contentDescription: String,
    val isHighlighted: Boolean = false,
    val onClick: () -> Unit = {},
)

@Composable
fun AppPostActionsRow(
    actions: List<AppPostAction>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        actions.forEach { action ->
            val interactionSource = remember(action.contentDescription) { MutableInteractionSource() }
            val pressed by interactionSource.collectIsPressedAsState()
            val scale by animateFloatAsState(
                targetValue = if (pressed) 0.9f else 1f,
                label = "postActionScale",
            )
            val tint by animateColorAsState(
                targetValue = if (action.isHighlighted || pressed) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                label = "postActionTint",
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xxs),
            ) {
                IconButton(
                    onClick = action.onClick,
                    modifier = Modifier
                        .size(32.dp)
                        .scale(scale),
                    interactionSource = interactionSource,
                ) {
                    Icon(
                        imageVector = action.icon,
                        contentDescription = action.contentDescription,
                        tint = tint,
                        modifier = Modifier.size(18.dp),
                    )
                }
                Text(
                    text = action.count,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppPostActionsRowPreview() {
    FediverseHubTheme {
        AppPostActionsRow(
            actions = listOf(
                AppPostAction(Icons.Outlined.ChatBubbleOutline, "24", "Replies"),
                AppPostAction(Icons.Outlined.Repeat, "118", "Boosts"),
                AppPostAction(Icons.Outlined.FavoriteBorder, "1K", "Favourites"),
            ),
        )
    }
}
