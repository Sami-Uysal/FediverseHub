package com.samiuysal.fediversehub.feature.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
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
fun LemmyAuthScreen(
    uiState: LemmyAuthUiState,
    onEvent: (LemmyAuthUiEvent) -> Unit,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
) {
    Column(modifier = modifier.fillMaxSize()) {
        if (showTopBar) {
            AppTopBar(
                title = "Lemmy account",
                subtitle = "Forum-first fediverse login",
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            uiState.errorMessage?.let { message ->
                AppErrorState(
                    message = message,
                    onRetry = { onEvent(LemmyAuthUiEvent.LoginClicked) },
                    modifier = Modifier.height(160.dp),
                )
            }

            if (uiState.account != null) {
                LemmyAccountPanel(
                    account = uiState.account,
                    isLoading = uiState.isLoading,
                    onLogout = { onEvent(LemmyAuthUiEvent.LogoutClicked) },
                )
            } else {
                LemmyLoginCard(
                    uiState = uiState,
                    onEvent = onEvent,
                )
            }
        }
    }
}

@Composable
private fun LemmyLoginCard(
    uiState: LemmyAuthUiState,
    onEvent: (LemmyAuthUiEvent) -> Unit,
) {
    AppCard {
        Text(text = "Connect your Lemmy instance", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(AppSpacing.xs))
        Text(
            text = "Use your Lemmy username/password. JWT stays local in this app.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(AppSpacing.lg))
        AppTextField(
            value = uiState.instanceUrl,
            onValueChange = { onEvent(LemmyAuthUiEvent.InstanceUrlChanged(it)) },
            label = "Instance URL",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AppSpacing.sm))
        OutlinedTextField(
            value = uiState.usernameOrEmail,
            onValueChange = { onEvent(LemmyAuthUiEvent.UsernameChanged(it)) },
            label = { Text("Username or email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AppSpacing.sm))
        OutlinedTextField(
            value = uiState.password,
            onValueChange = { onEvent(LemmyAuthUiEvent.PasswordChanged(it)) },
            label = { Text("Password") },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(AppSpacing.lg))
        if (uiState.isLoading) {
            AppLoading(message = "Logging into Lemmy...")
        } else {
            AppButton(
                text = "Continue with Lemmy",
                icon = Icons.AutoMirrored.Outlined.Login,
                onClick = { onEvent(LemmyAuthUiEvent.LoginClicked) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun LemmyAccountPanel(
    account: Account,
    isLoading: Boolean,
    onLogout: () -> Unit,
) {
    AppCard {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
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
fun LemmyAuthScreenPreview() {
    FediverseHubTheme {
        LemmyAuthScreen(
            uiState = LemmyAuthUiState(),
            onEvent = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 420)
@Composable
fun LemmyAuthAccountPreview() {
    FediverseHubTheme {
        LemmyAuthScreen(
            uiState = LemmyAuthUiState(
                account = Account(
                    id = "lemmy-preview",
                    platform = PlatformType.LEMMY,
                    instanceUrl = "lemmy.world",
                    username = "sami",
                    displayName = "sami",
                    avatarUrl = null,
                    accessToken = "token",
                ),
            ),
            onEvent = {},
        )
    }
}
