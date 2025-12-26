package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 习惯实体类
 *
 * 用于定义要养成的习惯
 * 支持多种打卡频率
 * 支持数值型习惯（如喝水量）
 */
@Entity(
    tableName = "habits",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedGoalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index(value = ["linkedGoalId"])]
)
data class HabitEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 习惯名称
    val name: String,

    // 描述
    val description: String = "",

    // 图标名称
    val iconName: String = "check_circle",

    // 颜色
    val color: String = "#4CAF50",

    // 打卡频率
    // DAILY: 每天
    // WEEKDAYS: 工作日
    // WEEKLY_TIMES: 每周X次
    // MONTHLY_TIMES: 每月X次
    // CUSTOM: 自定义
    val frequency: String = "DAILY",

    // 目标次数（用于WEEKLY_TIMES/MONTHLY_TIMES）
    val targetTimes: Int = 1,

    // 自定义频率规则，JSON格式
    // 如: {"weekdays": [1,2,3,4,5]} 表示周一到周五
    val customFrequency: String? = null,

    // 提醒时间，HH:mm格式
    val reminderTime: String? = null,

    // 是否为数值型习惯
    // 如：喝水8杯、走路10000步等
    val isNumeric: Boolean = false,

    // 目标数值（数值型习惯使用）
    val targetValue: Double? = null,

    // 数值单位
    val unit: String = "",

    // 关联目标ID
    val linkedGoalId: Long? = null,

    // 状态: ACTIVE(活跃), PAUSED(暂停), ARCHIVED(归档)
    val status: String = "ACTIVE",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 打卡频率枚举
 */
object HabitFrequency {
    const val DAILY = "DAILY"               // 每天
    const val WEEKDAYS = "WEEKDAYS"         // 工作日
    const val WEEKLY_TIMES = "WEEKLY_TIMES" // 每周X次
    const val MONTHLY_TIMES = "MONTHLY_TIMES" // 每月X次
    const val CUSTOM = "CUSTOM"             // 自定义
}

/**
 * 习惯状态枚举
 */
object HabitStatus {
    const val ACTIVE = "ACTIVE"     // 活跃
    const val PAUSED = "PAUSED"     // 暂停
    const val ARCHIVED = "ARCHIVED" // 归档
}
