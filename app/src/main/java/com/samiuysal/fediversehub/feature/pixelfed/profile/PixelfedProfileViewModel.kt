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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class PixelfedProfileViewModel @Inject constructor(
    accountStore: AccountStore,
    private val pixelfedRepository: PixelfedRepository,
) : ViewModel() {
    private val selectedAccountId = MutableStateFlow<String?>(null)
    private val remoteAccountId = MutableStateFlow<String?>(null)
    private val _followState = MutableStateFlow(PixelfedFollowUiState())
    val followState: StateFlow<PixelfedFollowUiState> = _followState.asStateFlow()
    private var currentAccount: Account? = null

    private val pixelfedAccount: Flow<Account?> =
        combine(accountStore.accounts, selectedAccountId, remoteAccountId) { accounts, activeAccountId, remoteId ->
            if (remoteId != null) {
                accounts.firstOrNull {
                    it.platform == PlatformType.PIXELFED && it.id == activeAccountId
                } ?: accounts.firstOrNull {
                    it.platform == PlatformType.PIXELFED
                } ?: publicPixelfedAccount()
            } else {
                accounts.firstOrNull {
                    it.platform == PlatformType.PIXELFED &&
                        it.id == activeAccountId &&
                        !it.accessToken.isNullOrBlank()
                } ?: accounts.firstOrNull {
                    it.platform == PlatformType.PIXELFED && !it.accessToken.isNullOrBlank()
                }
            }
        }.distinctUntilChanged()

    val uiState: StateFlow<PixelfedProfileUiState> =
        combine(pixelfedAccount, remoteAccountId) { account, remoteId -> account to remoteId }
            .flatMapLatest { (account, remoteId) ->
                flow {
                    currentAccount = account
                    if (account == null) {
                        _followState.value = PixelfedFollowUiState()
                        emit(PixelfedProfileUiState.NoAccount)
                        return@flow
                    }
                    emit(PixelfedProfileUiState.Loading)
                    val result = if (remoteId == null) {
                        pixelfedRepository.getOwnProfile(account)
                    } else {
                        pixelfedRepository.getProfile(account, remoteId)
                    }
                    _followState.value = PixelfedFollowUiState(isOwnProfile = remoteId == null)
                    if (remoteId != null && !account.accessToken.isNullOrBlank()) {
                        when (val relationship = pixelfedRepository.getRelationship(account, remoteId)) {
                            is AppResult.Success -> _followState.value = PixelfedFollowUiState(
                                isOwnProfile = false,
                                isFollowing = relationship.data.following || relationship.data.requested,
                            )
                            is AppResult.Failure -> _followState.value = PixelfedFollowUiState(
                                isOwnProfile = false,
                                errorMessage = relationship.error.userMessage(),
                            )
                        }
                    }
                    when (result) {
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
        combine(pixelfedAccount, remoteAccountId) { account, remoteId -> account to remoteId }
            .flatMapLatest { (account, remoteId) ->
                if (account == null) {
                    flow { emit(PagingData.empty()) }
                } else if (remoteId != null) {
                    pixelfedRepository.getProfileMediaPagingData(account, remoteId)
                } else {
                    pixelfedRepository.getProfileMediaPagingData(account)
                }
            }
            .map { pagingData -> pagingData.map(PixelfedMapper::postToUi) }
            .cachedIn(viewModelScope)

    fun selectAccount(account: Account?) {
        remoteAccountId.value = null
        _followState.value = PixelfedFollowUiState()
        selectedAccountId.value = account?.id
    }

    fun selectRemoteAccount(account: Account?, accountId: String) {
        remoteAccountId.value = accountId
        selectedAccountId.value = account?.takeIf { it.platform == PlatformType.PIXELFED }?.id
    }

    fun toggleFollow() {
        val account = currentAccount ?: return
        val targetId = remoteAccountId.value ?: return
        val current = _followState.value
        if (current.isOwnProfile || current.isLoading || account.accessToken.isNullOrBlank()) return
        val targetFollowing = !current.isFollowing
        _followState.value = current.copy(
            isFollowing = targetFollowing,
            isLoading = true,
            errorMessage = null,
        )
        viewModelScope.launch {
            when (val result = pixelfedRepository.setFollowing(account, targetId, targetFollowing)) {
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

    private fun publicPixelfedAccount(): Account =
        Account(
            id = "public-pixelfed-social",
            platform = PlatformType.PIXELFED,
            instanceUrl = "pixelfed.social",
            username = "public",
            displayName = "Pixelfed",
            avatarUrl = null,
            accessToken = null,
        )
}

data class PixelfedFollowUiState(
    val isOwnProfile: Boolean = true,
    val isFollowing: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)
