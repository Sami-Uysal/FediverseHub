package com.samiuysal.fediversehub.feature.lemmy.community

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class LemmyCommunityViewModel @Inject constructor(
    private val lemmyRepository: LemmyRepository,
    private val accountStore: AccountStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val communityName: String = checkNotNull(savedStateHandle[AppDestination.COMMUNITY_NAME_ARGUMENT])
    private val _uiState = MutableStateFlow<LemmyCommunityUiState>(LemmyCommunityUiState.Loading)
    val uiState: StateFlow<LemmyCommunityUiState> = _uiState.asStateFlow()
    private val _sort = MutableStateFlow(LemmySortType.HOT)
    val sort: StateFlow<LemmySortType> = _sort.asStateFlow()
    private val _effects = MutableSharedFlow<LemmyCommunityEffect>()
    val effects: SharedFlow<LemmyCommunityEffect> = _effects.asSharedFlow()

    val posts: Flow<PagingData<LemmyPostUiModel>> =
        combine(accountStore.accounts, accountStore.activeAccountIds, _sort) { accounts, activeIds, sort ->
            Triple(accounts.lemmyAccount(activeIds), activeIds, sort)
        }
            .distinctUntilChanged()
            .flatMapLatest { (account, _, sort) ->
                if (account == null) {
                    flowOf(PagingData.empty())
                } else {
                    lemmyRepository
                        .getPostsPagingData(
                            account = account,
                            sort = sort,
                            feedType = LemmyFeedType.ALL,
                            communityName = communityName,
                        )
                        .map { pagingData -> pagingData.map(LemmyPostMapper::domainToUi) }
                }
            }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    init {
        load()
    }

    fun retry() {
        load()
    }

    fun selectSort(sort: LemmySortType) {
        _sort.value = sort
    }

    fun toggleFollow() {
        val current = _uiState.value as? LemmyCommunityUiState.Success ?: return
        val before = current.community
        if (before.id.isBlank() || before.isFollowLoading) return
        val optimistic = before.copy(
            isSubscribed = !before.isSubscribed,
            isFollowLoading = true,
        )
        _uiState.value = current.copy(community = optimistic)
        viewModelScope.launch {
            when (
                val result = lemmyRepository.followCommunity(
                    account = lemmyAccount(),
                    communityId = before.id,
                    follow = optimistic.isSubscribed,
                )
            ) {
                is AppResult.Success -> {
                    _uiState.value = current.copy(
                        community = LemmyPostMapper.communityToUi(result.data).copy(isFollowLoading = false),
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = current.copy(community = before.copy(isFollowLoading = false))
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(LemmyCommunityEffect.NavigateToLogin)
                    }
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = LemmyCommunityUiState.Loading
            when (val result = lemmyRepository.getCommunity(lemmyAccount(), communityName)) {
                is AppResult.Success -> {
                    _uiState.value = LemmyCommunityUiState.Success(
                        community = LemmyPostMapper.communityToUi(result.data),
                    )
                }
                is AppResult.Failure -> {
                    _uiState.value = LemmyCommunityUiState.Error(result.error.lemmyMessage())
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(LemmyCommunityEffect.NavigateToLogin)
                    }
                }
            }
        }
    }

    private suspend fun lemmyAccount(): Account =
        accountStore.accounts.first().lemmyAccount(accountStore.activeAccountIds.first())
            ?: Account(
                id = "lemmy-community-preview",
                platform = PlatformType.LEMMY,
                instanceUrl = "lemmy.world",
                username = "preview",
                displayName = "Lemmy",
                avatarUrl = null,
                accessToken = null,
            )

    private fun List<Account>.lemmyAccount(activeIds: Map<PlatformType, String>): Account? =
        firstOrNull {
            it.platform == PlatformType.LEMMY &&
                it.id == activeIds[PlatformType.LEMMY] &&
                !it.accessToken.isNullOrBlank()
        } ?: firstOrNull {
            it.platform == PlatformType.LEMMY && !it.accessToken.isNullOrBlank()
        }
}

sealed interface LemmyCommunityEffect {
    data object NavigateToLogin : LemmyCommunityEffect
}

private fun AppError.lemmyMessage(): String = when (this) {
    AppError.Network -> "Ağ hatası. Bağlantıyı kontrol et."
    AppError.RateLimited -> "Çok hızlı istek atıldı. Biraz bekle."
    AppError.Unauthorized -> "Oturum süresi doldu. Lemmy hesabına tekrar giriş yap."
    is AppError.Server -> "Sunucu hatası $code. Biraz sonra tekrar dene."
    is AppError.Unknown -> message ?: "Community yüklenemedi."
}
