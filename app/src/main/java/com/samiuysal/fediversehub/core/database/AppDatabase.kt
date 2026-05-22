package com.samiuysal.fediversehub.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonMediaEntity
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonPostEntity
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonRemoteKeyEntity
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonTimelineDao

@Database(
    entities = [
        AccountEntity::class,
        MastodonPostEntity::class,
        MastodonMediaEntity::class,
        MastodonRemoteKeyEntity::class,
    ],
    version = 3,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mastodonTimelineDao(): MastodonTimelineDao
}
