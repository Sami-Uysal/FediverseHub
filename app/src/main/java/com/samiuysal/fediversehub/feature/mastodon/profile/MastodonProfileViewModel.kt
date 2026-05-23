package com.samiuysal.fediversehub.feature.mastodon.profile

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
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonProfileMapper
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MastodonProfileViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val mastodonRepository: MastodonRepository,
) : ViewModel() {
    private val selectedFilter = MutableStateFlow(MastodonProfileTimelineFilter.POSTS)
    private val selectedAccountId = MutableStateFlow<String?>(null)
    val selectedTimelineFilter: StateFlow<MastodonProfileTimelineFilter> = selectedFilter.asStateFlow()

    private val mastodonAccount: Flow<Account?> =
        combine(accountStore.accounts, selectedAccountId) { accounts, activeAccountId ->
            accounts.firstOrNull {
                it.platform == PlatformType.MASTODON &&
                    it.id == activeAccountId &&
                    !it.accessToken.isNullOrBlank()
            } ?: accounts.firstOrNull {
                    it.platform == PlatformType.MASTODON && !it.accessToken.isNullOrBlank()
                }
        }
            .distinctUntilChanged()

    val uiState: StateFlow<MastodonProfileUiState> =
        combine(mastodonAccount, selectedFilter) { account, filter -> account to filter }
            .flatMapLatest { (account, filter) ->
                flow {
                    if (account == null) {
                        emit(MastodonProfileUiState.NoAccount)
                        return@flow
                    }
                    emit(MastodonProfileUiState.Loading)
                    when (val result = mastodonRepository.getOwnProfile(account)) {
                        is AppResult.Success -> emit(
                            MastodonProfileUiState.Success(
                                profile = MastodonProfileMapper.domainToUi(result.data),
                                selectedFilter = filter,
                            ),
                        )
                        is AppResult.Failure -> emit(
                            MastodonProfileUiState.Error(result.error.userMessage()),
                        )
                    }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = MastodonProfileUiState.Loading,
            )

    val profilePosts: Flow<PagingData<MastodonPostUiModel>> =
        combine(mastodonAccount, selectedFilter) { account, filter -> account to filter }
            .flatMapLatest { (account, filter) ->
                if (account == null) {
                    flow { emit(PagingData.empty()) }
                } else {
                    mastodonRepository.getAccountStatusesPagingData(
                        account = account,
                        accountId = account.mastodonRemoteId(),
                        filter = filter,
                    )
                }
            }
            .map { pagingData -> pagingData.map(MastodonTimelineMapper::domainToUi) }
            .cachedIn(viewModelScope)

    fun selectFilter(filter: MastodonProfileTimelineFilter) {
        viewModelScope.launch {
            selectedFilter.emit(filter)
        }
    }

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
    }

    private fun Account.mastodonRemoteId(): String =
        id.substringAfterLast("-")
}
