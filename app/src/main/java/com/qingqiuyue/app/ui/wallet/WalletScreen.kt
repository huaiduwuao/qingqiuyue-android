package com.qingqiuyue.app.ui.wallet

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.qingqiuyue.app.data.api.APIService
import com.qingqiuyue.app.data.api.dto.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WalletViewModel @Inject constructor(private val api: APIService) : ViewModel() {
    private val _state = MutableStateFlow(WalletUiState())
    val state: StateFlow<WalletUiState> = _state.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(loading = true) }
            try {
                val balance = api.walletBalance().data
                val packages = api.diamondPackages().data ?: emptyList()
                _state.update {
                    it.copy(
                        balance = balance,
                        packages = packages,
                        loading = false,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(loading = false, error = e.message) }
            }
        }
    }

    fun purchase(pkg: DiamondPackage, onError: (String) -> Unit = {}) {
        viewModelScope.launch {
            try {
                val order = api.createOrder(CreateOrderRequest(pkg.id))
                order.data?.let { api.mockPay(MockPayRequest(it.orderId)) }
                load()  // 刷新余额
            } catch (e: Exception) {
                onError(e.message ?: "购买失败")
            }
        }
    }
}

data class WalletUiState(
    val balance: WalletBalance? = null,
    val packages: List<DiamondPackage> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WalletScreen(vm: WalletViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("钱包") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            BalanceCard(state.balance)
            Spacer(Modifier.height(24.dp))
            Text("钻石套餐", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(12.dp))
            if (state.packages.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                    if (state.loading) CircularProgressIndicator()
                    else Text(state.error ?: "暂无套餐")
                }
            } else {
                LazyVerticalGrid(columns = GridCells.Fixed(2), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(state.packages, key = { it.id }) { pkg ->
                        PackageCard(pkg, onBuy = { onError -> vm.purchase(pkg, onError) })
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceCard(balance: WalletBalance?) {
    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("余额", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(4.dp))
            Text(
                balance?.let { "¥%.2f".format(it.balance / 100.0) } ?: "—",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            balance?.diamonds?.let {
                Spacer(Modifier.height(4.dp))
                Text("$it 钻石", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun PackageCard(pkg: DiamondPackage, onBuy: (onError: (String) -> Unit) -> Unit) {
    var buying by remember { mutableStateOf(false) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(pkg.name, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text("${pkg.diamonds}", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Text("钻", style = MaterialTheme.typography.bodySmall)
            }
            pkg.bonus?.takeIf { it > 0 }?.let {
                Text("送 $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.tertiary)
            }
            Text("¥%.2f".format(pkg.priceCents / 100.0), style = MaterialTheme.typography.bodyMedium)

            // 显示错误信息
            errorMsg?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    if (buying) return@Button  // 防止重复点击
                    buying = true
                    errorMsg = null
                    onBuy { error ->
                        buying = false
                        errorMsg = error
                    }
                },
                enabled = !buying,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(if (buying) "处理中…" else "购买")
            }
        }
    }
}