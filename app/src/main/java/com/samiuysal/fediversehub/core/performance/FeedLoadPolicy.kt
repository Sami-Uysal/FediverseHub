package com.samiuysal.fediversehub.core.performance

import com.samiuysal.fediversehub.core.model.PlatformType
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedLoadPolicy @Inject constructor() {
    val isLowBandwidth: Boolean
        get() = false

    fun pageSize(platform: PlatformType, surface: FeedSurface): Int =
        pageSize(platform = platform, surface = surface, lowBandwidth = isLowBandwidth)

    companion object {
        fun pageSize(
            platform: PlatformType,
            surface: FeedSurface,
            lowBandwidth: Boolean,
        ): Int =
            when (platform) {
                PlatformType.MASTODON -> if (lowBandwidth) 10 else 30
                PlatformType.PIXELFED -> when (surface) {
                    FeedSurface.HOME -> if (lowBandwidth) 6 else 12
                    FeedSurface.PROFILE -> if (lowBandwidth) 12 else 24
                    FeedSurface.EXPLORE -> if (lowBandwidth) 9 else 15
                    FeedSurface.DETAIL -> if (lowBandwidth) 6 else 12
                }
                PlatformType.LEMMY -> if (lowBandwidth) 10 else 20
            }
    }
}

enum class FeedSurface {
    HOME,
    PROFILE,
    EXPLORE,
    DETAIL,
}
