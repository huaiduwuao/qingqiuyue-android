package com.qingqiuyue.app.ui.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingqiuyue.app.data.api.APIService
import com.qingqiuyue.app.data.store.TokenStore
import com.qingqiuyue.app.ui.components.AIGCBadgeInline
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

data class ChatMessage(
    val id: String,
    val role: Role,
    val text: String,
    val isAIGenerated: Boolean = false
) {
    enum class Role { User, Assistant }
}

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val api: APIService,
    private val chatClient: ChatAPIClient
) : ViewModel() {
    private val _state = MutableStateFlow(ChatUiState())
    val state: StateFlow<ChatUiState> = _state.asStateFlow()

    private val sessionId = "android-${System.currentTimeMillis()}"

    fun send(text: String) {
        if (text.isBlank() || _state.value.streaming) return
        val userMsg = ChatMessage(java.util.UUID.randomUUID().toString(), ChatMessage.Role.User, text)
        _state.update { it.copy(messages = it.messages + userMsg, input = "", streaming = true) }

        val assistantId = java.util.UUID.randomUUID().toString()
        _state.update { it.copy(messages = it.messages + ChatMessage(assistantId, ChatMessage.Role.Assistant, "")) }

        viewModelScope.launch {
            // 简化:走单次接口;流式见 ChatAPIClient.kt(SSE)
            try {
                val resp = api.moderateText(com.qingqiuyue.app.data.api.dto.ModerateTextRequest(text))
                if (resp.data?.passed == false) {
                    updateAssistant(assistantId, "消息含敏感词,无法发送")
                    _state.update { it.copy(streaming = false) }
                    return@launch
                }
                // SSE 流式：使用 ChatAPIClient 进行实时对话
                chatClient.stream(sessionId = "default_session", message = text)
                    .catch { e ->
                        updateAssistant(assistantId, "连接失败:${e.message}")
                    }
                    .collect { chunk ->
                        // 解析 SSE data 字段
                        if (chunk.startsWith("{")) {
                            val json = JSONObject(chunk)
                            val content = json.optString("content", "")
                            if (content.isNotEmpty()) {
                                updateAssistant(assistantId, content)
                            }
                        }
                    }
            } catch (e: Exception) {
                updateAssistant(assistantId, "出错了:${e.message}")
            } finally {
                _state.update { it.copy(streaming = false) }
            }
        }
    }

    private fun updateAssistant(id: String, text: String) {
        _state.update { st ->
            st.copy(messages = st.messages.map { if (it.id == id) it.copy(text = text) else it })
        }
    }

    fun onInputChange(s: String) { _state.update { it.copy(input = s) } }
}

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val input: String = "",
    val streaming: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(vm: ChatViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(state.messages.size) {
        if (state.messages.isNotEmpty()) {
            listState.animateScrollToItem(state.messages.size - 1)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("数字人") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            LazyColumn(
                state = listState,
                modifier = Modifier.weight(1f).padding(horizontal = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.messages, key = { it.id }) { msg ->
                    ChatBubble(msg)
                }
                if (state.streaming) {
                    item {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = state.input,
                    onValueChange = vm::onInputChange,
                    placeholder = { Text("说点什么…") },
                    modifier = Modifier.weight(1f),
                    maxLines = 4
                )
                Spacer(Modifier.width(8.dp))
                IconButton(
                    onClick = { vm.send(state.input) },
                    enabled = state.input.isNotBlank() && !state.streaming
                ) {
                    Icon(Icons.Default.Send, contentDescription = "发送")
                }
            }
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage) {
    val isUser = msg.role == ChatMessage.Role.User
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Column(horizontalAlignment = if (isUser) Alignment.End else Alignment.Start) {
            if (!isUser && msg.isAIGenerated) {
                AIGCBadgeInline("AI 生成")
                Spacer(Modifier.height(4.dp))
            }
            Surface(
                color = if (isUser) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(
                    topStart = 16.dp,
                    topEnd = 16.dp,
                    bottomStart = if (isUser) 16.dp else 4.dp,
                    bottomEnd = if (isUser) 4.dp else 16.dp
                ),
                modifier = Modifier.widthIn(max = 280.dp)
            ) {
                Text(
                    if (msg.text.isEmpty()) "…" else msg.text,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary
                            else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}