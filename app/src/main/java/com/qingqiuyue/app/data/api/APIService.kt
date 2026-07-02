package com.qingqiuyue.app.data.api

import com.qingqiuyue.app.data.api.dto.*
import retrofit2.http.*

/**
 * 统一 API 接口定义。所有请求挂 /api/* 前缀(网关路由到 core/content/gen 等)。
 */
interface APIService {

    // 认证
    @POST("api/core/auth/login")
    suspend fun login(@Body req: LoginRequest): ApiResponse<LoginResponse>

    // 内容
    @GET("api/content/recommend/feed")
    suspend fun feed(): ApiResponse<FeedResponse>

    @GET("api/content/video/list")
    suspend fun videoList(@Query("page") page: Int = 1): ApiResponse<PagedListResponse<VideoItem>>

    @GET("api/content/video/detail")
    suspend fun videoDetail(@Query("id") id: Long): ApiResponse<VideoItem>

    // AIGC
    @GET("api/ai/generate/workflows")
    suspend fun workflows(): ApiResponse<List<WorkflowItem>>

    @POST("api/ai/generate/video")
    suspend fun generateVideo(@Body req: GenerateRequest): ApiResponse<GenerateResponse>

    // 数字人(SSE 流式,见 ChatAPIClient,不在此 retrofit interface 中)

    // 钱包 / 支付
    @GET("api/core/wallet/balance")
    suspend fun walletBalance(): ApiResponse<WalletBalance>

    @GET("api/core/payment/diamond-packages")
    suspend fun diamondPackages(): ApiResponse<List<DiamondPackage>>

    @POST("api/core/payment/orders")
    suspend fun createOrder(@Body req: CreateOrderRequest): ApiResponse<Order>

    @POST("api/core/payment/mock-pay")
    suspend fun mockPay(@Body req: MockPayRequest): ApiResponse<Unit>

    // 创作者中心
    @GET("api/core/account/works")
    suspend fun accountWorks(@Query("page") page: Int = 1): ApiResponse<PagedListResponse<WorkItem>>

    @GET("api/core/account/monetize/summary")
    suspend fun monetizeSummary(): ApiResponse<MonetizeSummary>

    @POST("api/core/account/apply")
    suspend fun applyCreator(@Body req: ApplyRequest): ApiResponse<ApplyResponse>

    @GET("api/core/account/status")
    suspend fun creatorStatus(): ApiResponse<ApplyResponse>

    // 审核
    @POST("api/core/moderation/check")
    suspend fun moderateText(@Body req: ModerateTextRequest): ApiResponse<ModerateTextResponse>

    @POST("api/core/moderation/check-media")
    suspend fun checkMedia(@Body req: CheckMediaRequest): ApiResponse<CheckMediaResponse>
}