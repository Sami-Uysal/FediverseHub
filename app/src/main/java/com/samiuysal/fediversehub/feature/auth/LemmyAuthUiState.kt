package com.samiuysal.fediversehub.feature.auth

import com.samiuysal.fediversehub.core.model.Account

data class LemmyAuthUiState(
    val instanceUrl: String = "lemmy.world",
    val usernameOrEmail: String = "",
    val password: String = "",
    val account: Account? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

sealed interface LemmyAuthUiEvent {
    data class InstanceUrlChanged(val value: String) : LemmyAuthUiEvent
    data class UsernameChanged(val value: String) : LemmyAuthUiEvent
    data class PasswordChanged(val value: String) : LemmyAuthUiEvent
    data object LoginClicked : LemmyAuthUiEvent
    data object LogoutClicked : LemmyAuthUiEvent
}
