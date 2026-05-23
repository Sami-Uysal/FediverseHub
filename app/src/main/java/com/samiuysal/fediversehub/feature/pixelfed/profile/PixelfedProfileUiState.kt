package com.samiuysal.fediversehub.feature.pixelfed.profile

import com.samiuysal.fediversehub.feature.pixelfed.domain.PixelfedProfile

sealed interface PixelfedProfileUiState {
    data object Loading : PixelfedProfileUiState
    data object NoAccount : PixelfedProfileUiState
    data class Success(val profile: PixelfedProfile) : PixelfedProfileUiState
    data class Error(val message: String) : PixelfedProfileUiState
}
