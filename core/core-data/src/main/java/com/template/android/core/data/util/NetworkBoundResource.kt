package com.template.android.core.data.util

import com.template.android.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

/**
 * Emits Loading, then either fetches from network + saves locally, or serves the cached value.
 * Feature repositories call this to implement an offline-first strategy.
 */
inline fun <Local, Network> networkBoundResource(
    crossinline query: () -> Flow<Local>,
    crossinline fetch: suspend () -> Network,
    crossinline saveFetchResult: suspend (Network) -> Unit,
    crossinline shouldFetch: (Local) -> Boolean = { true },
): Flow<Result<Local>> = flow {
    emit(Result.Loading)
    val cached = query().first()
    if (shouldFetch(cached)) {
        try {
            saveFetchResult(fetch())
        } catch (t: Throwable) {
            Timber.e(t, "Network fetch failed, serving stale cache")
            emitAll(query().map { Result.Error(t) })
            return@flow
        }
    }
    emitAll(query().map { Result.Success(it) })
}
