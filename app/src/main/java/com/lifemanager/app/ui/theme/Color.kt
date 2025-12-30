package com.lifemanager.app.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * 卡通风格应用颜色定义
 *
 * 使用柔和的糖果色系，营造可爱活泼的视觉效果
 */
object AppColors {

    // ==================== 卡通主色调 - 薰衣草紫 ====================
    val Primary = Color(0xFF9B7EDE)            // 柔和紫色
    val OnPrimary = Color.White
    val PrimaryContainer = Color(0xFFEDE7FB)   // 淡紫色
    val OnPrimaryContainer = Color(0xFF2D1F5B)

    // ==================== 次要色调 - 薄荷绿 ====================
    val Secondary = Color(0xFF7DD3C0)          // 薄荷绿
    val OnSecondary = Color.White
    val SecondaryContainer = Color(0xFFD7F5EE) // 淡薄荷色
    val OnSecondaryContainer = Color(0xFF0D3D34)

    // ==================== 第三色调 - 蜜桃橙 ====================
    val Tertiary = Color(0xFFFFB385)           // 蜜桃橙
    val OnTertiary = Color.White
    val TertiaryContainer = Color(0xFFFFE8D9)  // 淡蜜桃色
    val OnTertiaryContainer = Color(0xFF3D2314)

    // ==================== 错误色 - 珊瑚红 ====================
    val Error = Color(0xFFFF7B7B)              // 柔和珊瑚红
    val OnError = Color.White
    val ErrorContainer = Color(0xFFFFE5E5)
    val OnErrorContainer = Color(0xFF5C1F1F)

    // ==================== 卡通背景色 ====================
    val Background = Color(0xFFFFF9F5)         // 奶油白
    val OnBackground = Color(0xFF2D2D3A)       // 深紫灰
    val Surface = Color.White
    val OnSurface = Color(0xFF2D2D3A)
    val SurfaceVariant = Color(0xFFF8F5FF)     // 淡紫白
    val OnSurfaceVariant = Color(0xFF5C5C6E)

    // ==================== 深色模式背景 ====================
    val DarkBackground = Color(0xFF1A1A2E)     // 深紫夜色
    val DarkOnBackground = Color(0xFFF0E6FF)
    val DarkSurface = Color(0xFF242438)
    val DarkOnSurface = Color(0xFFF0E6FF)
    val DarkSurfaceVariant = Color(0xFF2D2D42)

    // ==================== 卡通糖果色系 ====================
    val CandyPink = Color(0xFFFFB6C1)          // 糖果粉
    val CandyBlue = Color(0xFF87CEEB)          // 天空蓝
    val CandyYellow = Color(0xFFFFE66D)        // 柠檬黄
    val CandyMint = Color(0xFF98FB98)          // 薄荷绿
    val CandyLavender = Color(0xFFE6E6FA)      // 薰衣草
    val CandyPeach = Color(0xFFFFDAB9)         // 蜜桃色
    val CandyCoral = Color(0xFFFF7F7F)         // 珊瑚色
    val CandyLilac = Color(0xFFDDA0DD)         // 丁香紫

    // ==================== 可爱渐变色组 ====================
    val GradientSunrise = listOf(Color(0xFFFFB6C1), Color(0xFFFFE4B5))     // 日出渐变
    val GradientOcean = listOf(Color(0xFF87CEEB), Color(0xFF98FB98))       // 海洋渐变
    val GradientSunset = listOf(Color(0xFFFFB385), Color(0xFFFF7B7B))      // 日落渐变
    val GradientDream = listOf(Color(0xFF9B7EDE), Color(0xFFFFB6C1))       // 梦幻渐变
    val GradientForest = listOf(Color(0xFF7DD3C0), Color(0xFF98FB98))      // 森林渐变
    val GradientCandy = listOf(Color(0xFFDDA0DD), Color(0xFF87CEEB))       // 糖果渐变

    // ==================== 收入/支出颜色 - 可爱版 ====================
    val Income = Color(0xFF7DD3C0)             // 薄荷绿（收入）
    val IncomeLight = Color(0xFFD7F5EE)
    val Expense = Color(0xFFFF7B7B)            // 珊瑚红（支出）
    val ExpenseLight = Color(0xFFFFE5E5)

    // ==================== 可爱图表颜色 ====================
    val ChartColors = listOf(
        Color(0xFF9B7EDE),  // 薰衣草紫
        Color(0xFF7DD3C0),  // 薄荷绿
        Color(0xFFFFB385),  // 蜜桃橙
        Color(0xFFFF7B7B),  // 珊瑚红
        Color(0xFF87CEEB),  // 天空蓝
        Color(0xFFFFE66D),  // 柠檬黄
        Color(0xFFDDA0DD),  // 丁香紫
        Color(0xFF98FB98),  // 浅绿色
        Color(0xFFFFB6C1),  // 糖果粉
        Color(0xFFB0C4DE),  // 钢蓝色
        Color(0xFFF0E68C),  // 卡其色
        Color(0xFFE6E6FA)   // 薰衣草色
    )

    // ==================== 心情颜色 - 可爱版 ====================
    val MoodColors = listOf(
        Color(0xFFFF7B7B),  // 1 - 很差 - 珊瑚红
        Color(0xFFFFB385),  // 2 - 较差 - 蜜桃橙
        Color(0xFFFFE66D),  // 3 - 一般 - 柠檬黄
        Color(0xFF98FB98),  // 4 - 较好 - 浅绿
        Color(0xFF7DD3C0)   // 5 - 很好 - 薄荷绿
    )

    // ==================== 优先级颜色 - 可爱版 ====================
    val PriorityHigh = Color(0xFFFF7B7B)       // 高优先级 - 珊瑚红
    val PriorityMedium = Color(0xFFFFB385)     // 中优先级 - 蜜桃橙
    val PriorityLow = Color(0xFF7DD3C0)        // 低优先级 - 薄荷绿
    val PriorityNone = Color(0xFFB0B0C0)       // 无优先级 - 浅灰紫

    // ==================== 四象限颜色 ====================
    val QuadrantUrgentImportant = Color(0xFFFF7B7B)      // 重要且紧急 - 珊瑚红
    val QuadrantImportant = Color(0xFF9B7EDE)            // 重要不紧急 - 薰衣草紫
    val QuadrantUrgent = Color(0xFFFFB385)               // 紧急不重要 - 蜜桃橙
    val QuadrantNormal = Color(0xFF87CEEB)               // 不紧急不重要 - 天空蓝

    /**
     * 根据索引获取图表颜色
     */
    fun getChartColor(index: Int): Color {
        return ChartColors[index % ChartColors.size]
    }

    /**
     * 根据心情评分获取颜色
     */
    fun getMoodColor(score: Int): Color {
        return if (score in 1..5) MoodColors[score - 1] else PriorityNone
    }

    /**
     * 获取随机糖果色
     */
    fun getRandomCandyColor(index: Int): Color {
        val candyColors = listOf(
            CandyPink, CandyBlue, CandyYellow, CandyMint,
            CandyLavender, CandyPeach, CandyCoral, CandyLilac
        )
        return candyColors[index % candyColors.size]
    }
}
