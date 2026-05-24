package com.samiuysal.fediversehub.feature.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.database.AppDatabase
import com.samiuysal.fediversehub.core.datastore.AppPreferencesRepository
import com.samiuysal.fediversehub.core.datastore.ThemeMode
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.auth.domain.MastodonAuthRepository
import com.samiuysal.fediversehub.feature.auth.domain.PixelfedAuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val accountStore: AccountStore,
    private val mastodonAuthRepository: MastodonAuthRepository,
    private val pixelfedAuthRepository: PixelfedAuthRepository,
    private val appPreferencesRepository: AppPreferencesRepository,
    private val appDatabase: AppDatabase,
) : ViewModel() {
    val accounts: StateFlow<List<Account>> =
        accountStore.accounts.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val themeMode: StateFlow<ThemeMode> =
        appPreferencesRepository.themeMode.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ThemeMode.SYSTEM,
        )

    private val _cacheState = MutableStateFlow<CacheClearState>(CacheClearState.Idle)
    val cacheState: StateFlow<CacheClearState> = _cacheState.asStateFlow()

    fun logout(account: Account) {
        viewModelScope.launch {
            when (account.platform) {
                PlatformType.MASTODON -> mastodonAuthRepository.logout(account.id)
                PlatformType.PIXELFED -> pixelfedAuthRepository.logout(account.id)
                PlatformType.LEMMY -> accountStore.removeAccount(account.id)
            }
        }
    }

    fun selectThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            appPreferencesRepository.setThemeMode(mode)
        }
    }

    fun clearCache() {
        if (_cacheState.value == CacheClearState.Clearing) return
        viewModelScope.launch {
            _cacheState.value = CacheClearState.Clearing
            runCatching {
                withContext(Dispatchers.IO) {
                    appDatabase.clearAllTables()
                    context.cacheDir.deleteRecursively()
                }
            }.fold(
                onSuccess = { _cacheState.value = CacheClearState.Done },
                onFailure = { _cacheState.value = CacheClearState.Error },
            )
        }
    }
}

enum class CacheClearState {
    Idle,
    Clearing,
    Done,
    Error,
}
