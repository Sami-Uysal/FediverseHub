package com.samiuysal.fediversehub.feature.mastodon.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppMediaItem
import com.samiuysal.fediversehub.core.designsystem.component.AppPostAction
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.feature.mastodon.MastodonLinkPreviewUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.data.mock.MockMastodonData
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper

@Composable
fun MastodonPostDetailScreen(
    uiState: MastodonPostDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        MastodonPostDetailTopBar(onBack = onBack)
        when (uiState) {
            MastodonPostDetailUiState.Loading -> AppLoading(
                message = "Loading thread...",
                modifier = Modifier.weight(1f),
            )
            MastodonPostDetailUiState.Empty -> EmptyState(
                title = "Thread unavailable",
                message = "This post detail has no visible context.",
                modifier = Modifier.weight(1f),
            )
            is MastodonPostDetailUiState.Error -> AppErrorState(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )
            is MastodonPostDetailUiState.Success -> MastodonPostDetailContent(
                uiState = uiState,
                onMediaSelected = onMediaSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun MastodonPostDetailTopBar(
    onBack: () -> Unit,
) {
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
                        text = "Thread",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                    )
                    Text(
                        text = "Mastodon conversation",
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
private fun MastodonPostDetailContent(
    uiState: MastodonPostDetailUiState.Success,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val threadItems = remember(uiState) {
        buildList {
            uiState.ancestors.forEach { add(ThreadItem(it, ThreadItemType.Ancestor)) }
            add(ThreadItem(uiState.post, ThreadItemType.Selected))
            uiState.descendants.forEach { add(ThreadItem(it, ThreadItemType.Descendant)) }
        }
    }

    if (threadItems.isEmpty()) {
        EmptyState(
            title = "Thread unavailable",
            message = "This post detail has no visible context.",
            modifier = modifier,
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        itemsIndexed(
            items = threadItems,
            key = { _, item -> "${item.type}-${item.post.id}" },
            contentType = { _, _ -> "mastodon-detail-post" },
        ) { index, item ->
            MastodonDetailPostCard(
                item = item,
                threadLineTop = index > 0,
                threadLineBottom = index < threadItems.lastIndex,
                replyTarget = item.replyTarget(threadItems, index),
                onMediaSelected = onMediaSelected,
            )
        }
    }
}

@Composable
private fun MastodonDetailPostCard(
    item: ThreadItem,
    threadLineTop: Boolean,
    threadLineBottom: Boolean,
    replyTarget: String?,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
) {
    val post = item.post
    val actions = remember(post.replies, post.boosts, post.favourites) {
        listOf(
            AppPostAction(Icons.Outlined.ChatBubbleOutline, post.replies.compactMetric(), "Replies"),
            AppPostAction(Icons.Outlined.Repeat, post.boosts.compactMetric(), "Boosts"),
            AppPostAction(Icons.Outlined.FavoriteBorder, post.favourites.compactMetric(), "Favourites"),
        )
    }
    val linkPreview = remember(post.linkPreview) { post.linkPreview?.toAppLinkPreview() }
    val mediaItems = remember(post.media) {
        post.media.map {
            AppMediaItem(
                previewUrl = it.previewUrl,
                fullUrl = it.fullUrl,
                altText = it.altText,
            )
        }
    }
    val mediaNavigationItems = remember(post.media) {
        post.media.mapNotNull { media ->
            val url = media.fullUrl ?: media.previewUrl
            url?.let { it to !media.altText.isNullOrBlank() }
        }
    }
    val mediaUrls = remember(mediaNavigationItems) { mediaNavigationItems.map { it.first } }
    val mediaHasAlt = remember(mediaNavigationItems) { mediaNavigationItems.map { it.second } }
    val context = remember(item.type, post.replyContext) {
        when (item.type) {
            ThreadItemType.Ancestor -> replyTarget?.let { "Replying to $it" } ?: "Earlier in thread"
            ThreadItemType.Selected -> replyTarget?.let { "Replying to $it" } ?: post.replyContext
            ThreadItemType.Descendant -> replyTarget?.let { "Replying to $it" } ?: "Reply"
        }
    }
    val indentLevel = remember(item.type) {
        when (item.type) {
            ThreadItemType.Ancestor -> 0
            ThreadItemType.Selected -> 0
            ThreadItemType.Descendant -> 1
        }
    }

    AppPostCard(
        displayName = post.displayName,
        username = post.username,
        timeAgo = post.timeAgo,
        avatarUrl = post.avatarUrl,
        content = post.content,
        mediaUrl = post.mediaUrl,
        mediaItems = mediaItems,
        hasAltText = post.hasAltText,
        boostedByDisplayName = post.boostedByDisplayName,
        boostedByAvatarUrl = post.boostedByAvatarUrl,
        replyContext = context,
        threadLineTop = threadLineTop,
        threadLineBottom = threadLineBottom,
        threadIndentLevel = indentLevel,
        isThreadFocused = item.type == ThreadItemType.Selected,
        linkPreview = linkPreview,
        actions = actions,
        onMediaClick = { index ->
            if (mediaUrls.isNotEmpty()) {
                onMediaSelected(mediaUrls, mediaHasAlt, index.coerceIn(mediaUrls.indices))
            }
        },
    )
}

private data class ThreadItem(
    val post: MastodonPostUiModel,
    val type: ThreadItemType,
)

private enum class ThreadItemType {
    Ancestor,
    Selected,
    Descendant,
}

private fun ThreadItem.replyTarget(
    threadItems: List<ThreadItem>,
    index: Int,
): String? = when (type) {
    ThreadItemType.Ancestor -> post.replyContext?.substringAfter("Replying to ", missingDelimiterValue = "")
        ?.takeIf(String::isNotBlank)
    ThreadItemType.Selected -> threadItems.getOrNull(index - 1)?.post?.username
    ThreadItemType.Descendant -> threadItems.getOrNull(index - 1)?.post?.username
}

private fun MastodonLinkPreviewUiModel.toAppLinkPreview(): AppLinkPreview =
    AppLinkPreview(
        domain = domain,
        title = title,
        description = description,
        thumbnailUrl = thumbnailUrl,
    )

private fun Int.compactMetric(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonPostDetailScreenPreview() {
    FediverseHubTheme {
        val posts = MockMastodonData.homeTimeline.map(MastodonTimelineMapper::domainToUi)
        MastodonPostDetailScreen(
            uiState = MastodonPostDetailUiState.Success(
                ancestors = posts.take(1),
                post = posts[1],
                descendants = posts.drop(2),
            ),
            onBack = {},
            onRetry = {},
            onMediaSelected = { _, _, _ -> },
        )
    }
}
