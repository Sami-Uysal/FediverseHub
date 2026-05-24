package com.samiuysal.fediversehub.core.common.error

class AppErrorException(
    val appError: AppError,
    override val message: String = appError.userMessage(),
) : RuntimeException(message)

fun Throwable.userFacingMessage(fallback: String): String =
    (this as? AppErrorException)?.appError?.userMessage() ?: fallback

fun AppError.userMessage(): String = when (this) {
    AppError.Network -> "İnternet bağlantısı yok gibi. Bağlantını kontrol et."
    AppError.RateLimited -> "Çok hızlı istek atıldı. Biraz bekle, tekrar dene."
    AppError.Unauthorized -> "Oturum süresi doldu. Hesaba tekrar giriş yap."
    is AppError.Server -> "Sunucu şu an yanıt veremiyor. Biraz sonra tekrar dene."
    is AppError.Unknown -> "Bir şey ters gitti. Tekrar dene."
}
