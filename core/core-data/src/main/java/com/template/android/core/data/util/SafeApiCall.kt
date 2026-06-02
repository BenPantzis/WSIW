package com.template.android.core.data.util

import com.template.android.core.common.Result
import retrofit2.HttpException
import java.io.IOException

suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> = try {
    Result.Success(apiCall())
} catch (e: HttpException) {
    Result.Error(e)
} catch (e: IOException) {
    Result.Error(e)
}
