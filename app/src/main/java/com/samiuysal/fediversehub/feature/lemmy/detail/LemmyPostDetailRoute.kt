package com.samiuysal.fediversehub.feature.lemmy.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LemmyPostDetailRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onUnauthorized: () -> Unit,
    viewModel: LemmyPostDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                LemmyPostDetailEffect.NavigateToLogin -> onUnauthorized()
            }
        }
    }

    LemmyPostDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onRetryComments = viewModel::retryComments,
        onToggleComment = viewModel::toggleComment,
        modifier = Modifier.padding(contentPadding),
    )
}
