package com.samiuysal.fediversehub.feature.mastodon.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class MastodonAppDto(
    @SerialName("client_id") val clientId: String,
    @SerialName("client_secret") val clientSecret: String,
    val id: String? = null,
    val name: String? = null,
    val website: String? = null,
    @SerialName("redirect_uri") val redirectUri: String? = null,
)
