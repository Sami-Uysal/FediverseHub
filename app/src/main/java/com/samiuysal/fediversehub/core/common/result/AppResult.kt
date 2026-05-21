package com.samiuysal.fediversehub.core.common.result

import com.samiuysal.fediversehub.core.common.error.AppError

sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Failure(val error: AppError) : AppResult<Nothing>
}
