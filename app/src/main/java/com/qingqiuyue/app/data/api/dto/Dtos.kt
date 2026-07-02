package com.qingqiuyue.app.data.api.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/** 后端统一响应包络 */
@JsonClass(generateAdapter = true)
data class ApiResponse<T>(
    @Json(name = "code") val code: Int = 0,
    @Json(name = "msg") val msg: String? = null,
    @Json(name = "data") val data: T? = null,
    @Json(name = "success") val success: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class LoginRequest(val username: String, val password: String)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val token: String,
    @Json(name = "refreshToken") val refreshToken: String? = null,
    val user: UserInfo? = null
)

@JsonClass(generateAdapter = true)
data class UserInfo(
    val id: Long,
    val name: String,
    val nickname: String? = null,
    val avatar: String? = null,
    @Json(name = "isCreator") val isCreator: Boolean? = null
)

@JsonClass(generateAdapter = true)
data class VideoItem(
    val id: Long,
    val title: String,
    val subtitle: String? = null,
    val contentType: String? = null,
    val coverUrl: String? = null,
    val playUrl: String? = null,
    val duration: Int? = null,
    val readNum: Int? = null,
    val agreeNum: Int? = null,
    val commentNum: Int? = null,
    val publishTime: String? = null,
    val isAIGenerated: Boolean? = null,
    val author: String? = null
)

@JsonClass(generateAdapter = true)
data class PagedListResponse<T>(
    val records: List<T> = emptyList(),
    val totalRow: Int = 0,
    val page: Int? = null,
    val pageSize: Int? = null
)

@JsonClass(generateAdapter = true)
data class FeedResponse(
    val records: List<VideoItem> = emptyList(),
    val totalRow: Int? = null
)

@JsonClass(generateAdapter = true)
data class WorkflowItem(val id: String, val name: String, val description: String? = null)

@JsonClass(generateAdapter = true)
data class GenerateRequest(
    val workflowId: String,
    val prompt: String,
    val params: Map<String, Any>? = null
)

@JsonClass(generateAdapter = true)
data class GenerateResponse(val taskId: String, val status: String, val url: String? = null)

@JsonClass(generateAdapter = true)
data class WalletBalance(
    val balance: Long,        // 分
    val frozen: Long? = null,
    val diamonds: Long? = null
)

@JsonClass(generateAdapter = true)
data class DiamondPackage(
    val id: String,
    val name: String,
    val diamonds: Int,
    val bonus: Int? = null,
    val priceCents: Int,
    val currency: String? = null
)

@JsonClass(generateAdapter = true)
data class CreateOrderRequest(val packageId: String)

@JsonClass(generateAdapter = true)
data class MockPayRequest(val orderId: String)

@JsonClass(generateAdapter = true)
data class Order(
    val orderId: String,
    val orderNo: String? = null,
    val amount: Long,
    val status: String,
    val packageId: String? = null
)

@JsonClass(generateAdapter = true)
data class WorkItem(
    val id: Long,
    val title: String,
    val contentType: String? = null,
    val coverUrl: String? = null,
    val readNum: Int? = null,
    val agreeNum: Int? = null,
    val commentNum: Int? = null,
    val status: String? = null,
    val publishTime: String? = null
)

@JsonClass(generateAdapter = true)
data class MonetizeSummary(
    val totalIncome: Long,
    val totalExpense: Long,
    val balance: Long,
    val byType: Map<String, Long> = emptyMap(),
    val recent30Days: Long = 0
)

@JsonClass(generateAdapter = true)
data class ApplyRequest(val reason: String)

@JsonClass(generateAdapter = true)
data class ApplyResponse(
    val id: Long? = null,
    val status: String? = null,        // none | pending | approved | rejected
    val reason: String? = null,
    val reviewNote: String? = null
)

@JsonClass(generateAdapter = true)
data class ModerateTextRequest(val text: String)

@JsonClass(generateAdapter = true)
data class ModerateTextResponse(
    val passed: Boolean,
    val reason: String? = null,
    val keywords: List<String>? = null
)

@JsonClass(generateAdapter = true)
data class CheckMediaRequest(
    val mediaType: String,    // "image" | "video"
    val url: String
)

@JsonClass(generateAdapter = true)
data class CheckMediaResponse(
    val passed: Boolean,
    val riskLevel: Int,
    val categories: List<String>? = null,
    val confidence: Double? = null,
    val reason: String? = null
)