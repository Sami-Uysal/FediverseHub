package com.samiuysal.fediversehub.feature.mastodon.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.userMessage
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.auth.domain.AccountStore
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.navigation.AppDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class MastodonPostDetailViewModel @Inject constructor(
    private val mastodonRepository: MastodonRepository,
    private val accountStore: AccountStore,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val postId: String = checkNotNull(savedStateHandle[AppDestination.POST_ID_ARGUMENT])

    private val _uiState = MutableStateFlow<MastodonPostDetailUiState>(MastodonPostDetailUiState.Loading)
    val uiState: StateFlow<MastodonPostDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun retry() {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.update { MastodonPostDetailUiState.Loading }
            val account = mastodonAccount()
            when (val result = mastodonRepository.getPostDetail(account = account, postId = postId)) {
                is AppResult.Success -> {
                    val detail = result.data
                    _uiState.update {
                        MastodonPostDetailUiState.Success(
                            ancestors = detail.ancestors.map(MastodonTimelineMapper::domainToUi),
                            post = MastodonTimelineMapper.domainToUi(detail.post),
                            descendants = detail.descendants.map(MastodonTimelineMapper::domainToUi),
                        )
                    }
                }
                is AppResult.Failure -> {
                    _uiState.update { MastodonPostDetailUiState.Error(result.error.userMessage()) }
                }
            }
        }
    }

    private suspend fun mastodonAccount(): Account =
        accountStore.accounts.first().firstOrNull { it.platform == PlatformType.MASTODON }
            ?: Account(
                id = "mastodon-detail-preview",
                platform = PlatformType.MASTODON,
                instanceUrl = "mastodon.social",
                username = "preview",
                displayName = "Mastodon",
                avatarUrl = null,
                accessToken = null,
            )
}
