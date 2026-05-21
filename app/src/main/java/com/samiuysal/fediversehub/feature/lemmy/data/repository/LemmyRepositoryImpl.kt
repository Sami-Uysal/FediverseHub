package com.samiuysal.fediversehub.feature.lemmy.data.repository

import androidx.paging.PagingData
import com.samiuysal.fediversehub.core.common.error.AppError
import com.samiuysal.fediversehub.core.common.result.AppResult
import com.samiuysal.fediversehub.core.model.Account
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPost
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyPostPage
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmyRepository
import com.samiuysal.fediversehub.feature.lemmy.domain.LemmySortType
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class LemmyRepositoryImpl @Inject constructor() : LemmyRepository {
    override fun getPostsPagingData(
        account: Account,
        sort: LemmySortType,
    ): Flow<PagingData<LemmyPost>> = flow {
        emit(PagingData.empty())
    }

    override suspend fun getPosts(
        account: Account,
        page: LemmyPostPage,
    ): AppResult<List<LemmyPost>> = AppResult.Failure(AppError.Network)
}
