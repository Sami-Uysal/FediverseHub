package com.samiuysal.fediversehub.di

import android.content.Context
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.samiuysal.fediversehub.core.database.AppDatabase
import com.samiuysal.fediversehub.feature.mastodon.data.local.MastodonTimelineDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context,
    ): AppDatabase = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "fediversehub.db",
    )
        .addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
        .fallbackToDestructiveMigration(true)
        .build()

    @Provides
    fun provideMastodonTimelineDao(
        database: AppDatabase,
    ): MastodonTimelineDao = database.mastodonTimelineDao()

    private val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE mastodon_posts ADD COLUMN statusRemoteId TEXT NOT NULL DEFAULT ''")
            db.execSQL("UPDATE mastodon_posts SET statusRemoteId = remoteId WHERE statusRemoteId = ''")
        }
    }

    private val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE mastodon_posts ADD COLUMN isReblogged INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE mastodon_posts ADD COLUMN isFavourited INTEGER NOT NULL DEFAULT 0")
            db.execSQL("ALTER TABLE mastodon_posts ADD COLUMN isBookmarked INTEGER NOT NULL DEFAULT 0")
        }
    }

    private val MIGRATION_4_5 = object : Migration(4, 5) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL("ALTER TABLE mastodon_posts ADD COLUMN visibility TEXT NOT NULL DEFAULT 'public'")
        }
    }
}
