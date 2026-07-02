package com.qingqiuyue.app.ui.chat

import com.qingqiuyue.app.data.api.dto.ChatRequest
import com.qingqiuyue.app.data.api.dto.ChatResponse
import com.qingqiuyue.app.data.store.TokenStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数字人 SSE 流式客户端(基于 OkHttp + okhttp-sse)
 *
 * 用法:
 *   val client = ChatAPIClient(okHttpClient, tokenStore)
 *   client.stream(sessionId = "abc", message = "hello")
 *       .collect { chunk -> /* 累加到 UI */ }
 *
 * OkHttp SSE 默认不会主动解析 data 字段为对象 — 我们手动按 \n\n 切分,解析每个 event 的 data。
 */
@Singleton
class ChatAPIClient @Inject constructor(
    private val client: OkHttpClient,
    private val tokenStore: TokenStore
) {
    private val factory = EventSources.createFactory(client)
    private val baseUrl = "http://10.0.2.2:9080"  // 模拟器访问宿主机;真机改 IP

    fun stream(sessionId: String, message: String): Flow<String> = flow {
        val body = JSONObject().apply {
            put("sessionId", sessionId)
            put("message", message)
        }.toString()

        val req = Request.Builder()
            .url("$baseUrl/api/avatar/chat")
            .post(okhttp3.RequestBody.create("application/json".toMediaType(), body))
            .header("Accept", "text/event-stream")
            .apply {
                tokenStore.token?.let { header("Authorization", "Bearer $it") }
            }
            .build()

        val channel = kotlinx.coroutines.channels.Channel<String>(kotlinx.coroutines.channels.Channel.UNLIMITED)

        val listener = object : EventSourceListener() {
            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                channel.trySend(data)
                if (data == "[DONE]") {
                    eventSource.cancel()
                    channel.close()
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: okhttp3.Response?) {
                channel.close(t)
            }

            override fun onClosed(eventSource: EventSource) {
                channel.close()
            }
        }

        val es = factory.newEventSource(req, listener)
        try {
            for (chunk in channel) {
                emit(chunk)
            }
        } finally {
            es.cancel()
        }
    }

    /** 字符串扩展,简化 MediaType 构造 */
    private fun String.toMediaType(): okhttp3.MediaType =
        okhttp3.MediaType.parse(this)
}