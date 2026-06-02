package com.template.android.core.network.model

sealed interface ApiResponse<out T> {
    data class Success<T>(val data: T) : ApiResponse<T>
    data class HttpError(val code: Int, val message: String?) : ApiResponse<Nothing>
    data class NetworkError(val throwable: Throwable) : ApiResponse<Nothing>
}
