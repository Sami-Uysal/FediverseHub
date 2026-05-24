package com.samiuysal.fediversehub.feature.lemmy.data.remote

import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentsResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostsResponseDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import javax.inject.Inject

class LemmyKtorApi @Inject constructor(
    private val httpClient: HttpClient,
) : LemmyApi {
    override suspend fun login(
        instanceUrl: String,
        request: LemmyLoginRequestDto,
    ): LemmyLoginResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v3/user/login") {
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    override suspend fun getPosts(
        instanceUrl: String,
        accessToken: String?,
        page: Int,
        limit: Int,
        sort: String,
        feedType: String,
    ): LemmyPostsResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v3/post/list") {
            withAuth(accessToken)
            parameter("type_", feedType)
            parameter("sort", sort)
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }

    override suspend fun getPost(
        instanceUrl: String,
        accessToken: String?,
        postId: Int,
    ): LemmyPostResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v3/post") {
            withAuth(accessToken)
            parameter("id", postId)
        }.body()
    }

    override suspend fun getComments(
        instanceUrl: String,
        accessToken: String?,
        postId: Int,
        limit: Int,
    ): LemmyCommentsResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v3/comment/list") {
            withAuth(accessToken)
            parameter("type_", "All")
            parameter("sort", "Hot")
            parameter("post_id", postId)
            parameter("limit", limit)
            parameter("max_depth", 8)
        }.body()
    }

    private fun io.ktor.client.request.HttpRequestBuilder.withAuth(accessToken: String?) {
        accessToken?.takeIf { it.isNotBlank() }?.let { token ->
            bearerAuth(token)
            parameter("auth", token)
        }
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
