package com.samiuysal.fediversehub.feature.mastodon.data.remote

import com.samiuysal.fediversehub.feature.mastodon.data.dto.MastodonStatusDto
import com.samiuysal.fediversehub.feature.mastodon.domain.MastodonTimelinePage
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import javax.inject.Inject

class MastodonKtorApi @Inject constructor(
    private val httpClient: HttpClient,
) : MastodonApi {
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
