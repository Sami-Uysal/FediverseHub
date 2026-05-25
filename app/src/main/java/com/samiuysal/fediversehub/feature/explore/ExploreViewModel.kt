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
import com.samiuysal.fediversehub.core.performance.PerfLogger
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
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
    private val lemmyRepository: LemmyRepository,
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
    private val _lemmyTab = MutableStateFlow(LemmyExploreTab.ALL)
    val lemmyTab: StateFlow<LemmyExploreTab> = _lemmyTab.asStateFlow()
    private val _lemmySort = MutableStateFlow(LemmySortType.HOT)
    val lemmySort: StateFlow<LemmySortType> = _lemmySort.asStateFlow()
    private val _lemmyCommunitiesState = MutableStateFlow(LemmyCommunitiesUiState())
    val lemmyCommunitiesState: StateFlow<LemmyCommunitiesUiState> = _lemmyCommunitiesState.asStateFlow()
    private var currentLemmyAccount: Account? = null

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
                } ?: PlatformType.PIXELFED.publicExploreAccount()
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

    private val lemmyAccount: Flow<Account?> =
        combine(accounts, selectedAccountId, selectedPlatform) { accounts, selectedId, platform ->
            if (platform != PlatformType.LEMMY) {
                null
            } else {
                accounts.firstOrNull {
                    it.platform == PlatformType.LEMMY &&
                        it.id == selectedId &&
                        !it.accessToken.isNullOrBlank()
                } ?: accounts.firstOrNull {
                    it.platform == PlatformType.LEMMY && !it.accessToken.isNullOrBlank()
                } ?: PlatformType.LEMMY.publicExploreAccount()
            }
        }.distinctUntilChanged()

    val lemmyExploreFeed: Flow<PagingData<LemmyPostUiModel>> =
        combine(lemmyAccount, _lemmySort, _lemmyTab) { account, sort, tab ->
            Triple(account, sort, tab)
        }
            .distinctUntilChanged()
            .flatMapLatest { (account, sort, tab) ->
                if (account == null) {
                    flowOf(PagingData.empty())
                } else {
                    lemmyRepository
                        .getPostsPagingData(
                            account = account,
                            sort = sort,
                            feedType = tab.feedType,
                        )
                        .map { pagingData -> pagingData.map(LemmyPostMapper::domainToUi) }
                }
            }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    fun selectPlatform(platform: PlatformType) {
        selectedPlatform.value = platform
        if (platform == PlatformType.MASTODON && currentMastodonAccount == null) {
            currentMastodonAccount = PlatformType.MASTODON.publicExploreAccount()
            loadMastodonTab(currentMastodonAccount ?: return, _mastodonState.value.selectedTab)
        }
        if (platform == PlatformType.LEMMY && currentLemmyAccount == null && _lemmyTab.value == LemmyExploreTab.COMMUNITIES) {
            currentLemmyAccount = PlatformType.LEMMY.publicExploreAccount()
            loadLemmyCommunities(currentLemmyAccount)
        }
    }

    fun selectAccount(account: Account?) {
        selectedAccountId.value = account?.id
        if (selectedPlatform.value == PlatformType.MASTODON) {
            val mastodonAccount = account
                ?.takeIf { it.platform == PlatformType.MASTODON && !it.accessToken.isNullOrBlank() }
                ?: PlatformType.MASTODON.publicExploreAccount()
            currentMastodonAccount = mastodonAccount
            loadMastodonTab(mastodonAccount, _mastodonState.value.selectedTab)
        }
        if (selectedPlatform.value == PlatformType.LEMMY) {
            currentLemmyAccount = account
                ?.takeIf { it.platform == PlatformType.LEMMY && !it.accessToken.isNullOrBlank() }
                ?: PlatformType.LEMMY.publicExploreAccount()
        }
        if (_lemmyTab.value == LemmyExploreTab.COMMUNITIES) {
            loadLemmyCommunities(currentLemmyAccount)
        }
    }

    fun selectMastodonTab(tab: MastodonExploreTab) {
        _mastodonState.update { it.copy(selectedTab = tab) }
        currentMastodonAccount?.let { loadMastodonTab(it, tab) }
    }

    fun selectLemmyTab(tab: LemmyExploreTab) {
        _lemmyTab.value = tab
        if (tab == LemmyExploreTab.COMMUNITIES) {
            loadLemmyCommunities(currentLemmyAccount)
        }
    }

    fun selectLemmySort(sort: LemmySortType) {
        _lemmySort.value = sort
        if (_lemmyTab.value == LemmyExploreTab.COMMUNITIES) {
            loadLemmyCommunities(currentLemmyAccount)
        }
    }

    fun refreshLemmyCommunities() {
        loadLemmyCommunities(currentLemmyAccount)
    }

    fun refreshMastodon(account: Account?) {
        val mastodonAccount = account
            ?.takeIf { it.platform == PlatformType.MASTODON && !it.accessToken.isNullOrBlank() }
            ?: currentMastodonAccount
            ?: PlatformType.MASTODON.publicExploreAccount()
        loadedTabsByAccount[mastodonAccount.id]?.remove(_mastodonState.value.selectedTab)
        loadMastodonTab(mastodonAccount, _mastodonState.value.selectedTab)
    }

    private fun loadMastodonTab(account: Account, tab: MastodonExploreTab) {
        val loadedTabs = loadedTabsByAccount.getOrPut(account.id) { mutableSetOf() }
        if (tab in loadedTabs) {
            restoreCachedTab(account.id, tab)
            return
        }
        viewModelScope.launch {
            val perfMark = PerfLogger.mark("explore_${tab.name.lowercase()}_load")
            _mastodonState.update { it.copy(loadingTab = tab, errorMessage = null) }
            when (tab) {
                MastodonExploreTab.POSTS -> loadPosts(account)
                MastodonExploreTab.TAGS -> loadTags(account)
                MastodonExploreTab.LINKS -> loadLinks(account)
            }
            PerfLogger.end(perfMark, account.instanceUrl)
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

    private fun loadLemmyCommunities(account: Account?) {
        val targetAccount = account ?: PlatformType.LEMMY.publicExploreAccount()
        viewModelScope.launch {
            val perfMark = PerfLogger.mark("lemmy_communities_load")
            _lemmyCommunitiesState.value = LemmyCommunitiesUiState(isLoading = true)
            when (
                val result = withContext(Dispatchers.IO) {
                    lemmyRepository.getCommunities(
                        account = targetAccount,
                        page = LemmyPostPage(
                            sort = _lemmySort.value,
                            feedType = LemmyFeedType.ALL,
                        ),
                    )
                }
            ) {
                is AppResult.Success -> {
                    val communities: List<LemmyCommunityUiModel> = withContext(Dispatchers.Default) {
                        result.data.map(LemmyPostMapper::communityToUi)
                    }
                    _lemmyCommunitiesState.value = LemmyCommunitiesUiState(communities = communities)
                }
                is AppResult.Failure -> {
                    _lemmyCommunitiesState.value = LemmyCommunitiesUiState(errorMessage = result.error.userMessage())
                }
            }
            PerfLogger.end(perfMark, targetAccount.instanceUrl)
        }
    }
}

private val LemmyExploreTab.feedType: LemmyFeedType
    get() = when (this) {
        LemmyExploreTab.ALL -> LemmyFeedType.ALL
        LemmyExploreTab.LOCAL -> LemmyFeedType.LOCAL
        LemmyExploreTab.COMMUNITIES -> LemmyFeedType.ALL
    }

private fun PlatformType.publicExploreAccount(): Account =
    when (this) {
        PlatformType.MASTODON -> Account(
            id = "public-mastodon-mastodon.social",
            platform = PlatformType.MASTODON,
            instanceUrl = "mastodon.social",
            username = "public",
            displayName = "Mastodon Public",
            avatarUrl = null,
            accessToken = null,
        )
        PlatformType.PIXELFED -> Account(
            id = "public-pixelfed-pixelfed.social",
            platform = PlatformType.PIXELFED,
            instanceUrl = "pixelfed.social",
            username = "public",
            displayName = "Pixelfed Public",
            avatarUrl = null,
            accessToken = null,
        )
        PlatformType.LEMMY -> Account(
            id = "public-lemmy-lemmy.world",
            platform = PlatformType.LEMMY,
            instanceUrl = "lemmy.world",
            username = "public",
            displayName = "Lemmy Public",
            avatarUrl = null,
            accessToken = null,
        )
    }
