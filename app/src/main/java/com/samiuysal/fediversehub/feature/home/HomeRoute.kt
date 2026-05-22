package com.samiuysal.fediversehub.feature.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.collectAsLazyPagingItems
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.LemmyHomeScreen
import com.samiuysal.fediversehub.feature.mastodon.MastodonHomeScreen
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedHomeScreen

@Composable
fun HomeRoute(
    contentPadding: androidx.compose.foundation.layout.PaddingValues,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding),
    ) {
        PlatformSwitcher(
            selectedPlatform = uiState.selectedPlatform,
            onPlatformSelected = {
                viewModel.onEvent(HomeUiEvent.PlatformSelected(it))
            },
        )
        when (uiState.selectedPlatform) {
            PlatformType.MASTODON -> {
                val mastodonTimeline = viewModel.mastodonTimeline.collectAsLazyPagingItems()
                MastodonHomeScreen(
                    account = uiState.selectedAccount,
                    posts = mastodonTimeline,
                    modifier = Modifier.weight(1f),
                )
            }
            PlatformType.LEMMY -> {
                val lemmyPosts = viewModel.lemmyPosts.collectAsLazyPagingItems()
                LemmyHomeScreen(
                    account = uiState.selectedAccount,
                    posts = lemmyPosts,
                    modifier = Modifier.weight(1f),
                )
            }
            PlatformType.PIXELFED -> PixelfedHomeScreen(
                account = uiState.selectedAccount,
                posts = uiState.pixelfedPosts,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
