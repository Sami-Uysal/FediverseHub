package com.samiuysal.fediversehub.feature.mastodon.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mastodon_posts",
    indices = [
        Index(value = ["accountId", "timelinePosition"]),
        Index(value = ["accountId", "remoteId"], unique = true),
    ],
)
data class MastodonPostEntity(
    @PrimaryKey val localId: String,
    val accountId: String,
    val instanceUrl: String,
    val remoteId: String,
    val statusRemoteId: String,
    val uri: String?,
    val url: String?,
    val createdAt: String?,
    val authorRemoteId: String,
    val authorDisplayName: String,
    val authorUsername: String,
    val authorAvatarUrl: String?,
    val contentText: String,
    val boostedByDisplayName: String?,
    val boostedByAvatarUrl: String?,
    val inReplyToAccountId: String?,
    val linkDomain: String?,
    val linkTitle: String?,
    val linkDescription: String?,
    val linkThumbnailUrl: String?,
    val replyCount: Int,
    val reblogCount: Int,
    val favouriteCount: Int,
    val isReblogged: Boolean,
    val isFavourited: Boolean,
    val isBookmarked: Boolean,
    val visibility: String,
    val timelinePosition: Long,
    val cachedAt: Long,
)
