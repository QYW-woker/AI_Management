package com.lifemanager.app.core.util

import java.text.DecimalFormat

/**
 * 扩展函数工具类
 *
 * 提供各种类型的便捷扩展方法
 */

// ==================== 金额格式化 ====================

/**
 * 格式化金额（带千分位，保留两位小数）
 */
fun Double.formatAmount(): String {
    val format = DecimalFormat("#,##0.00")
    return format.format(this)
}

/**
 * 格式化金额（智能显示，大金额显示为万）
 */
fun Double.formatAmountSmart(): String {
    return when {
        this >= 100_000_000 -> String.format("%.2f亿", this / 100_000_000)
        this >= 10_000 -> String.format("%.2f万", this / 10_000)
        else -> formatAmount()
    }
}

/**
 * 格式化金额（简洁模式，无小数）
 */
fun Double.formatAmountCompact(): String {
    return when {
        this >= 100_000_000 -> String.format("%.1f亿", this / 100_000_000)
        this >= 10_000 -> String.format("%.1f万", this / 10_000)
        this >= 1_000 -> String.format("%.1fk", this / 1_000)
        else -> this.toInt().toString()
    }
}

/**
 * 格式化为带正负号的金额
 */
fun Double.formatSignedAmount(): String {
    val sign = if (this >= 0) "+" else ""
    return "$sign${formatAmount()}"
}

// ==================== 百分比格式化 ====================

/**
 * 格式化百分比（一位小数）
 */
fun Double.formatPercent(): String {
    return String.format("%.1f", this)
}

/**
 * 格式化百分比（整数）
 */
fun Double.formatPercentInt(): String {
    return String.format("%.0f", this)
}

/**
 * Float格式化百分比
 */
fun Float.formatPercent(): String {
    return String.format("%.1f", this)
}

// ==================== 进度计算 ====================

/**
 * 计算进度百分比（0-100）
 */
fun calculateProgress(current: Double, target: Double): Float {
    if (target <= 0) return 0f
    val progress = (current / target * 100).toFloat()
    return progress.coerceIn(0f, 100f)
}

/**
 * 计算进度比例（0-1）
 */
fun calculateProgressRatio(current: Double, target: Double): Float {
    if (target <= 0) return 0f
    val ratio = (current / target).toFloat()
    return ratio.coerceIn(0f, 1f)
}

// ==================== 字符串工具 ====================

/**
 * 安全截取字符串
 */
fun String.safeSubstring(start: Int, end: Int): String {
    if (this.isEmpty()) return ""
    val safeStart = start.coerceIn(0, this.length)
    val safeEnd = end.coerceIn(safeStart, this.length)
    return this.substring(safeStart, safeEnd)
}

/**
 * 首字母大写
 */
fun String.capitalizeFirst(): String {
    return if (this.isEmpty()) this else this[0].uppercaseChar() + this.substring(1)
}

// ==================== 列表工具 ====================

/**
 * 安全获取列表元素
 */
fun <T> List<T>.getOrDefault(index: Int, default: T): T {
    return if (index in indices) this[index] else default
}

/**
 * 列表求和
 */
fun List<Double>.sumOrZero(): Double {
    return if (this.isEmpty()) 0.0 else this.sum()
}

// ==================== 时间格式化 ====================

/**
 * 时间戳格式化为时间字符串（HH:mm）
 */
fun Long.formatToTime(): String {
    val hours = (this / 1000 / 60 / 60) % 24
    val minutes = (this / 1000 / 60) % 60
    return String.format("%02d:%02d", hours, minutes)
}

/**
 * 分钟数格式化为时长字符串
 */
fun Int.formatDuration(): String {
    return when {
        this < 60 -> "${this}分钟"
        this % 60 == 0 -> "${this / 60}小时"
        else -> "${this / 60}小时${this % 60}分钟"
    }
}

/**
 * 分钟数格式化为简短时长（如 2h30m）
 */
fun Int.formatDurationShort(): String {
    return when {
        this < 60 -> "${this}m"
        this % 60 == 0 -> "${this / 60}h"
        else -> "${this / 60}h${this % 60}m"
    }
}
