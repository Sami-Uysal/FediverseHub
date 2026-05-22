package com.samiuysal.fediversehub.feature.pixelfed

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppCard
import com.samiuysal.fediversehub.core.designsystem.component.AppIconButton
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.MockFediverseData

@Composable
fun PixelfedHomeScreen(
    account: Account?,
    posts: List<PixelfedPostUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
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
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.lg),
        ) {
            item(key = "grid-preview") {
                ProfileGridPreview(posts = posts)
            }
            items(
                items = posts,
                key = { it.id },
            ) { post ->
                PixelfedPostCard(post = post)
            }
        }
    }
}

@Composable
private fun ProfileGridPreview(posts: List<PixelfedPostUiModel>) {
    AppCard(contentPadding = PaddingValues(AppSpacing.md)) {
        Text(
            text = "Explore grid",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(AppSpacing.md))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier
                .fillMaxWidth()
                .height(132.dp),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            userScrollEnabled = false,
        ) {
            items(posts, key = { "grid-${it.id}" }) { post ->
                PixelfedImage(
                    imageUrl = post.imageUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(AppRadius.sm)),
                )
            }
        }
    }
}

@Composable
private fun PixelfedPostCard(post: PixelfedPostUiModel) {
    AppCard(contentPadding = PaddingValues(AppSpacing.md)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = AppSpacing.md),
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clip(RoundedCornerShape(AppRadius.lg))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            PixelfedImage(
                imageUrl = post.imageUrl,
                contentDescription = post.caption,
                modifier = Modifier.fillMaxSize(),
            )
        }
        Spacer(Modifier.height(AppSpacing.md))
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppIconButton(
                icon = Icons.Outlined.FavoriteBorder,
                contentDescription = "Like",
                onClick = {},
            )
            AppIconButton(
                icon = Icons.Outlined.ChatBubbleOutline,
                contentDescription = "Comment",
                onClick = {},
            )
            Text(
                text = "${post.likes} likes · ${post.comments} comments",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Text(
            text = post.caption,
            modifier = Modifier.padding(horizontal = AppSpacing.sm),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
private fun AppMediaCarousel(
    imageUrls: List<String>,
    modifier: Modifier = Modifier,
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
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
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

    val request = remember(context, imageUrl) {
        ImageRequest.Builder(context)
            .data(imageUrl)
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

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PixelfedHomeScreenPreview() {
    FediverseHubTheme {
        PixelfedHomeScreen(
            account = MockFediverseData.homeState.accounts.first { it.platform == PlatformType.PIXELFED },
            posts = MockFediverseData.homeState.pixelfedPosts,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PixelfedPostCardPreview() {
    FediverseHubTheme {
        PixelfedPostCard(post = MockFediverseData.homeState.pixelfedPosts.first())
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun PixelfedGridPreview() {
    FediverseHubTheme {
        ProfileGridPreview(posts = MockFediverseData.homeState.pixelfedPosts)
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
