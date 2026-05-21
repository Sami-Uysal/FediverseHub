package com.samiuysal.fediversehub.feature.lemmy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.ModeComment
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.samiuysal.fediversehub.core.designsystem.component.AppCard
import com.samiuysal.fediversehub.core.designsystem.component.AppTopBar
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account

@Composable
fun LemmyHomeScreen(
    account: Account?,
    posts: List<LemmyPostUiModel>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        AppTopBar(
            title = "All communities",
            subtitle = "${account?.instanceUrl.orEmpty()} · Hot posts",
            actions = {
                AssistChip(
                    onClick = {},
                    label = { Text("Hot") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.PushPin,
                            contentDescription = null,
                        )
                    },
                )
            },
        )
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            items(
                items = posts,
                key = { it.id },
            ) { post ->
                LemmyPostCard(post = post)
            }
        }
    }
}

@Composable
private fun LemmyPostCard(post: LemmyPostUiModel) {
    AppCard(contentPadding = PaddingValues(AppSpacing.lg)) {
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
            VoteColumn(score = post.score)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(AppSpacing.xs))
                Text(
                    text = "c/${post.community} · ${post.domain ?: "self"} · ${post.author} · ${post.timeAgo}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(AppSpacing.md))
                Text(
                    text = post.previewText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(AppSpacing.md))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
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
                            text = "${post.comments} comments",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Text(
                        text = "Save",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                if (post.nestedComments.isNotEmpty()) {
                    Spacer(Modifier.height(AppSpacing.lg))
                    NestedCommentPreview(comments = post.nestedComments.take(3))
                }
            }
        }
    }
}

@Composable
private fun VoteColumn(score: Int) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowUp,
            contentDescription = "Upvote",
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = score.toString(),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
        )
        Icon(
            imageVector = Icons.Outlined.KeyboardArrowDown,
            contentDescription = "Downvote",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun NestedCommentPreview(comments: List<CommentUiModel>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.62f),
                shape = RoundedCornerShape(AppRadius.md),
            )
            .padding(AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        comments.forEach { comment ->
            Row {
                if (comment.depth > 0) {
                    Spacer(Modifier.width((comment.depth * 14).dp))
                    Box(
                        modifier = Modifier
                            .width(2.dp)
                            .height(42.dp)
                            .background(
                                color = MaterialTheme.colorScheme.outline,
                                shape = RoundedCornerShape(AppRadius.full),
                            ),
                    )
                    Spacer(Modifier.width(AppSpacing.sm))
                }
                Column {
                    Text(
                        text = comment.author,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = if (comment.isCollapsed) "Collapsed thread" else comment.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (comment.isCollapsed) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            Color.Unspecified
                        },
                    )
                }
            }
        }
    }
}
