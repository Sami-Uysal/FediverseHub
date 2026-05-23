package com.samiuysal.fediversehub.feature.mastodon.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
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
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.common.error.AppErrorException
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppMediaItem
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.data.mock.MockMastodonData
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.feature.profile.ProfilePlatformTopBar

@Composable
fun MastodonProfileScreen(
    uiState: MastodonProfileUiState,
    selectedPlatform: PlatformType,
    posts: LazyPagingItems<MastodonPostUiModel>,
    onFilterSelected: (MastodonProfileTimelineFilter) -> Unit,
    onPostSelected: (String) -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        MastodonProfileUiState.Loading -> AppLoading(
            message = "Loading profile...",
            modifier = modifier.fillMaxSize(),
        )
        MastodonProfileUiState.NoAccount -> EmptyState(
            title = "Log in required",
            message = "Connect Mastodon to view your profile.",
            modifier = modifier,
        )
        is MastodonProfileUiState.Error -> AppErrorState(
            message = uiState.message,
            onRetry = {},
            modifier = modifier,
        )
        is MastodonProfileUiState.Success -> MastodonProfileContent(
            profile = uiState.profile,
            selectedPlatform = selectedPlatform,
            selectedFilter = uiState.selectedFilter,
            posts = posts,
            onFilterSelected = onFilterSelected,
            onPostSelected = onPostSelected,
            onPlatformSelected = onPlatformSelected,
            onSettingsClick = onSettingsClick,
            modifier = modifier,
        )
    }
}

@Composable
private fun MastodonProfileContent(
    profile: MastodonProfileUiModel,
    selectedPlatform: PlatformType,
    selectedFilter: MastodonProfileTimelineFilter,
    posts: LazyPagingItems<MastodonPostUiModel>,
    onFilterSelected: (MastodonProfileTimelineFilter) -> Unit,
    onPostSelected: (String) -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val refreshError by remember(posts) {
        derivedStateOf { posts.loadState.refresh as? LoadState.Error }
    }
    val isInitialLoading by remember(posts) {
        derivedStateOf { posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0 }
    }
    val isEmpty by remember(posts) {
        derivedStateOf { posts.loadState.refresh is LoadState.NotLoading && posts.itemCount == 0 }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        item(key = "profile-platform-indicator", contentType = "profile-platform-indicator") {
            ProfilePlatformTopBar(
                selectedPlatform = selectedPlatform,
                onPlatformSelected = onPlatformSelected,
                onSettingsClick = onSettingsClick,
            )
        }
        item(key = "profile-header", contentType = "profile-header") {
            ProfileHeader(
                profile = profile,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
            )
        }
        when {
            isInitialLoading -> item(key = "profile-post-loading") {
                AppLoading(
                    message = "Loading posts...",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
            }
            refreshError != null -> item(key = "profile-post-error") {
                val error = refreshError
                AppErrorState(
                    message = error?.error.profileMessage(),
                    onRetry = posts::retry,
                    modifier = Modifier.height(240.dp),
                )
            }
            isEmpty -> item(key = "profile-post-empty") {
                EmptyState(
                    title = "No posts here",
                    message = "This profile tab has nothing visible yet.",
                    modifier = Modifier.height(260.dp),
                )
            }
            else -> {
                items(
                    count = posts.itemCount,
                    key = posts.itemKey { it.id },
                    contentType = posts.itemContentType { "profile-post" },
                ) { index ->
                    val post = posts[index]
                    if (post != null) {
                        ProfilePostRow(
                            post = post,
                            onPostSelected = onPostSelected,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MastodonProfileContentPreview(
    profile: MastodonProfileUiModel,
    posts: List<MastodonPostUiModel>,
    selectedPlatform: PlatformType,
    selectedFilter: MastodonProfileTimelineFilter,
    onFilterSelected: (MastodonProfileTimelineFilter) -> Unit,
    onPostSelected: (String) -> Unit,
    onPlatformSelected: (PlatformType) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        item {
            ProfilePlatformTopBar(
                selectedPlatform = selectedPlatform,
                onPlatformSelected = onPlatformSelected,
                onSettingsClick = onSettingsClick,
            )
        }
        item {
            ProfileHeader(
                profile = profile,
                selectedFilter = selectedFilter,
                onFilterSelected = onFilterSelected,
            )
        }
        items(posts, key = { it.id }) { post ->
            ProfilePostRow(post = post, onPostSelected = onPostSelected)
        }
    }
}

@Composable
private fun ProfileHeader(
    profile: MastodonProfileUiModel,
    selectedFilter: MastodonProfileTimelineFilter,
    onFilterSelected: (MastodonProfileTimelineFilter) -> Unit,
) {
    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            ProfileHeaderImage(
                imageUrl = profile.headerUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3.2f),
            )
            AppAvatar(
                imageUrl = profile.avatarUrl,
                name = profile.displayName,
                size = 82.dp,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .offset(x = AppSpacing.lg, y = 36.dp),
            )
        }
        Column(
            modifier = Modifier.padding(
                start = AppSpacing.lg,
                top = 44.dp,
                end = AppSpacing.lg,
                bottom = AppSpacing.md,
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = profile.displayName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = profile.username,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            if (profile.note.isNotBlank()) {
                Text(
                    text = profile.note,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
            ProfileCounts(profile = profile)
            if (profile.fields.isNotEmpty()) {
                ProfileFields(fields = profile.fields)
            }
        }
        ProfileTabs(
            selectedFilter = selectedFilter,
            onFilterSelected = onFilterSelected,
        )
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
    }
}

@Composable
private fun ProfileHeaderImage(
    imageUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (LocalInspectionMode.current || imageUrl == null) {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
        )
        return
    }
    val request = ImageRequest.Builder(LocalContext.current)
        .data(imageUrl)
        .crossfade(false)
        .memoryCachePolicy(CachePolicy.ENABLED)
        .diskCachePolicy(CachePolicy.ENABLED)
        .precision(Precision.INEXACT)
        .size(960, 300)
        .build()
    AsyncImage(
        model = request,
        contentDescription = "Profile header",
        modifier = modifier,
        contentScale = ContentScale.Crop,
    )
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
private fun ProfileTabs(
    selectedFilter: MastodonProfileTimelineFilter,
    onFilterSelected: (MastodonProfileTimelineFilter) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
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

@Composable
private fun ProfilePostRow(
    post: MastodonPostUiModel,
    onPostSelected: (String) -> Unit,
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

private fun Throwable?.profileMessage(): String =
    (this as? AppErrorException)?.message ?: "Profile posts could not be loaded. Try again."

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MastodonProfileScreenPreview() {
    FediverseHubTheme {
        MastodonProfileContentPreview(
            profile = MastodonProfileUiModel(
                id = "1",
                displayName = "Sami Uysal",
                username = "@sami@mastodon.social",
                avatarUrl = null,
                headerUrl = null,
                note = "Android-first Fediverse client work in progress.",
                followersCount = 1200,
                followingCount = 340,
                statusesCount = 280,
                fields = listOf(
                    MastodonProfileFieldUiModel("Website", "fediversehub.local"),
                    MastodonProfileFieldUiModel("Stack", "Kotlin + Compose"),
                ),
            ),
            posts = MockMastodonData.homeTimeline.map(MastodonTimelineMapper::domainToUi),
            selectedPlatform = PlatformType.MASTODON,
            selectedFilter = MastodonProfileTimelineFilter.POSTS,
            onFilterSelected = {},
            onPostSelected = {},
            onPlatformSelected = {},
            onSettingsClick = {},
        )
    }
}
