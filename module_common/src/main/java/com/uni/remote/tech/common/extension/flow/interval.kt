package com.uni.remote.tech.common.extension.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.util.concurrent.TimeUnit
import kotlin.time.Duration

fun interval(
    initialDelay: Long = 0L,
    delay: Long,
    unit: TimeUnit = TimeUnit.MILLISECONDS,
    initialCount: Long = 0L
): Flow<Long> {
    require(initialDelay >= 0) { "Expected non-negative delay, but has $initialDelay ms" }
    require(delay >= 0) { "Expected non-negative delay, but has $delay ms" }
    return flow {
        delay(unit.toMillis(initialDelay))

        var count = initialCount
        while (true) {
            emit(count++)
            delay(unit.toMillis(delay))
        }
    }
}

/**
 * Returns a [Flow] that emits a 0L after the [initialDelay] and ever-increasing numbers
 * after each [period] of time thereafter.
 *
 * @param initialDelay must be greater than or equal to [Duration.ZERO]
 * @param period must be greater than or equal to [Duration.ZERO]
 */
fun interval(
    initialDelay: Duration,
    period: Duration,
): Flow<Long> {
    require(initialDelay >= Duration.ZERO) { "Expected non-negative delay, but has $initialDelay ms" }
    require(period >= Duration.ZERO) { "Expected non-negative period, but has $period ms" }

    return flow {
        delay(initialDelay)

        var count = 0L
        while (true) {
            emit(count++)
            delay(period)
        }
    }
}

/**
 * Returns a [Flow] that emits a 0L after the [initialDelayMillis] and ever-increasing numbers
 * after each [periodMillis] of time thereafter.
 *
 * @param initialDelayMillis must be non-negative
 * @param periodMillis must be non-negative
 */
fun interval(
    initialDelayMillis: Long,
    periodMillis: Long,
): Flow<Long> {
    require(initialDelayMillis >= 0) { "Expected non-negative delay, but has $initialDelayMillis ms" }
    require(periodMillis >= 0) { "Expected non-negative periodMillis, but has $periodMillis ms" }

    return flow {
        delay(initialDelayMillis)

        var count = 0L
        while (true) {
            emit(count++)
            delay(periodMillis)
        }
    }
}
