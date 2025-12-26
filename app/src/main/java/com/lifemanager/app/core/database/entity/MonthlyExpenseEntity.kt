package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 月度开销记录实体类
 *
 * 用于记录每月的固定和可变开销
 * 如房租、水电、餐饮、交通等生活成本
 * 支持设置预算进行对比
 */
@Entity(
    tableName = "monthly_expenses",
    foreignKeys = [
        ForeignKey(
            entity = CustomFieldEntity::class,
            parentColumns = ["id"],
            childColumns = ["fieldId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["yearMonth", "fieldId"]),
        Index(value = ["yearMonth"]),
        Index(value = ["fieldId"])
    ]
)
data class MonthlyExpenseEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 年月
    val yearMonth: Int,

    // 关联的自定义字段ID
    val fieldId: Long?,

    // 实际金额
    val amount: Double,

    // 预算金额（可选）
    val budgetAmount: Double? = null,

    // 开销类型
    // FIXED: 固定开销（如房租、物业费）
    // VARIABLE: 可变开销（如餐饮、娱乐）
    val expenseType: String = "VARIABLE",

    // 备注
    val note: String = "",

    // 是否从日常记账自动汇总
    val isAutoAggregated: Boolean = false,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 开销类型枚举
 */
object ExpenseType {
    const val FIXED = "FIXED"       // 固定开销
    const val VARIABLE = "VARIABLE" // 可变开销
}
