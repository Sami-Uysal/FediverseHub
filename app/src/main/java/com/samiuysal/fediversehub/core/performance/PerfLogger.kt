package com.samiuysal.fediversehub.core.performance

import android.os.SystemClock
import android.util.Log
import com.samiuysal.fediversehub.BuildConfig

object PerfLogger {
    fun mark(name: String): PerfMark = PerfMark(name, SystemClock.elapsedRealtime())

    fun log(name: String, detail: String? = null) {
        if (!BuildConfig.DEBUG) return
        Log.d(TAG, buildMessage(name, detail, null))
    }

    fun end(mark: PerfMark, detail: String? = null) {
        if (!BuildConfig.DEBUG) return
        val elapsed = SystemClock.elapsedRealtime() - mark.startedAtMillis
        Log.d(TAG, buildMessage(mark.name, detail, elapsed))
    }

    private fun buildMessage(
        name: String,
        detail: String?,
        elapsedMillis: Long?,
    ): String = buildString {
        append(name)
        elapsedMillis?.let { append(" in ").append(it).append("ms") }
        detail?.takeIf(String::isNotBlank)?.let { append(" | ").append(it) }
    }

    private const val TAG = "FediversePerf"
}

data class PerfMark(
    val name: String,
    val startedAtMillis: Long,
)
