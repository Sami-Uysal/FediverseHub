package com.samiuysal.fediversehub.feature.lemmy.community

import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel

sealed interface LemmyCommunityUiState {
    data object Loading : LemmyCommunityUiState
    data class Success(val community: LemmyCommunityUiModel) : LemmyCommunityUiState
    data class Error(val message: String) : LemmyCommunityUiState
}

data class LemmyPostComposerUiState(
    val isOpen: Boolean = false,
    val type: LemmyPostComposeType = LemmyPostComposeType.TEXT,
    val title: String = "",
    val body: String = "",
    val url: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

enum class LemmyPostComposeType {
    TEXT,
    LINK,
}
