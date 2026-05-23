package com.samiuysal.fediversehub.feature.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.samiuysal.fediversehub.feature.mastodon.MastodonNewPostComposeSheet
import com.samiuysal.fediversehub.feature.mastodon.MastodonReplyComposeSheet
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedHomeScreen

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
    selectedPlatform: PlatformType,
    selectedAccount: Account?,
    onPlatformSelected: (PlatformType) -> Unit,
    onMastodonPostSelected: (String) -> Unit,
    onMastodonMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    onMastodonUnauthorized: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val mastodonActionOverrides by viewModel.mastodonActionOverrides.collectAsStateWithLifecycle()
    val replyComposeState by viewModel.replyComposeState.collectAsStateWithLifecycle()
    val newPostComposeState by viewModel.newPostComposeState.collectAsStateWithLifecycle()

    LaunchedEffect(selectedPlatform) {
        viewModel.selectPlatform(selectedPlatform)
    }
    LaunchedEffect(selectedAccount?.id) {
        viewModel.selectAccount(selectedAccount)
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                HomeEffect.NavigateToMastodonLogin -> onMastodonUnauthorized()
            }
        }
    }

    HomeScreenContent(
        uiState = uiState,
        contentPadding = contentPadding,
        onPlatformSelected = onPlatformSelected,
        onComposerClick = viewModel::openNewPostCompose,
        mastodonContent = { modifier ->
            val mastodonTimeline = viewModel.mastodonTimeline.collectAsLazyPagingItems()
            MastodonHomeScreen(
                account = uiState.selectedAccount,
                posts = mastodonTimeline,
                actionOverrides = mastodonActionOverrides,
                modifier = modifier,
                showTopBar = false,
                onPostClick = onMastodonPostSelected,
                onMediaClick = onMastodonMediaSelected,
                onPostAction = viewModel::onMastodonAction,
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

    replyComposeState?.let { state ->
        MastodonReplyComposeSheet(
            state = state,
            onTextChanged = viewModel::onReplyTextChanged,
            onDismiss = viewModel::dismissReplyCompose,
            onSend = viewModel::submitReply,
        )
    }

    newPostComposeState?.let { state ->
        MastodonNewPostComposeSheet(
            state = state,
            onTextChanged = viewModel::onNewPostTextChanged,
            onVisibilityChanged = viewModel::onNewPostVisibilityChanged,
            onContentWarningEnabledChanged = viewModel::onNewPostContentWarningEnabledChanged,
            onContentWarningChanged = viewModel::onNewPostContentWarningChanged,
            onDismiss = viewModel::dismissNewPostCompose,
            onSend = viewModel::submitNewPost,
        )
    }
}

@Composable
fun HomeScreenContent(
    uiState: HomeUiState,
    contentPadding: PaddingValues,
    onPlatformSelected: (PlatformType) -> Unit,
    onComposerClick: () -> Unit,
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
        )
        HomeComposerPreview(
            account = uiState.selectedAccount,
            selectedPlatform = uiState.selectedPlatform,
            onClick = onComposerClick,
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
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.background.copy(alpha = 0.98f),
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = AppSpacing.md),
            ) {
                PlatformSwitcher(
                    selectedPlatform = selectedPlatform,
                    onPlatformSelected = onPlatformSelected,
                    modifier = Modifier.align(Alignment.CenterStart),
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
            onComposerClick = {},
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
            )
            HomeComposerPreview(
                account = state.accounts.first { it.platform == PlatformType.MASTODON },
                selectedPlatform = PlatformType.MASTODON,
                onClick = {},
            )
        }
    }
}
