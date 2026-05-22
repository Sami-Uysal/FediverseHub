package com.samiuysal.fediversehub.core.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing

data class AppBottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
)

@Composable
fun AppBottomBar(
    items: List<AppBottomNavItem>,
    selectedRoute: String,
    onItemSelected: (String) -> Unit,
    accentColor: Color = MaterialTheme.colorScheme.primary,
) {
    Surface(
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.98f),
        tonalElevation = 0.dp,
        shadowElevation = 10.dp,
    ) {
        androidx.compose.foundation.layout.Column {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
                thickness = 1.dp,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                items.forEach { item ->
                    BottomBarItem(
                        item = item,
                        selected = selectedRoute == item.route,
                        accentColor = accentColor,
                        onClick = { onItemSelected(item.route) },
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomBarItem(
    item: AppBottomNavItem,
    selected: Boolean,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.94f else 1f,
        animationSpec = spring(stiffness = 520f, dampingRatio = 0.72f),
        label = "bottomItemScale",
    )
    val containerColor by animateColorAsState(
        targetValue = Color.Transparent,
        label = "bottomItemContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) {
            accentColor
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        },
        label = "bottomItemContent",
    )

    Box(
        modifier = modifier
            .height(46.dp)
            .scale(scale)
            .background(containerColor)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = item.label,
            tint = contentColor,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 120)
@Composable
fun AppBottomBarPreview() {
    FediverseHubTheme {
        AppBottomBar(
            items = listOf(
                AppBottomNavItem("home", "Home", Icons.Outlined.Home),
                AppBottomNavItem("search", "Search", Icons.Outlined.Search),
                AppBottomNavItem("discover", "Discover", Icons.Outlined.Explore),
                AppBottomNavItem("notifications", "Notifications", Icons.Outlined.Notifications),
                AppBottomNavItem("profile", "Profile", Icons.Outlined.Person),
            ),
            selectedRoute = "home",
            onItemSelected = {},
        )
    }
}
