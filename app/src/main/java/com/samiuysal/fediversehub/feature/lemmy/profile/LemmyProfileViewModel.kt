package com.samiuysal.fediversehub.feature.lemmy.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyProfile
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class LemmyProfileViewModel @Inject constructor(
    private val lemmyRepository: LemmyRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<LemmyProfileUiState>(LemmyProfileUiState.NoAccount)
    val uiState: StateFlow<LemmyProfileUiState> = _uiState.asStateFlow()
    private var selectedAccount: Account? = null
    private var targetUsername: String? = null

    fun selectAccount(account: Account?) {
        targetUsername = null
        val safeAccount = account?.takeIf {
            it.platform == PlatformType.LEMMY && !it.accessToken.isNullOrBlank()
        }
        if (safeAccount?.id == selectedAccount?.id && _uiState.value is LemmyProfileUiState.Success) return
        selectedAccount = safeAccount
        if (safeAccount == null) {
            _uiState.value = LemmyProfileUiState.NoAccount
            return
        }
        load()
    }

    fun selectUser(account: Account?, username: String) {
        targetUsername = username
        selectedAccount = account?.takeIf { it.platform == PlatformType.LEMMY } ?: publicLemmyAccount()
        load()
    }

    fun retry() {
        load()
    }

    fun selectTab(tab: LemmyProfileTab) {
        _uiState.update { state ->
            (state as? LemmyProfileUiState.Success)?.copy(selectedTab = tab) ?: state
        }
    }

    private fun load() {
        val account = selectedAccount ?: run {
            _uiState.value = LemmyProfileUiState.NoAccount
            return
        }
        viewModelScope.launch {
            _uiState.value = LemmyProfileUiState.Loading
            val result = withContext(Dispatchers.IO) {
                val username = targetUsername
                if (username == null) {
                    lemmyRepository.getOwnProfile(account)
                } else {
                    lemmyRepository.getProfile(account, username)
                }
            }
            when (result) {
                is AppResult.Success -> {
                    val profile = withContext(Dispatchers.Default) { result.data.toUi() }
                    _uiState.value = LemmyProfileUiState.Success(profile = profile)
                }
                is AppResult.Failure -> {
                    _uiState.value = LemmyProfileUiState.Error(result.error.lemmyProfileMessage())
                }
            }
        }
    }

    private fun LemmyProfile.toUi(): LemmyProfileUiModel =
        LemmyProfileUiModel(
            id = id,
            name = name,
            displayName = displayName,
            avatarUrl = avatarUrl,
            bannerUrl = bannerUrl,
            bio = bio,
            postCount = postCount,
            commentCount = commentCount,
            posts = posts.map(LemmyPostMapper::domainToUi),
            comments = comments.map(LemmyPostMapper::commentToUi),
            savedPosts = savedPosts.map(LemmyPostMapper::domainToUi),
            savedComments = savedComments.map(LemmyPostMapper::commentToUi),
        )

    private fun publicLemmyAccount(): Account =
        Account(
            id = "public-lemmy-world",
            platform = PlatformType.LEMMY,
            instanceUrl = "lemmy.world",
            username = "public",
            displayName = "Lemmy",
            avatarUrl = null,
            accessToken = null,
        )
}

private fun AppError.lemmyProfileMessage(): String = when (this) {
    AppError.Network -> "Ağ hatası. Bağlantıyı kontrol et."
    AppError.RateLimited -> "Çok hızlı istek atıldı. Biraz bekle."
    AppError.Unauthorized -> "Lemmy profili için giriş yap."
    is AppError.Server -> "Sunucu hatası $code. Biraz sonra tekrar dene."
    is AppError.Unknown -> "Lemmy profili yüklenemedi. Tekrar dene."
}
