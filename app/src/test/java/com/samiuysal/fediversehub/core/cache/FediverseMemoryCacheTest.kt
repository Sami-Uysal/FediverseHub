package com.samiuysal.fediversehub.core.cache

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test

class FediverseMemoryCacheTest {
    @Test
    fun getOrPut_reusesCachedValueWithinTtl() = runBlocking {
        val cache = FediverseMemoryCache()
        var loadCount = 0

        val first = cache.getOrPut("mastodon:public:post:1") {
            loadCount += 1
            "first"
        }
        val second = cache.getOrPut("mastodon:public:post:1") {
            loadCount += 1
            "second"
        }

        assertEquals("first", first)
        assertEquals("first", second)
        assertEquals(1, loadCount)
    }

    @Test
    fun getOrPut_dedupesInFlightLoads() = runBlocking {
        val cache = FediverseMemoryCache()
        var loadCount = 0

        val values = List(3) {
            async {
                cache.getOrPut("lemmy:public:post:42") {
                    loadCount += 1
                    "value"
                }
            }
        }.awaitAll()

        assertEquals(listOf("value", "value", "value"), values)
        assertEquals(1, loadCount)
    }
}
