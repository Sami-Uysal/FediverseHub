package com.samiuysal.fediversehub.core.network

import com.samiuysal.fediversehub.core.common.error.AppError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.serialization.SerializationException

object NetworkErrorMapper {
    fun map(throwable: Throwable): AppError = when (throwable) {
        is ClientRequestException -> when (throwable.response.status) {
            HttpStatusCode.Unauthorized -> AppError.Unauthorized
            HttpStatusCode.TooManyRequests -> AppError.RateLimited
            else -> AppError.Server(throwable.response.status.value)
        }
        is ServerResponseException -> AppError.Server(throwable.response.status.value)
        is HttpRequestTimeoutException -> AppError.Network
        is SocketTimeoutException -> AppError.Network
        is IOException -> AppError.Network
        is SerializationException -> AppError.Unknown(null)
        else -> AppError.Unknown(null)
    }
}
