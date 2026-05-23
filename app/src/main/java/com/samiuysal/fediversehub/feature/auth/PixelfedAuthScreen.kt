package com.samiuysal.fediversehub.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppCard
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppSecondaryButton
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account

@Composable
fun PixelfedAuthScreen(
    uiState: PixelfedAuthUiState,
    onEvent: (PixelfedAuthUiEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        AppTopBar(
            title = "Pixelfed account",
            subtitle = "Photo-first fediverse login",
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
                    onRetry = { onEvent(PixelfedAuthUiEvent.LoginClicked) },
                    modifier = Modifier.height(160.dp),
                )
            }

            if (uiState.account != null) {
                PixelfedAccountPanel(
                    account = uiState.account,
                    isLoading = uiState.isLoading,
                    onLogout = { onEvent(PixelfedAuthUiEvent.LogoutClicked) },
                )
            } else {
                AuthConnectCard(
                    title = "Connect your Pixelfed instance",
                    description = "Sign in with the instance where your photos live. This account stays separate from Mastodon and Lemmy.",
                    instanceUrl = uiState.instanceUrl,
                    instanceHint = "Example: pixelfed.social or pixelfed.de",
                    buttonText = "Continue with Pixelfed",
                    loadingMessage = "Preparing Pixelfed OAuth...",
                    isLoading = uiState.isLoading,
                    onInstanceChanged = { onEvent(PixelfedAuthUiEvent.InstanceUrlChanged(it)) },
                    onLogin = { onEvent(PixelfedAuthUiEvent.LoginClicked) },
                )
            }
        }
    }
}

@Composable
private fun PixelfedAccountPanel(
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
