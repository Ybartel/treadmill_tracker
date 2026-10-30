package com.naynff.treadmilltracker.domain.usecase

import kotlin.coroutines.cancellation.CancellationException

/** Like runCatching, but lets coroutine cancellation through. */
internal inline fun <T> resultOf(block: () -> T): Result<T> = try {
    Result.success(block())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    Result.failure(e)
}
