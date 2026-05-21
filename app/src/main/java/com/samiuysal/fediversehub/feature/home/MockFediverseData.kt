package com.samiuysal.fediversehub.feature.home

import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.core.model.PlatformType
import com.samiuysal.fediversehub.feature.lemmy.CommentUiModel
import com.samiuysal.fediversehub.feature.lemmy.LemmyPostUiModel
import com.samiuysal.fediversehub.feature.mastodon.MastodonPostUiModel
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
        mastodonPosts = listOf(
            MastodonPostUiModel(
                id = "m1",
                displayName = "Nora Dev",
                username = "@nora@hachyderm.io",
                avatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=120&h=120&fit=crop",
                timeAgo = "8m",
                content = "Compose feed performansında en büyük kazanç stable UI model + LazyColumn key kombinasyonundan geliyor. Bugün timeline cache katmanını RemoteMediator'a hazırladım.",
                mediaUrl = null,
                replies = 12,
                boosts = 28,
                favourites = 91,
            ),
            MastodonPostUiModel(
                id = "m2",
                displayName = "Moshidon Labs",
                username = "@labs@fosstodon.org",
                avatarUrl = null,
                timeAgo = "31m",
                content = "A unified Fediverse client should still respect platform grammar. Timeline is not a post board, and a photo grid is not a tweet list.",
                mediaUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=900&h=520&fit=crop",
                replies = 5,
                boosts = 44,
                favourites = 130,
            ),
            MastodonPostUiModel(
                id = "m3",
                displayName = "Android Weekly",
                username = "@androidweekly@mastodon.social",
                avatarUrl = "https://images.unsplash.com/photo-1556157382-97eda2d62296?w=120&h=120&fit=crop",
                timeAgo = "1h",
                content = "Material 3 works best when product teams add a clear component layer instead of sprinkling raw Material calls across every feature.",
                mediaUrl = null,
                replies = 18,
                boosts = 61,
                favourites = 204,
            ),
        ),
        lemmyPosts = listOf(
            LemmyPostUiModel(
                id = "l1",
                title = "What is the cleanest way to cache multiple Fediverse account feeds?",
                community = "androiddev",
                domain = "self.lemmy.world",
                author = "u/cache-first",
                timeAgo = "14m",
                score = 284,
                comments = 73,
                previewText = "I am comparing per-account RemoteKeys vs per-feed RemoteKeys for mixed Mastodon, Lemmy and Pixelfed clients.",
                nestedComments = listOf(
                    CommentUiModel("c1", null, "room_mediator", "Keep the remote key scoped by accountId + platform + feed type.", 0, false),
                    CommentUiModel("c2", "c1", "pagingfan", "This also makes refresh invalidation much less surprising.", 1, false),
                    CommentUiModel("c3", null, "api_edge", "Do not let Lemmy sort modes share the same cache namespace.", 0, false),
                ),
            ),
            LemmyPostUiModel(
                id = "l2",
                title = "Show HN: A Compose comment tree using flattened rows",
                community = "programming",
                domain = "github.com",
                author = "u/compose_tree",
                timeAgo = "1h",
                score = 146,
                comments = 29,
                previewText = "The tree renders as a LazyColumn with depth metadata and local collapse state.",
                nestedComments = listOf(
                    CommentUiModel("c4", null, "lazy_keys", "Flattening is the right default for large trees.", 0, false),
                    CommentUiModel("c5", "c4", "state_holder", "Collapse state can live by comment id in the ViewModel.", 1, true),
                ),
            ),
            LemmyPostUiModel(
                id = "l3",
                title = "Sync-style dense cards still feel better for link-heavy communities",
                community = "fediverse",
                domain = "blog.example.dev",
                author = "u/fedi_reader",
                timeAgo = "3h",
                score = 91,
                comments = 18,
                previewText = "Card hierarchy matters: title first, source second, metrics always scannable.",
                nestedComments = emptyList(),
            ),
        ),
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
