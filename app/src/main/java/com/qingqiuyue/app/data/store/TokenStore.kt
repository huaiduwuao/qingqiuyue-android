package com.qingqiuyue.app.data.store

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * JWT Token 存储(EncryptedSharedPreferences)
 *
 * @Hilt 注入用法:
 *   @Inject lateinit var tokenStore: TokenStore
 *   token.save("eyJ...")
 *   token.token  // 同步读取
 *
 * 设计权衡:
 *   - 用 EncryptedSharedPreferences(基于 Android Keystore),无需自己管 AES Key
 *   - 不导出 StateFlow 的初始 token,避免构造时机问题(改为首次读 SharedPreferences)
 */
@Singleton
class TokenStore @Inject constructor(@ApplicationContext private val context: Context) {

    private val masterKey: MasterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "qingqiuyue_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val _tokenFlow = MutableStateFlow<String?>(prefs.getString(KEY_TOKEN, null))
    val tokenFlow: StateFlow<String?> = _tokenFlow.asStateFlow()

    /** 当前 token(同步读取,用于 OkHttp 拦截器) */
    val token: String? get() = _tokenFlow.value

    val isLoggedIn: Boolean get() = token != null

    fun save(token: String) {
        prefs.edit().putString(KEY_TOKEN, token).apply()
        _tokenFlow.value = token
    }

    fun clear() {
        prefs.edit().remove(KEY_TOKEN).apply()
        _tokenFlow.value = null
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
    }
}