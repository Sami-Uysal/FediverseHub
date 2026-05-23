package com.samiuysal.fediversehub.feature.auth

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Login
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.samiuysal.fediversehub.core.designsystem.component.AppButton
import com.samiuysal.fediversehub.core.designsystem.component.AppCard
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppTextField
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing

@Composable
fun AuthConnectCard(
    title: String,
    description: String,
    instanceUrl: String,
    instanceHint: String,
    buttonText: String,
    loadingMessage: String,
    isLoading: Boolean,
    onInstanceChanged: (String) -> Unit,
    onLogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AppCard(modifier = modifier) {
        Column {
            Text(text = title, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(AppSpacing.lg))
            AppTextField(
                value = instanceUrl,
                onValueChange = onInstanceChanged,
                label = "Instance URL",
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(AppSpacing.xs))
            Text(
                text = instanceHint,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(AppSpacing.lg))
            if (isLoading) {
                AppLoading(message = loadingMessage)
            } else {
                AppButton(
                    text = buttonText,
                    icon = Icons.AutoMirrored.Outlined.Login,
                    onClick = onLogin,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
