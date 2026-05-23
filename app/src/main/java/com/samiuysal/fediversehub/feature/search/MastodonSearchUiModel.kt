package com.samiuysal.fediversehub.feature.search

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonSearchCategory

@Immutable
data class MastodonSearchAccountUiModel(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val note: String,
)

@Immutable
data class MastodonHashtagUiModel(
    val name: String,
)

@Immutable
data class MastodonSearchResultsUiModel(
    val posts: List<MastodonPostUiModel> = emptyList(),
    val accounts: List<MastodonSearchAccountUiModel> = emptyList(),
    val hashtags: List<MastodonHashtagUiModel> = emptyList(),
) {
    fun isEmpty(category: MastodonSearchCategory): Boolean = when (category) {
        MastodonSearchCategory.POSTS -> posts.isEmpty()
        MastodonSearchCategory.ACCOUNTS -> accounts.isEmpty()
        MastodonSearchCategory.HASHTAGS -> hashtags.isEmpty()
    }
}

sealed interface MastodonSearchUiState {
    data object Idle : MastodonSearchUiState
    data object Loading : MastodonSearchUiState
    data class Success(val results: MastodonSearchResultsUiModel) : MastodonSearchUiState
    data class Error(val message: String) : MastodonSearchUiState
}
