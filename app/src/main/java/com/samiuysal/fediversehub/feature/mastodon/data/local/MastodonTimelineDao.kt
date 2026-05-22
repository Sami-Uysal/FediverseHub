package com.samiuysal.fediversehub.feature.mastodon.data.local

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface MastodonTimelineDao {
    @Transaction
    @Query(
        """
        SELECT * FROM mastodon_posts
        WHERE accountId = :accountId
        ORDER BY timelinePosition ASC
        """,
    )
    fun homeTimelinePagingSource(accountId: String): PagingSource<Int, MastodonPostWithMedia>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertPosts(posts: List<MastodonPostEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertMedia(media: List<MastodonMediaEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRemoteKey(remoteKey: MastodonRemoteKeyEntity)

    @Query("SELECT * FROM mastodon_remote_keys WHERE accountId = :accountId")
    suspend fun remoteKey(accountId: String): MastodonRemoteKeyEntity?

    @Query("SELECT COALESCE(MAX(timelinePosition), -1) FROM mastodon_posts WHERE accountId = :accountId")
    suspend fun maxTimelinePosition(accountId: String): Long

    @Query("DELETE FROM mastodon_posts WHERE accountId = :accountId")
    suspend fun clearTimeline(accountId: String)

    @Query("DELETE FROM mastodon_remote_keys WHERE accountId = :accountId")
    suspend fun clearRemoteKey(accountId: String)
}
