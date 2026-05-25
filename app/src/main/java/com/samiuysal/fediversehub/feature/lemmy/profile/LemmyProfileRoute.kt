package com.samiuysal.fediversehub.feature.lemmy.profile

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.LemmyAuthRoute
import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.profile.ProfilePlatformTopBar

@Composable
fun LemmyProfileRoute(
    selectedPlatform: PlatformType,
    platformAccounts: List<Account>,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPostSelected: (String) -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onAccountSelected: (Account) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: LemmyProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ProfilePlatformTopBar(
            selectedPlatform = selectedPlatform,
            platformAccounts = platformAccounts,
            selectedAccount = selectedAccount,
            onPlatformSelected = onPlatformSelected,
            onAccountSelected = onAccountSelected,
            onSettingsClick = onSettingsClick,
        )
        when (val state = uiState) {
            LemmyProfileUiState.NoAccount -> LemmyAuthRoute(
                modifier = Modifier.weight(1f),
                showTopBar = false,
            )
            LemmyProfileUiState.Loading -> AppLoading(
                message = "Lemmy profili yükleniyor...",
                modifier = Modifier.fillMaxSize(),
            )
            is LemmyProfileUiState.Error -> AppErrorState(
                message = state.message,
                onRetry = viewModel::retry,
                modifier = Modifier.fillMaxSize(),
            )
            is LemmyProfileUiState.Success -> LemmyProfileContent(
                state = state,
                onTabSelected = viewModel::selectTab,
                onPostSelected = onPostSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
fun LemmyUserProfileRoute(
    username: String,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onPostSelected: (String) -> Unit,
    viewModel: LemmyProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedAccount?.id, username) {
        viewModel.selectUser(selectedAccount, username)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(end = AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
            }
            Text(
                text = "Lemmy profil",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        when (val state = uiState) {
            LemmyProfileUiState.NoAccount -> EmptyState(
                title = "Profil açılamadı",
                message = "Bu Lemmy profili şu an yüklenemedi.",
                modifier = Modifier.fillMaxSize(),
            )
            LemmyProfileUiState.Loading -> AppLoading(
                message = "Lemmy profili yükleniyor...",
                modifier = Modifier.fillMaxSize(),
            )
            is LemmyProfileUiState.Error -> AppErrorState(
                message = state.message,
                onRetry = viewModel::retry,
                modifier = Modifier.fillMaxSize(),
            )
            is LemmyProfileUiState.Success -> LemmyProfileContent(
                state = state.copy(selectedTab = state.selectedTab.takeUnless { it == LemmyProfileTab.SAVED } ?: LemmyProfileTab.POSTS),
                onTabSelected = viewModel::selectTab,
                onPostSelected = onPostSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LemmyProfileContent(
    state: LemmyProfileUiState.Success,
    onTabSelected: (LemmyProfileTab) -> Unit,
    onPostSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
    ) {
        item(contentType = "lemmy-profile-header") {
            LemmyProfileHeader(profile = state.profile)
        }
        item(contentType = "lemmy-profile-tabs") {
            LemmyProfileTabs(
                selectedTab = state.selectedTab,
                onTabSelected = onTabSelected,
            )
        }
        when (state.selectedTab) {
            LemmyProfileTab.POSTS -> {
                if (state.profile.posts.isEmpty()) {
                    item { EmptyState(title = "Post yok", message = "Bu profilde görünen post yok.") }
                }
                items(
                    items = state.profile.posts,
                    key = { it.id },
                    contentType = { "lemmy-profile-post" },
                ) { post ->
                    LemmyProfilePostRow(post = post, onClick = { onPostSelected(post.id) })
                }
            }
            LemmyProfileTab.COMMENTS -> {
                if (state.profile.comments.isEmpty()) {
                    item { EmptyState(title = "Yorum yok", message = "Bu profilde görünen yorum yok.") }
                }
                items(
                    items = state.profile.comments,
                    key = { it.id },
                    contentType = { "lemmy-profile-comment" },
                ) { comment ->
                    LemmyProfileCommentRow(comment = comment)
                }
            }
            LemmyProfileTab.SAVED -> {
                if (state.profile.savedPosts.isEmpty() && state.profile.savedComments.isEmpty()) {
                    item { EmptyState(title = "Saved boş", message = "Kaydettiğin post ve yorumlar burada görünecek.") }
                }
                items(
                    items = state.profile.savedPosts,
                    key = { "saved-post-${it.id}" },
                    contentType = { "lemmy-profile-saved-post" },
                ) { post ->
                    LemmyProfilePostRow(post = post, onClick = { onPostSelected(post.id) })
                }
                items(
                    items = state.profile.savedComments,
                    key = { "saved-comment-${it.id}" },
                    contentType = { "lemmy-profile-saved-comment" },
                ) { comment ->
                    LemmyProfileCommentRow(
                        comment = comment,
                        onClick = { comment.postId.takeIf(String::isNotBlank)?.let(onPostSelected) },
                    )
                }
            }
        }
    }
}

@Composable
private fun LemmyProfileHeader(profile: LemmyProfileUiModel) {
    Column {
        profile.bannerUrl?.let { bannerUrl ->
            val context = LocalContext.current
            val request = remember(context, bannerUrl) {
                ImageRequest.Builder(context)
                    .data(bannerUrl)
                    .size(720, 180)
                    .precision(Precision.INEXACT)
                    .crossfade(false)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(88.dp)
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(
                imageUrl = profile.avatarUrl,
                name = profile.displayName,
                size = 58.dp,
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "@${profile.name}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(AppSpacing.xs))
                Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                    ProfileCount(profile.postCount, "posts")
                    ProfileCount(profile.commentCount, "comments")
                }
            }
        }
        if (profile.bio.isNotBlank()) {
            Text(
                text = profile.bio,
                modifier = Modifier.padding(start = AppSpacing.lg, end = AppSpacing.lg, bottom = AppSpacing.sm),
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun LemmyProfileTabs(
    selectedTab: LemmyProfileTab,
    onTabSelected: (LemmyProfileTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        LemmyProfileTab.entries.forEach { tab ->
            FilterChip(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun LemmyProfilePostRow(
    post: LemmyPostUiModel,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text(
            text = "c/${post.community} · ${post.timeAgo}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
        if (post.previewText.isNotBlank()) {
            Text(
                text = post.previewText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = "${post.score} puan · ${post.comments} yorum",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
}

@Composable
private fun LemmyProfileCommentRow(
    comment: CommentUiModel,
    onClick: (() -> Unit)? = null,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        comment.postTitle?.takeIf(String::isNotBlank)?.let { title ->
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = comment.author,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = comment.text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 4,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = "${comment.score} puan",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
}

@Composable
private fun ProfileCount(value: Int, label: String) {
    Column {
        Text("$value", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private val LemmyProfileTab.label: String
    get() = when (this) {
        LemmyProfileTab.POSTS -> "Posts"
        LemmyProfileTab.COMMENTS -> "Comments"
        LemmyProfileTab.SAVED -> "Saved"
    }
