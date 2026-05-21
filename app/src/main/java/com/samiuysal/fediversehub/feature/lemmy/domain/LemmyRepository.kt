package com.samiuysal.fediversehub.feature.lemmy.domain

import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import kotlinx.coroutines.flow.Flow

interface LemmyRepository {
    fun getPostsPagingData(
        account: Account,
        sort: LemmySortType = LemmySortType.HOT,
    ): Flow<PagingData<LemmyPost>>

    suspend fun getPosts(
        account: Account,
        page: LemmyPostPage = LemmyPostPage(),
    ): AppResult<List<LemmyPost>>
}
