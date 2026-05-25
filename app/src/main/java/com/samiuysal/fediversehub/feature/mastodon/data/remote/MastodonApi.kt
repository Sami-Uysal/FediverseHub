package com.samiuysal.fediversehub.feature.mastodon.data.remote

import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAppDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonContextDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonNotificationDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonHashtagDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonPreviewCardDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonRelationshipDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonSearchDto
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

    suspend fun getAccount(
        instanceUrl: String,
        accessToken: String,
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

    suspend fun getHashtagTimeline(
        instanceUrl: String,
        accessToken: String,
        hashtag: String,
        maxId: String? = null,
        limit: Int = 30,
    ): List<MastodonStatusDto>

    suspend fun search(
        instanceUrl: String,
        accessToken: String,
        query: String,
        type: String,
        limit: Int = 20,
    ): MastodonSearchDto

    suspend fun getTrendingStatuses(
        instanceUrl: String,
        accessToken: String?,
        limit: Int = 20,
    ): List<MastodonStatusDto>

    suspend fun getTrendingTags(
        instanceUrl: String,
        accessToken: String?,
        limit: Int = 20,
    ): List<MastodonHashtagDto>

    suspend fun getTrendingLinks(
        instanceUrl: String,
        accessToken: String?,
        limit: Int = 20,
    ): List<MastodonPreviewCardDto>

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
