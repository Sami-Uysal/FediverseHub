package com.samiuysal.fediversehub.feature.pixelfed

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppIconButton
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.MockFediverseData
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedComment

@Composable
fun PixelfedHomeScreen(
    account: Account?,
    posts: LazyPagingItems<PixelfedPostUiModel>,
    actionOverrides: Map<String, PixelfedPostUiModel>,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    onPostClick: (String) -> Unit,
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit,
    onLikeClick: (PixelfedPostUiModel) -> Unit,
    onCommentsClick: (PixelfedPostUiModel) -> Unit,
) {
    Column(modifier = modifier) {
        if (showTopBar) {
            AppTopBar(
                title = account?.displayName ?: "Pixelfed",
                subtitle = "@${account?.username.orEmpty()} · ${account?.instanceUrl.orEmpty()}",
                actions = {
                    AppIconButton(
                        icon = Icons.Outlined.GridView,
                        contentDescription = "Profile grid",
                        onClick = {},
                    )
                },
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = AppSpacing.xl),
        ) {
            when {
                posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0 -> {
                    item(key = "pixelfed-loading", contentType = "pixelfed-loading") {
                        PixelfedFeedSkeleton()
                    }
                }
                posts.loadState.refresh is LoadState.Error && posts.itemCount == 0 -> {
                    val error = posts.loadState.refresh as LoadState.Error
                    item(key = "pixelfed-error", contentType = "pixelfed-error") {
                        AppErrorState(
                            message = error.error.localizedMessage ?: "Pixelfed feed failed.",
                            onRetry = posts::retry,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )
                    }
                }
                posts.itemCount == 0 -> {
                    item(key = "pixelfed-empty", contentType = "pixelfed-empty") {
                        EmptyState(
                            title = "No photos yet",
                            message = "Your Pixelfed home feed will appear here.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                        )
                    }
                }
            }
            items(
                count = posts.itemCount,
                key = posts.itemKey { it.id },
                contentType = posts.itemContentType { "pixelfed-post" },
            ) { index ->
                posts[index]?.let { post ->
                    val visiblePost = actionOverrides[post.id] ?: post
                    PixelfedPostCard(
                        post = visiblePost,
                        onClick = { onPostClick(visiblePost.id) },
                        onMediaClick = onMediaClick,
                        onLikeClick = onLikeClick,
                        onCommentsClick = onCommentsClick,
                    )
                }
            }
            if (posts.loadState.append is LoadState.Loading) {
                item(key = "pixelfed-append-loading", contentType = "pixelfed-append-loading") {
                    AppLoading(
                        message = "Loading more...",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(96.dp),
                    )
                }
            }
        }
    }
}

@Composable
fun PixelfedHomeScreenContent(
    account: Account?,
    posts: List<PixelfedPostUiModel>,
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    onPostClick: (String) -> Unit = {},
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit = { _, _, _ -> },
    onLikeClick: (PixelfedPostUiModel) -> Unit = {},
    onCommentsClick: (PixelfedPostUiModel) -> Unit = {},
) {
    Column(modifier = modifier) {
        if (showTopBar) {
            AppTopBar(
                title = account?.displayName ?: "Pixelfed",
                subtitle = "@${account?.username.orEmpty()} · ${account?.instanceUrl.orEmpty()}",
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(bottom = AppSpacing.xl),
        ) {
            items(posts, key = { it.id }) { post ->
                PixelfedPostCard(
                    post = post,
                    onClick = { onPostClick(post.id) },
                    onMediaClick = onMediaClick,
                    onLikeClick = onLikeClick,
                    onCommentsClick = onCommentsClick,
                )
            }
        }
    }
}

@Composable
private fun PixelfedPostCard(
    post: PixelfedPostUiModel,
    onClick: () -> Unit,
    onMediaClick: (List<String>, List<Boolean>, Int) -> Unit,
    onLikeClick: (PixelfedPostUiModel) -> Unit,
    onCommentsClick: (PixelfedPostUiModel) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            AppAvatar(
                imageUrl = post.avatarUrl,
                name = post.displayName,
                size = 38.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = "${post.username} · ${post.timeAgo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Icon(
                imageVector = Icons.Outlined.MoreHoriz,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        AppMediaCarousel(
            imageUrls = post.fullImageUrls.ifEmpty { listOf(post.imageUrl) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg),
            onImageClick = { index ->
                onMediaClick(
                    post.fullImageUrls.ifEmpty { listOf(post.imageUrl) },
                    post.altFlags,
                    index,
                )
            },
        )
        Spacer(Modifier.height(AppSpacing.sm))
        Row(
            modifier = Modifier.padding(horizontal = AppSpacing.lg),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppLikeButton(
                likes = post.likes,
                isLiked = post.isLiked,
                onClick = { onLikeClick(post) },
            )
            AppIconButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                onClick = { onCommentsClick(post) },
            )
            Text(
                text = "${post.comments} comments",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        PixelfedCaption(
            caption = post.caption,
            modifier = Modifier.padding(
                start = AppSpacing.lg,
                top = AppSpacing.xs,
                end = AppSpacing.lg,
                bottom = AppSpacing.md,
            ),
        )
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f),
        )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PixelfedCommentsSheet(
    state: PixelfedCommentsState,
    onDismiss: () -> Unit,
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = "Comments",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            when {
                state.isLoading -> AppLoading(
                    message = "Loading comments...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
                state.errorMessage != null -> AppErrorState(
                    message = state.errorMessage,
                    onRetry = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                )
                state.comments.isEmpty() -> EmptyState(
                    title = "No comments",
                    message = "No public replies visible yet.",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                )
                else -> state.comments.take(30).forEach { comment ->
                    PixelfedCommentRow(comment = comment)
                }
            }
            Spacer(Modifier.height(AppSpacing.lg))
        }
    }
}

@Composable
private fun PixelfedCommentRow(comment: PixelfedComment) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        AppAvatar(
            imageUrl = comment.avatarUrl,
            name = comment.displayName,
            size = 32.dp,
        )
        Column(modifier = Modifier.weight(1f)) {
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Text(
                    text = comment.displayName,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
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

@Composable
private fun AppMediaCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
    onImageClick: (Int) -> Unit = {},
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(RoundedCornerShape(AppRadius.lg))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        val selectedImage = imageUrls.firstOrNull()
        if (selectedImage != null) {
            PixelfedImage(
                imageUrl = selectedImage,
                imageSize = FEED_IMAGE_SIZE,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(AppRadius.lg))
                    .then(Modifier),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { onImageClick(0) },
            )
        }
        if (imageUrls.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                imageUrls.forEachIndexed { index, _ ->
                    Box(
                        modifier = Modifier
                            .size(if (index == 0) 8.dp else 6.dp)
                            .clip(CircleShape)
                            .background(
                                if (index == 0) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.36f)
                                },
                            ),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppLikeButton(
    likes: Int,
    isLiked: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        IconButton(onClick = onClick) {
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
        Text(
            text = "$likes",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun PixelfedImage(
    imageUrl: String,
    imageSize: Int,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    if (isPreview) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        )
        return
    }

    val request = remember(context, imageUrl, imageSize) {
        ImageRequest.Builder(context)
            .data(imageUrl)
            .size(imageSize, imageSize)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    AsyncImage(
        model = request,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun PixelfedCaption(
    caption: String,
    modifier: Modifier = Modifier,
) {
    if (caption.isBlank()) return
    var expanded by rememberSaveable(caption) { mutableStateOf(false) }
    Column(modifier = modifier) {
        Text(
            text = caption,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = if (expanded) Int.MAX_VALUE else 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (!expanded && caption.length > COLLAPSED_CAPTION_THRESHOLD) {
            Text(
                text = "devamını oku",
                modifier = Modifier.clickable { expanded = true },
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun PixelfedFeedSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
    ) {
        repeat(2) {
            PixelfedPostSkeleton()
        }
    }
}

@Composable
private fun PixelfedPostSkeleton() {
    val skeletonColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.sm)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(skeletonColor),
            )
            Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                Box(
                    modifier = Modifier
                        .width(148.dp)
                        .height(16.dp)
                        .clip(RoundedCornerShape(AppRadius.sm))
                        .background(skeletonColor),
                )
                Box(
                    modifier = Modifier
                        .width(92.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(AppRadius.sm))
                        .background(skeletonColor),
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(AppRadius.lg))
                .background(skeletonColor),
        )
    }
}

private const val FEED_IMAGE_SIZE = 720
private const val COLLAPSED_CAPTION_THRESHOLD = 72

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PixelfedHomeScreenPreview() {
    FediverseHubTheme {
        PixelfedHomeScreenContent(
            account = MockFediverseData.homeState.accounts.first { it.platform == PlatformType.PIXELFED },
            posts = MockFediverseData.homeState.pixelfedPosts,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PixelfedPostCardPreview() {
    FediverseHubTheme {
        PixelfedPostCard(
            post = MockFediverseData.homeState.pixelfedPosts.first(),
            onClick = {},
            onMediaClick = { _, _, _ -> },
            onLikeClick = {},
            onCommentsClick = {},
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppMediaCarouselPreview() {
    FediverseHubTheme {
        AppMediaCarousel(
            imageUrls = MockFediverseData.homeState.pixelfedPosts.map { it.imageUrl },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppLikeButtonPreview() {
    FediverseHubTheme {
        AppLikeButton(
            likes = 1284,
            isLiked = true,
            onClick = {},
        )
    }
}
