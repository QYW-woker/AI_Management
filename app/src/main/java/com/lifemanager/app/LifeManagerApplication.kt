package com.lifemanager.app

import android.app.Application
import android.content.Context
import android.content.res.Configuration
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/**
 * AI智能生活管理APP - Application入口类
 *
 * 使用Hilt进行依赖注入，作为整个应用的入口点
 * 负责初始化全局配置和依赖注入框架
 */
@HiltAndroidApp
class LifeManagerApplication : Application() {

    companion object {
        private val LANGUAGE_KEY = stringPreferencesKey("language")
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
     * 更新语言配置
     */
    private fun updateLocale(context: Context): Context {
        val language = runBlocking {
            try {
                context.settingsDataStore.data
                    .map { preferences -> preferences[LANGUAGE_KEY] ?: LANGUAGE_CHINESE }
                    .first()
            } catch (e: Exception) {
                LANGUAGE_CHINESE
            }
        }

        val locale = getLocale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }
}
