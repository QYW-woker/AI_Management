package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 存钱计划实体类
 *
 * 用于设定和追踪存钱目标
 * 支持多种存钱策略
 */
@Entity(tableName = "savings_plans")
data class SavingsPlanEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 计划名称
    val name: String,

    // 描述
    val description: String = "",

    // 目标金额
    val targetAmount: Double,

    // 当前已存金额
    val currentAmount: Double = 0.0,

    // 开始日期，epochDay格式
    val startDate: Int,

    // 目标日期，epochDay格式
    val targetDate: Int,

    // 存钱策略
    // FIXED_DAILY: 每天固定金额
    // FIXED_WEEKLY: 每周固定金额
    // FIXED_MONTHLY: 每月固定金额
    // INCREASING: 递增存钱（如365天挑战）
    // CUSTOM: 自定义
    val strategy: String = "FIXED_MONTHLY",

    // 每期存款金额（固定策略使用）
    val periodAmount: Double? = null,

    // 起始金额（递增策略使用）
    val startAmount: Double? = null,

    // 递增金额（递增策略使用）
    val incrementAmount: Double? = null,

    // 图标名称
    val iconName: String = "savings",

    // 颜色
    val color: String = "#4CAF50",

    // 状态: ACTIVE(进行中), COMPLETED(已完成), PAUSED(暂停), CANCELLED(已取消)
    val status: String = "ACTIVE",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 存钱策略枚举
 */
object SavingsStrategy {
    const val FIXED_DAILY = "FIXED_DAILY"       // 每天固定
    const val FIXED_WEEKLY = "FIXED_WEEKLY"     // 每周固定
    const val FIXED_MONTHLY = "FIXED_MONTHLY"   // 每月固定
    const val INCREASING = "INCREASING"         // 递增存钱
    const val CUSTOM = "CUSTOM"                 // 自定义
}

/**
 * 存钱计划状态枚举
 */
object SavingsPlanStatus {
    const val ACTIVE = "ACTIVE"         // 进行中
    const val COMPLETED = "COMPLETED"   // 已完成
    const val PAUSED = "PAUSED"         // 暂停
    const val CANCELLED = "CANCELLED"   // 已取消
}
