package com.samiuysal.fediversehub.core.common.error

sealed class AppError {
    data object Network : AppError()
    data object Unauthorized : AppError()
    data object RateLimited : AppError()
    data class Server(val code: Int) : AppError()
    data class Unknown(val message: String?) : AppError()
}
