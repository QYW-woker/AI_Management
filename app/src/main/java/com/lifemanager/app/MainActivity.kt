package com.lifemanager.app

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.navigation.compose.rememberNavController
import com.lifemanager.app.core.theme.ThemeManager
import com.lifemanager.app.ui.navigation.AdaptiveNavigation
import com.lifemanager.app.ui.navigation.Screen
import com.lifemanager.app.ui.navigation.rememberWindowSizeClass
import com.lifemanager.app.ui.theme.LifeManagerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import java.util.Locale
import javax.inject.Inject

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

/**
 * 主Activity
 *
 * 应用的唯一Activity入口
 * 使用Jetpack Compose构建UI
 * 通过Hilt进行依赖注入
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var themeManager: ThemeManager

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(updateLocale(newBase))
    }

    /**
     * 更新语言配置
     */
    private fun updateLocale(context: Context): Context {
        val languageKey = stringPreferencesKey("language")
        val language = runBlocking {
            try {
                context.settingsDataStore.data
                    .map { preferences -> preferences[languageKey] ?: LifeManagerApplication.LANGUAGE_CHINESE }
                    .first()
            } catch (e: Exception) {
                LifeManagerApplication.LANGUAGE_CHINESE
            }
        }

        val locale = LifeManagerApplication.getLocale(language)
        Locale.setDefault(locale)

        val configuration = Configuration(context.resources.configuration)
        configuration.setLocale(locale)
        configuration.setLayoutDirection(locale)

        return context.createConfigurationContext(configuration)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 启用边到边显示
        enableEdgeToEdge()

        setContent {
            // 获取深色模式设置
            val isDarkMode by themeManager.isDarkMode.collectAsState(initial = false)

            // 应用主题
            LifeManagerTheme(darkTheme = isDarkMode) {
                // 导航控制器
                val navController = rememberNavController()
                // 窗口尺寸类型
                val windowSizeClass = rememberWindowSizeClass()

                // 处理从悬浮球传来的导航intent
                LaunchedEffect(Unit) {
                    handleNavigationIntent(intent)?.let { route ->
                        navController.navigate(route) {
                            launchSingleTop = true
                        }
                    }
                }

                // 自适应导航容器
                AdaptiveNavigation(
                    navController = navController,
                    windowSizeClass = windowSizeClass,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun handleNavigationIntent(intent: Intent?): String? {
        return when (intent?.getStringExtra("navigate_to")) {
            "ai_assistant" -> Screen.AIAssistant.route
            else -> null
        }
    }
}
