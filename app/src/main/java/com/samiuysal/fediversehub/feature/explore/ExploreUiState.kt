package com.samiuysal.fediversehub.feature.explore

import androidx.compose.runtime.Immutable
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonHashtag
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTrendLink

enum class MastodonExploreTab {
    POSTS,
    TAGS,
    LINKS,
}

enum class LemmyExploreTab {
    ALL,
    LOCAL,
    COMMUNITIES,
}

@Immutable
data class MastodonExploreUiState(
    val selectedTab: MastodonExploreTab = MastodonExploreTab.POSTS,
    val loadingTab: MastodonExploreTab? = null,
    val errorMessage: String? = null,
    val posts: List<MastodonPostUiModel> = emptyList(),
    val tags: List<MastodonHashtag> = emptyList(),
    val links: List<MastodonTrendLink> = emptyList(),
) {
    val isSelectedTabLoading: Boolean
        get() = loadingTab == selectedTab
}
