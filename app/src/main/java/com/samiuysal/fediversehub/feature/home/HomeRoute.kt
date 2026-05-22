package com.samiuysal.fediversehub.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.samiuysal.fediversehub.core.designsystem.theme.FediverseHubTheme
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.LemmyHomeScreen
import com.samiuysal.fediversehub.feature.lemmy.LemmyHomeScreenContent
import com.samiuysal.fediversehub.feature.mastodon.MastodonHomeScreen
import com.samiuysal.fediversehub.feature.mastodon.MastodonHomeScreenContent
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedHomeScreen

@Composable
fun HomeRoute(
    contentPadding: PaddingValues,
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
            )
        },
        lemmyContent = { modifier ->
            val lemmyPosts = viewModel.lemmyPosts.collectAsLazyPagingItems()
            LemmyHomeScreen(
                account = uiState.selectedAccount,
                posts = lemmyPosts,
                modifier = modifier,
            )
        },
        pixelfedContent = { modifier ->
            PixelfedHomeScreen(
                account = uiState.selectedAccount,
                posts = uiState.pixelfedPosts,
                modifier = modifier,
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
        PlatformSwitcher(
            selectedPlatform = uiState.selectedPlatform,
            onPlatformSelected = onPlatformSelected,
        )
        when (uiState.selectedPlatform) {
            PlatformType.MASTODON -> mastodonContent(Modifier.weight(1f))
            PlatformType.LEMMY -> lemmyContent(Modifier.weight(1f))
            PlatformType.PIXELFED -> pixelfedContent(Modifier.weight(1f))
        }
    }
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
                )
            },
            lemmyContent = { modifier ->
                LemmyHomeScreenContent(
                    account = state.selectedAccount,
                    posts = state.lemmyPosts,
                    modifier = modifier,
                )
            },
            pixelfedContent = { modifier ->
                PixelfedHomeScreen(
                    account = state.selectedAccount,
                    posts = state.pixelfedPosts,
                    modifier = modifier,
                )
            },
        )
    }
}
