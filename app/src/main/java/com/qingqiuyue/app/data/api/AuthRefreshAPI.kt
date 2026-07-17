package com.qingqiuyue.app.data.api

import com.qingqiuyue.app.data.api.dto.ApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

/**
 * Auth 专用 API 接口（用于登录和 Token 刷新）
 */
interface AuthRefreshAPI {

    @FormUrlEncoded
    @POST("api/core/auth/refresh")
    suspend fun refreshToken(@Field("refresh_token") refreshToken: String): ApiResponse<TokenResponse>

    @FormUrlEncoded
    @POST("api/core/auth/logout")
    suspend fun logout(@Field("refresh_token") refreshToken: String): ApiResponse<Unit>
}

/**
 * Token 响应
 */
data class TokenResponse(
    val token: String,
    val refreshToken: String? = null,
    val expiresIn: Long = 0
)
