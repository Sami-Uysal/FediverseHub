package com.samiuysal.fediversehub.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.PixelfedAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class PixelfedAuthViewModel @Inject constructor(
    private val repository: PixelfedAuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(PixelfedAuthUiState())
    val uiState: StateFlow<PixelfedAuthUiState> = _uiState.asStateFlow()

    private val _effects = MutableSharedFlow<PixelfedAuthEffect>()
    val effects: SharedFlow<PixelfedAuthEffect> = _effects.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.accounts.collect { accounts ->
                _uiState.update { state ->
                    state.copy(
                        account = accounts.firstOrNull { it.platform == PlatformType.PIXELFED },
                    )
                }
            }
        }
    }

    fun onEvent(event: PixelfedAuthUiEvent) {
        when (event) {
            is PixelfedAuthUiEvent.InstanceUrlChanged -> {
                _uiState.update { it.copy(instanceUrl = event.value, errorMessage = null) }
            }
            PixelfedAuthUiEvent.LoginClicked -> startLogin()
            is PixelfedAuthUiEvent.OAuthCallbackReceived -> handleCallback(event.callbackUrl)
            PixelfedAuthUiEvent.LogoutClicked -> logout()
            PixelfedAuthUiEvent.ErrorShown -> _uiState.update { it.copy(errorMessage = null) }
        }
    }

    private fun startLogin() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.startLogin(_uiState.value.instanceUrl)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false) }
                    _effects.emit(PixelfedAuthEffect.OpenAuthorizeUrl(result.data))
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.error.userMessage())
                    }
                }
            }
        }
    }

    private fun handleCallback(callbackUrl: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.handleCallback(callbackUrl)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(isLoading = false, account = result.data) }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.error.userMessage())
                    }
                }
            }
        }
    }

    private fun logout() {
        val account = _uiState.value.account ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (val result = repository.logout(account.id)) {
                is AppResult.Success -> _uiState.update { it.copy(isLoading = false, account = null) }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.error.userMessage())
                    }
                }
            }
        }
    }

    private fun AppError.userMessage(): String = when (this) {
        AppError.Network -> "Network error. Check instance URL and connection."
        AppError.RateLimited -> "Instance rate limit reached. Try again later."
        AppError.Unauthorized -> "Pixelfed authorization failed. Please retry login."
        is AppError.Server -> "Server error: $code"
        is AppError.Unknown -> message ?: "Unexpected auth error."
    }
}
