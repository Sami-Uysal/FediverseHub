package com.samiuysal.fediversehub.feature.mastodon.data.remote

import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAppDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonContextDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonNotificationDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonTokenDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage

interface MastodonApi {
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
    ): MastodonTokenDto

    suspend fun verifyCredentials(
        instanceUrl: String,
        accessToken: String,
    ): MastodonAccountDto

    suspend fun getHomeTimeline(
        instanceUrl: String,
        accessToken: String,
        page: MastodonTimelinePage = MastodonTimelinePage(),
    ): List<MastodonStatusDto>

    suspend fun getStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto

    suspend fun getStatusContext(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonContextDto

    suspend fun getNotifications(
        instanceUrl: String,
        accessToken: String,
        maxId: String? = null,
        limit: Int = 30,
    ): List<MastodonNotificationDto>

    suspend fun getAccountStatuses(
        instanceUrl: String,
        accessToken: String,
        accountId: String,
        maxId: String? = null,
        limit: Int = 30,
        excludeReplies: Boolean = false,
        onlyMedia: Boolean = false,
    ): List<MastodonStatusDto>

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

    suspend fun boostStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto

    suspend fun unboostStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto

    suspend fun bookmarkStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto

    suspend fun unbookmarkStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
    ): MastodonStatusDto

    suspend fun replyToStatus(
        instanceUrl: String,
        accessToken: String,
        statusId: String,
        text: String,
        visibility: String,
    ): MastodonStatusDto

    suspend fun createStatus(
        instanceUrl: String,
        accessToken: String,
        text: String,
        visibility: String,
        spoilerText: String?,
    ): MastodonStatusDto
}
