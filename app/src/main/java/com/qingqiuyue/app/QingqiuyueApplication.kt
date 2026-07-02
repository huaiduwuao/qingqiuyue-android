package com.qingqiuyue.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * 应用入口
 *
 * @HiltAndroidApp 触发 Hilt 编译期生成 DI 组件。
 */
@HiltAndroidApp
class QingqiuyueApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Timber.i("[qingqiuyue-android] started, version=${BuildConfig.VERSION_NAME}, apiBase=${BuildConfig.API_BASE}")
    }
}