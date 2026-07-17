package com.qingqiuyue.app.di

import com.qingqiuyue.app.BuildConfig
import com.qingqiuyue.app.data.api.APIService
import com.qingqiuyue.app.data.api.AuthRefreshAPI
import com.qingqiuyue.app.data.store.TokenStore
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

/**
 * Token 刷新拦截器
 * 当收到 401 响应时，自动使用 Refresh Token 获取新的 Access Token
 */
class TokenAuthenticator @Inject constructor(
    private val tokenStore: TokenStore,
    @Named("Auth") private val authRetrofit: Retrofit
) : Authenticator {

    override fun authenticate(route: Route?, response: Response): Request? {
        // 避免无限循环
        if (response.request.header("X-Retry-Auth") != null) {
            return null
        }

        val refreshToken = tokenStore.refreshToken
        if (refreshToken == null) {
            // 没有 refresh token，清除登录状态
            tokenStore.clear()
            return null
        }

        // 尝试刷新 token
        return runBlocking {
            try {
                val authAPI = authRetrofit.create(AuthRefreshAPI::class.java)
                val refreshResponse = authAPI.refreshToken(refreshToken).execute()

                if (refreshResponse.isSuccessful) {
                    val newToken = refreshResponse.body()?.token
                    if (newToken != null) {
                        tokenStore.updateAccessToken(newToken)
                        // 重试原请求
                        response.request.newBuilder()
                            .header("Authorization", "Bearer $newToken")
                            .header("X-Retry-Auth", "true")
                            .build()
                    } else {
                        tokenStore.clear()
                        null
                    }
                } else {
                    // 刷新失败，清除登录状态
                    tokenStore.clear()
                    null
                }
            } catch (e: Exception) {
                tokenStore.clear()
                null
            }
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideMoshi(): Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    @Provides
    @Singleton
    @Named("Base")
    fun provideOkHttpClient(tokenStore: TokenStore): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "qingqiuyue-android/${BuildConfig.VERSION_NAME}")
                    .apply {
                        tokenStore.token?.let { header("Authorization", "Bearer $it") }
                    }
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(logging)
            .build()
    }

    /**
     * 提供带 Token 刷新功能的 OkHttpClient
     */
    @Provides
    @Singleton
    @Named("AuthOKHttp")
    fun provideAuthOkHttpClient(
        tokenStore: TokenStore,
        authenticator: TokenAuthenticator
    ): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor { chain ->
                val req = chain.request().newBuilder()
                    .header("User-Agent", "qingqiuyue-android/${BuildConfig.VERSION_NAME}")
                    .apply {
                        tokenStore.token?.let { header("Authorization", "Bearer $it") }
                    }
                    .build()
                chain.proceed(req)
            }
            .addInterceptor(logging)
            .authenticator(authenticator)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        @Named("AuthOKHttp") client: OkHttpClient,
        moshi: Moshi
    ): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE)
            .client(client)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    /**
     * Auth 专用 Retrofit（用于 Token 刷新）
     */
    @Provides
    @Singleton
    @Named("Auth")
    fun provideAuthRetrofit(moshi: Moshi): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()

    @Provides
    @Singleton
    fun provideTokenAuthenticator(
        tokenStore: TokenStore,
        @Named("Auth") authRetrofit: Retrofit
    ): TokenAuthenticator = TokenAuthenticator(tokenStore, authRetrofit)

    @Provides
    @Singleton
    fun provideAPIService(retrofit: Retrofit): APIService = retrofit.create(APIService::class.java)
}