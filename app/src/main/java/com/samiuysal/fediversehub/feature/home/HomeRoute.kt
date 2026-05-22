package com.samiuysal.fediversehub.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.samiuysal.fediversehub.core.designsystem.component.AppAvatar
import com.samiuysal.fediversehub.core.designsystem.theme.AppSpacing
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.LemmyHomeScreen
import com.samiuysal.fediversehub.feature.lemmy.LemmyHomeScreenContent
import com.samiuysal.fediversehub.feature.mastodon.MastodonHomeScreen
import com.samiuysal.fediversehub.feature.mastodon.MastodonHomeScreenContent
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedHomeScreen

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
    onMastodonPostSelected: (String) -> Unit,
    onMastodonMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        contentPadding = contentPadding,
        onPlatformSelected = { viewModel.onEvent(HomeUiEvent.PlatformSelected(it)) },
        mastodonContent = { modifier ->
            val mastodonTimeline = viewModel.mastodonTimeline.collectAsLazyPagingItems()
            MastodonHomeScreen(
                account = uiState.selectedAccount,
                posts = mastodonTimeline,
                modifier = modifier,
                showTopBar = false,
                onPostClick = onMastodonPostSelected,
                onMediaClick = onMastodonMediaSelected,
            )
        },
        lemmyContent = { modifier ->
            val lemmyPosts = viewModel.lemmyPosts.collectAsLazyPagingItems()
            LemmyHomeScreen(
                account = uiState.selectedAccount,
                posts = lemmyPosts,
                modifier = modifier,
                showTopBar = false,
            )
        },
        pixelfedContent = { modifier ->
            PixelfedHomeScreen(
                account = uiState.selectedAccount,
                posts = uiState.pixelfedPosts,
                modifier = modifier,
                showTopBar = false,
            )
        },
    )
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    mastodonContent: @Composable (Modifier) -> Unit,
    lemmyContent: @Composable (Modifier) -> Unit,
    pixelfedContent: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        HomeTopBar(
            selectedPlatform = uiState.selectedPlatform,
            onPlatformSelected = onPlatformSelected,
            account = uiState.selectedAccount,
        )
        HomeComposerPreview(
            account = uiState.selectedAccount,
            selectedPlatform = uiState.selectedPlatform,
            onClick = {},
        )
        when (uiState.selectedPlatform) {
            PlatformType.MASTODON -> mastodonContent(Modifier.weight(1f))
            PlatformType.LEMMY -> lemmyContent(Modifier.weight(1f))
            PlatformType.PIXELFED -> pixelfedContent(Modifier.weight(1f))
        }
    }
}

@Composable
fun HomeTopBar(
    selectedPlatform: PlatformType,
    onPlatformSelected: (PlatformType) -> Unit,
    account: Account?,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                PlatformSwitcher(
                    selectedPlatform = selectedPlatform,
                    onPlatformSelected = onPlatformSelected,
                    modifier = Modifier.weight(1f, fill = false),
                )
                Spacer(modifier = Modifier.weight(1f))
                AppAvatar(
                    imageUrl = account?.avatarUrl,
                    name = account?.displayName ?: account?.username ?: "F",
                    size = 34.dp,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.68f))
        }
    }
}

@Composable
fun HomeComposerPreview(
    account: Account?,
    selectedPlatform: PlatformType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.background,
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.lg, vertical = AppSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                AppAvatar(
                    imageUrl = account?.avatarUrl,
                    name = account?.displayName ?: account?.username ?: "F",
                    size = 42.dp,
                )
                Text(
                    text = selectedPlatform.composerPlaceholder,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Normal,
                )
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.72f))
        }
    }
}

private val PlatformType.composerPlaceholder: String
    get() = when (this) {
        PlatformType.MASTODON -> "Aklında ne var?"
        PlatformType.LEMMY -> "Bir gönderi paylaş..."
        PlatformType.PIXELFED -> "Bir fotoğraf paylaş..."
    }

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun HomeScreenContentPreview() {
    FediverseHubTheme {
        val state = MockFediverseData.homeState
        HomeScreenContent(
            uiState = state,
            contentPadding = PaddingValues(),
            onPlatformSelected = {},
            mastodonContent = { modifier ->
                MastodonHomeScreenContent(
                    account = state.selectedAccount,
                    posts = state.mastodonPosts,
                    modifier = modifier,
                    showTopBar = false,
                )
            },
            lemmyContent = { modifier ->
                LemmyHomeScreenContent(
                    account = state.selectedAccount,
                    posts = state.lemmyPosts,
                    modifier = modifier,
                    showTopBar = false,
                )
            },
            pixelfedContent = { modifier ->
                PixelfedHomeScreen(
                    account = state.selectedAccount,
                    posts = state.pixelfedPosts,
                    modifier = modifier,
                    showTopBar = false,
                )
            },
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 160)
@Composable
fun HomeTopComposerPreview() {
    FediverseHubTheme {
        val state = MockFediverseData.homeState
        Column {
            HomeTopBar(
                selectedPlatform = PlatformType.MASTODON,
                onPlatformSelected = {},
                account = state.accounts.first { it.platform == PlatformType.MASTODON },
            )
            HomeComposerPreview(
                account = state.accounts.first { it.platform == PlatformType.MASTODON },
                selectedPlatform = PlatformType.MASTODON,
                onClick = {},
            )
        }
    }
}
