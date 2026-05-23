package com.samiuysal.fediversehub.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.MastodonAuthRepository
import com.samiuysal.fediversehub.feature.auth.domain.PixelfedAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val mastodonAuthRepository: MastodonAuthRepository,
    private val pixelfedAuthRepository: PixelfedAuthRepository,
) : ViewModel() {
    val accounts: StateFlow<List<Account>> =
        accountStore.accounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    fun logout(account: Account) {
        viewModelScope.launch {
            when (account.platform) {
                PlatformType.MASTODON -> mastodonAuthRepository.logout(account.id)
                PlatformType.PIXELFED -> pixelfedAuthRepository.logout(account.id)
                PlatformType.LEMMY -> accountStore.removeAccount(account.id)
            }
        }
    }
}
