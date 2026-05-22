package com.samiuysal.fediversehub.feature.mastodon.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MastodonPostDetailRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    viewModel: MastodonPostDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    MastodonPostDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onMediaSelected = onMediaSelected,
        modifier = Modifier.padding(contentPadding),
    )
}
