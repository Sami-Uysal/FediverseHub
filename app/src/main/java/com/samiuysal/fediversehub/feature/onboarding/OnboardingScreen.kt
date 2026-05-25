package com.samiuysal.fediversehub.feature.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.outlined.AlternateEmail
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.component.AppButton
import com.samiuysal.fediversehub.core.designsystem.component.AppSecondaryButton
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.PlatformColors
import com.samiuysal.fediversehub.core.model.PlatformType

@Composable
fun OnboardingScreen(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
    onAddAccount: () -> Unit,
    onExplore: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedInfo = selectedPlatform.info
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            BrandMark()
            Text(
                text = "FediverseHub",
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = "Mastodon, Pixelfed ve Lemmy hesaplarını tek, hızlı ve sade bir Android deneyiminde topla.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        PlatformSummary()

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            Text(
                text = "Başlamak için platform seç",
                style = MaterialTheme.typography.titleMedium,
            )
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PlatformType.entries.forEach { platform ->
                    FilterChip(
                        selected = selectedPlatform == platform,
                        onClick = { onPlatformSelected(platform) },
                        label = { Text(platform.info.name) },
                        leadingIcon = {
                            Icon(
                                imageVector = platform.info.icon,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp),
                            )
                        },
                    )
                }
            }
            SelectedPlatformPanel(
                info = selectedInfo,
                selected = true,
            )
        }

        Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            AppButton(
                text = "${selectedInfo.name} hesabı ekle",
                icon = Icons.AutoMirrored.Outlined.Login,
                onClick = onAddAccount,
                modifier = Modifier.fillMaxWidth(),
            )
            AppSecondaryButton(
                text = "Şimdilik keşfet",
                icon = Icons.Outlined.Explore,
                onClick = onExplore,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BrandMark() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        listOf(
            PlatformColors.mastodon,
            PlatformColors.pixelfed,
            PlatformColors.lemmy,
        ).forEach { color ->
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(RoundedCornerShape(AppRadius.sm))
                    .background(color),
            )
        }
    }
}

@Composable
private fun PlatformSummary() {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        PlatformType.entries.forEach { platform ->
            SelectedPlatformPanel(
                info = platform.info,
                selected = false,
            )
        }
    }
}

@Composable
private fun SelectedPlatformPanel(
    info: PlatformInfo,
    selected: Boolean,
) {
    val shape = RoundedCornerShape(AppRadius.sm)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .then(
                if (selected) {
                    Modifier.border(
                        BorderStroke(1.dp, info.color.copy(alpha = 0.8f)),
                        shape,
                    )
                } else {
                    Modifier
                },
            ),
        color = if (selected) {
            info.color.copy(alpha = 0.08f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        shape = shape,
    ) {
        Row(
            modifier = Modifier.padding(AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                color = info.color.copy(alpha = 0.14f),
                contentColor = info.color,
                shape = RoundedCornerShape(AppRadius.sm),
            ) {
                Icon(
                    imageVector = info.icon,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(AppSpacing.sm)
                        .size(22.dp),
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = info.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(AppSpacing.xs))
                Text(
                    text = info.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Immutable
private data class PlatformInfo(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val color: androidx.compose.ui.graphics.Color,
)

private val PlatformType.info: PlatformInfo
    get() = when (this) {
        PlatformType.MASTODON -> PlatformInfo(
            name = "Mastodon",
            description = "Kısa gönderiler, timeline, bildirimler ve sosyal etkileşimler.",
            icon = Icons.Outlined.AlternateEmail,
            color = PlatformColors.mastodon,
        )
        PlatformType.PIXELFED -> PlatformInfo(
            name = "Pixelfed",
            description = "Fotoğraf odaklı feed, profil grid’i, yorumlar ve beğeniler.",
            icon = Icons.Outlined.PhotoCamera,
            color = PlatformColors.pixelfed,
        )
        PlatformType.LEMMY -> PlatformInfo(
            name = "Lemmy",
            description = "Community postları, yorum ağaçları, oylar ve kaydetme.",
            icon = Icons.Outlined.Forum,
            color = PlatformColors.lemmy,
        )
    }

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun OnboardingScreenPreview() {
    FediverseHubTheme {
        OnboardingScreen(
            selectedPlatform = PlatformType.MASTODON,
            onPlatformSelected = {},
            onAddAccount = {},
            onExplore = {},
        )
    }
}
