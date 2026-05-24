package com.samiuysal.fediversehub.feature.lemmy.data.remote

import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentsResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentActionResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunitiesResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunityResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostActionResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostsResponseDto

interface LemmyApi {
    suspend fun login(instanceUrl: String, request: LemmyLoginRequestDto): LemmyLoginResponseDto

    suspend fun getPosts(
        instanceUrl: String,
        accessToken: String?,
        page: Int,
        limit: Int,
        sort: String,
        feedType: String,
        communityName: String? = null,
    ): LemmyPostsResponseDto

    suspend fun getPost(
        instanceUrl: String,
        accessToken: String?,
        postId: Int,
    ): LemmyPostResponseDto

    suspend fun getComments(
        instanceUrl: String,
        accessToken: String?,
        postId: Int,
        limit: Int,
    ): LemmyCommentsResponseDto

    suspend fun votePost(
        instanceUrl: String,
        accessToken: String,
        postId: Int,
        score: Int,
    ): LemmyPostActionResponseDto

    suspend fun savePost(
        instanceUrl: String,
        accessToken: String,
        postId: Int,
        saved: Boolean,
    ): LemmyPostActionResponseDto

    suspend fun voteComment(
        instanceUrl: String,
        accessToken: String,
        commentId: Int,
        score: Int,
    ): LemmyCommentActionResponseDto

    suspend fun getCommunity(
        instanceUrl: String,
        accessToken: String?,
        communityName: String,
    ): LemmyCommunityResponseDto

    suspend fun getCommunities(
        instanceUrl: String,
        accessToken: String?,
        page: Int,
        limit: Int,
        sort: String,
        feedType: String,
    ): LemmyCommunitiesResponseDto

    suspend fun followCommunity(
        instanceUrl: String,
        accessToken: String,
        communityId: Int,
        follow: Boolean,
    ): LemmyCommunityResponseDto
}
