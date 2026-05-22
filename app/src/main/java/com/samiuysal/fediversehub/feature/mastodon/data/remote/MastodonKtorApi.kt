package com.samiuysal.fediversehub.feature.mastodon.data.remote

import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAccountDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonAppDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonTokenDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.FormDataContent
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.Parameters
import javax.inject.Inject

class MastodonKtorApi @Inject constructor(
    private val httpClient: HttpClient,
) : MastodonApi {
    override suspend fun registerApp(
        instanceUrl: String,
        clientName: String,
        redirectUri: String,
        scopes: String,
        website: String?,
    ): MastodonAppDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v1/apps") {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("client_name", clientName)
                        append("redirect_uris", redirectUri)
                        append("scopes", scopes)
                        website?.let { append("website", it) }
                    },
                ),
            )
        }.body()
    }

    override suspend fun exchangeCodeForToken(
        instanceUrl: String,
        clientId: String,
        clientSecret: String,
        redirectUri: String,
        code: String,
        scopes: String,
    ): MastodonTokenDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/oauth/token") {
            setBody(
                FormDataContent(
                    Parameters.build {
                        append("grant_type", "authorization_code")
                        append("client_id", clientId)
                        append("client_secret", clientSecret)
                        append("redirect_uri", redirectUri)
                        append("code", code)
                        append("scope", scopes)
                    },
                ),
            )
        }.body()
    }

    override suspend fun verifyCredentials(
        instanceUrl: String,
        accessToken: String,
    ): MastodonAccountDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v1/accounts/verify_credentials") {
            bearerAuth(accessToken)
        }.body()
    }

    override suspend fun getHomeTimeline(
        instanceUrl: String,
        accessToken: String,
        page: MastodonTimelinePage,
    ): List<MastodonStatusDto> {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v1/timelines/home") {
            bearerAuth(accessToken)
            parameter("limit", page.limit)
            page.maxId?.let { parameter("max_id", it) }
            page.sinceId?.let { parameter("since_id", it) }
            page.minId?.let { parameter("min_id", it) }
        }.body()
    }

    private fun String.normalizedHttpsBaseUrl(): String {
        val trimmed = trim().trimEnd('/')
        return when {
            trimmed.startsWith("https://") -> trimmed
            trimmed.startsWith("http://") -> trimmed.replaceFirst("http://", "https://")
            else -> "https://$trimmed"
        }
    }
}
