package com.samiuysal.fediversehub.feature.mastodon.data.local

import androidx.room.Embedded
import androidx.room.Relation

data class MastodonPostWithMedia(
    @Embedded val post: MastodonPostEntity,
    @Relation(
        parentColumn = "localId",
        entityColumn = "postLocalId",
    )
    val media: List<MastodonMediaEntity>,
)
