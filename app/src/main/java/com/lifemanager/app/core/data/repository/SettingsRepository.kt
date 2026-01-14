package com.lifemanager.app.core.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

/**
 * 货币符号枚举
 */
enum class CurrencySymbol(val symbol: String, val displayName: String) {
    CNY("¥", "人民币 (¥)"),
    USD("$", "美元 ($)"),
    EUR("€", "欧元 (€)"),
    GBP("£", "英镑 (£)"),
    JPY("¥", "日元 (¥)"),
    NONE("", "无符号")
}

/**
 * 日期格式枚举
 */
enum class DateFormat(val pattern: String, val displayName: String) {
    ISO("yyyy-MM-dd", "2024-01-15"),
    CN("yyyy年M月d日", "2024年1月15日"),
    US("MM/dd/yyyy", "01/15/2024"),
    EU("dd/MM/yyyy", "15/01/2024"),
    COMPACT("yy.MM.dd", "24.01.15")
}

/**
 * 周起始日枚举
 */
enum class WeekStartDay(val displayName: String) {
    SUNDAY("周日"),
    MONDAY("周一")
}

/**
 * 首页卡片配置
 */
data class HomeCardConfig(
    val showTodayStats: Boolean = true,
    val showMonthlyFinance: Boolean = true,
    val showTopGoals: Boolean = true,
    val showHabitProgress: Boolean = true,
    val showAIInsight: Boolean = true,
    val showQuickActions: Boolean = true,
    val cardOrder: List<String> = listOf(
        "todayStats", "monthlyFinance", "topGoals",
        "habitProgress", "aiInsight", "quickActions"
    )
)

/**
 * 设置数据类
 */
data class AppSettings(
    val isDarkMode: Boolean = false,
    val enableNotification: Boolean = true,
    val reminderTime: String = "09:00",
    val autoBackup: Boolean = false,
    val language: String = "简体中文",
    // 显示格式设置
    val currencySymbol: CurrencySymbol = CurrencySymbol.CNY,
    val decimalPlaces: Int = 2,
    val useThousandSeparator: Boolean = true,
    val dateFormat: DateFormat = DateFormat.CN,
    val weekStartDay: WeekStartDay = WeekStartDay.MONDAY,
    // 首页卡片配置
    val homeCardConfig: HomeCardConfig = HomeCardConfig()
)

/**
 * 设置仓库
 * 使用DataStore持久化设置
 */
@Singleton
class SettingsRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val IS_DARK_MODE = booleanPreferencesKey("is_dark_mode")
        val ENABLE_NOTIFICATION = booleanPreferencesKey("enable_notification")
        val REMINDER_TIME = stringPreferencesKey("reminder_time")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val LANGUAGE = stringPreferencesKey("language")
        // 显示格式设置
        val CURRENCY_SYMBOL = stringPreferencesKey("currency_symbol")
        val DECIMAL_PLACES = intPreferencesKey("decimal_places")
        val USE_THOUSAND_SEPARATOR = booleanPreferencesKey("use_thousand_separator")
        val DATE_FORMAT = stringPreferencesKey("date_format")
        val WEEK_START_DAY = stringPreferencesKey("week_start_day")
        // 首页卡片设置
        val SHOW_TODAY_STATS = booleanPreferencesKey("show_today_stats")
        val SHOW_MONTHLY_FINANCE = booleanPreferencesKey("show_monthly_finance")
        val SHOW_TOP_GOALS = booleanPreferencesKey("show_top_goals")
        val SHOW_HABIT_PROGRESS = booleanPreferencesKey("show_habit_progress")
        val SHOW_AI_INSIGHT = booleanPreferencesKey("show_ai_insight")
        val SHOW_QUICK_ACTIONS = booleanPreferencesKey("show_quick_actions")
        val CARD_ORDER = stringPreferencesKey("card_order")
    }

    /**
     * 获取设置流
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            val cardOrderStr = preferences[PreferencesKeys.CARD_ORDER] ?: ""
            val cardOrder = if (cardOrderStr.isNotEmpty()) {
                cardOrderStr.split(",")
            } else {
                HomeCardConfig().cardOrder
            }

            AppSettings(
                isDarkMode = preferences[PreferencesKeys.IS_DARK_MODE] ?: false,
                enableNotification = preferences[PreferencesKeys.ENABLE_NOTIFICATION] ?: true,
                reminderTime = preferences[PreferencesKeys.REMINDER_TIME] ?: "09:00",
                autoBackup = preferences[PreferencesKeys.AUTO_BACKUP] ?: false,
                language = preferences[PreferencesKeys.LANGUAGE] ?: "简体中文",
                // 显示格式设置
                currencySymbol = preferences[PreferencesKeys.CURRENCY_SYMBOL]?.let {
                    try { CurrencySymbol.valueOf(it) } catch (e: Exception) { CurrencySymbol.CNY }
                } ?: CurrencySymbol.CNY,
                decimalPlaces = preferences[PreferencesKeys.DECIMAL_PLACES] ?: 2,
                useThousandSeparator = preferences[PreferencesKeys.USE_THOUSAND_SEPARATOR] ?: true,
                dateFormat = preferences[PreferencesKeys.DATE_FORMAT]?.let {
                    try { DateFormat.valueOf(it) } catch (e: Exception) { DateFormat.CN }
                } ?: DateFormat.CN,
                weekStartDay = preferences[PreferencesKeys.WEEK_START_DAY]?.let {
                    try { WeekStartDay.valueOf(it) } catch (e: Exception) { WeekStartDay.MONDAY }
                } ?: WeekStartDay.MONDAY,
                // 首页卡片配置
                homeCardConfig = HomeCardConfig(
                    showTodayStats = preferences[PreferencesKeys.SHOW_TODAY_STATS] ?: true,
                    showMonthlyFinance = preferences[PreferencesKeys.SHOW_MONTHLY_FINANCE] ?: true,
                    showTopGoals = preferences[PreferencesKeys.SHOW_TOP_GOALS] ?: true,
                    showHabitProgress = preferences[PreferencesKeys.SHOW_HABIT_PROGRESS] ?: true,
                    showAIInsight = preferences[PreferencesKeys.SHOW_AI_INSIGHT] ?: true,
                    showQuickActions = preferences[PreferencesKeys.SHOW_QUICK_ACTIONS] ?: true,
                    cardOrder = cardOrder
                )
            )
        }

    /**
     * 设置深色模式
     */
    suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_DARK_MODE] = enabled
        }
    }

    /**
     * 设置通知开关
     */
    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.ENABLE_NOTIFICATION] = enabled
        }
    }

    /**
     * 设置提醒时间
     */
    suspend fun setReminderTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.REMINDER_TIME] = time
        }
    }

    /**
     * 设置自动备份
     */
    suspend fun setAutoBackup(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.AUTO_BACKUP] = enabled
        }
    }

    /**
     * 设置语言
     */
    suspend fun setLanguage(language: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.LANGUAGE] = language
        }
    }

    // ==================== 显示格式设置 ====================

    /**
     * 设置货币符号
     */
    suspend fun setCurrencySymbol(symbol: CurrencySymbol) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CURRENCY_SYMBOL] = symbol.name
        }
    }

    /**
     * 设置小数位数
     */
    suspend fun setDecimalPlaces(places: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DECIMAL_PLACES] = places.coerceIn(0, 4)
        }
    }

    /**
     * 设置是否使用千位分隔符
     */
    suspend fun setUseThousandSeparator(use: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_THOUSAND_SEPARATOR] = use
        }
    }

    /**
     * 设置日期格式
     */
    suspend fun setDateFormat(format: DateFormat) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DATE_FORMAT] = format.name
        }
    }

    /**
     * 设置周起始日
     */
    suspend fun setWeekStartDay(day: WeekStartDay) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEK_START_DAY] = day.name
        }
    }

    // ==================== 首页卡片设置 ====================

    /**
     * 设置首页卡片显示状态
     */
    suspend fun setHomeCardVisibility(cardKey: String, visible: Boolean) {
        context.dataStore.edit { preferences ->
            when (cardKey) {
                "todayStats" -> preferences[PreferencesKeys.SHOW_TODAY_STATS] = visible
                "monthlyFinance" -> preferences[PreferencesKeys.SHOW_MONTHLY_FINANCE] = visible
                "topGoals" -> preferences[PreferencesKeys.SHOW_TOP_GOALS] = visible
                "habitProgress" -> preferences[PreferencesKeys.SHOW_HABIT_PROGRESS] = visible
                "aiInsight" -> preferences[PreferencesKeys.SHOW_AI_INSIGHT] = visible
                "quickActions" -> preferences[PreferencesKeys.SHOW_QUICK_ACTIONS] = visible
            }
        }
    }

    /**
     * 设置首页卡片顺序
     */
    suspend fun setCardOrder(order: List<String>) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CARD_ORDER] = order.joinToString(",")
        }
    }

    /**
     * 重置首页卡片为默认配置
     */
    suspend fun resetHomeCardConfig() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.SHOW_TODAY_STATS] = true
            preferences[PreferencesKeys.SHOW_MONTHLY_FINANCE] = true
            preferences[PreferencesKeys.SHOW_TOP_GOALS] = true
            preferences[PreferencesKeys.SHOW_HABIT_PROGRESS] = true
            preferences[PreferencesKeys.SHOW_AI_INSIGHT] = true
            preferences[PreferencesKeys.SHOW_QUICK_ACTIONS] = true
            preferences[PreferencesKeys.CARD_ORDER] = HomeCardConfig().cardOrder.joinToString(",")
        }
    }
}
