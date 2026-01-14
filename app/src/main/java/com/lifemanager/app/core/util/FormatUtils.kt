package com.lifemanager.app.core.util

import com.lifemanager.app.core.data.repository.AppSettings
import com.lifemanager.app.core.data.repository.CurrencySymbol
import com.lifemanager.app.core.data.repository.DateFormat
import com.lifemanager.app.core.data.repository.WeekStartDay
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 格式化工具类
 *
 * 提供基于用户设置的格式化功能
 */
object FormatUtils {

    // ==================== 金额格式化 ====================

    /**
     * 根据用户设置格式化金额
     *
     * @param amount 金额数值
     * @param settings 用户设置
     * @param showSymbol 是否显示货币符号
     * @return 格式化后的金额字符串
     */
    fun formatAmount(
        amount: Double,
        settings: AppSettings,
        showSymbol: Boolean = true
    ): String {
        val pattern = buildPattern(settings.decimalPlaces, settings.useThousandSeparator)
        val symbols = DecimalFormatSymbols(Locale.getDefault()).apply {
            if (settings.useThousandSeparator) {
                groupingSeparator = ','
            }
            decimalSeparator = '.'
        }
        val format = DecimalFormat(pattern, symbols)
        val formattedNumber = format.format(amount)

        return if (showSymbol && settings.currencySymbol != CurrencySymbol.NONE) {
            "${settings.currencySymbol.symbol}$formattedNumber"
        } else {
            formattedNumber
        }
    }

    /**
     * 构建DecimalFormat模式
     */
    private fun buildPattern(decimalPlaces: Int, useThousandSeparator: Boolean): String {
        val integerPart = if (useThousandSeparator) "#,##0" else "0"
        return if (decimalPlaces > 0) {
            "$integerPart.${"0".repeat(decimalPlaces)}"
        } else {
            integerPart
        }
    }

    /**
     * 智能格式化金额（大金额显示为万/亿）
     */
    fun formatAmountSmart(
        amount: Double,
        settings: AppSettings,
        showSymbol: Boolean = true
    ): String {
        val symbol = if (showSymbol && settings.currencySymbol != CurrencySymbol.NONE) {
            settings.currencySymbol.symbol
        } else ""

        return when {
            amount >= 100_000_000 -> {
                val formatted = String.format("%.2f亿", amount / 100_000_000)
                "$symbol$formatted"
            }
            amount >= 10_000 -> {
                val formatted = String.format("%.2f万", amount / 10_000)
                "$symbol$formatted"
            }
            else -> formatAmount(amount, settings, showSymbol)
        }
    }

    /**
     * 格式化金额（简洁模式，用于图表等）
     */
    fun formatAmountCompact(amount: Double, settings: AppSettings): String {
        val symbol = if (settings.currencySymbol != CurrencySymbol.NONE) {
            settings.currencySymbol.symbol
        } else ""

        return when {
            amount >= 100_000_000 -> "$symbol${String.format("%.1f亿", amount / 100_000_000)}"
            amount >= 10_000 -> "$symbol${String.format("%.1f万", amount / 10_000)}"
            amount >= 1_000 -> "$symbol${String.format("%.1fk", amount / 1_000)}"
            else -> "$symbol${amount.toInt()}"
        }
    }

    /**
     * 格式化带符号的金额（收入用+，支出用-）
     */
    fun formatSignedAmount(
        amount: Double,
        isIncome: Boolean,
        settings: AppSettings
    ): String {
        val sign = if (isIncome) "+" else "-"
        val absAmount = kotlin.math.abs(amount)
        return "$sign${formatAmount(absAmount, settings)}"
    }

    // ==================== 日期格式化 ====================

    /**
     * 根据用户设置格式化日期
     */
    fun formatDate(date: LocalDate, settings: AppSettings): String {
        val formatter = DateTimeFormatter.ofPattern(settings.dateFormat.pattern)
        return date.format(formatter)
    }

    /**
     * 格式化日期（从epochDay）
     */
    fun formatDate(epochDay: Int, settings: AppSettings): String {
        val date = LocalDate.ofEpochDay(epochDay.toLong())
        return formatDate(date, settings)
    }

    /**
     * 格式化日期范围
     */
    fun formatDateRange(startDate: LocalDate, endDate: LocalDate, settings: AppSettings): String {
        return "${formatDate(startDate, settings)} - ${formatDate(endDate, settings)}"
    }

    /**
     * 获取相对日期描述
     */
    fun getRelativeDateDescription(date: LocalDate): String {
        val today = LocalDate.now()
        val daysDiff = (date.toEpochDay() - today.toEpochDay()).toInt()

        return when (daysDiff) {
            0 -> "今天"
            1 -> "明天"
            -1 -> "昨天"
            2 -> "后天"
            -2 -> "前天"
            in 3..7 -> "${daysDiff}天后"
            in -7..-3 -> "${-daysDiff}天前"
            else -> null
        } ?: ""
    }

    /**
     * 获取周起始日
     */
    fun getWeekStartDay(settings: AppSettings): DayOfWeek {
        return when (settings.weekStartDay) {
            WeekStartDay.SUNDAY -> DayOfWeek.SUNDAY
            WeekStartDay.MONDAY -> DayOfWeek.MONDAY
        }
    }

    /**
     * 获取本周的起始日期
     */
    fun getWeekStart(date: LocalDate, settings: AppSettings): LocalDate {
        val weekStartDay = getWeekStartDay(settings)
        var current = date
        while (current.dayOfWeek != weekStartDay) {
            current = current.minusDays(1)
        }
        return current
    }

    /**
     * 获取本周的结束日期
     */
    fun getWeekEnd(date: LocalDate, settings: AppSettings): LocalDate {
        return getWeekStart(date, settings).plusDays(6)
    }

    // ==================== 辅助方法 ====================

    /**
     * 获取预览文本（用于设置界面预览）
     */
    fun getAmountPreview(settings: AppSettings): String {
        return formatAmount(12345.67, settings)
    }

    /**
     * 获取日期预览文本
     */
    fun getDatePreview(settings: AppSettings): String {
        return formatDate(LocalDate.of(2024, 1, 15), settings)
    }
}

/**
 * 扩展函数：使用设置格式化金额
 */
fun Double.formatWithSettings(settings: AppSettings, showSymbol: Boolean = true): String {
    return FormatUtils.formatAmount(this, settings, showSymbol)
}

/**
 * 扩展函数：使用设置智能格式化金额
 */
fun Double.formatSmartWithSettings(settings: AppSettings, showSymbol: Boolean = true): String {
    return FormatUtils.formatAmountSmart(this, settings, showSymbol)
}

/**
 * 扩展函数：使用设置格式化日期
 */
fun LocalDate.formatWithSettings(settings: AppSettings): String {
    return FormatUtils.formatDate(this, settings)
}

/**
 * 扩展函数：Int epochDay 转格式化日期
 */
fun Int.toFormattedDate(settings: AppSettings): String {
    return FormatUtils.formatDate(this, settings)
}
