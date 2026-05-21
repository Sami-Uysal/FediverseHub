package com.samiuysal.fediversehub.feature.home

import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.data.mock.MockLemmyData
import com.samiuysal.fediversehub.feature.lemmy.mapper.LemmyPostMapper
import com.samiuysal.fediversehub.feature.mastodon.data.mock.MockMastodonData
import com.samiuysal.fediversehub.feature.mastodon.mapper.MastodonTimelineMapper
import com.samiuysal.fediversehub.feature.pixelfed.PixelfedPostUiModel

object MockFediverseData {
    private val accounts = listOf(
        Account(
            id = "mastodon-1",
            platform = PlatformType.MASTODON,
            instanceUrl = "mastodon.social",
            username = "sami",
            displayName = "Sami Uysal",
            avatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=120&h=120&fit=crop",
            accessToken = null,
        ),
        Account(
            id = "lemmy-1",
            platform = PlatformType.LEMMY,
            instanceUrl = "lemmy.world",
            username = "sami",
            displayName = "sami",
            avatarUrl = null,
            accessToken = null,
        ),
        Account(
            id = "pixelfed-1",
            platform = PlatformType.PIXELFED,
            instanceUrl = "pixelfed.social",
            username = "sami.photos",
            displayName = "Sami Photos",
            avatarUrl = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=120&h=120&fit=crop",
            accessToken = null,
        ),
    )

    val homeState = HomeUiState(
        accounts = accounts,
        mastodonPosts = MockMastodonData.homeTimeline.map(MastodonTimelineMapper::domainToUi),
        lemmyPosts = MockLemmyData.posts.map(LemmyPostMapper::domainToUi),
        pixelfedPosts = listOf(
            PixelfedPostUiModel(
                id = "p1",
                displayName = "Leyla Street",
                username = "@leyla@pixelfed.social",
                avatarUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120&h=120&fit=crop",
                imageUrl = "https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?w=1100&h=1100&fit=crop",
                caption = "Golden hour over the old district. No filter, just patience.",
                likes = 1284,
                comments = 86,
                timeAgo = "22m",
            ),
            PixelfedPostUiModel(
                id = "p2",
                displayName = "Minimal Frames",
                username = "@frames@pixelfed.art",
                avatarUrl = null,
                imageUrl = "https://images.unsplash.com/photo-1494526585095-c41746248156?w=1100&h=1100&fit=crop",
                caption = "Concrete, glass, soft shadows.",
                likes = 842,
                comments = 41,
                timeAgo = "1h",
            ),
            PixelfedPostUiModel(
                id = "p3",
                displayName = "Aegean Notes",
                username = "@aegean@pixelfed.social",
                avatarUrl = "https://images.unsplash.com/photo-1527980965255-d3b416303d12?w=120&h=120&fit=crop",
                imageUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=1100&h=1100&fit=crop",
                caption = "Blue, white, and a little wind.",
                likes = 2301,
                comments = 118,
                timeAgo = "2h",
            ),
        ),
    )
}
