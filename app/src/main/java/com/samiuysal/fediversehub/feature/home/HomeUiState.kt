package com.samiuysal.fediversehub.feature.home

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel

@Immutable
data class HomeUiState(
    val selectedPlatform: PlatformType = PlatformType.MASTODON,
    val accounts: List<Account> = emptyList(),
    val isMastodonLoading: Boolean = false,
    val mastodonErrorMessage: String? = null,
    val mastodonPosts: List<MastodonPostUiModel> = emptyList(),
    val lemmyPosts: List<LemmyPostUiModel> = emptyList(),
    val pixelfedPosts: List<PixelfedPostUiModel> = emptyList(),
) {
    val selectedAccount: Account?
        get() = accounts.firstOrNull { it.platform == selectedPlatform }
}

sealed interface HomeUiEvent {
    data class PlatformSelected(val platform: PlatformType) : HomeUiEvent
}
