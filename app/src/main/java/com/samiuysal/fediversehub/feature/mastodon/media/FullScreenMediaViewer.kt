package com.samiuysal.fediversehub.feature.mastodon.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ZoomIn
import androidx.compose.material.icons.outlined.ZoomOut
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import coil.size.Size
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme

@Composable
fun FullScreenMediaViewer(
    urls: List<String>,
    altFlags: List<Boolean>,
    initialIndex: Int,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val safeUrls = remember(urls) { urls.filter { it.isNotBlank() } }
    val pagerState = rememberPagerState(
        initialPage = initialIndex.coerceIn(0, (safeUrls.size - 1).coerceAtLeast(0)),
        pageCount = { safeUrls.size },
    )
    val zoomedPages = remember { mutableStateMapOf<Int, Boolean>() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        if (safeUrls.isNotEmpty()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                val isZoomed = zoomedPages[page] == true
                MediaPage(
                    url = safeUrls[page],
                    isZoomed = isZoomed,
                    onToggleZoom = { zoomedPages[page] = !isZoomed },
                )
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            if (safeUrls.size > 1) {
                Surface(
                    color = Color.Black.copy(alpha = 0.48f),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(AppRadius.full),
                ) {
                    Text(
                        text = "${pagerState.currentPage + 1}/${safeUrls.size}",
                        modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }

        if (safeUrls.isNotEmpty()) {
            val page = pagerState.currentPage
            val isZoomed = zoomedPages[page] == true
            IconButton(
                onClick = { zoomedPages[page] = !isZoomed },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(AppSpacing.md),
            ) {
                Icon(
                    imageVector = if (isZoomed) Icons.Outlined.ZoomOut else Icons.Outlined.ZoomIn,
                    contentDescription = "Toggle zoom",
                    tint = Color.White,
                )
            }

            if (altFlags.getOrNull(page) == true) {
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(AppSpacing.lg),
                    color = Color.Black.copy(alpha = 0.68f),
                    contentColor = Color.White,
                    shape = RoundedCornerShape(AppRadius.sm),
                ) {
                    Text(
                        text = "ALT",
                        modifier = Modifier.padding(horizontal = AppSpacing.sm, vertical = AppSpacing.xs),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}

@Composable
private fun MediaPage(
    url: String,
    isZoomed: Boolean,
    onToggleZoom: () -> Unit,
) {
    val context = LocalContext.current
    val request = remember(context, url) {
        ImageRequest.Builder(context)
            .data(url)
            .size(Size.ORIGINAL)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable(onClick = onToggleZoom),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = request,
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = if (isZoomed) 1.65f else 1f
                    scaleY = if (isZoomed) 1.65f else 1f
                },
            contentScale = ContentScale.Fit,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun FullScreenMediaViewerPreview() {
    FediverseHubTheme {
        FullScreenMediaViewer(
            urls = listOf("https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=1400"),
            altFlags = listOf(true),
            initialIndex = 0,
            onBack = {},
        )
    }
}
