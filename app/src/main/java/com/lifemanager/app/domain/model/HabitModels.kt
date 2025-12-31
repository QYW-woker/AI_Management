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

// ============ 打卡增强功能模型 ============

/**
 * 打卡日历数据
 */
data class HabitCalendarData(
    val habitId: Long,
    val habitName: String,
    val habitColor: String,
    val month: Int,                    // YYYYMM格式
    val checkedDays: Set<Int>,         // 已打卡的日期（epochDay）
    val totalDaysInMonth: Int,
    val completedDays: Int,
    val completionRate: Float
)

/**
 * 成就徽章
 */
data class HabitAchievement(
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val color: String,
    val isUnlocked: Boolean,
    val unlockedAt: Long? = null,
    val progress: Int = 0,
    val targetProgress: Int = 100
)

/**
 * 成就类型
 */
object AchievementType {
    const val FIRST_CHECKIN = "first_checkin"
    const val STREAK_7 = "streak_7"
    const val STREAK_21 = "streak_21"
    const val STREAK_30 = "streak_30"
    const val STREAK_100 = "streak_100"
    const val TOTAL_100 = "total_100"
    const val PERFECT_WEEK = "perfect_week"
    const val PERFECT_MONTH = "perfect_month"
}

/**
 * 获取成就定义
 */
fun getAchievementDefinitions(): List<HabitAchievement> = listOf(
    HabitAchievement(
        id = AchievementType.FIRST_CHECKIN, name = "初心者",
        description = "完成第一次打卡", icon = "star", color = "#FFD700",
        isUnlocked = false, targetProgress = 1
    ),
    HabitAchievement(
        id = AchievementType.STREAK_7, name = "小试牛刀",
        description = "连续打卡7天", icon = "local_fire_department", color = "#FF6B6B",
        isUnlocked = false, targetProgress = 7
    ),
    HabitAchievement(
        id = AchievementType.STREAK_21, name = "习惯养成",
        description = "连续打卡21天", icon = "emoji_events", color = "#4ECDC4",
        isUnlocked = false, targetProgress = 21
    ),
    HabitAchievement(
        id = AchievementType.STREAK_30, name = "月度冠军",
        description = "连续打卡30天", icon = "military_tech", color = "#FFE66D",
        isUnlocked = false, targetProgress = 30
    ),
    HabitAchievement(
        id = AchievementType.STREAK_100, name = "百日成就",
        description = "连续打卡100天", icon = "workspace_premium", color = "#FF69B4",
        isUnlocked = false, targetProgress = 100
    ),
    HabitAchievement(
        id = AchievementType.TOTAL_100, name = "百次打卡",
        description = "累计打卡100次", icon = "verified", color = "#9B59B6",
        isUnlocked = false, targetProgress = 100
    ),
    HabitAchievement(
        id = AchievementType.PERFECT_WEEK, name = "完美一周",
        description = "一周内每天都完成所有习惯", icon = "stars", color = "#3498DB",
        isUnlocked = false, targetProgress = 7
    ),
    HabitAchievement(
        id = AchievementType.PERFECT_MONTH, name = "完美一月",
        description = "一个月内每天都完成所有习惯", icon = "diamond", color = "#E74C3C",
        isUnlocked = false, targetProgress = 30
    )
)

/**
 * 周度统计
 */
data class WeeklyHabitStats(
    val weekNumber: Int,
    val weekLabel: String,
    val totalCheckins: Int,
    val possibleCheckins: Int,
    val completionRate: Float,
    val isCurrentWeek: Boolean,
    val dailyData: List<DailyHabitData>
)

/**
 * 每日打卡数据
 */
data class DailyHabitData(
    val date: Int,
    val dayLabel: String,
    val completed: Int,
    val total: Int,
    val isToday: Boolean
)

/**
 * 月度统计
 */
data class MonthlyHabitStats(
    val yearMonth: Int,
    val monthLabel: String,
    val totalCheckins: Int,
    val possibleCheckins: Int,
    val completionRate: Float,
    val perfectDays: Int,
    val bestStreak: Int,
    val mostActiveHabit: String?
)

/**
 * 习惯排行
 */
data class HabitRankItem(
    val habitId: Long,
    val habitName: String,
    val habitColor: String,
    val rank: Int,
    val streak: Int,
    val totalCheckins: Int,
    val completionRate: Float
)

/**
 * 激励语
 */
val motivationalMessages = listOf(
    "太棒了！保持这个势头！",
    "每一次坚持都是成长的一步！",
    "你的努力正在塑造更好的自己！",
    "又完成了一天，继续加油！",
    "坚持就是胜利！",
    "新的一天，新的开始！",
    "行动是成功的阶梯！",
    "今天的你比昨天更优秀！",
    "小步快跑，终将抵达远方！",
    "每日精进，未来可期！"
)

/**
 * 根据打卡情况获取激励语
 */
fun getMotivationalMessage(streak: Int): String {
    return when {
        streak >= 100 -> "传奇！连续${streak}天的坚持，你就是榜样！"
        streak >= 30 -> "太厉害了！一个月的坚持，习惯已经深入骨髓！"
        streak >= 21 -> "恭喜！21天习惯养成，你做到了！"
        streak >= 7 -> "一周连续打卡！你正在建立强大的习惯！"
        streak >= 3 -> "连续${streak}天！好的开始是成功的一半！"
        streak == 1 -> motivationalMessages.random()
        else -> "新的开始！让我们一起养成好习惯！"
    }
}

/**
 * 习惯详情
 */
data class HabitDetailData(
    val habit: HabitEntity,
    val currentStreak: Int,
    val longestStreak: Int,
    val totalCheckins: Int,
    val firstCheckinDate: Int?,
    val completionRateThisMonth: Float,
    val completionRateThisWeek: Float,
    val recentCheckins: List<Int>,
    val achievements: List<HabitAchievement>
)

/**
 * 补打卡状态
 */
data class RetroCheckinState(
    val habitId: Long = 0,
    val selectedDate: Int = 0,
    val note: String = "",
    val isShowing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
