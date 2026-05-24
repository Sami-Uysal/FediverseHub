package com.samiuysal.fediversehub.feature.lemmy.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.common.error.userFacingMessage
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType

@Composable
fun LemmyCommunityScreen(
    uiState: LemmyCommunityUiState,
    composerState: LemmyPostComposerUiState,
    posts: LazyPagingItems<LemmyPostUiModel>,
    selectedSort: LemmySortType,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSortSelected: (LemmySortType) -> Unit,
    onFollowClick: () -> Unit,
    onOpenComposer: () -> Unit,
    onCloseComposer: () -> Unit,
    onComposerTypeSelected: (LemmyPostComposeType) -> Unit,
    onComposerTitleChanged: (String) -> Unit,
    onComposerBodyChanged: (String) -> Unit,
    onComposerUrlChanged: (String) -> Unit,
    onSubmitPost: () -> Unit,
    onPostSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        LemmyCommunityTopBar(onBack = onBack)
        when (uiState) {
            LemmyCommunityUiState.Loading -> AppLoading(
                message = "Community yükleniyor...",
                modifier = Modifier.weight(1f),
            )
            is LemmyCommunityUiState.Error -> AppErrorState(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )
            is LemmyCommunityUiState.Success -> LemmyCommunityContent(
                community = uiState.community,
                composerState = composerState,
                posts = posts,
                selectedSort = selectedSort,
                onSortSelected = onSortSelected,
                onFollowClick = onFollowClick,
                onOpenComposer = onOpenComposer,
                onCloseComposer = onCloseComposer,
                onComposerTypeSelected = onComposerTypeSelected,
                onComposerTitleChanged = onComposerTitleChanged,
                onComposerBodyChanged = onComposerBodyChanged,
                onComposerUrlChanged = onComposerUrlChanged,
                onSubmitPost = onSubmitPost,
                onPostSelected = onPostSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LemmyCommunityTopBar(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Community",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
}

@Composable
private fun LemmyCommunityContent(
    community: LemmyCommunityUiModel,
    composerState: LemmyPostComposerUiState,
    posts: LazyPagingItems<LemmyPostUiModel>,
    selectedSort: LemmySortType,
    onSortSelected: (LemmySortType) -> Unit,
    onFollowClick: () -> Unit,
    onOpenComposer: () -> Unit,
    onCloseComposer: () -> Unit,
    onComposerTypeSelected: (LemmyPostComposeType) -> Unit,
    onComposerTitleChanged: (String) -> Unit,
    onComposerBodyChanged: (String) -> Unit,
    onComposerUrlChanged: (String) -> Unit,
    onSubmitPost: () -> Unit,
    onPostSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        item(key = "community-header", contentType = "community-header") {
            LemmyCommunityHeader(
                community = community,
                onFollowClick = onFollowClick,
                onOpenComposer = onOpenComposer,
            )
            if (composerState.isOpen) {
                LemmyPostComposer(
                    state = composerState,
                    onClose = onCloseComposer,
                    onTypeSelected = onComposerTypeSelected,
                    onTitleChanged = onComposerTitleChanged,
                    onBodyChanged = onComposerBodyChanged,
                    onUrlChanged = onComposerUrlChanged,
                    onSubmit = onSubmitPost,
                )
            }
            LemmyCommunitySort(
                selectedSort = selectedSort,
                onSortSelected = onSortSelected,
            )
        }
        when {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0 -> {
                item(key = "community-posts-loading", contentType = "community-loading") {
                    AppLoading(message = "Postlar yükleniyor...", modifier = Modifier.height(180.dp))
                }
            }
            posts.loadState.refresh is LoadState.Error && posts.itemCount == 0 -> {
                val error = posts.loadState.refresh as LoadState.Error
                item(key = "community-posts-error", contentType = "community-error") {
                    AppErrorState(
                        message = error.error.userFacingMessage("Community postları yüklenemedi."),
                        onRetry = posts::retry,
                        modifier = Modifier.height(220.dp),
                    )
                }
            }
            posts.itemCount == 0 -> {
                item(key = "community-posts-empty", contentType = "community-empty") {
                    EmptyState(
                        title = "Post yok",
                        message = "Bu community için post görünmüyor.",
                        modifier = Modifier.height(220.dp),
                    )
                }
            }
            else -> {
                items(
                    count = posts.itemCount,
                    key = posts.itemKey { it.id },
                    contentType = posts.itemContentType { "community-post" },
                ) { index ->
                    posts[index]?.let { post ->
                        LemmyCommunityPostRow(post = post, onClick = { onPostSelected(post.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun LemmyCommunityHeader(
    community: LemmyCommunityUiModel,
    onFollowClick: () -> Unit,
    onOpenComposer: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        val bannerUrl = community.bannerUrl
        if (!bannerUrl.isNullOrBlank()) {
            val context = LocalContext.current
            val request = remember(bannerUrl) {
                ImageRequest.Builder(context)
                    .data(bannerUrl)
                    .size(960, 320)
                    .precision(Precision.INEXACT)
                    .crossfade(false)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)),
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.Top,
        ) {
            LemmyCommunityIcon(community)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Text(
                    text = community.title,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "c/${community.name}",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (community.description.isNotBlank()) {
                    Text(
                        text = community.description,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
                Text(
                    text = "${community.subscribers.compactMetric()} abone · ${community.posts.compactMetric()} post",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            AssistChip(
                onClick = onFollowClick,
                enabled = !community.isFollowLoading,
                label = {
                    if (community.isFollowLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    } else {
                        Text(if (community.isSubscribed) "Subscribed" else "Subscribe")
                    }
                },
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg),
            horizontalArrangement = Arrangement.End,
        ) {
            AssistChip(
                onClick = onOpenComposer,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = null,
                    )
                },
                label = { Text("Post oluştur") },
            )
        }
        Spacer(modifier = Modifier.height(AppSpacing.sm))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
    }
}

@Composable
private fun LemmyPostComposer(
    state: LemmyPostComposerUiState,
    onClose: () -> Unit,
    onTypeSelected: (LemmyPostComposeType) -> Unit,
    onTitleChanged: (String) -> Unit,
    onBodyChanged: (String) -> Unit,
    onUrlChanged: (String) -> Unit,
    onSubmit: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Yeni post",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            IconButton(onClick = onClose, enabled = !state.isSubmitting) {
                Icon(Icons.Outlined.Close, contentDescription = "Kapat")
            }
        }
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
            label = { Text("Title") },
        )
        if (state.type == LemmyPostComposeType.LINK) {
            OutlinedTextField(
                value = state.url,
                onValueChange = onUrlChanged,
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSubmitting,
                singleLine = true,
                label = { Text("URL") },
                placeholder = { Text("https://...") },
            )
        }
        OutlinedTextField(
            value = state.body,
            onValueChange = onBodyChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp),
            enabled = !state.isSubmitting,
            minLines = 3,
            label = { Text("Body") },
        )
        state.errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onClose, enabled = !state.isSubmitting) {
                Text("Vazgeç")
            }
            Spacer(modifier = Modifier.width(AppSpacing.sm))
            Button(
                onClick = onSubmit,
                enabled = !state.isSubmitting && state.title.isNotBlank(),
            ) {
                if (state.isSubmitting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                    Spacer(modifier = Modifier.width(AppSpacing.xs))
                }
                Text("Yayınla")
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.62f))
}

@Composable
private fun LemmyCommunityIcon(community: LemmyCommunityUiModel) {
    val iconUrl = community.iconUrl
    Box(
        modifier = Modifier
            .size(58.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.14f)),
        contentAlignment = Alignment.Center,
    ) {
        if (!iconUrl.isNullOrBlank()) {
            val context = LocalContext.current
            val request = remember(iconUrl) {
                ImageRequest.Builder(context)
                    .data(iconUrl)
                    .size(160, 160)
                    .precision(Precision.INEXACT)
                    .crossfade(false)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
            )
        } else {
            Icon(Icons.Outlined.Groups, contentDescription = null)
        }
    }
}

@Composable
private fun LemmyCommunitySort(
    selectedSort: LemmySortType,
    onSortSelected: (LemmySortType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        lemmySortTypes.forEach { sort ->
            FilterChip(
                selected = selectedSort == sort,
                onClick = { onSortSelected(sort) },
                label = { Text(sort.label) },
            )
        }
    }
}

@Composable
private fun LemmyCommunityPostRow(
    post: LemmyPostUiModel,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${post.author} · ${post.timeAgo}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = "${post.score.compactMetric()} puan · ${post.comments.compactMetric()} yorum",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
}

private val lemmySortTypes = listOf(
    LemmySortType.ACTIVE,
    LemmySortType.HOT,
    LemmySortType.NEW,
    LemmySortType.TOP,
)

private val LemmySortType.label: String
    get() = when (this) {
        LemmySortType.ACTIVE -> "Active"
        LemmySortType.HOT -> "Hot"
        LemmySortType.NEW -> "New"
        LemmySortType.TOP -> "Top"
    }

private fun Int.compactMetric(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}
