package com.samiuysal.fediversehub.feature.home

import androidx.lifecycle.ViewModel
import com.samiuysal.fediversehub.core.model.PlatformType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

@HiltViewModel
class HomeViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(MockFediverseData.homeState)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun onEvent(event: HomeUiEvent) {
        when (event) {
            is HomeUiEvent.PlatformSelected -> selectPlatform(event.platform)
        }
    }

    private fun selectPlatform(platform: PlatformType) {
        _uiState.update { it.copy(selectedPlatform = platform) }
    }
}
