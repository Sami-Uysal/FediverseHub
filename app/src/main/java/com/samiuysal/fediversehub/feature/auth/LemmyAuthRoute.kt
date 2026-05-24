package com.samiuysal.fediversehub.feature.auth

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun LemmyAuthRoute(
    modifier: Modifier = Modifier,
    showTopBar: Boolean = true,
    viewModel: LemmyAuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    LemmyAuthScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier,
        showTopBar = showTopBar,
    )
}
