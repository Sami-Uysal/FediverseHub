package com.samiuysal.fediversehub.feature.lemmy.detail

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ExpandLess
import androidx.compose.material.icons.outlined.ExpandMore
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.feature.home.MockFediverseData
import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommentActionType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostActionType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel

@Composable
fun LemmyPostDetailScreen(
    uiState: LemmyPostDetailUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onRetryComments: () -> Unit,
    onToggleComment: (String) -> Unit,
    onPostAction: (LemmyPostActionType) -> Unit,
    onCommentAction: (CommentUiModel, LemmyCommentActionType) -> Unit,
    onCommentTextChanged: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onReplyComment: (CommentUiModel) -> Unit,
    onCancelReply: () -> Unit,
    onCommunityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        LemmyPostDetailTopBar(onBack = onBack)
        when (uiState) {
            LemmyPostDetailUiState.Loading -> AppLoading(
                message = "Lemmy gönderisi yükleniyor...",
                modifier = Modifier.weight(1f),
            )
            is LemmyPostDetailUiState.Error -> AppErrorState(
                message = uiState.message,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )
            is LemmyPostDetailUiState.Success -> LemmyPostDetailContent(
                state = uiState,
                onRetryComments = onRetryComments,
                onToggleComment = onToggleComment,
                onPostAction = onPostAction,
                onCommentAction = onCommentAction,
                onCommentTextChanged = onCommentTextChanged,
                onSubmitComment = onSubmitComment,
                onReplyComment = onReplyComment,
                onCancelReply = onCancelReply,
                onCommunityClick = onCommunityClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LemmyPostDetailTopBar(onBack: () -> Unit) {
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
                        text = "Lemmy",
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
private fun LemmyPostDetailContent(
    state: LemmyPostDetailUiState.Success,
    onRetryComments: () -> Unit,
    onToggleComment: (String) -> Unit,
    onPostAction: (LemmyPostActionType) -> Unit,
    onCommentAction: (CommentUiModel, LemmyCommentActionType) -> Unit,
    onCommentTextChanged: (String) -> Unit,
    onSubmitComment: () -> Unit,
    onReplyComment: (CommentUiModel) -> Unit,
    onCancelReply: () -> Unit,
    onCommunityClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val visibleComments = remember(state.comments, state.collapsedCommentIds) {
        state.comments.visibleWithCollapse(state.collapsedCommentIds)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        item(key = "lemmy-detail-post", contentType = "lemmy-detail-post") {
            LemmyDetailPost(
                post = state.post,
                onPostAction = onPostAction,
                onCommunityClick = onCommunityClick,
            )
        }

        item(key = "lemmy-comment-composer", contentType = "lemmy-comment-composer") {
            LemmyCommentComposer(
                state = state.composer,
                onTextChanged = onCommentTextChanged,
                onSubmit = onSubmitComment,
                onCancelReply = onCancelReply,
            )
        }

        item(key = "lemmy-comments-header", contentType = "lemmy-comments-header") {
            CommentsHeader(count = state.post.comments)
        }

        if (state.isCommentsLoading) {
            item(key = "lemmy-comments-loading", contentType = "lemmy-comments-loading") {
                AppLoading(
                    message = "Yorumlar yükleniyor...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            }
        } else if (state.commentsErrorMessage != null) {
            item(key = "lemmy-comments-error", contentType = "lemmy-comments-error") {
                AppErrorState(
                    message = state.commentsErrorMessage,
                    onRetry = onRetryComments,
                    modifier = Modifier.height(220.dp),
                )
            }
        } else if (state.comments.isEmpty()) {
            item(key = "lemmy-comments-empty", contentType = "lemmy-comments-empty") {
                EmptyState(
                    title = "Yorum yok",
                    message = "Bu gönderide henüz yorum görünmüyor.",
                    modifier = Modifier.height(220.dp),
                )
            }
        } else {
            items(
                items = visibleComments,
                key = { it.comment.id },
                contentType = { "lemmy-comment" },
            ) { item ->
                LemmyCommentRow(
                    item = item,
                    onToggleComment = onToggleComment,
                    onCommentAction = onCommentAction,
                    onReplyComment = onReplyComment,
                )
            }
        }
    }
}

@Composable
private fun LemmyCommentComposer(
    state: LemmyCommentComposerUiState,
    onTextChanged: (String) -> Unit,
    onSubmit: () -> Unit,
    onCancelReply: () -> Unit,
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
                text = state.parentAuthor?.let { "$it yanıtlanıyor" } ?: "Yorum yaz",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (state.parentId != null) {
                TextButton(onClick = onCancelReply, enabled = !state.isSubmitting) {
                    Text("Vazgeç")
                }
            }
        }
        OutlinedTextField(
            value = state.text,
            onValueChange = onTextChanged,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 96.dp),
            enabled = !state.isSubmitting,
            minLines = 3,
            placeholder = { Text("Düşünceni yaz...") },
            supportingText = {
                state.errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error)
                }
            },
        )
        Button(
            onClick = onSubmit,
            enabled = !state.isSubmitting && state.text.isNotBlank(),
            modifier = Modifier.align(Alignment.End),
        ) {
            if (state.isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary,
                )
                Spacer(modifier = Modifier.width(AppSpacing.xs))
            }
            Text(if (state.parentId == null) "Yorum gönder" else "Yanıt gönder")
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
}

@Composable
private fun LemmyDetailPost(
    post: LemmyPostUiModel,
    onPostAction: (LemmyPostActionType) -> Unit,
    onCommunityClick: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Surface(
                modifier = Modifier.clickable { onCommunityClick(post.community) },
                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f),
                contentColor = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(AppRadius.full),
            ) {
                Text(
                    text = "c/${post.community}",
                    modifier = Modifier.padding(horizontal = AppSpacing.md, vertical = AppSpacing.xs),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Text(
                text = post.domain ?: "self",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Text(
            text = post.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = "${post.author} · ${post.timeAgo}",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        LemmyDetailImage(post = post)

        if (post.previewText.isNotBlank()) {
            Text(
                text = post.previewText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        LemmyDetailLinkPreview(post = post)

        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            VoteButton(
                isLoading = post.loadingAction == LemmyPostActionType.UPVOTE,
                icon = Icons.Outlined.KeyboardArrowUp,
                isHighlighted = post.isUpvoted,
                onClick = { onPostAction(LemmyPostActionType.UPVOTE) },
            )
            Text(
                text = "${post.score.compactMetric()} puan",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
            )
            VoteButton(
                isLoading = post.loadingAction == LemmyPostActionType.DOWNVOTE,
                icon = Icons.Outlined.KeyboardArrowDown,
                isHighlighted = post.isDownvoted,
                onClick = { onPostAction(LemmyPostActionType.DOWNVOTE) },
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
            ) {
                Icon(
                    imageVector = Icons.Outlined.ModeComment,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = "${post.comments.compactMetric()} yorum",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Row(
                modifier = Modifier.clickable(
                    enabled = post.loadingAction != LemmyPostActionType.SAVE,
                    onClick = { onPostAction(LemmyPostActionType.SAVE) },
                ),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (post.loadingAction == LemmyPostActionType.SAVE) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Icon(
                        imageVector = if (post.isSaved) Icons.Outlined.Bookmark else Icons.Outlined.BookmarkBorder,
                        contentDescription = null,
                        tint = if (post.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    text = if (post.isSaved) "Saved" else "Save",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (post.isSaved) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
}

@Composable
private fun LemmyDetailImage(post: LemmyPostUiModel) {
    val thumbnailUrl = post.thumbnailUrl?.takeIf(String::isNotBlank) ?: return
    val context = LocalContext.current
    val request = remember(thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(thumbnailUrl)
            .size(960, 540)
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
            .height(220.dp)
            .clip(RoundedCornerShape(AppRadius.md)),
    )
}

@Composable
private fun LemmyDetailLinkPreview(post: LemmyPostUiModel) {
    val url = post.url?.takeIf(String::isNotBlank) ?: return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f),
        shape = RoundedCornerShape(AppRadius.md),
    ) {
        Column(
            modifier = Modifier.padding(AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = post.domain ?: "link",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = url,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun CommentsHeader(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Yorumlar",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
        AssistChip(
            onClick = {},
            label = { Text(count.compactMetric()) },
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
}

@Composable
private fun LemmyCommentRow(
    item: LemmyVisibleComment,
    onToggleComment: (String) -> Unit,
    onCommentAction: (CommentUiModel, LemmyCommentActionType) -> Unit,
    onReplyComment: (CommentUiModel) -> Unit,
) {
    val comment = item.comment
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                verticalAlignment = Alignment.Top,
            ) {
                if (comment.depth > 0) {
                    Spacer(modifier = Modifier.width((comment.depth * 14).coerceAtMost(70).dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(52.dp)
                            .background(
                                color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.44f),
                                shape = RoundedCornerShape(AppRadius.full),
                            ),
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = comment.author,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                        )
                        Text(
                            text = "${comment.score.compactMetric()} puan",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        VoteButton(
                            isLoading = comment.loadingAction == LemmyCommentActionType.UPVOTE,
                            icon = Icons.Outlined.KeyboardArrowUp,
                            isHighlighted = comment.isUpvoted,
                            onClick = { onCommentAction(comment, LemmyCommentActionType.UPVOTE) },
                        )
                        VoteButton(
                            isLoading = comment.loadingAction == LemmyCommentActionType.DOWNVOTE,
                            icon = Icons.Outlined.KeyboardArrowDown,
                            isHighlighted = comment.isDownvoted,
                            onClick = { onCommentAction(comment, LemmyCommentActionType.DOWNVOTE) },
                        )
                        if (item.hasChildren) {
                            Icon(
                                imageVector = if (item.isCollapsed) {
                                    Icons.Outlined.ExpandMore
                                } else {
                                    Icons.Outlined.ExpandLess
                                },
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .size(24.dp)
                                    .clickable { onToggleComment(comment.id) },
                            )
                        }
                    }
                    Text(
                        text = if (comment.isCollapsed) "Silinmiş veya gizlenmiş yorum" else comment.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (comment.isCollapsed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color.Unspecified
                        },
                    )
                    TextButton(
                        onClick = { onReplyComment(comment) },
                        enabled = !comment.isCollapsed,
                    ) {
                        Text("Yanıtla")
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.42f))
        }
    }
}

@Composable
private fun VoteButton(
    isLoading: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isHighlighted: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(26.dp)
            .clip(RoundedCornerShape(AppRadius.full))
            .clickable(enabled = !isLoading, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(15.dp), strokeWidth = 2.dp)
        } else {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isHighlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun List<CommentUiModel>.visibleWithCollapse(
    collapsedIds: Set<String>,
): List<LemmyVisibleComment> {
    val parentIds = mapNotNull { it.parentId }.toSet()
    val result = ArrayList<LemmyVisibleComment>(size)
    var hiddenDepth: Int? = null

    forEachIndexed { index, comment ->
        val hidden = hiddenDepth
        if (hidden != null && comment.depth > hidden) {
            return@forEachIndexed
        }
        if (hidden != null && comment.depth <= hidden) {
            hiddenDepth = null
        }

        val hasChildren = comment.id in parentIds ||
            (getOrNull(index + 1)?.depth ?: -1) > comment.depth
        val isCollapsed = comment.id in collapsedIds
        result += LemmyVisibleComment(
            comment = comment,
            hasChildren = hasChildren,
            isCollapsed = isCollapsed,
        )
        if (hasChildren && isCollapsed) {
            hiddenDepth = comment.depth
        }
    }

    return result
}

private fun Int.compactMetric(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun LemmyPostDetailScreenPreview() {
    FediverseHubTheme {
        val post = MockFediverseData.homeState.lemmyPosts.first()
        LemmyPostDetailScreen(
            uiState = LemmyPostDetailUiState.Success(
                post = post,
                comments = post.nestedComments,
            ),
            onBack = {},
            onRetry = {},
            onRetryComments = {},
            onToggleComment = {},
            onPostAction = {},
            onCommentAction = { _, _ -> },
            onCommentTextChanged = {},
            onSubmitComment = {},
            onReplyComment = {},
            onCancelReply = {},
            onCommunityClick = {},
        )
    }
}
