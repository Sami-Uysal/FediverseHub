package com.samiuysal.fediversehub.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonRepository
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mastodonRepository: MastodonRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow(MockFediverseData.homeState)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshMastodonTimeline()
    }

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.PlatformSelected -> selectPlatform(event.platform)
        }
    }

    private fun selectPlatform(platform: PlatformType) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }

    private fun refreshMastodonTimeline() {
        val account = _uiState.value.accounts.firstOrNull { it.platform == PlatformType.MASTODON }
            ?: return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isMastodonLoading = true,
                    mastodonErrorMessage = null,
                )
            }

            when (val result = mastodonRepository.getHomeTimeline(account)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        isMastodonLoading = false,
                        mastodonPosts = result.data.map(MastodonTimelineMapper::domainToUi),
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        isMastodonLoading = false,
                        mastodonErrorMessage = result.error.toUserMessage(),
                    )
                }
            }
        }
    }

    private fun AppError.toUserMessage(): String = when (this) {
        AppError.Network -> "Network connection failed."
        AppError.RateLimited -> "Mastodon rate limit reached."
        AppError.Unauthorized -> "Mastodon account needs login."
        is AppError.Server -> "Mastodon server error: $code"
        is AppError.Unknown -> message ?: "Unknown Mastodon error."
    }
}
