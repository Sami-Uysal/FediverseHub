package com.samiuysal.fediversehub.feature.lemmy.data.remote

import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentsResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentActionRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentActionResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunitiesResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunityFollowRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunityResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCreateCommentRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCreateCommentResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCreatePostRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCreatePostResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostActionRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostActionResponseDto
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
        communityName: String?,
    ): LemmyPostsResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v3/post/list") {
            withAuth(accessToken)
            parameter("type_", feedType)
            parameter("sort", sort)
            parameter("page", page)
            parameter("limit", limit)
            communityName?.takeIf(String::isNotBlank)?.let { parameter("community_name", it) }
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

    override suspend fun votePost(
        instanceUrl: String,
        accessToken: String,
        postId: Int,
        score: Int,
    ): LemmyPostActionResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v3/post/like") {
            bearerAuth(accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(LemmyPostActionRequestDto(postId = postId, score = score, auth = accessToken))
        }.body()
    }

    override suspend fun createComment(
        instanceUrl: String,
        accessToken: String,
        postId: Int,
        parentId: Int?,
        content: String,
    ): LemmyCreateCommentResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v3/comment") {
            bearerAuth(accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
                LemmyCreateCommentRequestDto(
                    postId = postId,
                    parentId = parentId,
                    content = content,
                    auth = accessToken,
                ),
            )
        }.body()
    }

    override suspend fun savePost(
        instanceUrl: String,
        accessToken: String,
        postId: Int,
        saved: Boolean,
    ): LemmyPostActionResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v3/post/save") {
            bearerAuth(accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(LemmyPostActionRequestDto(postId = postId, save = saved, auth = accessToken))
        }.body()
    }

    override suspend fun createPost(
        instanceUrl: String,
        accessToken: String,
        communityId: Int,
        title: String,
        body: String?,
        url: String?,
    ): LemmyCreatePostResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v3/post") {
            bearerAuth(accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
                LemmyCreatePostRequestDto(
                    communityId = communityId,
                    name = title,
                    body = body?.takeIf(String::isNotBlank),
                    url = url?.takeIf(String::isNotBlank),
                    auth = accessToken,
                ),
            )
        }.body()
    }

    override suspend fun voteComment(
        instanceUrl: String,
        accessToken: String,
        commentId: Int,
        score: Int,
    ): LemmyCommentActionResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v3/comment/like") {
            bearerAuth(accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(LemmyCommentActionRequestDto(commentId = commentId, score = score, auth = accessToken))
        }.body()
    }

    override suspend fun getCommunity(
        instanceUrl: String,
        accessToken: String?,
        communityName: String,
    ): LemmyCommunityResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v3/community") {
            withAuth(accessToken)
            parameter("name", communityName)
        }.body()
    }

    override suspend fun getCommunities(
        instanceUrl: String,
        accessToken: String?,
        page: Int,
        limit: Int,
        sort: String,
        feedType: String,
    ): LemmyCommunitiesResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.get("$baseUrl/api/v3/community/list") {
            withAuth(accessToken)
            parameter("type_", feedType)
            parameter("sort", sort)
            parameter("page", page)
            parameter("limit", limit)
        }.body()
    }

    override suspend fun followCommunity(
        instanceUrl: String,
        accessToken: String,
        communityId: Int,
        follow: Boolean,
    ): LemmyCommunityResponseDto {
        val baseUrl = instanceUrl.normalizedHttpsBaseUrl()
        return httpClient.post("$baseUrl/api/v3/community/follow") {
            bearerAuth(accessToken)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            setBody(
                LemmyCommunityFollowRequestDto(
                    communityId = communityId,
                    follow = follow,
                    auth = accessToken,
                ),
            )
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
