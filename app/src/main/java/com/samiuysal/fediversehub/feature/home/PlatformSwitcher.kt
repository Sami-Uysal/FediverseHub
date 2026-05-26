package com.samiuysal.fediversehub.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.PlatformColors
import com.samiuysal.fediversehub.core.model.PlatformType

@Composable
fun PlatformSwitcher(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }

    androidx.compose.foundation.layout.Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    onClick = { expanded = true },
                )
                .padding(horizontal = AppSpacing.xs, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = selectedPlatform.label,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = selectedPlatform.accentColor,
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.widthIn(min = 148.dp, max = 180.dp),
        ) {
            PlatformType.entries.forEach { platform ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = platform.label,
                            fontWeight = if (platform == selectedPlatform) {
                                FontWeight.Bold
                            } else {
                                FontWeight.Medium
                            },
                        )
                    },
                    onClick = {
                        expanded = false
                        onPlatformSelected(platform)
                    },
                    leadingIcon = {
                        Surface(
                            modifier = Modifier.size(10.dp),
                            color = platform.accentColor,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            content = {},
                        )
                    },
                )
            }
        }
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

@Preview(showBackground = true, widthDp = 390, heightDp = 160)
@Composable
fun PlatformSwitcherPreview() {
    FediverseHubTheme {
        PlatformSwitcher(
            selectedPlatform = PlatformType.MASTODON,
            onPlatformSelected = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
