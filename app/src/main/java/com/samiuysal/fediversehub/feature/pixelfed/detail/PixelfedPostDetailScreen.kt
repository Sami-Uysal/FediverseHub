package com.samiuysal.fediversehub.feature.pixelfed.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.feature.home.MockFediverseData
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedComment

@Composable
fun PixelfedPostDetailScreen(
    uiState: PixelfedPostDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRetryComments: () -> Unit,
    onLikeClick: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        PixelfedPostDetailTopBar(onBack = onBack)
        when (uiState) {
            PixelfedPostDetailUiState.Loading -> AppLoading(
                message = "Pixelfed post yükleniyor...",
                modifier = Modifier.weight(1f),
            )
            is PixelfedPostDetailUiState.Error -> AppErrorState(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )
            is PixelfedPostDetailUiState.Success -> PixelfedPostDetailContent(
                state = uiState,
                onRetryComments = onRetryComments,
                onLikeClick = onLikeClick,
                onMediaSelected = onMediaSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PixelfedPostDetailTopBar(onBack: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back",
                    )
                }
                Column {
                    Text(
                        text = "Pixelfed",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Post detail",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
        }
    }
}

@Composable
private fun PixelfedPostDetailContent(
    state: PixelfedPostDetailUiState.Success,
    onRetryComments: () -> Unit,
    onLikeClick: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        item(key = "pixelfed-detail-post", contentType = "pixelfed-detail-post") {
            PixelfedPostHero(
                post = state.post,
                onLikeClick = onLikeClick,
                onMediaSelected = onMediaSelected,
            )
        }
        item(key = "pixelfed-comments-title", contentType = "pixelfed-comments-title") {
            CommentsHeader(
                count = state.post.comments,
                isLoading = state.isCommentsLoading,
            )
        }
        when {
            state.isCommentsLoading -> {
                item(key = "pixelfed-comments-loading", contentType = "pixelfed-comments-loading") {
                    AppLoading(
                        message = "Yorumlar yükleniyor...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                    )
                }
            }
            state.commentsErrorMessage != null -> {
                item(key = "pixelfed-comments-error", contentType = "pixelfed-comments-error") {
                    AppErrorState(
                        message = state.commentsErrorMessage,
                        onRetry = onRetryComments,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                    )
                }
            }
            state.comments.isEmpty() -> {
                item(key = "pixelfed-comments-empty", contentType = "pixelfed-comments-empty") {
                    EmptyState(
                        title = "Yorum yok",
                        message = "Görünen public yorum yok.",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                    )
                }
            }
            else -> {
                items(
                    items = state.comments,
                    key = { it.id },
                    contentType = { "pixelfed-comment" },
                ) { comment ->
                    PixelfedCommentRow(comment = comment)
                }
            }
        }
    }
}

@Composable
private fun PixelfedPostHero(
    post: PixelfedPostUiModel,
    onLikeClick: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(
                imageUrl = post.avatarUrl,
                name = post.displayName,
                size = 42.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${post.username} · ${post.timeAgo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        PixelfedDetailImage(
            imageUrl = post.imageUrl,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg)
                .clickable {
                    onMediaSelected(
                        post.fullImageUrls.ifEmpty { listOf(post.imageUrl) },
                        post.altFlags,
                        0,
                    )
                },
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LikeButton(
                likes = post.likes,
                isLiked = post.isLiked,
                isLoading = post.isLoadingLike,
                onClick = onLikeClick,
            )
            Icon(
                imageVector = Icons.Outlined.ChatBubbleOutline,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = "${post.comments} comments",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        if (post.caption.isNotBlank()) {
            Text(
                text = post.caption,
                modifier = Modifier.padding(
                    start = AppSpacing.lg,
                    end = AppSpacing.lg,
                    bottom = AppSpacing.lg,
                ),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
    }
}

@Composable
private fun PixelfedDetailImage(
    imageUrl: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val request = remember(context, imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(DETAIL_IMAGE_SIZE, DETAIL_IMAGE_SIZE)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(AppRadius.md))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
        )
    }
}

@Composable
private fun LikeButton(
    likes: Int,
    isLiked: Boolean,
    isLoading: Boolean,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        IconButton(onClick = onClick, enabled = !isLoading) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
            } else {
                Icon(
                    imageVector = if (isLiked) Icons.Outlined.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Like",
                    tint = if (isLiked) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )
            }
        }
        Text(
            text = "$likes",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CommentsHeader(
    count: Int,
    isLoading: Boolean,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Comments",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = "$count",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (isLoading) {
            Spacer(Modifier.weight(1f))
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        }
    }
}

@Composable
private fun PixelfedCommentRow(comment: PixelfedComment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        AppAvatar(
            imageUrl = comment.avatarUrl,
            name = comment.displayName,
            size = 34.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = comment.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = comment.timeAgo,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

private const val DETAIL_IMAGE_SIZE = 1080

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PixelfedPostDetailScreenPreview() {
    FediverseHubTheme {
        PixelfedPostDetailScreen(
            uiState = PixelfedPostDetailUiState.Success(
                post = MockFediverseData.homeState.pixelfedPosts.first(),
            ),
            onBack = {},
            onRetry = {},
            onRetryComments = {},
            onLikeClick = {},
            onMediaSelected = { _, _, _ -> },
        )
    }
}
