package com.samiuysal.fediversehub.feature.mastodon.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "mastodon_media",
    foreignKeys = [
        ForeignKey(
            entity = MastodonPostEntity::class,
            parentColumns = ["localId"],
            childColumns = ["postLocalId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [
        Index(value = ["postLocalId"]),
        Index(value = ["accountId"]),
    ],
)
data class MastodonMediaEntity(
    @PrimaryKey val localId: String,
    val postLocalId: String,
    val accountId: String,
    val remoteId: String,
    val type: String,
    val url: String?,
    val previewUrl: String?,
    val description: String?,
    val sortOrder: Int,
)
