package com.samiuysal.fediversehub.feature.mastodon.detail

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.samiuysal.fediversehub.feature.mastodon.MastodonReplyComposeSheet

@Composable
fun MastodonPostDetailRoute(
    contentPadding: PaddingValues,
    onBack: () -> Unit,
    onMediaSelected: (List<String>, List<Boolean>, Int) -> Unit,
    onAccountSelected: (String) -> Unit,
    onUnauthorized: () -> Unit,
    viewModel: MastodonPostDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val replyComposeState by viewModel.replyComposeState.collectAsStateWithLifecycle()

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                MastodonPostDetailEffect.NavigateToMastodonLogin -> onUnauthorized()
            }
        }
    }

    MastodonPostDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::retry,
        onMediaSelected = onMediaSelected,
        onAccountSelected = onAccountSelected,
        onPostAction = viewModel::onPostAction,
        modifier = Modifier.padding(contentPadding),
    )

    replyComposeState?.let { state ->
        MastodonReplyComposeSheet(
            state = state,
            onTextChanged = viewModel::onReplyTextChanged,
            onDismiss = viewModel::dismissReplyCompose,
            onSend = viewModel::submitReply,
        )
    }
}
