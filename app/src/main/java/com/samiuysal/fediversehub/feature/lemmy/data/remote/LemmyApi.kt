package com.samiuysal.fediversehub.feature.lemmy.data.remote

import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentsResponseDto
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
}
