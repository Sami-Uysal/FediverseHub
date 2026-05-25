package com.samiuysal.fediversehub.feature.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.LemmyAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LemmyAuthViewModel @Inject constructor(
    private val repository: LemmyAuthRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(LemmyAuthUiState())
    val uiState: StateFlow<LemmyAuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.accounts.collect { accounts ->
                _uiState.update { state ->
                    state.copy(account = accounts.firstOrNull { it.platform == PlatformType.LEMMY })
                }
            }
        }
    }

    fun onEvent(event: LemmyAuthUiEvent) {
        when (event) {
            is LemmyAuthUiEvent.InstanceUrlChanged -> {
                _uiState.update { it.copy(instanceUrl = event.value, errorMessage = null) }
            }
            is LemmyAuthUiEvent.UsernameChanged -> {
                _uiState.update { it.copy(usernameOrEmail = event.value, errorMessage = null) }
            }
            is LemmyAuthUiEvent.PasswordChanged -> {
                _uiState.update { it.copy(password = event.value, errorMessage = null) }
            }
            LemmyAuthUiEvent.LoginClicked -> login()
            LemmyAuthUiEvent.LogoutClicked -> logout()
        }
    }

    private fun login() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            when (
                val result = repository.login(
                    instanceUrl = state.instanceUrl,
                    usernameOrEmail = state.usernameOrEmail,
                    password = state.password,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update {
                        it.copy(
                            account = result.data,
                            password = "",
                            isLoading = false,
                            errorMessage = null,
                        )
                    }
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
                is AppResult.Success -> {
                    _uiState.update { it.copy(account = null, isLoading = false) }
                }
                is AppResult.Failure -> {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = result.error.userMessage())
                    }
                }
            }
        }
    }

    private fun AppError.userMessage(): String = when (this) {
        AppError.Network -> LEMMY_LOGIN_ERROR
        AppError.RateLimited -> "Çok hızlı deneme yapıldı. Biraz bekle, tekrar dene."
        AppError.Unauthorized -> LEMMY_LOGIN_ERROR
        is AppError.Server -> if (code == 400) {
            LEMMY_LOGIN_ERROR
        } else {
            "Sunucu şu an girişe yanıt veremiyor. Biraz sonra tekrar dene."
        }
        is AppError.Unknown -> LEMMY_LOGIN_ERROR
    }

    private companion object {
        const val LEMMY_LOGIN_ERROR = "Giriş yapılamadı. Instance, kullanıcı adı veya şifreyi kontrol et."
    }
}
