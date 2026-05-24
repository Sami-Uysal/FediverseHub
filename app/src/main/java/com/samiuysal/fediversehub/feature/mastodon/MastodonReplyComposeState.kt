package com.samiuysal.fediversehub.feature.mastodon

import androidx.compose.runtime.Immutable

const val MastodonReplyMaxCharacters = 500

enum class MastodonVisibility(
    val apiValue: String,
    val label: String,
) {
    PUBLIC("public", "Public"),
    UNLISTED("unlisted", "Unlisted"),
    PRIVATE("private", "Private"),
    DIRECT("direct", "Direct"),
}

@Immutable
data class MastodonReplyComposeState(
    val parent: MastodonPostUiModel,
    val text: String,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val maxCharacters: Int = MastodonReplyMaxCharacters,
)

@Immutable
data class MastodonNewPostComposeState(
    val text: String = "",
    val visibility: MastodonVisibility = MastodonVisibility.PUBLIC,
    val isContentWarningEnabled: Boolean = false,
    val contentWarning: String = "",
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val maxCharacters: Int = MastodonReplyMaxCharacters,
)

val MastodonReplyComposeState.canSend: Boolean
    get() {
        val trimmed = text.trim()
        val mention = parent.username.trim()
        val body = trimmed.removePrefix(mention).trim()
        return body.isNotBlank() && text.length <= maxCharacters
    }

val MastodonNewPostComposeState.canSend: Boolean
    get() = text.trim().isNotBlank() && text.length <= maxCharacters
