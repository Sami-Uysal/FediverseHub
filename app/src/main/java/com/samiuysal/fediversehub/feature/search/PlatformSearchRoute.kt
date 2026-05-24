package com.samiuysal.fediversehub.feature.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Tag
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppMediaItem
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.PlatformSwitcher
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchCategory

@Composable
fun PlatformSearchRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
    viewModel: MastodonSearchViewModel = hiltViewModel(),
) {
    val query by viewModel.queryState.collectAsStateWithLifecycle()
    val category by viewModel.categoryState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }

    SearchScreen(
        selectedPlatform = selectedPlatform,
        query = query,
        category = category,
        mastodonState = uiState,
        contentPadding = contentPadding,
        onPlatformSelected = onPlatformSelected,
        onQueryChanged = viewModel::onQueryChanged,
        onCategorySelected = viewModel::onCategorySelected,
        onPostSelected = onPostSelected,
        onAccountSelected = onAccountSelected,
        onHashtagSelected = onHashtagSelected,
    )
}

@Composable
private fun SearchScreen(
    selectedPlatform: PlatformType,
    query: String,
    category: MastodonSearchCategory,
    mastodonState: MastodonSearchUiState,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (MastodonSearchCategory) -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        SearchTopBar(
            selectedPlatform = selectedPlatform,
            onPlatformSelected = onPlatformSelected,
        )
        if (selectedPlatform == PlatformType.MASTODON) {
            MastodonSearchContent(
                query = query,
                category = category,
                uiState = mastodonState,
                onQueryChanged = onQueryChanged,
                onCategorySelected = onCategorySelected,
                onPostSelected = onPostSelected,
                onAccountSelected = onAccountSelected,
                onHashtagSelected = onHashtagSelected,
            )
        } else {
            EmptyState(
                title = "${selectedPlatform.label} arama yakında",
                message = "Şimdilik Mastodon arama aktif. ${selectedPlatform.label} araması burada açılacak.",
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SearchTopBar(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
) {
    Surface(
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatformSwitcher(
                    selectedPlatform = selectedPlatform,
                    onPlatformSelected = onPlatformSelected,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))
        }
    }
}

@Composable
private fun MastodonSearchContent(
    query: String,
    category: MastodonSearchCategory,
    uiState: MastodonSearchUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (MastodonSearchCategory) -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            placeholder = { Text("Search Mastodon") },
            singleLine = true,
        )
        SearchCategoryRow(
            selected = category,
            onSelected = onCategorySelected,
        )
        when (uiState) {
            MastodonSearchUiState.Idle -> EmptyState(
                title = "Search Mastodon",
                message = "Find posts, accounts and hashtags.",
                modifier = Modifier.weight(1f),
            )
            MastodonSearchUiState.Loading -> AppLoading(
                message = "Searching...",
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            )
            is MastodonSearchUiState.Error -> AppErrorState(
                message = uiState.message,
                onRetry = { onQueryChanged(query) },
                modifier = Modifier.weight(1f),
            )
            is MastodonSearchUiState.Success -> SearchResults(
                category = category,
                results = uiState.results,
                onPostSelected = onPostSelected,
                onAccountSelected = onAccountSelected,
                onHashtagSelected = onHashtagSelected,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun SearchCategoryRow(
    selected: MastodonSearchCategory,
    onSelected: (MastodonSearchCategory) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        MastodonSearchCategory.entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(category) },
                label = { Text(category.label) },
            )
        }
    }
}

@Composable
private fun SearchResults(
    category: MastodonSearchCategory,
    results: MastodonSearchResultsUiModel,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (results.isEmpty(category)) {
        EmptyState(
            title = "No results",
            message = "Try another query or category.",
            modifier = modifier,
        )
        return
    }
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = AppSpacing.xl),
    ) {
        when (category) {
            MastodonSearchCategory.POSTS -> items(
                items = results.posts,
                key = { it.id },
                contentType = { "search-post" },
            ) { post ->
                SearchPostRow(post = post, onPostSelected = onPostSelected)
            }
            MastodonSearchCategory.ACCOUNTS -> items(
                items = results.accounts,
                key = { it.id },
                contentType = { "search-account" },
            ) { account ->
                SearchAccountRow(account = account, onClick = onAccountSelected)
            }
            MastodonSearchCategory.HASHTAGS -> items(
                items = results.hashtags,
                key = { it.name },
                contentType = { "search-hashtag" },
            ) { hashtag ->
                SearchHashtagRow(hashtag = hashtag, onClick = onHashtagSelected)
            }
        }
    }
}

@Composable
private fun SearchPostRow(
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

@Composable
private fun SearchAccountRow(
    account: MastodonSearchAccountUiModel,
    onClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(account.id) }
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(
            imageUrl = account.avatarUrl,
            name = account.displayName,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = account.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = account.username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (account.note.isNotBlank()) {
                Text(
                    text = account.note,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}

@Composable
private fun SearchHashtagRow(
    hashtag: MastodonHashtagUiModel,
    onClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(hashtag.name.removePrefix("#")) }
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Tag,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = hashtag.name,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}

private val MastodonSearchCategory.label: String
    get() = when (this) {
        MastodonSearchCategory.POSTS -> "Posts"
        MastodonSearchCategory.ACCOUNTS -> "Accounts"
        MastodonSearchCategory.HASHTAGS -> "Hashtags"
    }

private val PlatformType.label: String
    get() = when (this) {
        PlatformType.MASTODON -> "Mastodon"
        PlatformType.LEMMY -> "Lemmy"
        PlatformType.PIXELFED -> "Pixelfed"
    }
