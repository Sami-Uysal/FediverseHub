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
    val activeAccountIds: Map<PlatformType, String> = emptyMap(),
    val isMastodonLoading: Boolean = false,
    val mastodonErrorMessage: String? = null,
    val mastodonPosts: List<MastodonPostUiModel> = emptyList(),
    val lemmyPosts: List<LemmyPostUiModel> = emptyList(),
    val pixelfedPosts: List<PixelfedPostUiModel> = emptyList(),
) {
    val selectedAccount: Account?
        get() {
            val platformAccounts = accounts.filter { it.platform == selectedPlatform }
            return platformAccounts.firstOrNull { it.id == activeAccountIds[selectedPlatform] }
                ?: platformAccounts.firstOrNull()
        }
}

sealed interface HomeUiEvent {
    data class PlatformSelected(val platform: PlatformType) : HomeUiEvent
}
