package com.lifemanager.app.core.util

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * 日期时间工具类
 *
 * 提供日期相关的转换和格式化方法
 */
object DateUtils {

    // 常用日期格式
    private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    private val DATE_CHINESE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日")
    private val MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月")
    private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm")
    private val DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

    /**
     * 获取今天的日期
     */
    fun today(): LocalDate = LocalDate.now()

    /**
     * 获取今天的epochDay
     */
    fun todayEpochDay(): Int = LocalDate.now().toEpochDay().toInt()

    /**
     * 获取当前年月（格式YYYYMM）
     */
    fun currentYearMonth(): Int {
        val now = LocalDate.now()
        return now.year * 100 + now.monthValue
    }

    /**
     * LocalDate转epochDay
     */
    fun toEpochDay(date: LocalDate): Int = date.toEpochDay().toInt()

    /**
     * epochDay转LocalDate
     */
    fun fromEpochDay(epochDay: Int): LocalDate = LocalDate.ofEpochDay(epochDay.toLong())

    /**
     * 年月转LocalDate（当月第一天）
     */
    fun yearMonthToDate(yearMonth: Int): LocalDate {
        val year = yearMonth / 100
        val month = yearMonth % 100
        return LocalDate.of(year, month, 1)
    }

    /**
     * LocalDate转年月
     */
    fun dateToYearMonth(date: LocalDate): Int {
        return date.year * 100 + date.monthValue
    }

    /**
     * 获取月份的第一天和最后一天
     */
    fun getMonthRange(yearMonth: Int): Pair<Int, Int> {
        val firstDay = yearMonthToDate(yearMonth)
        val lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth())
        return toEpochDay(firstDay) to toEpochDay(lastDay)
    }

    /**
     * 格式化日期为 yyyy-MM-dd
     */
    fun formatDate(date: LocalDate): String = date.format(DATE_FORMATTER)

    /**
     * 格式化日期为 yyyy年MM月dd日
     */
    fun formatDateChinese(date: LocalDate): String = date.format(DATE_CHINESE_FORMATTER)

    /**
     * 格式化年月为 yyyy年MM月
     */
    fun formatYearMonth(yearMonth: Int): String {
        return yearMonthToDate(yearMonth).format(MONTH_FORMATTER)
    }

    /**
     * 格式化epochDay为日期字符串
     */
    fun formatEpochDay(epochDay: Int): String = formatDate(fromEpochDay(epochDay))

    /**
     * 格式化时间为 HH:mm
     */
    fun formatTime(dateTime: LocalDateTime): String = dateTime.format(TIME_FORMATTER)

    /**
     * 格式化时长（分钟）为可读字符串
     */
    fun formatDuration(minutes: Int): String {
        return when {
            minutes < 60 -> "${minutes}分钟"
            minutes % 60 == 0 -> "${minutes / 60}小时"
            else -> "${minutes / 60}小时${minutes % 60}分钟"
        }
    }

    /**
     * 计算两个日期之间的天数
     */
    fun daysBetween(start: LocalDate, end: LocalDate): Long {
        return ChronoUnit.DAYS.between(start, end)
    }

    /**
     * 获取相对日期描述
     */
    fun getRelativeDateString(epochDay: Int): String {
        val today = todayEpochDay()
        val diff = today - epochDay
        return when {
            diff == 0 -> "今天"
            diff == 1 -> "昨天"
            diff == 2 -> "前天"
            diff in 3..7 -> "${diff}天前"
            diff == -1 -> "明天"
            diff == -2 -> "后天"
            else -> formatEpochDay(epochDay)
        }
    }

    /**
     * 获取本周的开始日期（周一）
     */
    fun getWeekStart(date: LocalDate = today()): LocalDate {
        return date.minusDays((date.dayOfWeek.value - 1).toLong())
    }

    /**
     * 获取本月的开始日期
     */
    fun getMonthStart(date: LocalDate = today()): LocalDate {
        return date.withDayOfMonth(1)
    }

    /**
     * 获取本季度的开始日期
     */
    fun getQuarterStart(date: LocalDate = today()): LocalDate {
        val quarterStartMonth = ((date.monthValue - 1) / 3) * 3 + 1
        return date.withMonth(quarterStartMonth).withDayOfMonth(1)
    }

    /**
     * 获取本年的开始日期
     */
    fun getYearStart(date: LocalDate = today()): LocalDate {
        return date.withDayOfYear(1)
    }

    /**
     * 获取上个月的年月值
     */
    fun getPreviousYearMonth(yearMonth: Int): Int {
        val date = yearMonthToDate(yearMonth).minusMonths(1)
        return dateToYearMonth(date)
    }

    /**
     * 获取下个月的年月值
     */
    fun getNextYearMonth(yearMonth: Int): Int {
        val date = yearMonthToDate(yearMonth).plusMonths(1)
        return dateToYearMonth(date)
    }
}
