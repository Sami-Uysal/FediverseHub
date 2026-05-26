package com.samiuysal.fediversehub.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import android.util.Log
import com.samiuysal.fediversehub.BuildConfig
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.core.performance.PerfLogger
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostActionType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.community.LemmyPostComposeType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyFeedType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostActionType
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonNewPostComposeState
import com.samiuysal.fediversehub.feature.mastodon.MastodonReplyComposeState
import com.samiuysal.fediversehub.feature.mastodon.MastodonVisibility
import com.samiuysal.fediversehub.feature.mastodon.canSend
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedCommentsState
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedRepository
import com.samiuysal.fediversehub.feature.pixelfed.mapper.PixelfedMapper
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
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mastodonRepository: MastodonRepository,
    private val lemmyRepository: LemmyRepository,
    private val pixelfedRepository: PixelfedRepository,
    private val accountStore: AccountStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val _effects = MutableSharedFlow<HomeEffect>()
    val effects: SharedFlow<HomeEffect> = _effects.asSharedFlow()
    private val _mastodonActionOverrides = MutableStateFlow<Map<String, MastodonPostUiModel>>(emptyMap())
    val mastodonActionOverrides: StateFlow<Map<String, MastodonPostUiModel>> =
        _mastodonActionOverrides.asStateFlow()
    private val _replyComposeState = MutableStateFlow<MastodonReplyComposeState?>(null)
    val replyComposeState: StateFlow<MastodonReplyComposeState?> = _replyComposeState.asStateFlow()
    private val _newPostComposeState = MutableStateFlow<MastodonNewPostComposeState?>(null)
    val newPostComposeState: StateFlow<MastodonNewPostComposeState?> = _newPostComposeState.asStateFlow()
    private val _pixelfedPostComposerState = MutableStateFlow(PixelfedPostComposerUiState())
    val pixelfedPostComposerState: StateFlow<PixelfedPostComposerUiState> =
        _pixelfedPostComposerState.asStateFlow()
    private val _lemmyPostComposerState = MutableStateFlow(LemmyHomePostComposerUiState())
    val lemmyPostComposerState: StateFlow<LemmyHomePostComposerUiState> =
        _lemmyPostComposerState.asStateFlow()
    private val _pixelfedActionOverrides = MutableStateFlow<Map<String, PixelfedPostUiModel>>(emptyMap())
    val pixelfedActionOverrides: StateFlow<Map<String, PixelfedPostUiModel>> =
        _pixelfedActionOverrides.asStateFlow()
    private val _pixelfedCommentsState = MutableStateFlow<PixelfedCommentsState?>(null)
    val pixelfedCommentsState: StateFlow<PixelfedCommentsState?> =
        _pixelfedCommentsState.asStateFlow()
    private val _lemmySort = MutableStateFlow(LemmySortType.HOT)
    val lemmySort: StateFlow<LemmySortType> = _lemmySort.asStateFlow()
    private val _lemmyActionOverrides = MutableStateFlow<Map<String, LemmyPostUiModel>>(emptyMap())
    val lemmyActionOverrides: StateFlow<Map<String, LemmyPostUiModel>> =
        _lemmyActionOverrides.asStateFlow()

    val mastodonTimeline: Flow<PagingData<MastodonPostUiModel>> =
        uiState
            .map { state ->
                state.activeAccount(PlatformType.MASTODON)
                    ?.takeIf { !it.accessToken.isNullOrBlank() }
            }
            .distinctUntilChanged()
            .flatMapLatest { account ->
                if (account == null) {
                    flowOf(PagingData.empty())
                } else {
                    PerfLogger.log("mastodon_home_paging_start", account.instanceUrl)
                    mastodonRepository
                        .getHomeTimelinePagingData(account = account)
                        .map { pagingData ->
                            pagingData.map(MastodonTimelineMapper::domainToUi)
                        }
                    }
                }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    val lemmyPosts: Flow<PagingData<LemmyPostUiModel>> =
        combine(uiState, _lemmySort) { state, sort ->
            state.activeAccount(PlatformType.LEMMY)
                ?.takeIf { !it.accessToken.isNullOrBlank() } to sort
        }
            .distinctUntilChanged()
            .flatMapLatest { (account, sort) ->
                if (account == null) {
                    flowOf(PagingData.empty())
                } else {
                    PerfLogger.log("lemmy_home_paging_start", account.instanceUrl)
                    lemmyRepository
                        .getPostsPagingData(
                            account = account,
                            sort = sort,
                            feedType = LemmyFeedType.SUBSCRIBED,
                        )
                        .map { pagingData -> pagingData.map(LemmyPostMapper::domainToUi) }
                }
            }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    val pixelfedFeed: Flow<PagingData<PixelfedPostUiModel>> =
        uiState
            .map { state ->
                state.activeAccount(PlatformType.PIXELFED)
                    ?.takeIf { !it.accessToken.isNullOrBlank() }
            }
            .distinctUntilChanged()
            .flatMapLatest { account ->
                if (account == null) {
                    debugLog("Pixelfed home skipped: no active token account")
                    flowOf(PagingData.empty())
                } else {
                    debugLog(
                        "Pixelfed home loading: account=${account.id}, instance=${account.instanceUrl}, token=true",
                    )
                    PerfLogger.log("pixelfed_home_paging_start", account.instanceUrl)
                    pixelfedRepository
                        .getHomeFeedPagingData(account)
                        .map { pagingData -> pagingData.map(PixelfedMapper::postToUi) }
                }
            }
            .flowOn(Dispatchers.Default)
            .cachedIn(viewModelScope)

    init {
        combine(accountStore.accounts, accountStore.activeAccountIds) { storedAccounts, activeAccountIds ->
            storedAccounts to activeAccountIds
        }
            .onEach { (storedAccounts, activeAccountIds) ->
                val previousState = _uiState.value
                val validActiveIds = activeAccountIds.filter { (platform, activeId) ->
                    storedAccounts.any { it.platform == platform && it.id == activeId }
                }
                val nextState = previousState.copy(
                    accounts = storedAccounts,
                    activeAccountIds = validActiveIds,
                )
                PlatformType.entries.forEach { platform ->
                    if (previousState.activeAccount(platform)?.id != nextState.activeAccount(platform)?.id) {
                        clearPlatformTransientState(platform)
                    }
                }
                _uiState.update {
                    nextState
                }
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.PlatformSelected -> selectPlatform(event.platform)
        }
    }

    fun onMastodonAction(
        post: MastodonPostUiModel,
        action: MastodonPostActionType,
    ) {
        if (action == MastodonPostActionType.REPLY) {
            openReplyCompose(post)
            return
        }
        val account = _uiState.value.activeAccount(PlatformType.MASTODON)
            ?: return
        val before = _mastodonActionOverrides.value[post.detailId] ?: post
        val optimistic = before.optimistic(action).copy(loadingAction = action)
        setMastodonOverride(optimistic)
        viewModelScope.launch {
            val result = when (action) {
                MastodonPostActionType.REPLY -> return@launch
                MastodonPostActionType.BOOST -> mastodonRepository.setBoosted(
                    account = account,
                    postId = post.detailId,
                    boosted = optimistic.isBoosted,
                )
                MastodonPostActionType.FAVOURITE -> mastodonRepository.setFavourite(
                    account = account,
                    postId = post.detailId,
                    favourite = optimistic.isFavourited,
                )
                MastodonPostActionType.BOOKMARK -> mastodonRepository.setBookmarked(
                    account = account,
                    postId = post.detailId,
                    bookmarked = optimistic.isBookmarked,
                )
            }
            when (result) {
                is AppResult.Success -> {
                    val confirmed = MastodonTimelineMapper.domainToUi(result.data)
                    setMastodonOverride(optimistic.withActionStateFrom(confirmed).copy(loadingAction = null))
                }
                is AppResult.Failure -> {
                    setMastodonOverride(before.copy(loadingAction = null))
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    fun onReplyTextChanged(text: String) {
        _replyComposeState.update { state ->
            state?.copy(text = text, errorMessage = null)
        }
    }

    fun dismissReplyCompose() {
        if (_replyComposeState.value?.isSending == true) return
        _replyComposeState.value = null
    }

    fun submitReply() {
        val state = _replyComposeState.value ?: return
        if (!state.canSend) {
            _replyComposeState.value = state.copy(errorMessage = "Reply needs more than a mention.")
            return
        }
        val account = _uiState.value.activeAccount(PlatformType.MASTODON)
            ?: return
        _replyComposeState.value = state.copy(isSending = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = mastodonRepository.replyToPost(
                    account = account,
                    postId = state.parent.detailId,
                    text = state.text.trim(),
                    visibility = state.parent.visibility.ifBlank { "public" },
                )
            ) {
                is AppResult.Success -> {
                    val currentParent = _mastodonActionOverrides.value[state.parent.detailId] ?: state.parent
                    setMastodonOverride(
                        currentParent.copy(
                            replies = (currentParent.replies + 1).coerceAtLeast(0),
                            loadingAction = null,
                        ),
                    )
                    _replyComposeState.value = null
                }
                is AppResult.Failure -> {
                    _replyComposeState.value = state.copy(
                        isSending = false,
                        errorMessage = result.error.replyErrorMessage(),
                    )
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    fun openNewPostCompose() {
        if (_uiState.value.selectedPlatform != PlatformType.MASTODON) return
        _newPostComposeState.value = MastodonNewPostComposeState()
    }

    fun openPlatformComposer(platform: PlatformType = _uiState.value.selectedPlatform) {
        selectPlatform(platform)
        when (platform) {
            PlatformType.MASTODON -> openNewPostCompose()
            PlatformType.PIXELFED -> openPixelfedPostComposer()
            PlatformType.LEMMY -> openLemmyPostComposer()
        }
    }

    fun onNewPostTextChanged(text: String) {
        _newPostComposeState.update { state ->
            state?.copy(text = text, errorMessage = null)
        }
    }

    fun onNewPostVisibilityChanged(visibility: MastodonVisibility) {
        _newPostComposeState.update { state ->
            state?.copy(visibility = visibility, errorMessage = null)
        }
    }

    fun onNewPostContentWarningEnabledChanged(enabled: Boolean) {
        _newPostComposeState.update { state ->
            state?.copy(isContentWarningEnabled = enabled, errorMessage = null)
        }
    }

    fun onNewPostContentWarningChanged(contentWarning: String) {
        _newPostComposeState.update { state ->
            state?.copy(contentWarning = contentWarning, errorMessage = null)
        }
    }

    fun dismissNewPostCompose() {
        if (_newPostComposeState.value?.isSending == true) return
        _newPostComposeState.value = null
    }

    fun submitNewPost() {
        val state = _newPostComposeState.value ?: return
        if (!state.canSend) {
            _newPostComposeState.value = state.copy(errorMessage = "Post cannot be empty.")
            return
        }
        val account = _uiState.value.activeAccount(PlatformType.MASTODON)
            ?: return
        _newPostComposeState.value = state.copy(isSending = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = mastodonRepository.createPost(
                    account = account,
                    text = state.text.trim(),
                    visibility = state.visibility.apiValue,
                    spoilerText = state.contentWarning.takeIf {
                        state.isContentWarningEnabled && it.isNotBlank()
                    },
                )
            ) {
                is AppResult.Success -> {
                    _newPostComposeState.value = null
                }
                is AppResult.Failure -> {
                    _newPostComposeState.value = state.copy(
                        isSending = false,
                        errorMessage = result.error.postErrorMessage(),
                    )
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    fun openPixelfedPostComposer() {
        _pixelfedPostComposerState.value = PixelfedPostComposerUiState(isOpen = true)
    }

    fun dismissPixelfedPostComposer() {
        if (_pixelfedPostComposerState.value.isSubmitting) return
        _pixelfedPostComposerState.value = PixelfedPostComposerUiState()
    }

    fun onPixelfedPostTextChanged(text: String) {
        _pixelfedPostComposerState.update { it.copy(text = text, errorMessage = null) }
    }

    fun submitPixelfedPost() {
        val state = _pixelfedPostComposerState.value
        val text = state.text.trim()
        val account = _uiState.value.activeAccount(PlatformType.PIXELFED)
        when {
            account == null -> {
                _pixelfedPostComposerState.value = state.copy(errorMessage = "Pixelfed hesabı gerekli.")
                return
            }
            text.isBlank() -> {
                _pixelfedPostComposerState.value = state.copy(errorMessage = "Gönderi boş olamaz.")
                return
            }
        }
        _pixelfedPostComposerState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            when (val result = pixelfedRepository.createPost(account, text)) {
                is AppResult.Success -> {
                    _pixelfedPostComposerState.value = PixelfedPostComposerUiState()
                }
                is AppResult.Failure -> {
                    _pixelfedPostComposerState.update {
                        it.copy(isSubmitting = false, errorMessage = result.error.userMessage("Pixelfed gönderisi oluşturulamadı."))
                    }
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    fun openLemmyPostComposer() {
        _lemmyPostComposerState.update { it.copy(isOpen = true, errorMessage = null) }
        loadLemmyComposerCommunities()
    }

    fun dismissLemmyPostComposer() {
        if (_lemmyPostComposerState.value.isSubmitting) return
        _lemmyPostComposerState.value = LemmyHomePostComposerUiState()
    }

    fun onLemmyComposerTypeSelected(type: LemmyPostComposeType) {
        _lemmyPostComposerState.update { it.copy(type = type, errorMessage = null) }
    }

    fun onLemmyComposerCommunitySelected(communityId: String) {
        _lemmyPostComposerState.update { it.copy(selectedCommunityId = communityId, errorMessage = null) }
    }

    fun onLemmyComposerTitleChanged(value: String) {
        _lemmyPostComposerState.update { it.copy(title = value, errorMessage = null) }
    }

    fun onLemmyComposerBodyChanged(value: String) {
        _lemmyPostComposerState.update { it.copy(body = value, errorMessage = null) }
    }

    fun onLemmyComposerUrlChanged(value: String) {
        _lemmyPostComposerState.update { it.copy(url = value, errorMessage = null) }
    }

    fun submitLemmyPost() {
        val state = _lemmyPostComposerState.value
        val account = _uiState.value.activeAccount(PlatformType.LEMMY)
        val community = state.selectedCommunity
        val title = state.title.trim()
        when {
            account == null -> {
                _lemmyPostComposerState.value = state.copy(errorMessage = "Lemmy hesabı gerekli.")
                return
            }
            community == null || community.id.isBlank() -> {
                _lemmyPostComposerState.value = state.copy(errorMessage = "Topluluk seç.")
                return
            }
            title.isBlank() -> {
                _lemmyPostComposerState.value = state.copy(errorMessage = "Başlık zorunlu.")
                return
            }
        }
        _lemmyPostComposerState.value = state.copy(isSubmitting = true, errorMessage = null)
        viewModelScope.launch {
            when (
                val result = lemmyRepository.createPost(
                    account = account,
                    communityId = community.id,
                    title = title,
                    body = state.body.trim().takeIf(String::isNotBlank),
                    url = if (state.type == LemmyPostComposeType.LINK) {
                        state.url.trim().takeIf(String::isNotBlank)
                    } else {
                        null
                    },
                )
            ) {
                is AppResult.Success -> {
                    _lemmyPostComposerState.value = LemmyHomePostComposerUiState()
                }
                is AppResult.Failure -> {
                    _lemmyPostComposerState.update {
                        it.copy(isSubmitting = false, errorMessage = result.error.userMessage("Lemmy post oluşturulamadı."))
                    }
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    fun selectPlatform(platform: PlatformType) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }

    fun selectPlatformAndAccount(platform: PlatformType, account: Account?) {
        val previousState = _uiState.value
        if (account != null && previousState.activeAccount(account.platform)?.id != account.id) {
            clearPlatformTransientState(account.platform)
        }
        _uiState.update {
            it.copy(
                selectedPlatform = platform,
                activeAccountIds = if (account != null) {
                    it.activeAccountIds + (account.platform to account.id)
                } else {
                    it.activeAccountIds
                },
            )
        }
    }

    fun selectAccount(account: Account?) {
        if (account == null) return
        val previousAccountId = _uiState.value.activeAccountIds[account.platform]
        if (previousAccountId != null && previousAccountId != account.id) {
            clearPlatformTransientState(account.platform)
        }
        _uiState.update {
            it.copy(activeAccountIds = it.activeAccountIds + (account.platform to account.id))
        }
    }

    fun selectLemmySort(sort: LemmySortType) {
        _lemmySort.value = sort
    }

    fun onLemmyAction(post: LemmyPostUiModel, action: LemmyPostActionType) {
        val account = _uiState.value.activeAccount(PlatformType.LEMMY) ?: return
        val before = _lemmyActionOverrides.value[post.id] ?: post
        val optimistic = before.optimistic(action).copy(loadingAction = action)
        setLemmyOverride(optimistic)
        viewModelScope.launch {
            val result = when (action) {
                LemmyPostActionType.UPVOTE -> lemmyRepository.votePost(
                    account = account,
                    postId = post.id,
                    score = if (before.isUpvoted) 0 else 1,
                )
                LemmyPostActionType.DOWNVOTE -> lemmyRepository.votePost(
                    account = account,
                    postId = post.id,
                    score = if (before.isDownvoted) 0 else -1,
                )
                LemmyPostActionType.SAVE -> lemmyRepository.savePost(
                    account = account,
                    postId = post.id,
                    saved = !before.isSaved,
                )
            }
            when (result) {
                is AppResult.Success -> {
                    setLemmyOverride(LemmyPostMapper.domainToUi(result.data).copy(loadingAction = null))
                }
                is AppResult.Failure -> {
                    setLemmyOverride(before.copy(loadingAction = null))
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    fun onPixelfedLike(post: PixelfedPostUiModel) {
        val account = _uiState.value.activeAccount(PlatformType.PIXELFED)
            ?: return
        val before = _pixelfedActionOverrides.value[post.id] ?: post
        val optimistic = before.copy(
            isLiked = !before.isLiked,
            likes = (before.likes + if (!before.isLiked) 1 else -1).coerceAtLeast(0),
            isLoadingLike = true,
        )
        setPixelfedOverride(optimistic)
        viewModelScope.launch {
            when (
                val result = pixelfedRepository.setLiked(
                    account = account,
                    postId = post.id,
                    liked = optimistic.isLiked,
                )
            ) {
                is AppResult.Success -> {
                    setPixelfedOverride(
                        PixelfedMapper.postToUi(result.data).copy(isLoadingLike = false),
                    )
                }
                is AppResult.Failure -> {
                    setPixelfedOverride(before.copy(isLoadingLike = false))
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    fun openPixelfedComments(post: PixelfedPostUiModel) {
        val account = _uiState.value.activeAccount(PlatformType.PIXELFED)
            ?: return
        _pixelfedCommentsState.value = PixelfedCommentsState(postId = post.id, isLoading = true)
        viewModelScope.launch {
            when (val result = pixelfedRepository.getComments(account, post.id)) {
                is AppResult.Success -> {
                    _pixelfedCommentsState.value = PixelfedCommentsState(
                        postId = post.id,
                        comments = result.data,
                    )
                }
                is AppResult.Failure -> {
                    _pixelfedCommentsState.value = PixelfedCommentsState(
                        postId = post.id,
                        errorMessage = result.error.userMessage("Comments failed."),
                    )
                }
            }
        }
    }

    fun dismissPixelfedComments() {
        _pixelfedCommentsState.value = null
    }

    private fun loadLemmyComposerCommunities() {
        val account = _uiState.value.activeAccount(PlatformType.LEMMY)
        if (account == null) {
            _lemmyPostComposerState.update { it.copy(errorMessage = "Lemmy hesabı gerekli.") }
            return
        }
        val current = _lemmyPostComposerState.value
        if (current.communities.isNotEmpty() || current.isCommunitiesLoading) return
        _lemmyPostComposerState.update { it.copy(isCommunitiesLoading = true, errorMessage = null) }
        viewModelScope.launch {
            when (val result = lemmyRepository.getCommunities(account, LemmyPostPage(limit = 50))) {
                is AppResult.Success -> {
                    val communities = result.data.map(LemmyPostMapper::communityToUi)
                    _lemmyPostComposerState.update {
                        it.copy(
                            communities = communities,
                            selectedCommunityId = it.selectedCommunityId ?: communities.firstOrNull()?.id,
                            isCommunitiesLoading = false,
                            errorMessage = if (communities.isEmpty()) "Topluluk bulunamadı." else null,
                        )
                    }
                }
                is AppResult.Failure -> {
                    _lemmyPostComposerState.update {
                        it.copy(
                            isCommunitiesLoading = false,
                            errorMessage = result.error.userMessage("Topluluklar yüklenemedi."),
                        )
                    }
                    if (result.error is AppError.Unauthorized) {
                        _effects.emit(HomeEffect.NavigateToProfile)
                    }
                }
            }
        }
    }

    private fun HomeUiState.activeAccount(platform: PlatformType): Account? {
        val platformAccounts = accounts.filter { it.platform == platform }
        return platformAccounts.firstOrNull { it.id == activeAccountIds[platform] }
            ?: platformAccounts.firstOrNull()
    }

    private fun setMastodonOverride(post: MastodonPostUiModel) {
        _mastodonActionOverrides.update { overrides ->
            (overrides + (post.id to post) + (post.detailId to post)).bounded()
        }
    }

    private fun setPixelfedOverride(post: PixelfedPostUiModel) {
        _pixelfedActionOverrides.update { overrides ->
            (overrides + (post.id to post)).bounded()
        }
    }

    private fun setLemmyOverride(post: LemmyPostUiModel) {
        _lemmyActionOverrides.update { overrides ->
            (overrides + (post.id to post)).bounded()
        }
    }

    private fun <T> Map<String, T>.bounded(maxSize: Int = MAX_ACTION_OVERRIDES): Map<String, T> =
        if (size <= maxSize) {
            this
        } else {
            entries.drop(size - maxSize).associate { entry -> entry.key to entry.value }
        }

    private fun clearPlatformTransientState(platform: PlatformType) {
        when (platform) {
            PlatformType.MASTODON -> {
                _mastodonActionOverrides.value = emptyMap()
                _replyComposeState.value = null
                _newPostComposeState.value = null
            }
            PlatformType.PIXELFED -> {
                _pixelfedActionOverrides.value = emptyMap()
                _pixelfedCommentsState.value = null
            }
            PlatformType.LEMMY -> {
                _lemmyActionOverrides.value = emptyMap()
            }
        }
    }

    private fun openReplyCompose(post: MastodonPostUiModel) {
        val mention = post.username.takeIf { it.startsWith("@") } ?: "@${post.username}"
        _replyComposeState.value = MastodonReplyComposeState(
            parent = post,
            text = "$mention ",
        )
    }

    private fun AppError.replyErrorMessage(): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again to reply."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> "Reply could not be sent. Try again."
    }

    private fun AppError.postErrorMessage(): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again to post."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> "Post could not be sent. Try again."
    }

    private fun AppError.userMessage(fallback: String): String = when (this) {
        AppError.Unauthorized -> "Session expired. Log in again."
        AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
        AppError.Network -> "Network failed. Check your connection and retry."
        is AppError.Server -> "Server error $code. Try again shortly."
        is AppError.Unknown -> fallback
    }

    private fun MastodonPostUiModel.optimistic(action: MastodonPostActionType): MastodonPostUiModel =
        when (action) {
            MastodonPostActionType.REPLY -> this
            MastodonPostActionType.BOOST -> copy(
                isBoosted = !isBoosted,
                boosts = (boosts + if (!isBoosted) 1 else -1).coerceAtLeast(0),
            )
            MastodonPostActionType.FAVOURITE -> copy(
                isFavourited = !isFavourited,
                favourites = (favourites + if (!isFavourited) 1 else -1).coerceAtLeast(0),
            )
            MastodonPostActionType.BOOKMARK -> copy(isBookmarked = !isBookmarked)
        }

    private fun MastodonPostUiModel.withActionStateFrom(
        confirmed: MastodonPostUiModel,
    ): MastodonPostUiModel = copy(
        replies = confirmed.replies,
        boosts = confirmed.boosts,
        favourites = confirmed.favourites,
        isBoosted = confirmed.isBoosted,
        isFavourited = confirmed.isFavourited,
        isBookmarked = confirmed.isBookmarked,
    )

    private fun LemmyPostUiModel.optimistic(action: LemmyPostActionType): LemmyPostUiModel =
        when (action) {
            LemmyPostActionType.UPVOTE -> {
                val nextUpvoted = !isUpvoted
                copy(
                    isUpvoted = nextUpvoted,
                    isDownvoted = false,
                    score = score + when {
                        nextUpvoted && isDownvoted -> 2
                        nextUpvoted -> 1
                        else -> -1
                    },
                )
            }
            LemmyPostActionType.DOWNVOTE -> {
                val nextDownvoted = !isDownvoted
                copy(
                    isDownvoted = nextDownvoted,
                    isUpvoted = false,
                    score = score + when {
                        nextDownvoted && isUpvoted -> -2
                        nextDownvoted -> -1
                        else -> 1
                    },
                )
            }
            LemmyPostActionType.SAVE -> copy(isSaved = !isSaved)
        }

}

private fun debugLog(message: String) {
    if (BuildConfig.DEBUG) {
        Log.d("HomeViewModel", message)
    }
}

sealed interface HomeEffect {
    data object NavigateToProfile : HomeEffect
}

private const val MAX_ACTION_OVERRIDES = 160
