package com.samiuysal.fediversehub.core.cache

import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class FediverseMemoryCache @Inject constructor() {
    private val mutex = Mutex()
    private val entries = LinkedHashMap<String, Entry>(MAX_ENTRIES, 0.75f, true)
    private val inFlight = mutableMapOf<String, CompletableDeferred<Any>>()

    @Suppress("UNCHECKED_CAST")
    suspend fun <T : Any> getOrPut(
        key: String,
        ttlMillis: Long = DEFAULT_TTL_MILLIS,
        loader: suspend () -> T,
    ): T {
        val now = System.currentTimeMillis()
        val existing = mutex.withLock {
            entries[key]
                ?.takeIf { it.expiresAtMillis > now }
                ?.value
                ?.let { return it as T }

            inFlight[key]?.let { return@withLock CacheLookup.Waiting(it) }

            val deferred = CompletableDeferred<Any>()
            inFlight[key] = deferred
            CacheLookup.Owner(deferred)
        }

        return when (existing) {
            is CacheLookup.Waiting -> existing.deferred.await() as T
            is CacheLookup.Owner -> loadAndStore(
                key = key,
                ttlMillis = ttlMillis,
                deferred = existing.deferred,
                loader = loader,
            )
        }
    }

    suspend fun <T : Any> put(
        key: String,
        value: T,
        ttlMillis: Long = DEFAULT_TTL_MILLIS,
    ) {
        mutex.withLock {
            entries[key] = Entry(value = value, expiresAtMillis = System.currentTimeMillis() + ttlMillis)
            pruneLocked()
        }
    }

    suspend fun invalidate(key: String) {
        mutex.withLock {
            entries.remove(key)
            inFlight.remove(key)
        }
    }

    private suspend fun <T : Any> loadAndStore(
        key: String,
        ttlMillis: Long,
        deferred: CompletableDeferred<Any>,
        loader: suspend () -> T,
    ): T {
        return try {
            val value = loader()
            mutex.withLock {
                entries[key] = Entry(
                    value = value,
                    expiresAtMillis = System.currentTimeMillis() + ttlMillis,
                )
                inFlight.remove(key)
                pruneLocked()
            }
            deferred.complete(value)
            value
        } catch (throwable: Throwable) {
            mutex.withLock {
                inFlight.remove(key)
            }
            deferred.completeExceptionally(throwable)
            throw throwable
        }
    }

    private fun pruneLocked() {
        while (entries.size > MAX_ENTRIES) {
            val firstKey = entries.keys.firstOrNull() ?: return
            entries.remove(firstKey)
        }
    }

    private data class Entry(
        val value: Any,
        val expiresAtMillis: Long,
    )

    private sealed interface CacheLookup {
        data class Owner(val deferred: CompletableDeferred<Any>) : CacheLookup
        data class Waiting(val deferred: CompletableDeferred<Any>) : CacheLookup
    }

    companion object {
        const val DEFAULT_TTL_MILLIS = 5 * 60 * 1000L
        const val SHORT_TTL_MILLIS = 60 * 1000L
        private const val MAX_ENTRIES = 500
    }
}
