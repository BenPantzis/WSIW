package com.bsp.wsiw.core.data.util

import com.bsp.wsiw.core.common.Result
import com.bsp.wsiw.core.network.model.ApiResponse

fun <T> ApiResponse<T>.toResult(): Result<T> = when (this) {
    is ApiResponse.Success -> Result.Success(data)
    is ApiResponse.HttpError -> Result.Error(ApiException(code, message))
    is ApiResponse.NetworkError -> Result.Error(throwable)
}

class ApiException(val code: Int, message: String?) : Exception("HTTP $code: $message")
