package com.bsp.wsiw.core.data.remote

import com.bsp.wsiw.core.data.remote.model.AccessTokenBody
import com.bsp.wsiw.core.data.remote.model.AccessTokenDto
import com.bsp.wsiw.core.data.remote.model.AccountDto
import com.bsp.wsiw.core.data.remote.model.DeleteTokenDto
import com.bsp.wsiw.core.data.remote.model.RequestTokenBody
import com.bsp.wsiw.core.data.remote.model.RequestTokenDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.POST

interface TmdbAuthService {
    @POST("4/auth/request_token")
    suspend fun getRequestToken(@Body body: RequestTokenBody): RequestTokenDto

    @POST("4/auth/access_token")
    suspend fun getAccessToken(@Body body: AccessTokenBody): AccessTokenDto

    @HTTP(method = "DELETE", path = "4/auth/access_token", hasBody = true)
    suspend fun deleteAccessToken(@Body body: AccessTokenBody): DeleteTokenDto

    @GET("3/account")
    suspend fun getAccount(): AccountDto
}
