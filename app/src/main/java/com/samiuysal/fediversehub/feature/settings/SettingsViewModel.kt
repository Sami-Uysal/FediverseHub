package com.samiuysal.fediversehub.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountStore: AccountStore,
) : ViewModel() {
    val accounts: StateFlow<List<Account>> =
        accountStore.accounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun logout(accountId: String) {
        viewModelScope.launch {
            accountStore.removeAccount(accountId)
        }
    }
}
