package com.samiuysal.fediversehub.feature.lemmy.data.remote

import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginRequestDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyLoginResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentsResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommentActionResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunitiesResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCommunityResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostActionResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCreateCommentResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyCreatePostResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyPostsResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmySiteResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmySearchResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyRepliesResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyMentionsResponseDto
import com.samiuysal.fediversehub.feature.lemmy.data.dto.LemmyUserResponseDto

interface LemmyApi {
    suspend fun login(instanceUrl: String, request: LemmyLoginRequestDto): LemmyLoginResponseDto

    suspend fun getSite(instanceUrl: String, accessToken: String?): LemmySiteResponseDto

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

    suspend fun search(
        instanceUrl: String,
        accessToken: String?,
        query: String,
        type: String,
        page: Int,
        limit: Int,
        sort: String,
    ): LemmySearchResponseDto

    suspend fun getReplies(
        instanceUrl: String,
        accessToken: String,
        unreadOnly: Boolean,
        page: Int,
        limit: Int,
    ): LemmyRepliesResponseDto

    suspend fun getMentions(
        instanceUrl: String,
        accessToken: String,
        unreadOnly: Boolean,
        page: Int,
        limit: Int,
    ): LemmyMentionsResponseDto

    suspend fun createComment(
        instanceUrl: String,
        accessToken: String,
        postId: Int,
        parentId: Int?,
        content: String,
    ): LemmyCreateCommentResponseDto

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

    suspend fun createPost(
        instanceUrl: String,
        accessToken: String,
        communityId: Int,
        title: String,
        body: String?,
        url: String?,
    ): LemmyCreatePostResponseDto

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

    suspend fun getUser(
        instanceUrl: String,
        accessToken: String?,
        username: String,
        page: Int,
        limit: Int,
        sort: String,
        savedOnly: Boolean = false,
    ): LemmyUserResponseDto
}
