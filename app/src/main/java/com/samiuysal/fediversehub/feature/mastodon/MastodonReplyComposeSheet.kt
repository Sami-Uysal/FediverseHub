package com.samiuysal.fediversehub.feature.mastodon

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Send
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.feature.home.MockFediverseData

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastodonReplyComposeSheet(
    state: MastodonReplyComposeState,
    onTextChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        MastodonStatusComposeContent(
            title = "Reply",
            text = state.text,
            textLabel = "Write your reply",
            maxCharacters = state.maxCharacters,
            isSending = state.isSending,
            errorMessage = state.errorMessage,
            canSend = state.canSend,
            parent = state.parent,
            onTextChanged = onTextChanged,
            onSend = onSend,
            modifier = Modifier.padding(
                start = AppSpacing.lg,
                end = AppSpacing.lg,
                bottom = AppSpacing.xl,
            ),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MastodonNewPostComposeSheet(
    state: MastodonNewPostComposeState,
    onTextChanged: (String) -> Unit,
    onVisibilityChanged: (MastodonVisibility) -> Unit,
    onContentWarningEnabledChanged: (Boolean) -> Unit,
    onContentWarningChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = modifier,
    ) {
        MastodonStatusComposeContent(
            title = "New post",
            text = state.text,
            textLabel = "What's on your mind?",
            maxCharacters = state.maxCharacters,
            isSending = state.isSending,
            errorMessage = state.errorMessage,
            canSend = state.canSend,
            visibility = state.visibility,
            showVisibilitySelector = true,
            isContentWarningEnabled = state.isContentWarningEnabled,
            contentWarning = state.contentWarning,
            showContentWarningControls = true,
            onTextChanged = onTextChanged,
            onVisibilityChanged = onVisibilityChanged,
            onContentWarningEnabledChanged = onContentWarningEnabledChanged,
            onContentWarningChanged = onContentWarningChanged,
            onSend = onSend,
            modifier = Modifier.padding(
                start = AppSpacing.lg,
                end = AppSpacing.lg,
                bottom = AppSpacing.xl,
            ),
        )
    }
}

@Composable
private fun MastodonStatusComposeContent(
    title: String,
    text: String,
    textLabel: String,
    maxCharacters: Int,
    isSending: Boolean,
    errorMessage: String?,
    canSend: Boolean,
    modifier: Modifier = Modifier,
    parent: MastodonPostUiModel? = null,
    visibility: MastodonVisibility = MastodonVisibility.PUBLIC,
    showVisibilitySelector: Boolean = false,
    isContentWarningEnabled: Boolean = false,
    contentWarning: String = "",
    showContentWarningControls: Boolean = false,
    onTextChanged: (String) -> Unit,
    onVisibilityChanged: (MastodonVisibility) -> Unit = {},
    onContentWarningEnabledChanged: (Boolean) -> Unit = {},
    onContentWarningChanged: (String) -> Unit = {},
    onSend: () -> Unit,
) {
    val remaining = maxCharacters - text.length

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        parent?.let {
            ReplyParentPreview(parent = it)
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
        }
        if (showVisibilitySelector) {
            VisibilitySelector(
                selected = visibility,
                enabled = !isSending,
                onSelected = onVisibilityChanged,
            )
        }
        if (showContentWarningControls) {
            ContentWarningControls(
                enabled = !isSending,
                checked = isContentWarningEnabled,
                contentWarning = contentWarning,
                onCheckedChange = onContentWarningEnabledChanged,
                onContentWarningChanged = onContentWarningChanged,
            )
        }
        OutlinedTextField(
            value = text,
            onValueChange = { value ->
                if (value.length <= maxCharacters) {
                    onTextChanged(value)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = !isSending,
            label = { Text(textLabel) },
            supportingText = errorMessage?.let { message ->
                { Text(message, color = MaterialTheme.colorScheme.error) }
            },
            minLines = 4,
            maxLines = 6,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$remaining",
                style = MaterialTheme.typography.labelLarge,
                color = if (remaining < 0) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
            )
            Button(
                onClick = onSend,
                enabled = canSend && !isSending,
            ) {
                if (isSending) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(Modifier.size(AppSpacing.sm))
                } else {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.AutoMirrored.Outlined.Send,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                    )
                    Spacer(Modifier.size(AppSpacing.sm))
                }
                Text("Send")
            }
        }
    }
}

@Composable
fun MastodonReplyComposeContent(
    state: MastodonReplyComposeState,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    MastodonStatusComposeContent(
        title = "Reply",
        text = state.text,
        textLabel = "Write your reply",
        maxCharacters = state.maxCharacters,
        isSending = state.isSending,
        errorMessage = state.errorMessage,
        canSend = state.canSend,
        parent = state.parent,
        onTextChanged = onTextChanged,
        onSend = onSend,
        modifier = modifier,
    )
}

@Composable
private fun VisibilitySelector(
    selected: MastodonVisibility,
    enabled: Boolean,
    onSelected: (MastodonVisibility) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        MastodonVisibility.entries.forEach { visibility ->
            FilterChip(
                selected = visibility == selected,
                enabled = enabled,
                onClick = { onSelected(visibility) },
                label = { Text(visibility.label) },
            )
        }
    }
}

@Composable
private fun ContentWarningControls(
    enabled: Boolean,
    checked: Boolean,
    contentWarning: String,
    onCheckedChange: (Boolean) -> Unit,
    onContentWarningChanged: (String) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AssistChip(
                onClick = { onCheckedChange(!checked) },
                enabled = enabled,
                label = { Text("Content warning") },
            )
            Switch(
                checked = checked,
                enabled = enabled,
                onCheckedChange = onCheckedChange,
            )
        }
        if (checked) {
            OutlinedTextField(
                value = contentWarning,
                onValueChange = onContentWarningChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = enabled,
                label = { Text("CW text") },
                singleLine = true,
            )
        }
    }
}

@Composable
private fun ReplyParentPreview(
    parent: MastodonPostUiModel,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        AppAvatar(
            imageUrl = parent.avatarUrl,
            name = parent.displayName,
            size = 38.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = parent.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = parent.username,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = parent.content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

val MastodonReplyComposeState.canSend: Boolean
    get() {
        val trimmed = text.trim()
        val mention = parent.username.trim()
        val body = trimmed.removePrefix(mention).trim()
        return body.isNotBlank() && text.length <= maxCharacters
    }

val MastodonNewPostComposeState.canSend: Boolean
    get() = text.trim().isNotBlank() && text.length <= maxCharacters

@Preview(showBackground = true, widthDp = 390, heightDp = 420)
@Composable
fun MastodonReplyComposeContentPreview() {
    FediverseHubTheme {
        val post = MockFediverseData.homeState.mastodonPosts.first()
        MastodonReplyComposeContent(
            state = MastodonReplyComposeState(
                parent = post,
                text = "${post.username} This is a reply draft.",
            ),
            onTextChanged = {},
            onSend = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 520)
@Composable
fun MastodonNewPostComposeContentPreview() {
    FediverseHubTheme {
        MastodonStatusComposeContent(
            title = "New post",
            text = "Shipping a small FediverseHub update.",
            textLabel = "What's on your mind?",
            maxCharacters = MastodonReplyMaxCharacters,
            isSending = false,
            errorMessage = null,
            canSend = true,
            visibility = MastodonVisibility.PUBLIC,
            showVisibilitySelector = true,
            isContentWarningEnabled = true,
            contentWarning = "Product update",
            showContentWarningControls = true,
            onTextChanged = {},
            onSend = {},
            modifier = Modifier.padding(AppSpacing.lg),
        )
    }
}
