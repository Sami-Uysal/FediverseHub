package com.samiuysal.fediversehub.core.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [AccountEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase()
