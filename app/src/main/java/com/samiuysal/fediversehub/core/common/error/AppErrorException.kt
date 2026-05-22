package com.samiuysal.fediversehub.core.common.error

class AppErrorException(
    val appError: AppError,
    override val message: String = appError.userMessage(),
) : RuntimeException(message)

fun AppError.userMessage(): String = when (this) {
    AppError.Network -> "Network connection failed. Check your connection and try again."
    AppError.RateLimited -> "Rate limit reached. Wait a moment, then retry."
    AppError.Unauthorized -> "Session expired. Log in again to refresh your Mastodon timeline."
    is AppError.Server -> "Server error ${code}. Try again shortly."
    is AppError.Unknown -> message ?: "Something went wrong. Try again."
}
