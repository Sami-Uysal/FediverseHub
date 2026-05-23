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
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
        MastodonReplyComposeContent(
            state = state,
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

@Composable
fun MastodonReplyComposeContent(
    state: MastodonReplyComposeState,
    onTextChanged: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val remaining = state.maxCharacters - state.text.length
    val canSend = state.canSend

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(
            text = "Reply",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
        )
        ReplyParentPreview(parent = state.parent)
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
        OutlinedTextField(
            value = state.text,
            onValueChange = { value ->
                if (value.length <= state.maxCharacters) {
                    onTextChanged(value)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp),
            enabled = !state.isSending,
            label = { Text("Write your reply") },
            supportingText = state.errorMessage?.let { message ->
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
                enabled = canSend && !state.isSending,
            ) {
                if (state.isSending) {
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
