package com.lifemanager.app

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.content.res.Configuration
import dagger.hilt.android.HiltAndroidApp
import java.util.Locale

/**
 * AI智能生活管理APP - Application入口类
 *
 * 使用Hilt进行依赖注入，作为整个应用的入口点
 * 负责初始化全局配置和依赖注入框架
 */
@HiltAndroidApp
class LifeManagerApplication : Application() {

    companion object {
        private const val PREFS_NAME = "app_settings_cache"
        private const val KEY_LANGUAGE = "language"
        const val LANGUAGE_CHINESE = "简体中文"
        const val LANGUAGE_ENGLISH = "English"

        /**
         * 获取语言对应的Locale
         */
        fun getLocale(language: String): Locale {
            return when (language) {
                LANGUAGE_ENGLISH -> Locale.ENGLISH
                LANGUAGE_CHINESE -> Locale.SIMPLIFIED_CHINESE
                else -> Locale.SIMPLIFIED_CHINESE
            }
        }

        /**
         * 保存语言设置到快速缓存（供Application启动时使用）
         */
        fun saveLanguageToCache(context: Context, language: String) {
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_LANGUAGE, language)
                .apply()
        }
    }

    override fun onCreate() {
        super.onCreate()
        // 初始化操作会在这里进行
        // 例如：日志框架、崩溃上报、数据库预热等
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(updateLocale(base))
    }

    /**
     * 更新语言配置 - 使用SharedPreferences快速读取，避免阻塞
     */
    private fun updateLocale(context: Context): Context {
        // 使用SharedPreferences快速读取（不阻塞主线程）
        val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val language = prefs.getString(KEY_LANGUAGE, LANGUAGE_CHINESE) ?: LANGUAGE_CHINESE

        val locale = getLocale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}
