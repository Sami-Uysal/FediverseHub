package com.samiuysal.fediversehub.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samiuysal.fediversehub.BuildConfig
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.datastore.ThemeMode
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
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
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val cacheState by viewModel.cacheState.collectAsStateWithLifecycle()
    val account = selectedAccount?.takeIf { selected ->
        storedAccounts.any { it.id == selected.id }
    } ?: platformAccounts.firstOrNull()

    SettingsScreen(
        selectedPlatform = selectedPlatform,
        platformAccounts = platformAccounts,
        allAccounts = storedAccounts,
        account = account,
        themeMode = themeMode,
        cacheState = cacheState,
        appVersion = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
        contentPadding = contentPadding,
        onAccountSelected = onAccountSelected,
        onThemeModeSelected = viewModel::selectThemeMode,
        onClearCache = viewModel::clearCache,
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
    allAccounts: List<Account>,
    account: Account?,
    themeMode: ThemeMode,
    cacheState: CacheClearState,
    appVersion: String,
    contentPadding: PaddingValues,
    onAccountSelected: (Account) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onClearCache: () -> Unit,
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
                text = "Ayarlar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            SettingsSection(
                title = "Hesap",
                icon = Icons.Outlined.AccountCircle,
            ) {
                SettingsRow(
                    title = "Aktif platform",
                    value = selectedPlatform.label,
                )
                SettingsAccountRow(account = account)
                Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(
                        text = "Hesap değiştir",
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
                    title = "Bu platformdaki hesaplar",
                    value = "${platformAccounts.size}",
                )
                SettingsRow(
                    title = "Toplam hesap",
                    value = "${allAccounts.size}",
                )
            }

            SettingsSection(
                title = "Görünüm",
                icon = Icons.Outlined.Palette,
            ) {
                ThemeModeSelector(
                    selectedMode = themeMode,
                    onThemeModeSelected = onThemeModeSelected,
                )
            }

            SettingsSection(
                title = "Depolama",
                icon = Icons.Outlined.Storage,
            ) {
                CacheSection(
                    state = cacheState,
                    onClearCache = onClearCache,
                )
            }

            SettingsSection(
                title = "Güvenlik",
                icon = Icons.Outlined.Security,
            ) {
                SettingsRow(
                    title = "Token saklama",
                    value = "Android Keystore ile şifreli",
                )
            }

            SettingsSection(
                title = "Uygulama",
                icon = Icons.Outlined.Info,
            ) {
                SettingsRow(
                    title = "Sürüm",
                    value = "FediverseHub $appVersion",
                )
            }

            Button(
                enabled = account != null,
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Icon(Icons.AutoMirrored.Outlined.Logout, contentDescription = null)
                Text("Çıkış yap")
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.36f),
        shape = RoundedCornerShape(AppRadius.sm),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            content()
        }
    }
}

@Composable
private fun ThemeModeSelector(
    selectedMode: ThemeMode,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = "Theme mode",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
            ThemeMode.entries.forEach { mode ->
                FilterChip(
                    selected = selectedMode == mode,
                    onClick = { onThemeModeSelected(mode) },
                    label = { Text(mode.label) },
                )
            }
        }
    }
}

@Composable
private fun CacheSection(
    state: CacheClearState,
    onClearCache: () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = "Cache",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = when (state) {
                    CacheClearState.Idle -> "Offline cache ready"
                    CacheClearState.Clearing -> "Clearing cache..."
                    CacheClearState.Done -> "Cache cleared"
                    CacheClearState.Error -> "Cache could not be cleared"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            AssistChip(
                onClick = onClearCache,
                enabled = state != CacheClearState.Clearing,
                leadingIcon = {
                    Icon(Icons.Outlined.DeleteSweep, contentDescription = null)
                },
                label = {
                    Text("Clear")
                },
            )
        }
        if (state == CacheClearState.Error) {
            Text(
                text = "Tekrar dene. Hesapların silinmez.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
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
