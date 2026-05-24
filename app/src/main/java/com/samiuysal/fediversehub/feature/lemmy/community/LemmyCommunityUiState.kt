package com.samiuysal.fediversehub.feature.lemmy.community

import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel

sealed interface LemmyCommunityUiState {
    data object Loading : LemmyCommunityUiState
    data class Success(val community: LemmyCommunityUiModel) : LemmyCommunityUiState
    data class Error(val message: String) : LemmyCommunityUiState
}
