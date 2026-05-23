package com.samiuysal.fediversehub.feature.pixelfed.data.remote

import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAppDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonContextDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.pixelfed.data.dto.PixelfedTokenDto

interface PixelfedApi {
    suspend fun registerApp(
        instanceUrl: String,
        clientName: String,
        redirectUri: String,
        scopes: String,
        website: String?,
    ): MastodonAppDto

    suspend fun exchangeCodeForToken(
        instanceUrl: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        code: String,
        scopes: String,
    ): PixelfedTokenDto

    suspend fun verifyCredentials(instanceUrl: String, accessToken: String): MastodonAccountDto

    suspend fun getHomeFeed(
        instanceUrl: String,
        accessToken: String,
        maxId: String?,
        limit: Int,
    ): List<MastodonStatusDto>

    suspend fun getPublicFeed(
        instanceUrl: String,
        accessToken: String?,
        maxId: String?,
        limit: Int,
        local: Boolean = true,
    ): List<MastodonStatusDto>

    suspend fun getAccountStatuses(
        instanceUrl: String,
        accessToken: String,
        accountId: String,
        maxId: String?,
        limit: Int,
        onlyMedia: Boolean,
    ): List<MastodonStatusDto>

    suspend fun getStatusContext(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonContextDto

    suspend fun favouriteStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto

    suspend fun unfavouriteStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto
}
