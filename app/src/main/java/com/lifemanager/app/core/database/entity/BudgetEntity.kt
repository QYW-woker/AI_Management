package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 月度预算实体类
 *
 * 用于管理每月的预算设置和追踪
 * 支持总预算和分类预算
 */
@Entity(
    tableName = "budgets",
    indices = [
        Index(value = ["yearMonth"], unique = true)
    ]
)
data class BudgetEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 年月，格式：YYYYMM，如202412
    val yearMonth: Int,

    // 月度总预算金额
    val totalBudget: Double,

    // 分类预算，JSON格式
    // 如: {"餐饮": 2000, "交通": 500, "娱乐": 1000}
    val categoryBudgets: String = "{}",

    // 预算提醒阈值（百分比），达到此比例时提醒
    val alertThreshold: Int = 80,

    // 是否启用预算提醒
    val alertEnabled: Boolean = true,

    // 备注
    val note: String = "",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 预算状态枚举
 */
object BudgetStatus {
    const val NORMAL = "NORMAL"       // 正常（低于阈值）
    const val WARNING = "WARNING"     // 警告（接近阈值）
    const val EXCEEDED = "EXCEEDED"   // 超支
}
