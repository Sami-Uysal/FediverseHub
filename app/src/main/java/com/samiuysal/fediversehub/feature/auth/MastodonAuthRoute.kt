package com.samiuysal.fediversehub.feature.auth

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun MastodonAuthRoute(
    oauthCallbackUri: Uri?,
    onOAuthCallbackConsumed: () -> Unit,
    viewModel: MastodonAuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(oauthCallbackUri) {
        oauthCallbackUri?.let { uri ->
            viewModel.onEvent(MastodonAuthUiEvent.OAuthCallbackReceived(uri.toString()))
            onOAuthCallbackConsumed()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is MastodonAuthEffect.OpenAuthorizeUrl -> uriHandler.openUri(effect.url)
            }
        }
    }

    MastodonAuthScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
