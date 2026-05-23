package com.samiuysal.fediversehub.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AppState(
    val selectedPlatform: PlatformType = PlatformType.MASTODON,
    val accounts: List<Account> = emptyList(),
) {
    val selectedAccount: Account?
        get() = accounts.firstOrNull { it.platform == selectedPlatform }
}

@HiltViewModel
class AppStateViewModel @Inject constructor(
    accountStore: AccountStore,
) : ViewModel() {
    private val selectedPlatform = MutableStateFlow(PlatformType.MASTODON)

    val uiState: StateFlow<AppState> =
        combine(selectedPlatform, accountStore.accounts) { platform, accounts ->
            AppState(
                selectedPlatform = platform,
                accounts = accounts,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppState(),
        )

    fun selectPlatform(platform: PlatformType) {
        selectedPlatform.value = platform
    }
}
