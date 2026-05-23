package com.samiuysal.fediversehub.feature.mastodon.profile

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonProfileTimelineFilter

@Immutable
data class MastodonProfileUiModel(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val headerUrl: String?,
    val note: String,
    val followersCount: Int,
    val followingCount: Int,
    val statusesCount: Int,
    val fields: List<MastodonProfileFieldUiModel>,
)

@Immutable
data class MastodonProfileFieldUiModel(
    val name: String,
    val value: String,
)

sealed interface MastodonProfileUiState {
    data object Loading : MastodonProfileUiState
    data object NoAccount : MastodonProfileUiState
    data class Success(
        val profile: MastodonProfileUiModel,
        val selectedFilter: MastodonProfileTimelineFilter,
    ) : MastodonProfileUiState
    data class Error(val message: String) : MastodonProfileUiState
}
