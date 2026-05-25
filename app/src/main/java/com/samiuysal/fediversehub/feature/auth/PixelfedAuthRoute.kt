package com.samiuysal.fediversehub.feature.auth

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalUriHandler
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PixelfedAuthRoute(
    oauthCallbackUri: Uri?,
    onOAuthCallbackConsumed: () -> Unit,
    onLoginCompleted: () -> Unit = {},
    viewModel: PixelfedAuthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(oauthCallbackUri) {
        oauthCallbackUri
            ?.takeIf { it.path == "/pixelfed" }
            ?.let { uri ->
                viewModel.onEvent(PixelfedAuthUiEvent.OAuthCallbackReceived(uri.toString()))
                onOAuthCallbackConsumed()
            }
    }

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is PixelfedAuthEffect.OpenAuthorizeUrl -> uriHandler.openUri(effect.url)
                PixelfedAuthEffect.LoginCompleted -> onLoginCompleted()
            }
        }
    }

    PixelfedAuthScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}
