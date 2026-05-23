package com.samiuysal.fediversehub.feature.pixelfed.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.samiuysal.fediversehub.core.common.error.userMessage
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PixelfedProfileViewModel @Inject constructor(
    accountStore: AccountStore,
    private val pixelfedRepository: PixelfedRepository,
) : ViewModel() {
    private val selectedAccountId = MutableStateFlow<String?>(null)

    private val pixelfedAccount: Flow<Account?> =
        combine(accountStore.accounts, selectedAccountId) { accounts, activeAccountId ->
            accounts.firstOrNull {
                it.platform == PlatformType.PIXELFED &&
                    it.id == activeAccountId &&
                    !it.accessToken.isNullOrBlank()
            } ?: accounts.firstOrNull {
                it.platform == PlatformType.PIXELFED && !it.accessToken.isNullOrBlank()
            }
        }.distinctUntilChanged()

    val uiState: StateFlow<PixelfedProfileUiState> =
        pixelfedAccount
            .flatMapLatest { account ->
                flow {
                    if (account == null) {
                        emit(PixelfedProfileUiState.NoAccount)
                        return@flow
                    }
                    emit(PixelfedProfileUiState.Loading)
                    when (val result = pixelfedRepository.getOwnProfile(account)) {
                        is AppResult.Success -> emit(PixelfedProfileUiState.Success(result.data))
                        is AppResult.Failure -> emit(PixelfedProfileUiState.Error(result.error.userMessage()))
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = PixelfedProfileUiState.Loading,
            )

    val profileMedia: Flow<PagingData<PixelfedPostUiModel>> =
        pixelfedAccount
            .flatMapLatest { account ->
                if (account == null) {
                    flow { emit(PagingData.empty()) }
                } else {
                    pixelfedRepository.getProfileMediaPagingData(account)
                }
            }
            .map { pagingData -> pagingData.map(PixelfedMapper::postToUi) }
            .cachedIn(viewModelScope)

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
    }
}
