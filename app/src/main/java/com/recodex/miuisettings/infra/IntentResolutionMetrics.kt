package com.recodex.miuisettings.infra

import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IntentResolutionMetrics @Inject constructor() {
    private val totalAttempts = AtomicLong(0)
    private val totalSuccesses = AtomicLong(0)

    fun recordResult(successful: Boolean): ResolutionSnapshot {
        val attempts = totalAttempts.incrementAndGet()
        val successes = if (successful) {
            totalSuccesses.incrementAndGet()
        } else {
            totalSuccesses.get()
        }
        return ResolutionSnapshot(attempts, successes)
    }

    data class ResolutionSnapshot(
        val totalAttempts: Long,
        val totalSuccesses: Long
    ) {
        val successRate: Double
            get() = if (totalAttempts == 0L) 0.0 else totalSuccesses.toDouble() / totalAttempts.toDouble()
    }
}
