package com.samiuysal.fediversehub.feature.mastodon.detail

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel

sealed interface MastodonPostDetailUiState {
    data object Loading : MastodonPostDetailUiState
    data object Empty : MastodonPostDetailUiState

    @Immutable
    data class Success(
        val ancestors: List<MastodonPostUiModel>,
        val post: MastodonPostUiModel,
        val descendants: List<MastodonPostUiModel>,
    ) : MastodonPostDetailUiState

    data class Error(val message: String) : MastodonPostDetailUiState
}
