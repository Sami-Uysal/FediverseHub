package com.samiuysal.fediversehub.feature.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.PlatformSwitcher

@Composable
fun ProfilePlatformTopBar(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    selectedAccount: Account?,
    onPlatformSelected: (PlatformType) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = AppSpacing.md, end = AppSpacing.sm, top = AppSpacing.xs, bottom = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatformSwitcher(
                    selectedPlatform = selectedPlatform,
                    onPlatformSelected = onPlatformSelected,
                    modifier = Modifier.weight(1f, fill = false),
                )
                IconButton(onClick = onSettingsClick) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Settings",
                    )
                }
            }
            AccountSwitcher(
                accounts = platformAccounts,
                selectedAccount = selectedAccount,
                onAccountSelected = onAccountSelected,
                modifier = Modifier.padding(start = AppSpacing.md, end = AppSpacing.md, bottom = AppSpacing.sm),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        }
    }
}
