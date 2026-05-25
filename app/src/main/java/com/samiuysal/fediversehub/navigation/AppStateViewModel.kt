package com.samiuysal.fediversehub.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.datastore.AppPreferencesRepository
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AppState(
    val selectedPlatform: PlatformType = PlatformType.MASTODON,
    val accounts: List<Account> = emptyList(),
    val activeAccountIds: Map<PlatformType, String> = emptyMap(),
    val onboardingSeen: Boolean? = null,
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
    private val appPreferencesRepository: AppPreferencesRepository,
) : ViewModel() {
    private val selectedPlatformOverride = MutableStateFlow<PlatformType?>(null)

    val uiState: StateFlow<AppState> =
        combine(
            appPreferencesRepository.selectedPlatform,
            selectedPlatformOverride,
            accountStore.activeAccountIds,
            accountStore.accounts,
            appPreferencesRepository.onboardingSeen,
        ) { savedPlatform, platformOverride, activeIds, accounts, onboardingSeen ->
            val platform = platformOverride ?: savedPlatform
            val validActiveIds = activeIds.filter { (platform, activeId) ->
                accounts.any { it.platform == platform && it.id == activeId }
            }
            AppState(
                selectedPlatform = platform,
                accounts = accounts,
                activeAccountIds = validActiveIds,
                onboardingSeen = onboardingSeen || accounts.isNotEmpty(),
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppState(),
        )

    fun selectPlatform(platform: PlatformType) {
        selectedPlatformOverride.value = platform
        viewModelScope.launch {
            appPreferencesRepository.setSelectedPlatform(platform)
        }
    }

    fun selectAccount(account: Account) {
        selectedPlatformOverride.value = account.platform
        viewModelScope.launch {
            appPreferencesRepository.setSelectedPlatform(account.platform)
            accountStore.saveActiveAccount(account.platform, account.id)
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            appPreferencesRepository.setOnboardingSeen(true)
        }
    }
}
