package com.samiuysal.fediversehub.feature.mastodon

import androidx.compose.runtime.Immutable

const val MastodonReplyMaxCharacters = 500

@Immutable
data class MastodonReplyComposeState(
    val parent: MastodonPostUiModel,
    val text: String,
    val isSending: Boolean = false,
    val errorMessage: String? = null,
    val maxCharacters: Int = MastodonReplyMaxCharacters,
)
