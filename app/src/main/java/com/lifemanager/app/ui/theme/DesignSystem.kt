package com.lifemanager.app.ui.theme

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 统一设计系统
 *
 * 设计原则:
 * - 干净 (Clean)
 * - 克制 (Calm)
 * - 有呼吸感 (Spacing-driven)
 * - 轻灵不花哨 (Light, not flashy)
 * - 工具感但不冰冷
 */

// ==================== 间距系统 (8dp基准) ====================
@Immutable
object Spacing {
    /** 最小间距 4dp - 用于紧凑元素间 */
    val xs: Dp = 4.dp

    /** 小间距 8dp - 用于相关元素组内 */
    val sm: Dp = 8.dp

    /** 中间距 12dp - 用于列表项内部 */
    val md: Dp = 12.dp

    /** 标准间距 16dp - 用于卡片内边距、模块间 */
    val lg: Dp = 16.dp

    /** 大间距 24dp - 用于区块分隔 */
    val xl: Dp = 24.dp

    /** 超大间距 32dp - 用于页面主要区块 */
    val xxl: Dp = 32.dp

    /** 页面边距 - 水平 */
    val pageHorizontal: Dp = 20.dp

    /** 页面边距 - 垂直 */
    val pageVertical: Dp = 16.dp

    /** 卡片内边距 */
    val cardPadding: Dp = 16.dp

    /** 列表项内边距 - 垂直 */
    val listItemVertical: Dp = 14.dp

    /** 列表项内边距 - 水平 */
    val listItemHorizontal: Dp = 16.dp

    /** 列表项间距 */
    val listItemGap: Dp = 12.dp

    /** 模块间距 */
    val sectionGap: Dp = 24.dp

    /** 底部安全区 */
    val bottomSafe: Dp = 24.dp
}

// ==================== 页面内边距预设 ====================
object PagePadding {
    /** 标准页面内边距 */
    val standard = PaddingValues(
        horizontal = Spacing.pageHorizontal,
        vertical = Spacing.pageVertical
    )

    /** 列表页面内边距 */
    val list = PaddingValues(
        horizontal = Spacing.pageHorizontal,
        vertical = Spacing.md
    )

    /** 详情页内边距 */
    val detail = PaddingValues(
        horizontal = Spacing.pageHorizontal,
        vertical = Spacing.lg
    )
}

// ==================== 简洁色彩系统 ====================
@Immutable
object CleanColors {
    // ===== 主色 - 仅用于关键操作 =====
    val primary = Color(0xFF5B6EE1)          // 克制的蓝紫色
    val primaryLight = Color(0xFFEEF1FF)     // 极淡主色背景
    val onPrimary = Color.White

    // ===== 成功/收入色 =====
    val success = Color(0xFF34A853)          // 沉稳绿
    val successLight = Color(0xFFE8F5E9)
    val onSuccess = Color.White

    // ===== 警告色 =====
    val warning = Color(0xFFE8A23B)          // 柔和橙
    val warningLight = Color(0xFFFFF8E1)
    val onWarning = Color.White

    // ===== 错误/支出色 =====
    val error = Color(0xFFE53935)            // 柔和红
    val errorLight = Color(0xFFFFEBEE)
    val onError = Color.White

    // ===== 文本层级 =====
    val textPrimary = Color(0xFF1A1A1A)      // 主文本 - 高对比
    val textSecondary = Color(0xFF666666)    // 次要文本
    val textTertiary = Color(0xFF999999)     // 第三层文本
    val textPlaceholder = Color(0xFFBDBDBD)  // 占位符
    val textDisabled = Color(0xFFD0D0D0)     // 禁用状态

    // ===== 背景层级 =====
    val background = Color(0xFFFAFAFA)       // 页面背景 - 极淡灰
    val surface = Color.White                 // 卡片/内容区背景
    val surfaceVariant = Color(0xFFF5F5F5)   // 次级表面
    val surfaceElevated = Color.White         // 提升的表面

    // ===== 边框和分隔线 =====
    val divider = Color(0xFFEEEEEE)          // 分隔线 - 极淡
    val border = Color(0xFFE0E0E0)           // 边框
    val borderLight = Color(0xFFF0F0F0)      // 更淡的边框

    // ===== 优先级颜色 (克制使用) =====
    val priorityHigh = Color(0xFFE53935)     // 高优先级
    val priorityMedium = Color(0xFFE8A23B)   // 中优先级
    val priorityLow = Color(0xFF34A853)      // 低优先级
    val priorityNone = Color(0xFFBDBDBD)     // 无优先级

    // ===== 状态指示色 =====
    val completed = Color(0xFF34A853)
    val pending = Color(0xFFE8A23B)
    val overdue = Color(0xFFE53935)

    // ===== 深色模式 =====
    object Dark {
        val primary = Color(0xFF8B9EFF)
        val background = Color(0xFF121212)
        val surface = Color(0xFF1E1E1E)
        val surfaceVariant = Color(0xFF2C2C2C)
        val textPrimary = Color(0xFFF5F5F5)
        val textSecondary = Color(0xFFB0B0B0)
        val textTertiary = Color(0xFF808080)
        val divider = Color(0xFF2C2C2C)
        val border = Color(0xFF3C3C3C)
    }
}

// ==================== 简洁字体系统 (5个层级) ====================
@Immutable
object CleanTypography {
    /** 页面大标题 - 24sp SemiBold */
    val headline = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    )

    /** 区块标题 - 18sp Medium */
    val title = TextStyle(
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 26.sp
    )

    /** 列表主文本/正文 - 16sp Normal */
    val body = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 24.sp,
        letterSpacing = 0.2.sp
    )

    /** 次要信息 - 14sp Normal */
    val secondary = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 20.sp,
        letterSpacing = 0.15.sp
    )

    /** 说明/时间戳 - 12sp Normal */
    val caption = TextStyle(
        fontSize = 12.sp,
        fontWeight = FontWeight.Normal,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp
    )

    /** 按钮/标签 - 14sp Medium */
    val button = TextStyle(
        fontSize = 14.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    )

    /** 金额显示 - 大 */
    val amountLarge = TextStyle(
        fontSize = 28.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 36.sp,
        letterSpacing = (-0.5).sp
    )

    /** 金额显示 - 中 */
    val amountMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        lineHeight = 28.sp
    )

    /** 金额显示 - 小 */
    val amountSmall = TextStyle(
        fontSize = 16.sp,
        fontWeight = FontWeight.Medium,
        lineHeight = 22.sp
    )
}

// ==================== 圆角系统 ====================
@Immutable
object Radius {
    /** 小圆角 - 用于标签、小按钮 */
    val sm: Dp = 8.dp

    /** 中圆角 - 用于卡片、输入框 */
    val md: Dp = 12.dp

    /** 大圆角 - 用于大卡片、对话框 */
    val lg: Dp = 16.dp

    /** 超大圆角 - 用于底部弹窗 */
    val xl: Dp = 24.dp

    /** 胶囊形 */
    val full: Dp = 999.dp
}

// ==================== 阴影/高度系统 ====================
@Immutable
object Elevation {
    /** 无阴影 */
    val none: Dp = 0.dp

    /** 微阴影 - 用于列表项 */
    val xs: Dp = 1.dp

    /** 小阴影 - 用于卡片 */
    val sm: Dp = 2.dp

    /** 中阴影 - 用于悬浮按钮 */
    val md: Dp = 4.dp

    /** 大阴影 - 用于对话框 */
    val lg: Dp = 8.dp
}

// ==================== 图标尺寸 ====================
@Immutable
object IconSize {
    val xs: Dp = 16.dp
    val sm: Dp = 20.dp
    val md: Dp = 24.dp
    val lg: Dp = 32.dp
    val xl: Dp = 40.dp
}

// ==================== 动画时长 ====================
@Immutable
object Duration {
    /** 快速 - 150ms */
    const val fast: Int = 150

    /** 标准 - 250ms */
    const val standard: Int = 250

    /** 慢速 - 350ms */
    const val slow: Int = 350

    /** 进入动画 */
    const val enter: Int = 300

    /** 退出动画 */
    const val exit: Int = 200
}

// ==================== 触摸目标尺寸 ====================
@Immutable
object TouchTarget {
    /** 最小触摸目标 */
    val min: Dp = 48.dp

    /** 标准按钮高度 */
    val button: Dp = 48.dp

    /** 列表项最小高度 */
    val listItem: Dp = 56.dp

    /** 紧凑列表项高度 */
    val listItemCompact: Dp = 48.dp
}

// ==================== CompositionLocal 提供者 ====================
val LocalSpacing = staticCompositionLocalOf { Spacing }
val LocalCleanColors = staticCompositionLocalOf { CleanColors }
val LocalCleanTypography = staticCompositionLocalOf { CleanTypography }
