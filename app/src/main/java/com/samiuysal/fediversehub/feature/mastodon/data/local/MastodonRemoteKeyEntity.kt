package com.samiuysal.fediversehub.feature.mastodon.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mastodon_remote_keys")
data class MastodonRemoteKeyEntity(
    @PrimaryKey val accountId: String,
    val nextMaxId: String?,
    val refreshedAt: Long,
)
