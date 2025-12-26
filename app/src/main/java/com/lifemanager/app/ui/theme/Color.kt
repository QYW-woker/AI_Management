package com.lifemanager.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 应用颜色定义
 *
 * 包含主题色、语义色和图表色等
 */
object AppColors {

    // ==================== 主色调 ====================
    val Primary = Color(0xFF2196F3)
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFBBDEFB)
    val OnPrimaryContainer = Color(0xFF001F2A)

    // ==================== 次要色调 ====================
    val Secondary = Color(0xFF4CAF50)
    val OnSecondary = Color.White
    val SecondaryContainer = Color(0xFFC8E6C9)
    val OnSecondaryContainer = Color(0xFF002106)

    // ==================== 第三色调 ====================
    val Tertiary = Color(0xFFFF9800)
    val OnTertiary = Color.White
    val TertiaryContainer = Color(0xFFFFE0B2)
    val OnTertiaryContainer = Color(0xFF331B00)

    // ==================== 错误色 ====================
    val Error = Color(0xFFF44336)
    val OnError = Color.White
    val ErrorContainer = Color(0xFFFFDAD6)
    val OnErrorContainer = Color(0xFF410002)

    // ==================== 背景色 ====================
    val Background = Color(0xFFFAFAFA)
    val OnBackground = Color(0xFF1C1B1F)
    val Surface = Color.White
    val OnSurface = Color(0xFF1C1B1F)
    val SurfaceVariant = Color(0xFFF5F5F5)
    val OnSurfaceVariant = Color(0xFF49454F)

    // ==================== 深色模式背景 ====================
    val DarkBackground = Color(0xFF121212)
    val DarkOnBackground = Color(0xFFE6E1E5)
    val DarkSurface = Color(0xFF1E1E1E)
    val DarkOnSurface = Color(0xFFE6E1E5)
    val DarkSurfaceVariant = Color(0xFF2D2D2D)

    // ==================== 收入/支出颜色 ====================
    val Income = Color(0xFF4CAF50)           // 收入绿色
    val IncomeLight = Color(0xFFC8E6C9)      // 收入浅色背景
    val Expense = Color(0xFFF44336)          // 支出红色
    val ExpenseLight = Color(0xFFFFCDD2)     // 支出浅色背景

    // ==================== 图表颜色 ====================
    val ChartColors = listOf(
        Color(0xFF2196F3),  // 蓝色
        Color(0xFF4CAF50),  // 绿色
        Color(0xFFFF9800),  // 橙色
        Color(0xFF9C27B0),  // 紫色
        Color(0xFFE91E63),  // 粉色
        Color(0xFF00BCD4),  // 青色
        Color(0xFF3F51B5),  // 靛蓝
        Color(0xFF795548),  // 棕色
        Color(0xFF607D8B),  // 蓝灰
        Color(0xFFFFC107),  // 琥珀
        Color(0xFF8BC34A),  // 浅绿
        Color(0xFFFF5722)   // 深橙
    )

    // ==================== 标签类型颜色 ====================
    val SavingsColor = Color(0xFF2196F3)      // 储蓄类 - 蓝色
    val InvestmentColor = Color(0xFFFF9800)   // 投资类 - 橙色
    val ConsumptionColor = Color(0xFFF44336)  // 消费类 - 红色
    val FixedColor = Color(0xFF9C27B0)        // 固定支出 - 紫色
    val OtherColor = Color(0xFF9E9E9E)        // 其他 - 灰色

    // ==================== 心情颜色 ====================
    val MoodColors = listOf(
        Color(0xFFFF5252),  // 1 - 很差 - 红色
        Color(0xFFFF9800),  // 2 - 较差 - 橙色
        Color(0xFFFFC107),  // 3 - 一般 - 黄色
        Color(0xFF8BC34A),  // 4 - 较好 - 浅绿
        Color(0xFF4CAF50)   // 5 - 很好 - 绿色
    )

    // ==================== 优先级颜色 ====================
    val PriorityHigh = Color(0xFFF44336)      // 高优先级 - 红色
    val PriorityMedium = Color(0xFFFF9800)    // 中优先级 - 橙色
    val PriorityLow = Color(0xFF4CAF50)       // 低优先级 - 绿色
    val PriorityNone = Color(0xFF9E9E9E)      // 无优先级 - 灰色

    /**
     * 根据索引获取图表颜色
     */
    fun getChartColor(index: Int): Color {
        return ChartColors[index % ChartColors.size]
    }

    /**
     * 根据标签类型获取颜色
     */
    fun getTagTypeColor(tagType: String): Color {
        return when (tagType) {
            "SAVINGS" -> SavingsColor
            "INVESTMENT" -> InvestmentColor
            "CONSUMPTION" -> ConsumptionColor
            "FIXED" -> FixedColor
            else -> OtherColor
        }
    }

    /**
     * 根据心情评分获取颜色
     */
    fun getMoodColor(score: Int): Color {
        return if (score in 1..5) MoodColors[score - 1] else OtherColor
    }
}
