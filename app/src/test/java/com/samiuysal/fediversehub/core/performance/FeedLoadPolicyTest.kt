package com.samiuysal.fediversehub.core.performance

import com.samiuysal.fediversehub.core.model.PlatformType
import org.junit.Assert.assertEquals
import org.junit.Test

class FeedLoadPolicyTest {
    @Test
    fun pageSize_usesNormalReleaseDefaults() {
        assertEquals(30, FeedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.HOME, false))
        assertEquals(12, FeedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.HOME, false))
        assertEquals(15, FeedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.EXPLORE, false))
        assertEquals(20, FeedLoadPolicy.pageSize(PlatformType.LEMMY, FeedSurface.HOME, false))
    }

    @Test
    fun pageSize_usesSmallerDataSaverDefaults() {
        assertEquals(10, FeedLoadPolicy.pageSize(PlatformType.MASTODON, FeedSurface.HOME, true))
        assertEquals(6, FeedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.HOME, true))
        assertEquals(9, FeedLoadPolicy.pageSize(PlatformType.PIXELFED, FeedSurface.EXPLORE, true))
        assertEquals(10, FeedLoadPolicy.pageSize(PlatformType.LEMMY, FeedSurface.HOME, true))
    }
}
