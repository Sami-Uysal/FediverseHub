package com.samiuysal.fediversehub.feature.pixelfed.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PixelfedPostDetailRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    onAccountSelected: (String) -> Unit,
    onUnauthorized: () -> Unit,
    viewModel: PixelfedPostDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                PixelfedPostDetailEffect.NavigateToLogin -> onUnauthorized()
            }
        }
    }

    PixelfedPostDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onRetryComments = viewModel::retryComments,
        onLikeClick = viewModel::onLikeClick,
        onCommentDraftChange = viewModel::onCommentDraftChange,
        onSubmitComment = viewModel::submitComment,
        onMediaSelected = onMediaSelected,
        onAccountSelected = onAccountSelected,
        modifier = Modifier.padding(contentPadding),
    )
}
