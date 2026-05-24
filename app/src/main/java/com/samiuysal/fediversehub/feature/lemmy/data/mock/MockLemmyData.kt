package com.samiuysal.fediversehub.feature.lemmy.data.mock

import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyComment
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyCommunity
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost

object MockLemmyData {
    val posts = listOf(
        LemmyPost(
            id = "l1",
            title = "What is the cleanest way to cache multiple Fediverse account feeds?",
            communityId = "101",
            communityName = "androiddev",
            communityActorId = "androiddev@lemmy.world",
            domain = "self.lemmy.world",
            authorName = "cache-first",
            publishedAt = "14m",
            score = 284,
            commentCount = 73,
            previewText = "I am comparing per-account RemoteKeys vs per-feed RemoteKeys for mixed Mastodon, Lemmy and Pixelfed clients.",
            comments = listOf(
                LemmyComment("c1", null, "room_mediator", "Keep the remote key scoped by accountId + platform + feed type.", 0, false),
                LemmyComment("c2", "c1", "pagingfan", "This also makes refresh invalidation much less surprising.", 1, false),
                LemmyComment("c3", null, "api_edge", "Do not let Lemmy sort modes share the same cache namespace.", 0, false),
            ),
        ),
        LemmyPost(
            id = "l2",
            title = "Show HN: A Compose comment tree using flattened rows",
            communityId = "102",
            communityName = "programming",
            communityActorId = "programming@lemmy.ml",
            domain = "github.com",
            authorName = "compose_tree",
            publishedAt = "1h",
            score = 146,
            commentCount = 29,
            previewText = "The tree renders as a LazyColumn with depth metadata and local collapse state.",
            comments = listOf(
                LemmyComment("c4", null, "lazy_keys", "Flattening is the right default for large trees.", 0, false),
                LemmyComment("c5", "c4", "state_holder", "Collapse state can live by comment id in the ViewModel.", 1, true),
            ),
        ),
        LemmyPost(
            id = "l3",
            title = "Sync-style dense cards still feel better for link-heavy communities",
            communityId = "103",
            communityName = "fediverse",
            communityActorId = "fediverse@lemmy.world",
            domain = "blog.example.dev",
            authorName = "fedi_reader",
            publishedAt = "3h",
            score = 91,
            commentCount = 18,
            previewText = "Card hierarchy matters: title first, source second, metrics always scannable.",
            comments = emptyList(),
        ),
        LemmyPost(
            id = "l4",
            title = "A practical Room + Paging boundary for offline first Reddit-like feeds",
            communityId = "104",
            communityName = "kotlin",
            communityActorId = "kotlin@programming.dev",
            domain = "medium.example",
            authorName = "offline_mapper",
            publishedAt = "5h",
            score = 312,
            commentCount = 64,
            previewText = "Keep sort mode, community scope and account id in the remote key table from day one.",
            comments = listOf(
                LemmyComment("c6", null, "db_index", "A compound index on account + sort + community pays off quickly.", 0, false),
            ),
        ),
    )

    fun communityFor(name: String): LemmyCommunity =
        posts.firstOrNull { it.communityName == name }?.let { post ->
            LemmyCommunity(
                id = post.communityId.orEmpty(),
                name = post.communityName,
                title = post.communityName.replaceFirstChar { it.uppercase() },
                actorId = post.communityActorId,
                description = "Popular ${post.communityName} community.",
                iconUrl = null,
                bannerUrl = null,
                subscribers = 12_400,
                posts = 1_240,
                comments = 8_920,
                subscribed = false,
            )
        } ?: LemmyCommunity(
            id = "103",
            name = name,
            title = name.replaceFirstChar { it.uppercase() },
            actorId = "$name@lemmy.world",
            description = "Community detail preview.",
            iconUrl = null,
            bannerUrl = null,
            subscribers = 1_000,
            posts = 120,
            comments = 640,
            subscribed = false,
        )
}
