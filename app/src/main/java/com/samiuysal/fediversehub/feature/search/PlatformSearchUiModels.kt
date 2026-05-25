package com.samiuysal.fediversehub.feature.search

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySearchCategory
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedSearchCategory

@Immutable
data class PixelfedSearchAccountUiModel(
    val id: String,
    val displayName: String,
    val username: String,
    val avatarUrl: String?,
    val note: String,
)

@Immutable
data class PixelfedHashtagUiModel(
    val name: String,
)

@Immutable
data class PixelfedSearchResultsUiModel(
    val posts: List<PixelfedPostUiModel> = emptyList(),
    val accounts: List<PixelfedSearchAccountUiModel> = emptyList(),
    val hashtags: List<PixelfedHashtagUiModel> = emptyList(),
) {
    fun isEmpty(category: PixelfedSearchCategory): Boolean = when (category) {
        PixelfedSearchCategory.POSTS -> posts.isEmpty()
        PixelfedSearchCategory.ACCOUNTS -> accounts.isEmpty()
        PixelfedSearchCategory.HASHTAGS -> hashtags.isEmpty()
    }
}

sealed interface PixelfedSearchUiState {
    data object Idle : PixelfedSearchUiState
    data object Loading : PixelfedSearchUiState
    data class Success(val results: PixelfedSearchResultsUiModel) : PixelfedSearchUiState
    data class Error(val message: String) : PixelfedSearchUiState
}

@Immutable
data class LemmySearchUserUiModel(
    val id: String,
    val name: String,
    val displayName: String,
    val avatarUrl: String?,
    val bio: String,
)

@Immutable
data class LemmySearchResultsUiModel(
    val posts: List<LemmyPostUiModel> = emptyList(),
    val communities: List<LemmyCommunityUiModel> = emptyList(),
    val users: List<LemmySearchUserUiModel> = emptyList(),
) {
    fun isEmpty(category: LemmySearchCategory): Boolean = when (category) {
        LemmySearchCategory.POSTS -> posts.isEmpty()
        LemmySearchCategory.COMMUNITIES -> communities.isEmpty()
        LemmySearchCategory.USERS -> users.isEmpty()
    }
}

sealed interface LemmySearchUiState {
    data object Idle : LemmySearchUiState
    data object Loading : LemmySearchUiState
    data class Success(val results: LemmySearchResultsUiModel) : LemmySearchUiState
    data class Error(val message: String) : LemmySearchUiState
}

