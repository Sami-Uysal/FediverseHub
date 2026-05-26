package com.samiuysal.fediversehub.feature.home

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyCommunityUiModel
import com.samiuysal.fediversehub.feature.lemmy.community.LemmyPostComposeType
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

data class PixelfedPostComposerUiState(
    val isOpen: Boolean = false,
    val text: String = "",
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
)

data class LemmyHomePostComposerUiState(
    val isOpen: Boolean = false,
    val communities: List<LemmyCommunityUiModel> = emptyList(),
    val selectedCommunityId: String? = null,
    val type: LemmyPostComposeType = LemmyPostComposeType.TEXT,
    val title: String = "",
    val body: String = "",
    val url: String = "",
    val isCommunitiesLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,
) {
    val selectedCommunity: LemmyCommunityUiModel?
        get() = communities.firstOrNull { it.id == selectedCommunityId } ?: communities.firstOrNull()
}
