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
import androidx.compose.material.icons.outlined.Groups
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
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.component.AppErrorState
import com.samiuysal.fediversehub.core.designsystem.component.AppLinkPreview
import com.samiuysal.fediversehub.core.designsystem.component.AppLoading
import com.samiuysal.fediversehub.core.designsystem.component.AppMediaItem
import com.samiuysal.fediversehub.core.designsystem.component.AppPostCard
import com.samiuysal.fediversehub.core.designsystem.component.EmptyState
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.home.PlatformSwitcher
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchCategory
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchCategory
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchCategory

@Composable
fun PlatformSearchRoute(
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onPostSelected: (String) -> Unit,
    onPixelfedPostSelected: (String) -> Unit,
    onLemmyPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onPixelfedAccountSelected: (String) -> Unit,
    onLemmyCommunitySelected: (String) -> Unit,
    onLemmyUserSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
    mastodonViewModel: MastodonSearchViewModel = hiltViewModel(),
    pixelfedViewModel: PixelfedSearchViewModel = hiltViewModel(),
    lemmyViewModel: LemmySearchViewModel = hiltViewModel(),
) {
    val mastodonQuery by mastodonViewModel.queryState.collectAsStateWithLifecycle()
    val mastodonCategory by mastodonViewModel.categoryState.collectAsStateWithLifecycle()
    val mastodonState by mastodonViewModel.uiState.collectAsStateWithLifecycle()
    val pixelfedQuery by pixelfedViewModel.queryState.collectAsStateWithLifecycle()
    val pixelfedCategory by pixelfedViewModel.categoryState.collectAsStateWithLifecycle()
    val pixelfedState by pixelfedViewModel.uiState.collectAsStateWithLifecycle()
    val lemmyQuery by lemmyViewModel.queryState.collectAsStateWithLifecycle()
    val lemmyCategory by lemmyViewModel.categoryState.collectAsStateWithLifecycle()
    val lemmyState by lemmyViewModel.uiState.collectAsStateWithLifecycle()

    androidx.compose.runtime.LaunchedEffect(selectedAccount?.id) {
        mastodonViewModel.selectAccount(selectedAccount)
        pixelfedViewModel.selectAccount(selectedAccount)
        lemmyViewModel.selectAccount(selectedAccount)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        SearchTopBar(selectedPlatform = selectedPlatform, onPlatformSelected = onPlatformSelected)
        when (selectedPlatform) {
            PlatformType.MASTODON -> MastodonSearchContent(
                query = mastodonQuery,
                category = mastodonCategory,
                uiState = mastodonState,
                onQueryChanged = mastodonViewModel::onQueryChanged,
                onCategorySelected = mastodonViewModel::onCategorySelected,
                onPostSelected = onPostSelected,
                onAccountSelected = onAccountSelected,
                onHashtagSelected = onHashtagSelected,
            )
            PlatformType.PIXELFED -> PixelfedSearchContent(
                query = pixelfedQuery,
                category = pixelfedCategory,
                uiState = pixelfedState,
                onQueryChanged = pixelfedViewModel::onQueryChanged,
                onCategorySelected = pixelfedViewModel::onCategorySelected,
                onPostSelected = onPixelfedPostSelected,
                onAccountSelected = onPixelfedAccountSelected,
                onHashtagSelected = onHashtagSelected,
            )
            PlatformType.LEMMY -> LemmySearchContent(
                query = lemmyQuery,
                category = lemmyCategory,
                uiState = lemmyState,
                onQueryChanged = lemmyViewModel::onQueryChanged,
                onCategorySelected = lemmyViewModel::onCategorySelected,
                onPostSelected = onLemmyPostSelected,
                onCommunitySelected = onLemmyCommunitySelected,
                onUserSelected = onLemmyUserSelected,
            )
        }
    }
}

@Composable
private fun SearchTopBar(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
) {
    Surface(color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f)) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .padding(horizontal = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PlatformSwitcher(selectedPlatform = selectedPlatform, onPlatformSelected = onPlatformSelected)
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
    SearchShell(
        query = query,
        placeholder = "Mastodon ara",
        onQueryChanged = onQueryChanged,
        categoryRow = {
            SearchCategoryRow(
                entries = MastodonSearchCategory.entries,
                selected = category,
                label = { it.label },
                onSelected = onCategorySelected,
            )
        },
    ) {
        when (uiState) {
            MastodonSearchUiState.Idle -> SearchIdle("Mastodon ara", "Post, hesap ve hashtag bul.")
            MastodonSearchUiState.Loading -> SearchLoading()
            is MastodonSearchUiState.Error -> SearchError(uiState.message) { onQueryChanged(query) }
            is MastodonSearchUiState.Success -> MastodonSearchResults(
                category = category,
                results = uiState.results,
                onPostSelected = onPostSelected,
                onAccountSelected = onAccountSelected,
                onHashtagSelected = onHashtagSelected,
            )
        }
    }
}

@Composable
private fun PixelfedSearchContent(
    query: String,
    category: PixelfedSearchCategory,
    uiState: PixelfedSearchUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (PixelfedSearchCategory) -> Unit,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
) {
    SearchShell(
        query = query,
        placeholder = "Pixelfed ara",
        onQueryChanged = onQueryChanged,
        categoryRow = {
            SearchCategoryRow(
                entries = PixelfedSearchCategory.entries,
                selected = category,
                label = { it.label },
                onSelected = onCategorySelected,
            )
        },
    ) {
        when (uiState) {
            PixelfedSearchUiState.Idle -> SearchIdle("Pixelfed ara", "Fotoğraf, hesap ve hashtag bul.")
            PixelfedSearchUiState.Loading -> SearchLoading()
            is PixelfedSearchUiState.Error -> SearchError(uiState.message) { onQueryChanged(query) }
            is PixelfedSearchUiState.Success -> PixelfedSearchResults(
                category = category,
                results = uiState.results,
                onPostSelected = onPostSelected,
                onAccountSelected = onAccountSelected,
                onHashtagSelected = onHashtagSelected,
            )
        }
    }
}

@Composable
private fun LemmySearchContent(
    query: String,
    category: LemmySearchCategory,
    uiState: LemmySearchUiState,
    onQueryChanged: (String) -> Unit,
    onCategorySelected: (LemmySearchCategory) -> Unit,
    onPostSelected: (String) -> Unit,
    onCommunitySelected: (String) -> Unit,
    onUserSelected: (String) -> Unit,
) {
    SearchShell(
        query = query,
        placeholder = "Lemmy ara",
        onQueryChanged = onQueryChanged,
        categoryRow = {
            SearchCategoryRow(
                entries = LemmySearchCategory.entries,
                selected = category,
                label = { it.label },
                onSelected = onCategorySelected,
            )
        },
    ) {
        when (uiState) {
            LemmySearchUiState.Idle -> SearchIdle("Lemmy ara", "Post, community ve kullanıcı bul.")
            LemmySearchUiState.Loading -> SearchLoading()
            is LemmySearchUiState.Error -> SearchError(uiState.message) { onQueryChanged(query) }
            is LemmySearchUiState.Success -> LemmySearchResults(
                category = category,
                results = uiState.results,
                onPostSelected = onPostSelected,
                onCommunitySelected = onCommunitySelected,
                onUserSelected = onUserSelected,
            )
        }
    }
}

@Composable
private fun SearchShell(
    query: String,
    placeholder: String,
    onQueryChanged: (String) -> Unit,
    categoryRow: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
            placeholder = { Text(placeholder) },
            singleLine = true,
        )
        categoryRow()
        content()
    }
}

@Composable
private fun <T> SearchCategoryRow(
    entries: List<T>,
    selected: T,
    label: (T) -> String,
    onSelected: (T) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        entries.forEach { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelected(category) },
                label = { Text(label(category)) },
            )
        }
    }
}

@Composable
private fun SearchIdle(title: String, message: String) {
    EmptyState(title = title, message = message, modifier = Modifier.fillMaxSize())
}

@Composable
private fun SearchLoading() {
    AppLoading(message = "Aranıyor...", modifier = Modifier.fillMaxSize())
}

@Composable
private fun SearchError(message: String, onRetry: () -> Unit) {
    AppErrorState(message = message, onRetry = onRetry, modifier = Modifier.fillMaxSize())
}

@Composable
private fun MastodonSearchResults(
    category: MastodonSearchCategory,
    results: MastodonSearchResultsUiModel,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
) {
    if (results.isEmpty(category)) {
        EmptyState(title = "Sonuç yok", message = "Başka bir arama dene.", modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = AppSpacing.xl)) {
        when (category) {
            MastodonSearchCategory.POSTS -> items(results.posts, key = { it.id }, contentType = { "mastodon-search-post" }) {
                MastodonPostRow(post = it, onPostSelected = onPostSelected, onAccountSelected = onAccountSelected)
            }
            MastodonSearchCategory.ACCOUNTS -> items(results.accounts, key = { it.id }, contentType = { "mastodon-search-account" }) {
                AccountRow(id = it.id, displayName = it.displayName, username = it.username, avatarUrl = it.avatarUrl, note = it.note, onClick = onAccountSelected)
            }
            MastodonSearchCategory.HASHTAGS -> items(results.hashtags, key = { it.name }, contentType = { "mastodon-search-hashtag" }) {
                HashtagRow(name = it.name, onClick = onHashtagSelected)
            }
        }
    }
}

@Composable
private fun PixelfedSearchResults(
    category: PixelfedSearchCategory,
    results: PixelfedSearchResultsUiModel,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
    onHashtagSelected: (String) -> Unit,
) {
    if (results.isEmpty(category)) {
        EmptyState(title = "Sonuç yok", message = "Başka bir arama dene.", modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = AppSpacing.xl)) {
        when (category) {
            PixelfedSearchCategory.POSTS -> items(results.posts, key = { it.id }, contentType = { "pixelfed-search-post" }) {
                PixelfedPostRow(post = it, onPostSelected = onPostSelected, onAccountSelected = onAccountSelected)
            }
            PixelfedSearchCategory.ACCOUNTS -> items(results.accounts, key = { it.id }, contentType = { "pixelfed-search-account" }) {
                AccountRow(id = it.id, displayName = it.displayName, username = it.username, avatarUrl = it.avatarUrl, note = it.note, onClick = onAccountSelected)
            }
            PixelfedSearchCategory.HASHTAGS -> items(results.hashtags, key = { it.name }, contentType = { "pixelfed-search-hashtag" }) {
                HashtagRow(name = it.name, onClick = onHashtagSelected)
            }
        }
    }
}

@Composable
private fun LemmySearchResults(
    category: LemmySearchCategory,
    results: LemmySearchResultsUiModel,
    onPostSelected: (String) -> Unit,
    onCommunitySelected: (String) -> Unit,
    onUserSelected: (String) -> Unit,
) {
    if (results.isEmpty(category)) {
        EmptyState(title = "Sonuç yok", message = "Başka bir arama dene.", modifier = Modifier.fillMaxSize())
        return
    }
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(bottom = AppSpacing.xl)) {
        when (category) {
            LemmySearchCategory.POSTS -> items(results.posts, key = { it.id }, contentType = { "lemmy-search-post" }) {
                LemmyPostRow(post = it, onClick = { onPostSelected(it.id) })
            }
            LemmySearchCategory.COMMUNITIES -> items(results.communities, key = { it.id.ifBlank { it.name } }, contentType = { "lemmy-search-community" }) {
                LemmyCommunityRow(community = it, onClick = { onCommunitySelected(it.id.ifBlank { it.actorId ?: it.name }) })
            }
            LemmySearchCategory.USERS -> items(results.users, key = { it.id.ifBlank { it.name } }, contentType = { "lemmy-search-user" }) {
                AccountRow(id = it.name, displayName = it.displayName, username = "u/${it.name}", avatarUrl = it.avatarUrl, note = it.bio, onClick = onUserSelected)
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
            AppLinkPreview(it.domain, it.title, it.description, it.thumbnailUrl)
        }
    }
    val mediaItems = remember(post.media) {
        post.media.map { AppMediaItem(it.previewUrl, it.fullUrl, it.altText) }
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

@Composable
private fun PixelfedPostRow(
    post: PixelfedPostUiModel,
    onPostSelected: (String) -> Unit,
    onAccountSelected: (String) -> Unit,
) {
    AppPostCard(
        displayName = post.displayName,
        username = post.username,
        timeAgo = post.timeAgo,
        avatarUrl = post.avatarUrl,
        content = post.caption,
        mediaUrl = post.imageUrl,
        actions = emptyList(),
        onClick = { onPostSelected(post.id) },
        onAuthorClick = { onAccountSelected(post.authorAccountId) },
    )
}

@Composable
private fun LemmyPostRow(post: LemmyPostUiModel, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
    ) {
        Text("c/${post.community} · ${post.author}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (post.previewText.isNotBlank()) {
            Text(post.previewText, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        }
        Text("${post.score} puan · ${post.comments} yorum", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}

@Composable
private fun LemmyCommunityRow(community: LemmyCommunityUiModel, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Column(modifier = Modifier.weight(1f)) {
            Text(community.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("c/${community.name} · ${community.subscribers} abone", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (community.description.isNotBlank()) {
                Text(community.description, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}

@Composable
private fun AccountRow(
    id: String,
    displayName: String,
    username: String,
    avatarUrl: String?,
    note: String,
    onClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(id) }
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AppAvatar(imageUrl = avatarUrl, name = displayName)
        Column(modifier = Modifier.weight(1f)) {
            Text(displayName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(username, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (note.isNotBlank()) {
                Text(note, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
        }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}

@Composable
private fun HashtagRow(name: String, onClick: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick(name.removePrefix("#")) }
            .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(Icons.Outlined.Tag, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Text("#${name.removePrefix("#")}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))
}

private val MastodonSearchCategory.label: String
    get() = when (this) {
        MastodonSearchCategory.POSTS -> "Posts"
        MastodonSearchCategory.ACCOUNTS -> "Accounts"
        MastodonSearchCategory.HASHTAGS -> "Hashtags"
    }

private val PixelfedSearchCategory.label: String
    get() = when (this) {
        PixelfedSearchCategory.POSTS -> "Posts"
        PixelfedSearchCategory.ACCOUNTS -> "Accounts"
        PixelfedSearchCategory.HASHTAGS -> "Hashtags"
    }

private val LemmySearchCategory.label: String
    get() = when (this) {
        LemmySearchCategory.POSTS -> "Posts"
        LemmySearchCategory.COMMUNITIES -> "Communities"
        LemmySearchCategory.USERS -> "Users"
    }
