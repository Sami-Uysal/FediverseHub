package com.samiuysal.fediversehub.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.feature.lemmy.community.LemmyPostComposeType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelfedCreatePostSheet(
    state: PixelfedPostComposerUiState,
    onTextChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ComposerHeader(
                title = "Pixelfed",
                canSubmit = state.text.isNotBlank() && !state.isSubmitting,
                isSubmitting = state.isSubmitting,
                onDismiss = onDismiss,
                onSubmit = onSubmit,
            )
            OutlinedTextField(
                value = state.text,
                onValueChange = onTextChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 140.dp),
                enabled = !state.isSubmitting,
                minLines = 5,
                placeholder = { Text("Gönderi yaz") },
            )
            ComposerError(state.errorMessage)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LemmyCreatePostSheet(
    state: LemmyHomePostComposerUiState,
    onCommunitySelected: (String) -> Unit,
    onTypeSelected: (LemmyPostComposeType) -> Unit,
    onTitleChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    val selectedCommunity = state.selectedCommunity

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            ComposerHeader(
                title = "Lemmy",
                canSubmit = state.title.isNotBlank() &&
                        selectedCommunity != null &&
                        !state.isSubmitting &&
                        !state.isCommunitiesLoading,
                isSubmitting = state.isSubmitting,
                onDismiss = onDismiss,
                onSubmit = onSubmit,
            )

            CommunitySelector(
                state = state,
                onCommunitySelected = onCommunitySelected,
            )

            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
                FilterChip(
                    selected = state.type == LemmyPostComposeType.TEXT,
                    onClick = { onTypeSelected(LemmyPostComposeType.TEXT) },
                    enabled = !state.isSubmitting,
                    label = { Text("Text") },
                )
                FilterChip(
                    selected = state.type == LemmyPostComposeType.LINK,
                    onClick = { onTypeSelected(LemmyPostComposeType.LINK) },
                    enabled = !state.isSubmitting,
                    label = { Text("Link") },
                )
            }

            OutlinedTextField(
                value = state.title,
                onValueChange = onTitleChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting,
                singleLine = true,
                textStyle = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                placeholder = { Text("Başlık") },
            )

            OutlinedTextField(
                value = state.body,
                onValueChange = onBodyChanged,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp),
                enabled = !state.isSubmitting,
                minLines = 4,
                placeholder = { Text("Gövde metni (isteğe bağlı)") },
            )

            if (state.type == LemmyPostComposeType.LINK) {
                OutlinedTextField(
                    value = state.url,
                    onValueChange = onUrlChanged,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !state.isSubmitting,
                    singleLine = true,
                    placeholder = { Text("https://...") },
                )
            }

            ComposerError(state.errorMessage)
        }
    }
}

@Composable
private fun ComposerHeader(
    title: String,
    canSubmit: Boolean,
    isSubmitting: Boolean,
    onDismiss: () -> Unit,
    onSubmit: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onDismiss, enabled = !isSubmitting) {
            Icon(Icons.Outlined.Close, contentDescription = "Kapat")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Button(
            onClick = onSubmit,
            enabled = canSubmit,
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
            } else {
                Text("Paylaş")
            }
        }
    }
}

@Composable
private fun CommunitySelector(
    state: LemmyHomePostComposerUiState,
    onCommunitySelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedCommunity = state.selectedCommunity
    val communities = state.communities
    val isSubmitting = state.isSubmitting
    val isCommunitiesLoading = state.isCommunitiesLoading

    Surface(
        modifier = Modifier.clickable(
            enabled = !isSubmitting && communities.isNotEmpty(),
            onClick = { expanded = true },
        ),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f),
        shape = RoundedCornerShape(AppRadius.full),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Outlined.Groups, contentDescription = null)

            Text(
                text = when {
                    isCommunitiesLoading -> "Topluluklar yükleniyor"
                    selectedCommunity != null -> "c/${selectedCommunity.name}"
                    else -> "Bir topluluk seç"
                },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Icon(Icons.Outlined.ArrowDropDown, contentDescription = null)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            communities.forEach { community ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = "c/${community.name}",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    },
                    onClick = {
                        expanded = false
                        onCommunitySelected(community.id)
                    },
                )
            }
        }
    }
}

@Composable
private fun ComposerError(message: String?) {
    message?.let {
        Text(
            text = it,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
        )
    }
}
