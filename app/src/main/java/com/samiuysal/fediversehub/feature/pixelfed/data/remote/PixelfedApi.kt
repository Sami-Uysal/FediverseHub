package com.samiuysal.fediversehub.feature.pixelfed.data.remote

import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAppDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonContextDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonNotificationDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonRelationshipDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonSearchDto
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

    suspend fun getAccount(
        instanceUrl: String,
        accessToken: String?,
        accountId: String,
    ): MastodonAccountDto

    suspend fun getRelationships(
        instanceUrl: String,
        accessToken: String,
        accountIds: List<String>,
    ): List<MastodonRelationshipDto>

    suspend fun followAccount(
        instanceUrl: String,
        accessToken: String,
        accountId: String,
    ): MastodonRelationshipDto

    suspend fun unfollowAccount(
        instanceUrl: String,
        accessToken: String,
        accountId: String,
    ): MastodonRelationshipDto

    suspend fun search(
        instanceUrl: String,
        accessToken: String?,
        query: String,
        type: String,
        limit: Int,
    ): MastodonSearchDto

    suspend fun getNotifications(
        instanceUrl: String,
        accessToken: String,
        maxId: String? = null,
        limit: Int = 40,
    ): List<MastodonNotificationDto>

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
        accessToken: String?,
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

    suspend fun getStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto

    suspend fun postComment(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
        text: String,
    ): MastodonStatusDto

    suspend fun createStatus(
        instanceUrl: String,
        accessToken: String,
        text: String,
    ): MastodonStatusDto

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
