package com.samiuysal.fediversehub.feature.auth

import com.samiuysal.fediversehub.core.model.Account

data class PixelfedAuthUiState(
    val instanceUrl: String = "pixelfed.social",
    val account: Account? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface PixelfedAuthUiEvent {
    data class InstanceUrlChanged(val value: String) : PixelfedAuthUiEvent
    data object LoginClicked : PixelfedAuthUiEvent
    data class OAuthCallbackReceived(val callbackUrl: String) : PixelfedAuthUiEvent
    data object LogoutClicked : PixelfedAuthUiEvent
    data object ErrorShown : PixelfedAuthUiEvent
}

sealed interface PixelfedAuthEffect {
    data class OpenAuthorizeUrl(val url: String) : PixelfedAuthEffect
}
