package com.samiuysal.fediversehub.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.userMessage
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class PixelfedNotificationsViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val repository: PixelfedRepository,
) : ViewModel() {
    private val selectedAccountId = MutableStateFlow<String?>(null)
    private val _uiState = MutableStateFlow<PlatformNotificationUiState<PixelfedNotificationUiModel>>(
        PlatformNotificationUiState.Loading,
    )
    val uiState: StateFlow<PlatformNotificationUiState<PixelfedNotificationUiModel>> = _uiState.asStateFlow()

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            val account = accountStore.accounts.first().firstOrNull {
                it.platform == PlatformType.PIXELFED &&
                    it.id == selectedAccountId.value &&
                    !it.accessToken.isNullOrBlank()
            } ?: accountStore.accounts.first().firstOrNull {
                it.platform == PlatformType.PIXELFED && !it.accessToken.isNullOrBlank()
            }
            if (account == null) {
                _uiState.value = PlatformNotificationUiState.NoAccount
                return@launch
            }
            _uiState.value = PlatformNotificationUiState.Loading
            when (val result = repository.getNotifications(account)) {
                is AppResult.Success -> _uiState.value = PlatformNotificationUiState.Success(
                    result.data.map(PlatformNotificationMappers::pixelfedToUi),
                )
                is AppResult.Failure -> _uiState.value = PlatformNotificationUiState.Error(result.error.userMessage())
            }
        }
    }
}

