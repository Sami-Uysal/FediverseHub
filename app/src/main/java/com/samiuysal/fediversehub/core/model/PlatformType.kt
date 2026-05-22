package com.samiuysal.fediversehub.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class PlatformType {
    MASTODON,
    LEMMY,
    PIXELFED,
}
