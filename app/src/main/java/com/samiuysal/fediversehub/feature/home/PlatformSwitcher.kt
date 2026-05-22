package com.samiuysal.fediversehub.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.PlatformType

@Composable
fun PlatformSwitcher(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        PlatformType.entries.forEach { platform ->
            FilterChip(
                selected = selectedPlatform == platform,
                onClick = { onPlatformSelected(platform) },
                label = { Text(platform.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        }
    }
}

private val PlatformType.label: String
    get() = when (this) {
        PlatformType.MASTODON -> "Mastodon"
        PlatformType.LEMMY -> "Lemmy"
        PlatformType.PIXELFED -> "Pixelfed"
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
