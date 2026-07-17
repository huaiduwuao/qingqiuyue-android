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
 * 支持 Access Token 和 Refresh Token 自动刷新
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

    /** 当前 Access Token(同步读取,用于 OkHttp 拦截器) */
    val token: String? get() = _tokenFlow.value

    /** Refresh Token(用于刷新过期的 Access Token) */
    var refreshToken: String?
        get() = prefs.getString(KEY_REFRESH_TOKEN, null)
        private set(value) {
            if (value != null) {
                prefs.edit().putString(KEY_REFRESH_TOKEN, value).apply()
            } else {
                prefs.edit().remove(KEY_REFRESH_TOKEN).apply()
            }
        }

    val isLoggedIn: Boolean get() = token != null

    /**
     * 保存 Token 对(Access Token + Refresh Token)
     * @param accessToken JWT Access Token
     * @param refreshToken JWT Refresh Token
     */
    fun save(accessToken: String, refreshToken: String? = null) {
        prefs.edit().putString(KEY_TOKEN, accessToken).apply()
        _tokenFlow.value = accessToken
        if (refreshToken != null) {
            this.refreshToken = refreshToken
        }
    }

    /**
     * 仅保存 Access Token(用于刷新后的更新)
     */
    fun updateAccessToken(accessToken: String) {
        prefs.edit().putString(KEY_TOKEN, accessToken).apply()
        _tokenFlow.value = accessToken
    }

    /**
     * 清除所有 Token(登出时调用)
     */
    fun clear() {
        prefs.edit()
            .remove(KEY_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .apply()
        _tokenFlow.value = null
    }

    /**
     * 检查是否需要刷新 Token(Access Token 即将过期时)
     * @param expiresInSeconds Token 剩余有效期秒数，低于此值时需要刷新
     */
    fun needsRefresh(expiresInSeconds: Long = 300): Boolean {
        // TODO: 实现 JWT 解码检查过期时间
        // 暂时返回 refreshToken 存在且有值
        return this.refreshToken != null
    }

    companion object {
        private const val KEY_TOKEN = "auth_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
    }
}