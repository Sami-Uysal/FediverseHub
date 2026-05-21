package com.samiuysal.fediversehub.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius

@Composable
fun AppMediaPreview(
    mediaUrl: String?,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
) {
    if (mediaUrl == null) return

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1.72f)
            .clip(RoundedCornerShape(AppRadius.lg))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        AsyncImage(
            model = mediaUrl,
            contentDescription = contentDescription,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
        )
    }
}
