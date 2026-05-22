package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonPreviewCardDto(
    val url: String? = null,
    val title: String = "",
    val description: String = "",
    val image: String? = null,
    @SerialName("provider_name") val providerName: String? = null,
)
