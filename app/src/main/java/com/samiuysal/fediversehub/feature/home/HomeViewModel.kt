package com.samiuysal.fediversehub.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mastodonRepository: MastodonRepository,
    private val lemmyRepository: LemmyRepository,
    private val accountStore: AccountStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MockFediverseData.homeState)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    val mastodonTimeline: Flow<PagingData<MastodonPostUiModel>> =
        uiState
            .map { state ->
                state.accounts.firstOrNull { it.platform == PlatformType.MASTODON }
                    ?: fallbackAccount(PlatformType.MASTODON)
            }
            .distinctUntilChanged()
            .flatMapLatest { account ->
                mastodonRepository
                    .getHomeTimelinePagingData(account = account)
                    .map { pagingData ->
                        pagingData.map(MastodonTimelineMapper::domainToUi)
                    }
            }
            .cachedIn(viewModelScope)

    val lemmyPosts: Flow<PagingData<LemmyPostUiModel>> =
        lemmyRepository
            .getPostsPagingData(
                account = _uiState.value.accounts.first { it.platform == PlatformType.LEMMY },
                sort = LemmySortType.HOT,
            )
            .map { pagingData ->
                pagingData.map(LemmyPostMapper::domainToUi)
            }
            .cachedIn(viewModelScope)

    init {
        accountStore.accounts
            .onEach { storedAccounts ->
                _uiState.update { state ->
                    state.copy(accounts = mergeAccounts(storedAccounts))
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.PlatformSelected -> selectPlatform(event.platform)
        }
    }

    private fun selectPlatform(platform: PlatformType) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }

    private fun mergeAccounts(storedAccounts: List<Account>): List<Account> {
        val fallbackAccounts = MockFediverseData.homeState.accounts
            .filterNot { fallback ->
                storedAccounts.any { it.platform == fallback.platform }
            }
        return storedAccounts + fallbackAccounts
    }

    private fun fallbackAccount(platform: PlatformType): Account =
        MockFediverseData.homeState.accounts.first { it.platform == platform }
}
