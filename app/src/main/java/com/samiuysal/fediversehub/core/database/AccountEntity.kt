package com.samiuysal.fediversehub.core.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.samiuysal.fediversehub.core.model.PlatformType

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey val id: String,
    val platform: PlatformType,
    val instanceUrl: String,
    val username: String,
    val displayName: String?,
    val avatarUrl: String?,
    val accessToken: String?,
)
