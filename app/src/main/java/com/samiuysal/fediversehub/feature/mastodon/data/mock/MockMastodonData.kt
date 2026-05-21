package com.samiuysal.fediversehub.feature.mastodon.data.mock

import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaAttachment
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaType
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost

object MockMastodonData {
    val homeTimeline = listOf(
        MastodonPost(
            id = "m1",
            authorDisplayName = "Nora Dev",
            authorUsername = "nora@hachyderm.io",
            authorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=120&h=120&fit=crop",
            createdAt = null,
            contentText = "Compose feed performansında en büyük kazanç stable UI model + LazyColumn key kombinasyonundan geliyor. Bugün timeline cache katmanını RemoteMediator'a hazırladım.",
            mediaAttachments = emptyList(),
            replyCount = 12,
            reblogCount = 28,
            favouriteCount = 91,
            url = null,
        ),
        MastodonPost(
            id = "m2",
            authorDisplayName = "Moshidon Labs",
            authorUsername = "labs@fosstodon.org",
            authorAvatarUrl = null,
            createdAt = null,
            contentText = "A unified Fediverse client should still respect platform grammar. Timeline is not a post board, and a photo grid is not a tweet list.",
            mediaAttachments = listOf(
                MastodonMediaAttachment(
                    id = "media-m2",
                    type = MastodonMediaType.IMAGE,
                    url = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=900&h=520&fit=crop",
                    previewUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=900&h=520&fit=crop",
                    description = "Laptop and code editor on a desk",
                ),
            ),
            replyCount = 5,
            reblogCount = 44,
            favouriteCount = 130,
            url = null,
        ),
        MastodonPost(
            id = "m3",
            authorDisplayName = "Android Weekly",
            authorUsername = "androidweekly@mastodon.social",
            authorAvatarUrl = "https://images.unsplash.com/photo-1556157382-97eda2d62296?w=120&h=120&fit=crop",
            createdAt = null,
            contentText = "Material 3 works best when product teams add a clear component layer instead of sprinkling raw Material calls across every feature.",
            mediaAttachments = emptyList(),
            replyCount = 18,
            reblogCount = 61,
            favouriteCount = 204,
            url = null,
        ),
    )
}
