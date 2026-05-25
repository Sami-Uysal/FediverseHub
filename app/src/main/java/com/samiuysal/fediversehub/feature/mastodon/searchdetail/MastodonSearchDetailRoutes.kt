package com.samiuysal.fediversehub.feature.mastodon.searchdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppMediaItem
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter
import com.samiuysal.fediversehub.feature.mastodon.profile.MastodonProfileFieldUiModel
import com.samiuysal.fediversehub.feature.mastodon.profile.MastodonProfileUiModel

@Composable
fun MastodonAccountDetailRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    viewModel: MastodonAccountDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedFilter by viewModel.selectedTimelineFilter.collectAsStateWithLifecycle()
    val followState by viewModel.followState.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()

    MastodonAccountDetailScreen(
        uiState = uiState,
        followState = followState,
        selectedFilter = selectedFilter,
        posts = posts,
        contentPadding = contentPadding,
        onBack = onBack,
        onRetryProfile = viewModel::retry,
        onFollowClick = viewModel::toggleFollow,
        onFilterSelected = viewModel::selectFilter,
        onPostSelected = onPostSelected,
        onAccountSelected = onAccountSelected,
    )
}

@Composable
fun MastodonHashtagTimelineRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onPostSelected: (String) -> Unit,
    viewModel: MastodonHashtagTimelineViewModel = hiltViewModel(),
) {
    val hasAccount by viewModel.hasAccount.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()

    MastodonHashtagTimelineScreen(
        hashtag = viewModel.hashtag,
        hasAccount = hasAccount,
        posts = posts,
        contentPadding = contentPadding,
        onBack = onBack,
        onPostSelected = onPostSelected,
    )
}

@Composable
private fun MastodonAccountDetailScreen(
    uiState: MastodonAccountDetailUiState,
    followState: MastodonFollowUiState,
    selectedFilter: MastodonProfileTimelineFilter,
    posts: LazyPagingItems<MastodonPostUiModel>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onRetryProfile: () -> Unit,
    onFollowClick: () -> Unit,
    onFilterSelected: (MastodonProfileTimelineFilter) -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
) {
    when (uiState) {
        MastodonAccountDetailUiState.Loading -> AppLoading(
            message = "Profil yükleniyor...",
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
        MastodonAccountDetailUiState.NoAccount -> EmptyState(
            title = "Mastodon hesabı gerekli",
            message = "Bu profili görmek için Mastodon hesabı bağla.",
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
        is MastodonAccountDetailUiState.Error -> AppErrorState(
            message = uiState.message,
            onRetry = onRetryProfile,
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
        is MastodonAccountDetailUiState.Success -> LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
            contentPadding = PaddingValues(bottom = AppSpacing.xl),
        ) {
            item(key = "account-top-bar", contentType = "top-bar") {
                DetailTopBar(title = uiState.profile.displayName, onBack = onBack)
            }
            item(key = "account-header", contentType = "account-header") {
                AccountHeader(
                    profile = uiState.profile,
                    followState = followState,
                    onFollowClick = onFollowClick,
                    selectedFilter = selectedFilter,
                    onFilterSelected = onFilterSelected,
                )
            }
            timelineItems(
                posts = posts,
                emptyTitle = "Post yok",
                emptyMessage = "Bu sekmede görünen içerik yok.",
                onPostSelected = onPostSelected,
                onAccountSelected = onAccountSelected,
            )
        }
    }
}

@Composable
private fun MastodonHashtagTimelineScreen(
    hashtag: String,
    hasAccount: Boolean,
    posts: LazyPagingItems<MastodonPostUiModel>,
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit = {},
) {
    if (!hasAccount) {
        EmptyState(
            title = "Mastodon hesabı gerekli",
            message = "Hashtag timeline için Mastodon hesabı bağla.",
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding),
        )
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        item(key = "hashtag-top-bar", contentType = "top-bar") {
            DetailTopBar(title = "#$hashtag", onBack = onBack)
        }
        timelineItems(
            posts = posts,
            emptyTitle = "Post yok",
            emptyMessage = "Bu hashtag için içerik bulunamadı.",
            onPostSelected = onPostSelected,
            onAccountSelected = onAccountSelected,
        )
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.timelineItems(
    posts: LazyPagingItems<MastodonPostUiModel>,
    emptyTitle: String,
    emptyMessage: String,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit = {},
) {
    val refreshState = posts.loadState.refresh
    when {
        refreshState is LoadState.Loading && posts.itemCount == 0 -> item(
            key = "timeline-loading",
            contentType = "loading",
        ) {
            AppLoading(
                message = "Yükleniyor...",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
            )
        }
        refreshState is LoadState.Error && posts.itemCount == 0 -> item(
            key = "timeline-error",
            contentType = "error",
        ) {
            AppErrorState(
                message = refreshState.error.userMessage(),
                onRetry = posts::retry,
                modifier = Modifier.height(220.dp),
            )
        }
        refreshState is LoadState.NotLoading && posts.itemCount == 0 -> item(
            key = "timeline-empty",
            contentType = "empty",
        ) {
            EmptyState(
                title = emptyTitle,
                message = emptyMessage,
                modifier = Modifier.height(220.dp),
            )
        }
        else -> {
            items(
                count = posts.itemCount,
                key = posts.itemKey { it.id },
                contentType = posts.itemContentType { "mastodon-search-detail-post" },
            ) { index ->
                val post = posts[index]
                if (post != null) {
                    MastodonPostRow(
                        post = post,
                        onPostSelected = onPostSelected,
                        onAccountSelected = onAccountSelected,
                    )
                }
            }
            if (posts.loadState.append is LoadState.Loading) {
                item(key = "timeline-append-loading", contentType = "append-loading") {
                    AppLoading(
                        message = "Devamı yükleniyor...",
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
private fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Geri")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
}

@Composable
private fun AccountHeader(
    profile: MastodonProfileUiModel,
    followState: MastodonFollowUiState,
    onFollowClick: () -> Unit,
    selectedFilter: MastodonProfileTimelineFilter,
    onFilterSelected: (MastodonProfileTimelineFilter) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppAvatar(
                imageUrl = profile.avatarUrl,
                name = profile.displayName,
                size = 72.dp,
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
                    text = profile.username,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            FollowButton(
                state = followState,
                onClick = onFollowClick,
            )
        }
        followState.errorMessage?.let { message ->
            Text(
                text = message,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }
        if (profile.note.isNotBlank()) {
            Text(
                text = profile.note,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        ProfileCounts(profile = profile)
        if (profile.fields.isNotEmpty()) {
            ProfileFields(fields = profile.fields)
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            MastodonProfileTimelineFilter.entries.forEach { filter ->
                FilterChip(
                    selected = selectedFilter == filter,
                    onClick = { onFilterSelected(filter) },
                    label = { Text(filter.label) },
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
}

@Composable
private fun FollowButton(
    state: MastodonFollowUiState,
    onClick: () -> Unit,
) {
    if (state.isOwnProfile) return
    val content: @Composable () -> Unit = {
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Text(if (state.isFollowing) "Unfollow" else "Follow")
        }
    }
    if (state.isFollowing) {
        OutlinedButton(onClick = onClick, enabled = !state.isLoading) {
            content()
        }
    } else {
        Button(onClick = onClick, enabled = !state.isLoading) {
            content()
        }
    }
}

@Composable
private fun ProfileCounts(profile: MastodonProfileUiModel) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.lg)) {
        ProfileCount(profile.statusesCount, "posts")
        ProfileCount(profile.followersCount, "followers")
        ProfileCount(profile.followingCount, "following")
    }
}

@Composable
private fun ProfileCount(value: Int, label: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        Text(
            text = value.compactMetric(),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ProfileFields(fields: List<MastodonProfileFieldUiModel>) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
        fields.forEach { field ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                Text(
                    text = field.name,
                    modifier = Modifier.weight(0.36f),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = field.value,
                    modifier = Modifier.weight(0.64f),
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun MastodonPostRow(
    post: MastodonPostUiModel,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
) {
    val linkPreview = remember(post.linkPreview) {
        post.linkPreview?.let {
            AppLinkPreview(
                domain = it.domain,
                title = it.title,
                description = it.description,
                thumbnailUrl = it.thumbnailUrl,
            )
        }
    }
    val mediaItems = remember(post.media) {
        post.media.map {
            AppMediaItem(
                previewUrl = it.previewUrl,
                fullUrl = it.fullUrl,
                altText = it.altText,
            )
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
        linkPreview = linkPreview,
        actions = emptyList(),
        onClick = { onPostSelected(post.detailId) },
        onAuthorClick = { onAccountSelected(post.authorAccountId) },
    )
}

private val MastodonProfileTimelineFilter.label: String
    get() = when (this) {
        MastodonProfileTimelineFilter.POSTS -> "Posts"
        MastodonProfileTimelineFilter.REPLIES -> "Replies"
        MastodonProfileTimelineFilter.MEDIA -> "Media"
    }

private fun Int.compactMetric(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}

private fun Throwable.userMessage(): String =
    (this as? AppErrorException)?.message ?: "İçerik yüklenemedi. Tekrar dene."
