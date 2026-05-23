package com.samiuysal.fediversehub.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.profile.AccountSwitcher

@Composable
fun SettingsRoute(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onAccountSelected: (Account) -> Unit,
    onBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val storedAccounts by viewModel.accounts.collectAsStateWithLifecycle()
    val account = selectedAccount?.takeIf { selected ->
        storedAccounts.any { it.id == selected.id }
    } ?: platformAccounts.firstOrNull()

    SettingsScreen(
        selectedPlatform = selectedPlatform,
        platformAccounts = platformAccounts,
        account = account,
        contentPadding = contentPadding,
        onAccountSelected = onAccountSelected,
        onBack = onBack,
        onLogout = {
            if (account != null) {
                viewModel.logout(account)
            }
        },
    )
}

@Composable
fun SettingsScreen(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    account: Account?,
    contentPadding: PaddingValues,
    onAccountSelected: (Account) -> Unit,
    onBack: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                )
            }
            Text(
                text = "Settings",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        Column(
            modifier = Modifier.padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            SettingsRow(
                title = "Active platform",
                value = selectedPlatform.label,
            )
            SettingsAccountRow(account = account)
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = "Account switch",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                AccountSwitcher(
                    accounts = platformAccounts,
                    selectedAccount = account,
                    onAccountSelected = onAccountSelected,
                )
            }
            SettingsRow(
                title = "Accounts on this platform",
                value = "${platformAccounts.size}",
            )
            SettingsRow(
                title = "App theme",
                value = "System default",
            )
            SettingsRow(
                title = "Cache / debug",
                value = "Offline cache active for Mastodon timeline",
            )
            TextButton(
                enabled = account != null,
                onClick = onLogout,
            ) {
                Text("Logout")
            }
        }
    }
}

@Composable
private fun SettingsAccountRow(account: Account?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(
            imageUrl = account?.avatarUrl,
            name = account?.displayName ?: account?.username ?: "P",
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account?.displayName ?: "No active account",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = account?.let { "@${it.username} • ${it.instanceUrl}" } ?: "Login required",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SettingsRow(
    title: String,
    value: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

private val PlatformType.label: String
    get() = when (this) {
        PlatformType.MASTODON -> "Mastodon"
        PlatformType.LEMMY -> "Lemmy"
        PlatformType.PIXELFED -> "Pixelfed"
    }
