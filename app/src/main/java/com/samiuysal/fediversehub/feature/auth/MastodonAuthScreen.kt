package com.samiuysal.fediversehub.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.Icons
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppButton
import com.samiuysal.fediversehub.core.designsystem.component.AppCard
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppSecondaryButton
import com.samiuysal.fediversehub.core.designsystem.component.AppTextField
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType

@Composable
fun MastodonAuthScreen(
    uiState: MastodonAuthUiState,
    onEvent: (MastodonAuthUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        AppTopBar(
            title = "Mastodon account",
            subtitle = "OAuth login setup",
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            uiState.errorMessage?.let { message ->
                AppErrorState(
                    message = message,
                    onRetry = { onEvent(MastodonAuthUiEvent.LoginClicked) },
                    modifier = Modifier.height(160.dp),
                )
            }

            if (uiState.account != null) {
                MastodonAccountPanel(
                    account = uiState.account,
                    isLoading = uiState.isLoading,
                    onLogout = { onEvent(MastodonAuthUiEvent.LogoutClicked) },
                )
            } else {
                MastodonLoginPanel(
                    instanceUrl = uiState.instanceUrl,
                    isLoading = uiState.isLoading,
                    onInstanceChanged = { onEvent(MastodonAuthUiEvent.InstanceUrlChanged(it)) },
                    onLogin = { onEvent(MastodonAuthUiEvent.LoginClicked) },
                )
            }

            AppCard(
                contentPadding = PaddingValues(AppSpacing.md),
            ) {
                Text(
                    text = "Token storage",
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(Modifier.height(AppSpacing.xs))
                Text(
                    text = "MVP 2 stores Mastodon account and access token in DataStore. Next hardening step: move tokens to Android Keystore backed encrypted storage and keep DataStore for account metadata only.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun MastodonLoginPanel(
    instanceUrl: String,
    isLoading: Boolean,
    onInstanceChanged: (String) -> Unit,
    onLogin: () -> Unit,
) {
    AppCard {
        Text(
            text = "Connect an instance",
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(Modifier.height(AppSpacing.sm))
        AppTextField(
            value = instanceUrl,
            onValueChange = onInstanceChanged,
            label = "Instance URL",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AppSpacing.md))
        if (isLoading) {
            AppLoading(message = "Preparing Mastodon OAuth...")
        } else {
            AppButton(
                text = "Continue with Mastodon",
                icon = Icons.AutoMirrored.Outlined.Login,
                onClick = onLogin,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun MastodonAccountPanel(
    account: Account,
    isLoading: Boolean,
    onLogout: () -> Unit,
) {
    AppCard {
        androidx.compose.foundation.layout.Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppAvatar(
                imageUrl = account.avatarUrl,
                name = account.displayName ?: account.username,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = account.displayName ?: account.username,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "@${account.username} · ${account.instanceUrl}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Spacer(Modifier.height(AppSpacing.md))
        if (isLoading) {
            AppLoading(message = "Signing out...")
        } else {
            AppSecondaryButton(
                text = "Logout",
                icon = Icons.AutoMirrored.Outlined.Logout,
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonAuthScreenPreview() {
    FediverseHubTheme {
        MastodonAuthScreen(
            uiState = MastodonAuthUiState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonAuthLoggedInPreview() {
    FediverseHubTheme {
        MastodonAuthScreen(
            uiState = MastodonAuthUiState(
                account = Account(
                    id = "mastodon-1",
                    platform = PlatformType.MASTODON,
                    instanceUrl = "mastodon.social",
                    username = "sami",
                    displayName = "Sami Uysal",
                    avatarUrl = null,
                    accessToken = null,
                ),
            ),
            onEvent = {},
        )
    }
}
