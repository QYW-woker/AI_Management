package com.lifemanager.app.domain.model

import com.lifemanager.app.core.database.entity.HabitEntity

/**
 * 习惯打卡模块数据模型
 */

/**
 * 习惯及其打卡状态
 */
data class HabitWithStatus(
    val habit: HabitEntity,
    val isCheckedToday: Boolean,
    val todayValue: Double? = null,
    val streak: Int = 0,              // 连续打卡天数
    val totalCheckins: Int = 0,       // 总打卡次数
    val completionRate: Float = 0f    // 完成率（本周/本月）
)

/**
 * 习惯统计数据
 */
data class HabitStats(
    val totalHabits: Int = 0,
    val activeHabits: Int = 0,
    val todayCompleted: Int = 0,
    val todayTotal: Int = 0,
    val todayCompletionRate: Float = 0f,
    val weeklyCompletionRate: Float = 0f,
    val longestStreak: Int = 0
)

/**
 * 习惯UI状态
 */
sealed class HabitUiState {
    object Loading : HabitUiState()
    data class Success(val message: String? = null) : HabitUiState()
    data class Error(val message: String) : HabitUiState()
}

/**
 * 习惯编辑状态
 */
data class HabitEditState(
    val id: Long = 0,
    val name: String = "",
    val description: String = "",
    val iconName: String = "check_circle",
    val color: String = "#4CAF50",
    val frequency: String = "DAILY",
    val targetTimes: Int = 1,
    val reminderTime: String? = null,
    val isNumeric: Boolean = false,
    val targetValue: Double? = null,
    val unit: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * 预定义的习惯颜色
 */
val habitColors = listOf(
    "#4CAF50" to "绿色",
    "#2196F3" to "蓝色",
    "#9C27B0" to "紫色",
    "#FF9800" to "橙色",
    "#F44336" to "红色",
    "#00BCD4" to "青色",
    "#E91E63" to "粉色",
    "#795548" to "棕色",
    "#607D8B" to "蓝灰",
    "#FF5722" to "深橙"
)

/**
 * 预定义的习惯图标
 */
val habitIcons = listOf(
    "check_circle" to "打卡",
    "fitness_center" to "健身",
    "local_drink" to "喝水",
    "book" to "阅读",
    "code" to "编程",
    "brush" to "绘画",
    "music_note" to "音乐",
    "translate" to "学习",
    "restaurant" to "饮食",
    "bedtime" to "睡眠",
    "directions_run" to "跑步",
    "self_improvement" to "冥想"
)

/**
 * 打卡频率选项
 */
val frequencyOptions = listOf(
    "DAILY" to "每天",
    "WEEKDAYS" to "工作日",
    "WEEKLY_TIMES" to "每周X次",
    "MONTHLY_TIMES" to "每月X次"
)

/**
 * 获取频率显示文本
 */
fun getFrequencyDisplayText(frequency: String, targetTimes: Int): String {
    return when (frequency) {
        "DAILY" -> "每天"
        "WEEKDAYS" -> "工作日"
        "WEEKLY_TIMES" -> "每周${targetTimes}次"
        "MONTHLY_TIMES" -> "每月${targetTimes}次"
        else -> "每天"
    }
}
