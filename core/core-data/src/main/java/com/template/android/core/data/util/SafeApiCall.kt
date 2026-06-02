package com.template.android.core.data.util

import com.template.android.core.common.Result
import retrofit2.HttpException
import timber.log.Timber
import java.io.IOException

// Catching only HttpException and IOException (not bare Exception) ensures
// CancellationException is never swallowed, keeping coroutine cancellation intact.
suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> = try {
    Result.Success(apiCall())
} catch (e: HttpException) {
    Timber.e(e, "HTTP error %d", e.code())
    Result.Error(e)
} catch (e: IOException) {
    Timber.e(e, "Network error")
    Result.Error(e)
}
