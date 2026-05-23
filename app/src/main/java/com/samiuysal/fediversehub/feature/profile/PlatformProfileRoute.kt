package com.samiuysal.fediversehub.feature.profile

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.mastodon.profile.MastodonProfileRoute

@Composable
fun PlatformProfileRoute(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    oauthCallbackUri: Uri?,
    onOAuthCallbackConsumed: () -> Unit,
    onPostSelected: (String) -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSettingsClick: () -> Unit,
) {
    when (selectedPlatform) {
        PlatformType.MASTODON -> MastodonProfileRoute(
            selectedPlatform = selectedPlatform,
            platformAccounts = platformAccounts,
            selectedAccount = selectedAccount,
            contentPadding = contentPadding,
            oauthCallbackUri = oauthCallbackUri,
            onOAuthCallbackConsumed = onOAuthCallbackConsumed,
            onPostSelected = onPostSelected,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
        )
        PlatformType.LEMMY,
        PlatformType.PIXELFED -> ComingSoonProfile(
            platform = selectedPlatform,
            platformAccounts = platformAccounts,
            account = selectedAccount,
            contentPadding = contentPadding,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
        )
    }
}

@Composable
private fun ComingSoonProfile(
    platform: PlatformType,
    platformAccounts: List<Account>,
    account: Account?,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSettingsClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ProfilePlatformTopBar(
            selectedPlatform = platform,
            platformAccounts = platformAccounts,
            selectedAccount = account,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(
                imageUrl = account?.avatarUrl,
                name = account?.displayName ?: platform.label,
                size = AppSpacing.xxl,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${platform.label} profile",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = account?.let { "@${it.username} • ${it.instanceUrl}" } ?: "Login coming soon",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        EmptyState(
            title = "${platform.label} profile coming soon",
            message = "Mastodon profile is live. ${platform.label} account and profile integration will plug into this shell next.",
            modifier = Modifier.weight(1f),
        )
    }
}

private val PlatformType.label: String
    get() = when (this) {
        PlatformType.MASTODON -> "Mastodon"
        PlatformType.LEMMY -> "Lemmy"
        PlatformType.PIXELFED -> "Pixelfed"
    }
