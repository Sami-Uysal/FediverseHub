package com.samiuysal.fediversehub.feature.auth

import com.samiuysal.fediversehub.core.model.Account

data class MastodonAuthUiState(
    val instanceUrl: String = "mastodon.social",
    val account: Account? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface MastodonAuthUiEvent {
    data class InstanceUrlChanged(val value: String) : MastodonAuthUiEvent
    data object LoginClicked : MastodonAuthUiEvent
    data class OAuthCallbackReceived(val callbackUrl: String) : MastodonAuthUiEvent
    data object LogoutClicked : MastodonAuthUiEvent
    data object ErrorShown : MastodonAuthUiEvent
}

sealed interface MastodonAuthEffect {
    data class OpenAuthorizeUrl(val url: String) : MastodonAuthEffect
    data object LoginCompleted : MastodonAuthEffect
}
