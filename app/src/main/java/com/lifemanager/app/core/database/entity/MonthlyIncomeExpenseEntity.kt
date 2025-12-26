package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 月度收支记录实体类
 *
 * 用于记录每月的收入和支出明细
 * 每条记录关联一个自定义字段（类别）
 * 支持周期性记录标记
 */
@Entity(
    tableName = "monthly_income_expense",
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
        Index(value = ["fieldId"]),
        Index(value = ["type"])
    ]
)
data class MonthlyIncomeExpenseEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 年月，格式为YYYYMM，如202412表示2024年12月
    val yearMonth: Int,

    // 类型: INCOME(收入) 或 EXPENSE(支出)
    val type: String,

    // 关联的自定义字段ID（分类）
    val fieldId: Long?,

    // 金额，单位为元，保留两位小数
    val amount: Double,

    // 记录日期，epochDay格式
    val recordDate: Int,

    // 备注说明
    val note: String = "",

    // 附件路径，JSON数组格式存储多个文件路径
    val attachments: String = "[]",

    // 是否为周期性记录
    // 如每月固定工资、固定房租等
    val isRecurring: Boolean = false,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 收支类型枚举
 */
object IncomeExpenseType {
    const val INCOME = "INCOME"     // 收入
    const val EXPENSE = "EXPENSE"   // 支出
}
