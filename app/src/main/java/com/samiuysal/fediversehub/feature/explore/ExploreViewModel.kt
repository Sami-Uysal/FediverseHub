package com.samiuysal.fediversehub.feature.explore

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
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val mastodonRepository: MastodonRepository,
    private val pixelfedRepository: PixelfedRepository,
    accountStore: AccountStore,
) : ViewModel() {
    private val selectedPlatform = MutableStateFlow(PlatformType.MASTODON)
    private val selectedAccountId = MutableStateFlow<String?>(null)
    private val accounts = accountStore.accounts
    private var currentMastodonAccount: Account? = null
    private val loadedTabsByAccount = mutableMapOf<String, MutableSet<MastodonExploreTab>>()
    private val postsCache = mutableMapOf<String, List<com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel>>()
    private val tagsCache = mutableMapOf<String, List<com.samiuysal.fediversehub.feature.mastodon.domain.MastodonHashtag>>()
    private val linksCache = mutableMapOf<String, List<com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTrendLink>>()

    private val _mastodonState = MutableStateFlow(MastodonExploreUiState())
    val mastodonState: StateFlow<MastodonExploreUiState> = _mastodonState.asStateFlow()

    private val pixelfedAccount: Flow<Account?> =
        combine(accounts, selectedAccountId, selectedPlatform) { accounts, selectedId, platform ->
            if (platform != PlatformType.PIXELFED) {
                null
            } else {
                accounts.firstOrNull {
                    it.platform == PlatformType.PIXELFED &&
                        it.id == selectedId &&
                        !it.accessToken.isNullOrBlank()
                } ?: accounts.firstOrNull {
                    it.platform == PlatformType.PIXELFED && !it.accessToken.isNullOrBlank()
                }
            }
        }.distinctUntilChanged()

    val pixelfedExploreFeed: Flow<PagingData<PixelfedPostUiModel>> =
        pixelfedAccount
            .flatMapLatest { account ->
                if (account == null) {
                    flowOf(PagingData.empty())
                } else {
                    pixelfedRepository.getExploreFeedPagingData(account)
                }
            }
            .map { pagingData -> pagingData.map(PixelfedMapper::postToUi) }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    fun selectPlatform(platform: PlatformType) {
        selectedPlatform.value = platform
    }

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
        if (account?.platform == PlatformType.MASTODON && !account.accessToken.isNullOrBlank()) {
            currentMastodonAccount = account
            loadMastodonTab(account, _mastodonState.value.selectedTab)
        } else {
            currentMastodonAccount = null
        }
    }

    fun selectMastodonTab(tab: MastodonExploreTab) {
        _mastodonState.update { it.copy(selectedTab = tab) }
        currentMastodonAccount?.let { loadMastodonTab(it, tab) }
    }

    fun refreshMastodon(account: Account?) {
        if (account?.platform == PlatformType.MASTODON && !account.accessToken.isNullOrBlank()) {
            loadedTabsByAccount[account.id]?.remove(_mastodonState.value.selectedTab)
            loadMastodonTab(account, _mastodonState.value.selectedTab)
        }
    }

    private fun loadMastodonTab(account: Account, tab: MastodonExploreTab) {
        val loadedTabs = loadedTabsByAccount.getOrPut(account.id) { mutableSetOf() }
        if (tab in loadedTabs) {
            restoreCachedTab(account.id, tab)
            return
        }
        viewModelScope.launch {
            _mastodonState.update { it.copy(loadingTab = tab, errorMessage = null) }
            when (tab) {
                MastodonExploreTab.POSTS -> loadPosts(account)
                MastodonExploreTab.TAGS -> loadTags(account)
                MastodonExploreTab.LINKS -> loadLinks(account)
            }
        }
    }

    private suspend fun loadPosts(account: Account) {
        when (val result = withContext(Dispatchers.IO) { mastodonRepository.getTrendingStatuses(account) }) {
            is AppResult.Success -> {
                val posts = withContext(Dispatchers.Default) {
                    result.data.map(MastodonTimelineMapper::domainToUi)
                }
                postsCache[account.id] = posts
                markLoaded(account.id, MastodonExploreTab.POSTS)
                _mastodonState.update { it.copy(loadingTab = null, posts = posts) }
            }
            is AppResult.Failure -> showError(result)
        }
    }

    private suspend fun loadTags(account: Account) {
        when (val result = withContext(Dispatchers.IO) { mastodonRepository.getTrendingTags(account) }) {
            is AppResult.Success -> {
                tagsCache[account.id] = result.data
                markLoaded(account.id, MastodonExploreTab.TAGS)
                _mastodonState.update { it.copy(loadingTab = null, tags = result.data) }
            }
            is AppResult.Failure -> showError(result)
        }
    }

    private suspend fun loadLinks(account: Account) {
        when (val result = withContext(Dispatchers.IO) { mastodonRepository.getTrendingLinks(account) }) {
            is AppResult.Success -> {
                linksCache[account.id] = result.data
                markLoaded(account.id, MastodonExploreTab.LINKS)
                _mastodonState.update { it.copy(loadingTab = null, links = result.data) }
            }
            is AppResult.Failure -> showError(result)
        }
    }

    private fun restoreCachedTab(accountId: String, tab: MastodonExploreTab) {
        _mastodonState.update { state ->
            when (tab) {
                MastodonExploreTab.POSTS -> state.copy(
                    loadingTab = null,
                    errorMessage = null,
                    posts = postsCache[accountId].orEmpty(),
                )
                MastodonExploreTab.TAGS -> state.copy(
                    loadingTab = null,
                    errorMessage = null,
                    tags = tagsCache[accountId].orEmpty(),
                )
                MastodonExploreTab.LINKS -> state.copy(
                    loadingTab = null,
                    errorMessage = null,
                    links = linksCache[accountId].orEmpty(),
                )
            }
        }
    }

    private fun markLoaded(accountId: String, tab: MastodonExploreTab) {
        loadedTabsByAccount.getOrPut(accountId) { mutableSetOf() }.add(tab)
    }

    private fun showError(result: AppResult.Failure) {
        _mastodonState.update {
            it.copy(
                loadingTab = null,
                errorMessage = result.error.userMessage(),
            )
        }
    }
}
