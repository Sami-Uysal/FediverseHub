package com.samiuysal.fediversehub.feature.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.userMessage
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@HiltViewModel
class LemmyNotificationsViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val repository: LemmyRepository,
) : ViewModel() {
    private val selectedAccountId = MutableStateFlow<String?>(null)
    private val selectedTab = MutableStateFlow(LemmyNotificationTab.REPLIES)
    private val _uiState = MutableStateFlow<PlatformNotificationUiState<LemmyNotificationUiModel>>(
        PlatformNotificationUiState.Loading,
    )
    val uiState: StateFlow<PlatformNotificationUiState<LemmyNotificationUiModel>> = _uiState.asStateFlow()
    val tabState: StateFlow<LemmyNotificationTab> = selectedTab.asStateFlow()

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
        load()
    }

    fun selectTab(tab: LemmyNotificationTab) {
        selectedTab.value = tab
        load()
    }

    fun retry() = load()

    private fun load() {
        viewModelScope.launch {
            val account = accountStore.accounts.first().firstOrNull {
                it.platform == PlatformType.LEMMY &&
                    it.id == selectedAccountId.value &&
                    !it.accessToken.isNullOrBlank()
            } ?: accountStore.accounts.first().firstOrNull {
                it.platform == PlatformType.LEMMY && !it.accessToken.isNullOrBlank()
            }
            if (account == null) {
                _uiState.value = PlatformNotificationUiState.NoAccount
                return@launch
            }
            _uiState.value = PlatformNotificationUiState.Loading
            val result = when (selectedTab.value) {
                LemmyNotificationTab.REPLIES -> repository.getReplies(account)
                LemmyNotificationTab.MENTIONS -> repository.getMentions(account)
            }
            when (result) {
                is AppResult.Success -> _uiState.value = PlatformNotificationUiState.Success(
                    result.data.map(PlatformNotificationMappers::lemmyToUi),
                )
                is AppResult.Failure -> _uiState.value = PlatformNotificationUiState.Error(result.error.userMessage())
            }
        }
    }
}

