package com.qingqiuyue.app.ui.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.qingqiuyue.app.ui.chat.ChatScreen
import com.qingqiuyue.app.ui.home.HomeFeedScreen
import com.qingqiuyue.app.ui.profile.ProfileScreen
import com.qingqiuyue.app.ui.wallet.WalletScreen

private enum class Tab(val label: String) { Home("首页"), Chat("数字人"), Wallet("钱包"), Profile("我的") }

/**
 * 主框架:BottomNavigation + 各 Tab 内容。
 *
 * 借鉴前端 /home/recommend 的 4 个一级 tab。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(onLogout: () -> Unit) {
    var current by remember { mutableStateOf(Tab.Home) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = current == Tab.Home,
                    onClick = { current = Tab.Home },
                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                    label = { Text(Tab.Home.label) }
                )
                NavigationBarItem(
                    selected = current == Tab.Chat,
                    onClick = { current = Tab.Chat },
                    icon = { Icon(Icons.Default.SmartToy, contentDescription = null) },
                    label = { Text(Tab.Chat.label) }
                )
                NavigationBarItem(
                    selected = current == Tab.Wallet,
                    onClick = { current = Tab.Wallet },
                    icon = { Icon(Icons.Default.CreditCard, contentDescription = null) },
                    label = { Text(Tab.Wallet.label) }
                )
                NavigationBarItem(
                    selected = current == Tab.Profile,
                    onClick = { current = Tab.Profile },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text(Tab.Profile.label) }
                )
            }
        }
    ) { padding ->
        Surface(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (current) {
                Tab.Home -> HomeFeedScreen()
                Tab.Chat -> ChatScreen()
                Tab.Wallet -> WalletScreen()
                Tab.Profile -> ProfileScreen(onLogout = onLogout)
            }
        }
    }
}