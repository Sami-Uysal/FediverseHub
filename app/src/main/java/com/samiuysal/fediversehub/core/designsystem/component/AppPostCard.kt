package com.samiuysal.fediversehub.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
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
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing

@Immutable
data class AppLinkPreview(
    val domain: String,
    val title: String,
    val description: String?,
    val thumbnailUrl: String?,
)

@Composable
fun AppPostCard(
    displayName: String,
    username: String,
    timeAgo: String,
    avatarUrl: String?,
    content: String,
    mediaUrl: String?,
    mediaItems: List<AppMediaItem> = emptyList(),
    hasAltText: Boolean = false,
    boostedByDisplayName: String? = null,
    boostedByAvatarUrl: String? = null,
    replyContext: String? = null,
    showThreadLine: Boolean = false,
    threadLineTop: Boolean = false,
    threadLineBottom: Boolean = showThreadLine,
    threadIndentLevel: Int = 0,
    isThreadFocused: Boolean = false,
    linkPreview: AppLinkPreview? = null,
    actions: List<AppPostAction>,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    onMediaClick: (Int) -> Unit = {},
    onMoreClick: () -> Unit = {},
) {
    val metaText = remember(username, timeAgo) { "  $username · $timeAgo" }
    val visibleMedia = remember(mediaItems, mediaUrl, hasAltText) {
        if (mediaItems.isNotEmpty()) {
            mediaItems
        } else if (mediaUrl != null) {
            listOf(AppMediaItem(mediaUrl, mediaUrl, if (hasAltText) "ALT" else null))
        } else {
            emptyList()
        }
    }
    val boundedIndent = threadIndentLevel.coerceIn(0, 2)
    val startPadding = AppSpacing.md + (boundedIndent * 10).dp
    val containerColor = if (isThreadFocused) {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.18f)
    } else {
        MaterialTheme.colorScheme.background
    }
    val hasThreadConnection = threadLineTop || threadLineBottom
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                },
            ),
        color = containerColor,
    ) {
        Column {
            if (boostedByDisplayName != null) {
                BoostedByRow(
                    displayName = boostedByDisplayName,
                    avatarUrl = boostedByAvatarUrl,
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(
                        start = startPadding,
                        end = AppSpacing.md,
                        top = if (boostedByDisplayName == null) AppSpacing.md else AppSpacing.sm,
                        bottom = if (boostedByDisplayName == null) AppSpacing.md else AppSpacing.sm,
                    ),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                ThreadAvatarColumn(
                    avatarUrl = avatarUrl,
                    displayName = displayName,
                    showThreadLineTop = threadLineTop,
                    showThreadLineBottom = threadLineBottom,
                    showThreadMarker = hasThreadConnection,
                    modifier = Modifier.fillMaxHeight(),
                )
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = displayName,
                            modifier = Modifier.weight(0.44f, fill = false),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = metaText,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        IconButton(onClick = onMoreClick) {
                            Icon(
                                imageVector = Icons.Outlined.MoreHoriz,
                                contentDescription = "More options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                    if (replyContext != null) {
                        Text(
                            text = replyContext,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                    Spacer(Modifier.height(AppSpacing.xs))
                    Text(
                        text = content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (linkPreview != null) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        AppCompactLinkPreview(linkPreview = linkPreview)
                    }
                    if (visibleMedia.isNotEmpty()) {
                        Spacer(Modifier.height(AppSpacing.sm))
                        AppMediaPreview(
                            mediaItems = visibleMedia,
                            contentDescription = "Post media",
                            onMediaClick = onMediaClick,
                        )
                    }
                    Spacer(Modifier.height(AppSpacing.sm))
                    AppPostActionsRow(
                        actions = actions,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
            )
        }
    }
}

@Composable
private fun BoostedByRow(
    displayName: String,
    avatarUrl: String?,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 14.dp, top = AppSpacing.sm, end = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Repeat,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        AppAvatar(
            imageUrl = avatarUrl,
            name = displayName,
            size = 20.dp,
        )
        Text(
            text = "$displayName boosted",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ThreadAvatarColumn(
    avatarUrl: String?,
    displayName: String,
    showThreadLineTop: Boolean,
    showThreadLineBottom: Boolean,
    showThreadMarker: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.width(42.dp),
        contentAlignment = Alignment.TopCenter,
    ) {
        if (showThreadLineTop) {
            ThreadLine(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .height(16.dp),
            )
        }
        if (showThreadLineBottom) {
            ThreadLine(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 46.dp)
                    .fillMaxHeight(),
            )
        }
        AppAvatar(
            imageUrl = avatarUrl,
            name = displayName,
            size = 42.dp,
            modifier = Modifier.align(Alignment.TopCenter),
        )
        if (showThreadMarker) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 50.dp)
                    .size(8.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.72f),
                        shape = RoundedCornerShape(AppRadius.full),
                    ),
            )
        }
    }
}

@Composable
private fun ThreadLine(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .width(2.dp)
            .background(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.76f),
                shape = RoundedCornerShape(AppRadius.full),
            ),
    )
}

@Composable
fun AppCompactLinkPreview(
    linkPreview: AppLinkPreview,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.48f),
        shape = RoundedCornerShape(AppRadius.md),
    ) {
        Row(
            modifier = Modifier.height(88.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            LinkPreviewThumbnail(
                thumbnailUrl = linkPreview.thumbnailUrl,
                domain = linkPreview.domain,
                modifier = Modifier
                    .width(104.dp)
                    .height(88.dp),
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = AppSpacing.md),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = linkPreview.domain,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = linkPreview.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                if (linkPreview.description != null) {
                    Text(
                        text = linkPreview.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        }
    }
}

@Composable
private fun LinkPreviewThumbnail(
    thumbnailUrl: String?,
    domain: String,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = AppRadius.md, bottomStart = AppRadius.md))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        if (thumbnailUrl != null && !isPreview) {
            val request = remember(context, thumbnailUrl) {
                ImageRequest.Builder(context)
                    .data(thumbnailUrl)
                    .size(208, 176)
                    .precision(Precision.INEXACT)
                    .crossfade(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = domain,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = "URL",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppPostCardPreview() {
    FediverseHubTheme {
        AppPostCard(
            displayName = "Nora Dev",
            username = "@nora@hachyderm.io",
            timeAgo = "8m",
            avatarUrl = null,
            content = "Compose feed performance starts with stable UI models, explicit LazyColumn keys and a component layer that keeps the timeline boring in the best way.",
            mediaUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=720&h=420&fit=crop",
            hasAltText = true,
            boostedByDisplayName = "BrianKrebs",
            linkPreview = AppLinkPreview(
                domain = "developer.android.com",
                title = "Jetpack Compose performance",
                description = "Stability, lazy lists and image loading guidance.",
                thumbnailUrl = null,
            ),
            actions = listOf(
                AppPostAction(Icons.Outlined.ChatBubbleOutline, "12", "Replies"),
                AppPostAction(Icons.Outlined.Repeat, "44", "Boosts"),
                AppPostAction(Icons.Outlined.FavoriteBorder, "130", "Favourites"),
            ),
        )
    }
}
