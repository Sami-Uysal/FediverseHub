package com.samiuysal.fediversehub.core.network

import com.samiuysal.fediversehub.core.common.error.AppError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException

object NetworkErrorMapper {
    fun map(throwable: Throwable): AppError = when (throwable) {
        is ClientRequestException -> when (throwable.response.status) {
            HttpStatusCode.Unauthorized -> AppError.Unauthorized
            HttpStatusCode.TooManyRequests -> AppError.RateLimited
            else -> AppError.Server(throwable.response.status.value)
        }
        is ServerResponseException -> AppError.Server(throwable.response.status.value)
        is IOException -> AppError.Network
        else -> AppError.Unknown(throwable.message)
    }
}
