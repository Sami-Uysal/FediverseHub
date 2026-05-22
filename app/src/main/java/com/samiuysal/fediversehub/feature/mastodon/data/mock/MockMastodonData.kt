package com.samiuysal.fediversehub.feature.mastodon.data.mock

import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaAttachment
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonMediaType
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonLinkPreview
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonPost

object MockMastodonData {
    val homeTimeline = listOf(
        MastodonPost(
            id = "m1",
            authorDisplayName = "Nora Dev",
            authorUsername = "nora@hachyderm.io",
            authorAvatarUrl = "https://images.unsplash.com/photo-1517841905240-472988babdf9?w=96&h=96&fit=crop",
            createdAt = null,
            contentText = "Python 3.15: features that didn't make the headlines blog.changs.co.uk/python-315-features",
            mediaAttachments = emptyList(),
            inReplyToAccountId = "thread-nix",
            linkPreview = MastodonLinkPreview(
                domain = "blog.changs.co.uk",
                title = "Python 3.15: features that didn't make the headlines",
                description = "It's that time of the year again: a compact tour of the quiet changes.",
                thumbnailUrl = null,
            ),
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
                    url = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=720&h=420&fit=crop",
                    previewUrl = "https://images.unsplash.com/photo-1516321318423-f06f85e504b3?w=720&h=420&fit=crop",
                    description = "Laptop and code editor on a desk",
                ),
            ),
            boostedByDisplayName = "BrianKrebs",
            boostedByAvatarUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=96&h=96&fit=crop",
            replyCount = 5,
            reblogCount = 44,
            favouriteCount = 130,
            url = null,
        ),
        MastodonPost(
            id = "m3",
            authorDisplayName = "Android Weekly",
            authorUsername = "androidweekly@mastodon.social",
            authorAvatarUrl = "https://images.unsplash.com/photo-1556157382-97eda2d62296?w=96&h=96&fit=crop",
            createdAt = null,
            contentText = "How to Convert Between Wealth and Income Tax paulgraham.com/winc.html #HackerNews #wealth #tax #finance",
            mediaAttachments = emptyList(),
            linkPreview = MastodonLinkPreview(
                domain = "paulgraham.com",
                title = "How to Convert Between Wealth and Income Tax",
                description = null,
                thumbnailUrl = null,
            ),
            replyCount = 18,
            reblogCount = 61,
            favouriteCount = 204,
            url = null,
        ),
    )
}
