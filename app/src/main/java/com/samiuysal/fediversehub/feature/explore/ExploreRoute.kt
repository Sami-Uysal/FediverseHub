package com.samiuysal.fediversehub.feature.explore

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemContentType
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.size.Precision
import com.samiuysal.fediversehub.core.common.error.userFacingMessage
import com.samiuysal.fediversehub.core.designsystem.component.AppCompactLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppMediaItem
import com.samiuysal.fediversehub.core.designsystem.component.AppPostAction
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppRadius
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTrendLink
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel

@Composable
fun ExploreRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPostSelected: (String) -> Unit,
    onPixelfedPostSelected: (String) -> Unit,
    onLemmyPostSelected: (String) -> Unit,
    onLemmyCommunitySelected: (String) -> Unit,
    onMastodonAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val mastodonState by viewModel.mastodonState.collectAsStateWithLifecycle()
    val lemmyTab by viewModel.lemmyTab.collectAsStateWithLifecycle()
    val lemmySort by viewModel.lemmySort.collectAsStateWithLifecycle()
    val lemmyCommunitiesState by viewModel.lemmyCommunitiesState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedPlatform) {
        viewModel.selectPlatform(selectedPlatform)
    }
    LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }

    when (selectedPlatform) {
        PlatformType.MASTODON -> MastodonExploreContent(
            account = selectedAccount,
            state = mastodonState,
            contentPadding = contentPadding,
            onTabSelected = viewModel::selectMastodonTab,
            onRetry = { viewModel.refreshMastodon(selectedAccount) },
            onPostSelected = onPostSelected,
            onAccountSelected = onMastodonAccountSelected,
            onHashtagSelected = onHashtagSelected,
        )
        PlatformType.PIXELFED -> {
            val posts = viewModel.pixelfedExploreFeed.collectAsLazyPagingItems()
            PixelfedExploreContent(
                account = selectedAccount,
                posts = posts,
                contentPadding = contentPadding,
                onPostSelected = onPixelfedPostSelected,
                onMediaSelected = onMediaSelected,
            )
        }
        PlatformType.LEMMY -> {
            val posts = viewModel.lemmyExploreFeed.collectAsLazyPagingItems()
            LemmyExploreContent(
                account = selectedAccount,
                posts = posts,
                selectedTab = lemmyTab,
                selectedSort = lemmySort,
                communitiesState = lemmyCommunitiesState,
                contentPadding = contentPadding,
                onTabSelected = viewModel::selectLemmyTab,
                onSortSelected = viewModel::selectLemmySort,
                onRetryCommunities = viewModel::refreshLemmyCommunities,
                onPostSelected = onLemmyPostSelected,
                onCommunitySelected = onLemmyCommunitySelected,
            )
        }
    }
}

@Composable
private fun MastodonExploreContent(
    account: Account?,
    state: MastodonExploreUiState,
    contentPadding: PaddingValues,
    onTabSelected: (MastodonExploreTab) -> Unit,
    onRetry: () -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ExploreHeader(
            title = "Keşfet",
            subtitle = account?.let { "@${it.username} · ${it.instanceUrl}" } ?: "Public Mastodon trendleri",
        )
        MastodonExploreTabs(
            selectedTab = state.selectedTab,
            onTabSelected = onTabSelected,
        )
        when {
            state.isSelectedTabLoading -> AppLoading(
                message = "Keşfet yükleniyor...",
                modifier = Modifier.weight(1f),
            )
            state.errorMessage != null -> AppErrorState(
                message = state.errorMessage,
                onRetry = onRetry,
                modifier = Modifier.weight(1f),
            )
            state.selectedTab == MastodonExploreTab.POSTS -> MastodonExplorePosts(
                posts = state.posts,
                onPostSelected = onPostSelected,
                onAccountSelected = onAccountSelected,
                modifier = Modifier.weight(1f),
            )
            state.selectedTab == MastodonExploreTab.TAGS -> MastodonExploreTags(
                tags = state.tags,
                onHashtagSelected = onHashtagSelected,
                modifier = Modifier.weight(1f),
            )
            state.selectedTab == MastodonExploreTab.LINKS -> MastodonExploreLinks(
                links = state.links,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ExploreHeader(
    title: String,
    subtitle: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
}

@Composable
private fun MastodonExploreTabs(
    selectedTab: MastodonExploreTab,
    onTabSelected: (MastodonExploreTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MastodonExploreTab.entries.forEach { tab ->
            FilterChip(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun MastodonExplorePosts(
    posts: List<MastodonPostUiModel>,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (posts.isEmpty()) {
        EmptyState(
            title = "Trending post yok",
            message = "Sunucu şu an post trendi döndürmedi.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(modifier = modifier.fillMaxWidth()) {
        items(
            items = posts,
            key = { it.id },
            contentType = { "mastodon-explore-post" },
        ) { post ->
            ExplorePostCard(
                post = post,
                onClick = { onPostSelected(post.detailId) },
                onAuthorClick = { onAccountSelected(post.authorAccountId) },
            )
        }
    }
}

@Composable
private fun ExplorePostCard(
    post: MastodonPostUiModel,
    onClick: () -> Unit,
    onAuthorClick: () -> Unit,
) {
    val mediaItems = remember(post.media) {
        post.media.map { AppMediaItem(it.previewUrl, it.fullUrl, it.altText) }
    }
    val linkPreview = remember(post.linkPreview) {
        post.linkPreview?.let {
            AppLinkPreview(it.domain, it.title, it.description, it.thumbnailUrl)
        }
    }
    val actions = remember(post.replies, post.boosts, post.favourites) {
        listOf(
            AppPostAction(Icons.Outlined.ChatBubbleOutline, post.replies.compactMetric(), "Replies"),
            AppPostAction(Icons.Outlined.Repeat, post.boosts.compactMetric(), "Boosts"),
            AppPostAction(Icons.Outlined.FavoriteBorder, post.favourites.compactMetric(), "Favourites"),
        )
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
        linkPreview = linkPreview,
        actions = actions,
        onClick = onClick,
        onAuthorClick = onAuthorClick,
    )
}

@Composable
private fun MastodonExploreTags(
    tags: List<com.samiuysal.fediversehub.feature.mastodon.domain.MastodonHashtag>,
    onHashtagSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) {
        EmptyState(
            title = "Trending hashtag yok",
            message = "Sunucu şu an tag trendi döndürmedi.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        items(
            items = tags,
            key = { it.name },
            contentType = { "mastodon-explore-tag" },
        ) { tag ->
            AssistChip(
                onClick = { onHashtagSelected(tag.name.removePrefix("#")) },
                label = { Text("#${tag.name.removePrefix("#")}") },
            )
        }
    }
}

@Composable
private fun MastodonExploreLinks(
    links: List<MastodonTrendLink>,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    if (links.isEmpty()) {
        EmptyState(
            title = "Trending link yok",
            message = "Sunucu şu an link trendi döndürmedi.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        items(
            items = links,
            key = { it.url },
            contentType = { "mastodon-explore-link" },
        ) { link ->
            val domain = remember(link.url, link.providerName) {
                link.providerName ?: Uri.parse(link.url).host.orEmpty()
            }
            Box(
                modifier = Modifier.clickable {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link.url)))
                    }
                },
            ) {
                AppCompactLinkPreview(
                    linkPreview = AppLinkPreview(
                        domain = domain,
                        title = link.title,
                        description = link.description,
                        thumbnailUrl = link.imageUrl,
                    ),
                )
            }
        }
    }
}

@Composable
private fun LemmyExploreContent(
    account: Account?,
    posts: androidx.paging.compose.LazyPagingItems<LemmyPostUiModel>,
    selectedTab: LemmyExploreTab,
    selectedSort: LemmySortType,
    communitiesState: LemmyCommunitiesUiState,
    contentPadding: PaddingValues,
    onTabSelected: (LemmyExploreTab) -> Unit,
    onSortSelected: (LemmySortType) -> Unit,
    onRetryCommunities: () -> Unit,
    onPostSelected: (String) -> Unit,
    onCommunitySelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ExploreHeader(
            title = "Lemmy Keşfet",
            subtitle = account?.let { "@${it.username} · ${it.instanceUrl}" } ?: "Public Lemmy keşfi",
        )
        LemmyExploreTabs(
            selectedTab = selectedTab,
            onTabSelected = onTabSelected,
        )
        LemmyExploreSort(
            selectedSort = selectedSort,
            onSortSelected = onSortSelected,
        )
        when {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0 -> AppLoading(
                message = "Lemmy keşfet yükleniyor...",
                modifier = Modifier.weight(1f),
            )
            posts.loadState.refresh is LoadState.Error && posts.itemCount == 0 -> {
                val error = posts.loadState.refresh as LoadState.Error
                AppErrorState(
                    message = error.error.userFacingMessage("Lemmy keşfet yüklenemedi."),
                    onRetry = posts::retry,
                    modifier = Modifier.weight(1f),
                )
            }
            selectedTab == LemmyExploreTab.COMMUNITIES -> LemmyCommunitiesDiscovery(
                state = communitiesState,
                onRetry = onRetryCommunities,
                onCommunitySelected = onCommunitySelected,
                modifier = Modifier.weight(1f),
            )
            posts.itemCount == 0 -> EmptyState(
                title = "Gönderi yok",
                message = "Bu keşif akışı şu an boş.",
                modifier = Modifier.weight(1f),
            )
            else -> LemmyExplorePostList(
                posts = posts,
                onPostSelected = onPostSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun LemmyExploreTabs(
    selectedTab: LemmyExploreTab,
    onTabSelected: (LemmyExploreTab) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        LemmyExploreTab.entries.forEach { tab ->
            FilterChip(
                selected = tab == selectedTab,
                onClick = { onTabSelected(tab) },
                label = { Text(tab.label) },
            )
        }
    }
}

@Composable
private fun LemmyExploreSort(
    selectedSort: LemmySortType,
    onSortSelected: (LemmySortType) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        lemmySortTypes.forEach { sort ->
            AssistChip(
                onClick = { onSortSelected(sort) },
                label = {
                    Text(
                        text = sort.label,
                        fontWeight = if (selectedSort == sort) FontWeight.Bold else FontWeight.Normal,
                    )
                },
            )
        }
    }
}

@Composable
private fun LemmyExplorePostList(
    posts: androidx.paging.compose.LazyPagingItems<LemmyPostUiModel>,
    onPostSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(vertical = AppSpacing.sm),
    ) {
        items(
            count = posts.itemCount,
            key = posts.itemKey { it.id },
            contentType = posts.itemContentType { "lemmy-explore-post" },
        ) { index ->
            posts[index]?.let { post ->
                LemmyExplorePostCard(
                    post = post,
                    onClick = { onPostSelected(post.id) },
                )
            }
        }
        if (posts.loadState.append is LoadState.Loading) {
            item(key = "lemmy-explore-append-loading", contentType = "lemmy-explore-loading") {
                AppLoading(message = "Daha fazla yükleniyor...", modifier = Modifier.height(96.dp))
            }
        }
    }
}

@Composable
private fun LemmyExplorePostCard(
    post: LemmyPostUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        LemmyExploreThumbnail(post = post)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            Text(
                text = "c/${post.community} · ${post.domain ?: "self"}",
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
            Text(
                text = "${post.author} · ${post.timeAgo}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.md)) {
                Text("${post.score.compactMetric()} puan", style = MaterialTheme.typography.labelMedium)
                Text("${post.comments.compactMetric()} yorum", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
}

@Composable
private fun LemmyExploreThumbnail(post: LemmyPostUiModel) {
    val thumbnailUrl = post.thumbnailUrl
    if (thumbnailUrl.isNullOrBlank()) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.sm))
                .padding(1.dp),
        )
        return
    }
    val context = LocalContext.current
    val request = remember(context, thumbnailUrl) {
        ImageRequest.Builder(context)
            .data(thumbnailUrl)
            .size(160, 160)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = Modifier
            .size(72.dp)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.sm)),
        contentScale = ContentScale.Crop,
    )
}

@Composable
private fun LemmyCommunitiesDiscovery(
    state: LemmyCommunitiesUiState,
    onRetry: () -> Unit,
    onCommunitySelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val communities = state.communities
    when {
        state.isLoading -> {
            AppLoading(message = "Community listesi yükleniyor...", modifier = modifier)
            return
        }
        state.errorMessage != null -> {
            AppErrorState(message = state.errorMessage, onRetry = onRetry, modifier = modifier)
            return
        }
    }
    if (communities.isEmpty()) {
        EmptyState(
            title = "Community bulunamadı",
            message = "Sunucu şu an popular community döndürmedi.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(AppSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        item(contentType = "lemmy-community-search-placeholder") {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                singleLine = true,
                leadingIcon = {
                    Icon(imageVector = Icons.Outlined.Search, contentDescription = null)
                },
                placeholder = { Text("Community ara") },
            )
        }
        items(
            items = communities,
            key = { it.id.ifBlank { it.name } },
            contentType = { "lemmy-community-discovery" },
        ) { community ->
            LemmyCommunityDiscoveryRow(
                community = community,
                onClick = { onCommunitySelected(community.id.ifBlank { community.actorId ?: community.name }) },
            )
        }
    }
}

@Composable
private fun LemmyCommunityDiscoveryRow(
    community: LemmyCommunityUiModel,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = AppSpacing.sm),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = community.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = "c/${community.name} · ${community.subscribers.compactMetric()} abone",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (community.description.isNotBlank()) {
                Text(
                    text = community.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        AssistChip(onClick = onClick, label = { Text("Aç") })
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.48f))
}

@Composable
private fun PixelfedExploreContent(
    account: Account?,
    posts: androidx.paging.compose.LazyPagingItems<PixelfedPostUiModel>,
    contentPadding: PaddingValues,
    onPostSelected: (String) -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        ExploreHeader(
            title = "Keşfet",
            subtitle = account?.let { "@${it.username} · ${it.instanceUrl}" } ?: "Public Pixelfed medya",
        )
        when {
            posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0 -> AppLoading(
                message = "Pixelfed keşfet yükleniyor...",
                modifier = Modifier.weight(1f),
            )
            posts.loadState.refresh is LoadState.Error && posts.itemCount == 0 -> {
                val error = posts.loadState.refresh as LoadState.Error
                AppErrorState(
                    message = error.error.userFacingMessage("Pixelfed Explore yüklenemedi."),
                    onRetry = posts::retry,
                    modifier = Modifier.weight(1f),
                )
            }
            posts.itemCount == 0 -> EmptyState(
                title = "Media yok",
                message = "Public Pixelfed timeline şu an boş.",
                modifier = Modifier.weight(1f),
            )
            else -> LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
                verticalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                items(
                    count = posts.itemCount,
                    key = posts.itemKey { it.id },
                    contentType = posts.itemContentType { "pixelfed-explore-media" },
                ) { index ->
                    posts[index]?.let { post ->
                        PixelfedExploreTile(
                            post = post,
                            onClick = {
                                onPostSelected(post.id)
                            },
                        )
                    }
                }
                if (posts.loadState.append is LoadState.Loading) {
                    item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(maxLineSpan) }) {
                        AppLoading(message = "Daha fazla yükleniyor...", modifier = Modifier.height(96.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PixelfedExploreTile(
    post: PixelfedPostUiModel,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    val request = remember(context, post.imageUrl) {
        ImageRequest.Builder(context)
            .data(post.imageUrl)
            .size(THUMBNAIL_IMAGE_SIZE, THUMBNAIL_IMAGE_SIZE)
            .precision(Precision.INEXACT)
            .crossfade(false)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .build()
    }
    AsyncImage(
        model = request,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(AppRadius.sm))
            .clickable(onClick = onClick),
        contentScale = ContentScale.Crop,
    )
}

private val MastodonExploreTab.label: String
    get() = when (this) {
        MastodonExploreTab.POSTS -> "Posts"
        MastodonExploreTab.TAGS -> "Tags"
        MastodonExploreTab.LINKS -> "Links"
    }

private val LemmyExploreTab.label: String
    get() = when (this) {
        LemmyExploreTab.ALL -> "All"
        LemmyExploreTab.LOCAL -> "Local"
        LemmyExploreTab.COMMUNITIES -> "Communities"
    }

private val lemmySortTypes = listOf(
    LemmySortType.ACTIVE,
    LemmySortType.HOT,
    LemmySortType.NEW,
    LemmySortType.TOP,
)

private val LemmySortType.label: String
    get() = when (this) {
        LemmySortType.ACTIVE -> "Active"
        LemmySortType.HOT -> "Hot"
        LemmySortType.NEW -> "New"
        LemmySortType.TOP -> "Top"
    }

private fun Int.compactMetric(): String = when {
    this >= 1_000_000 -> "${this / 1_000_000}M"
    this >= 1_000 -> "${this / 1_000}K"
    else -> toString()
}

private const val THUMBNAIL_IMAGE_SIZE = 240
