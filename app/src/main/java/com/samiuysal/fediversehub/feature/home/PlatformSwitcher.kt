package com.samiuysal.fediversehub.feature.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.PlatformColors
import com.samiuysal.fediversehub.core.model.PlatformType

@Composable
fun PlatformSwitcher(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.full),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            PlatformType.entries.forEach { platform ->
                PlatformSegment(
                    platform = platform,
                    selected = selectedPlatform == platform,
                    onClick = { onPlatformSelected(platform) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun PlatformSegment(
    platform: PlatformType,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val accent = platform.accentColor
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.97f else 1f,
        label = "platformSegmentScale",
    )
    val containerColor by animateColorAsState(
        targetValue = if (selected) accent else Color.Transparent,
        label = "platformSegmentContainer",
    )
    val contentColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "platformSegmentContent",
    )

    Box(
        modifier = modifier
            .height(34.dp)
            .scale(scale)
            .background(containerColor, androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.full))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick,
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = platform.label,
            style = MaterialTheme.typography.labelLarge,
            color = contentColor,
        )
    }
}

private val PlatformType.label: String
    get() = when (this) {
        PlatformType.MASTODON -> "Mastodon"
        PlatformType.LEMMY -> "Lemmy"
        PlatformType.PIXELFED -> "Pixelfed"
    }

private val PlatformType.accentColor: Color
    @Composable
    get() = when (this) {
        PlatformType.MASTODON -> PlatformColors.mastodon
        PlatformType.LEMMY -> PlatformColors.lemmy
        PlatformType.PIXELFED -> PlatformColors.pixelfed
    }

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PlatformSwitcherPreview() {
    FediverseHubTheme {
        PlatformSwitcher(
            selectedPlatform = PlatformType.MASTODON,
            onPlatformSelected = {},
        )
    }
}
