package com.samiuysal.fediversehub.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

data class AppState(
    val selectedPlatform: PlatformType = PlatformType.MASTODON,
    val accounts: List<Account> = emptyList(),
    val activeAccountIds: Map<PlatformType, String> = emptyMap(),
) {
    val platformAccounts: List<Account>
        get() = accounts.filter { it.platform == selectedPlatform }

    val selectedAccount: Account?
        get() {
            val platformAccounts = platformAccounts
            return platformAccounts.firstOrNull { it.id == activeAccountIds[selectedPlatform] }
                ?: platformAccounts.firstOrNull()
        }
}

@HiltViewModel
class AppStateViewModel @Inject constructor(
    private val accountStore: AccountStore,
) : ViewModel() {
    private val selectedPlatform = MutableStateFlow(PlatformType.MASTODON)

    val uiState: StateFlow<AppState> =
        combine(selectedPlatform, accountStore.activeAccountIds, accountStore.accounts) { platform, activeIds, accounts ->
            val validActiveIds = activeIds.filterValues { activeId ->
                accounts.any { it.id == activeId }
            }
            AppState(
                selectedPlatform = platform,
                accounts = accounts,
                activeAccountIds = validActiveIds,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppState(),
        )

    fun selectPlatform(platform: PlatformType) {
        selectedPlatform.value = platform
    }

    fun selectAccount(account: Account) {
        viewModelScope.launch {
            accountStore.saveActiveAccount(account.platform, account.id)
        }
    }
}
