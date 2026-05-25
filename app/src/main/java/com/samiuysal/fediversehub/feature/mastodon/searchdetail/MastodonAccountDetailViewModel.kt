package com.samiuysal.fediversehub.feature.mastodon.searchdetail

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
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
import com.samiuysal.fediversehub.feature.mastodon.profile.MastodonProfileUiModel
import com.samiuysal.fediversehub.navigation.AppDestination
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
import kotlinx.coroutines.flow.update

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class MastodonAccountDetailViewModel @Inject constructor(
    private val accountStore: AccountStore,
    private val mastodonRepository: MastodonRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val accountId: String = Uri.decode(checkNotNull(savedStateHandle[AppDestination.ACCOUNT_ID_ARGUMENT]))
    private val selectedFilter = MutableStateFlow(MastodonProfileTimelineFilter.POSTS)
    private val retrySignal = MutableStateFlow(0)
    private val _followState = MutableStateFlow(MastodonFollowUiState())
    private var currentAccount: Account? = null

    val selectedTimelineFilter: StateFlow<MastodonProfileTimelineFilter> = selectedFilter.asStateFlow()
    val followState: StateFlow<MastodonFollowUiState> = _followState.asStateFlow()

    private val mastodonAccount: Flow<Account?> = accountStore.accounts
        .map { accounts ->
            accounts.firstOrNull {
                it.platform == PlatformType.MASTODON && !it.accessToken.isNullOrBlank()
            }
        }
        .distinctUntilChanged()

    val uiState: StateFlow<MastodonAccountDetailUiState> = combine(
        mastodonAccount,
        retrySignal,
    ) { account, _ -> account }
        .flatMapLatest { account ->
            flow {
                currentAccount = account
                if (account == null) {
                    _followState.value = MastodonFollowUiState()
                    emit(MastodonAccountDetailUiState.NoAccount)
                    return@flow
                }
                emit(MastodonAccountDetailUiState.Loading)
                when (val result = mastodonRepository.getProfile(account, accountId)) {
                    is AppResult.Success -> {
                        val profile = MastodonProfileMapper.domainToUi(result.data)
                        val isOwnProfile = accountId == account.id.substringAfterLast("-")
                        _followState.value = MastodonFollowUiState(isOwnProfile = isOwnProfile)
                        if (!isOwnProfile) {
                            when (val relationship = mastodonRepository.getRelationship(account, accountId)) {
                                is AppResult.Success -> _followState.value = MastodonFollowUiState(
                                    isOwnProfile = false,
                                    isFollowing = relationship.data.following || relationship.data.requested,
                                )
                                is AppResult.Failure -> _followState.value = MastodonFollowUiState(
                                    isOwnProfile = false,
                                    errorMessage = relationship.error.userMessage(),
                                )
                            }
                        }
                        emit(MastodonAccountDetailUiState.Success(profile))
                    }
                    is AppResult.Failure -> emit(
                        MastodonAccountDetailUiState.Error(result.error.userMessage()),
                    )
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = MastodonAccountDetailUiState.Loading,
        )

    val posts: Flow<PagingData<MastodonPostUiModel>> =
        mastodonAccount
            .flatMapLatest { account ->
                if (account == null) {
                    flow { emit(PagingData.empty()) }
                } else {
                    selectedFilter.flatMapLatest { filter ->
                        mastodonRepository.getAccountStatusesPagingData(
                            account = account,
                            accountId = accountId,
                            filter = filter,
                        )
                    }
                }
            }
            .map { pagingData -> pagingData.map(MastodonTimelineMapper::domainToUi) }
            .cachedIn(viewModelScope)

    fun selectFilter(filter: MastodonProfileTimelineFilter) {
        viewModelScope.launch {
            selectedFilter.emit(filter)
        }
    }

    fun retry() {
        retrySignal.value += 1
    }

    fun toggleFollow() {
        val account = currentAccount ?: return
        val current = _followState.value
        if (current.isOwnProfile || current.isLoading) return
        val targetFollowing = !current.isFollowing
        _followState.value = current.copy(
            isFollowing = targetFollowing,
            isLoading = true,
            errorMessage = null,
        )
        viewModelScope.launch {
            when (val result = mastodonRepository.setFollowing(account, accountId, targetFollowing)) {
                is AppResult.Success -> _followState.value = _followState.value.copy(
                    isFollowing = result.data.following || result.data.requested,
                    isLoading = false,
                )
                is AppResult.Failure -> _followState.update {
                    it.copy(
                        isFollowing = current.isFollowing,
                        isLoading = false,
                        errorMessage = result.error.userMessage(),
                    )
                }
            }
        }
    }
}

sealed interface MastodonAccountDetailUiState {
    data object Loading : MastodonAccountDetailUiState
    data object NoAccount : MastodonAccountDetailUiState
    data class Success(val profile: MastodonProfileUiModel) : MastodonAccountDetailUiState
    data class Error(val message: String) : MastodonAccountDetailUiState
}

data class MastodonFollowUiState(
    val isOwnProfile: Boolean = true,
    val isFollowing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
