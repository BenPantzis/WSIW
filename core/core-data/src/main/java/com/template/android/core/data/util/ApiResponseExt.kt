package com.template.android.core.data.util

import com.template.android.core.common.Result
import com.template.android.core.network.model.ApiResponse

fun <T> ApiResponse<T>.toResult(): Result<T> = when (this) {
    is ApiResponse.Success -> Result.Success(data)
    is ApiResponse.HttpError -> Result.Error(ApiException(code, message))
    is ApiResponse.NetworkError -> Result.Error(throwable)
}

class ApiException(val code: Int, message: String?) : Exception("HTTP $code: $message")
