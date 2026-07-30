package com.bsp.wsiw.core.data.util

import com.bsp.wsiw.core.common.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import timber.log.Timber

@Suppress("TooGenericExceptionCaught")
inline fun <Local, Network> networkBoundResource(
    crossinline query: () -> Flow<Local>,
    crossinline fetch: suspend () -> Network,
    crossinline saveFetchResult: suspend (Network) -> Unit,
    crossinline shouldFetch: (Local) -> Boolean = { true },
): Flow<Result<Local>> = flow {
    emit(Result.Loading)
    val cached = query().first()
    if (shouldFetch(cached)) {
        // Serve stale cache immediately so the UI renders while the network request is in-flight
        emit(Result.Success(cached, isRefreshing = true))
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
