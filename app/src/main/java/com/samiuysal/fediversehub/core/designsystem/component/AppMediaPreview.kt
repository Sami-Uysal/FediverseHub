package com.samiuysal.fediversehub.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.tooling.preview.Preview
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing

@Composable
fun AppMediaPreview(
    mediaUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    hasAltText: Boolean = false,
) {
    if (mediaUrl == null) return
    val context = LocalContext.current
    val isPreview = LocalInspectionMode.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.72f)
            .clip(RoundedCornerShape(AppRadius.md))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        if (!isPreview) {
            val request = remember(context, mediaUrl) {
                ImageRequest.Builder(context)
                    .data(mediaUrl)
                    .size(720, 420)
                    .crossfade(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
            AsyncImage(
                model = request,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
        if (hasAltText) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(AppSpacing.sm),
                color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.76f),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(AppRadius.sm),
            ) {
                Text(
                    text = "ALT",
                    modifier = Modifier.padding(horizontal = AppSpacing.xs, vertical = AppSpacing.xxs),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun AppMediaPreviewPreview() {
    FediverseHubTheme {
        AppMediaPreview(
            mediaUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=720&h=420&fit=crop",
            contentDescription = "Preview image",
        )
    }
}
