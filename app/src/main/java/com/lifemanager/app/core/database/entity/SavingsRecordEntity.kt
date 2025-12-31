package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 存钱记录类型
 */
object RecordType {
    const val DEPOSIT = "DEPOSIT"       // 存款
    const val WITHDRAWAL = "WITHDRAWAL" // 取款
}

/**
 * 存钱记录实体类
 *
 * 记录每次存款/取款的详情
 */
@Entity(
    tableName = "savings_records",
    foreignKeys = [
        ForeignKey(
            entity = SavingsPlanEntity::class,
            parentColumns = ["id"],
            childColumns = ["planId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["planId"]),
        Index(value = ["date"]),
        Index(value = ["type"])
    ]
)
data class SavingsRecordEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 所属计划ID
    val planId: Long,

    // 金额（正数表示存款，负数表示取款）
    val amount: Double,

    // 记录类型: DEPOSIT 或 WITHDRAWAL
    val type: String = RecordType.DEPOSIT,

    // 日期，epochDay格式
    val date: Int,

    // 备注
    val note: String = "",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
) {
    /**
     * 是否为存款
     */
    fun isDeposit(): Boolean = type == RecordType.DEPOSIT

    /**
     * 是否为取款
     */
    fun isWithdrawal(): Boolean = type == RecordType.WITHDRAWAL

    /**
     * 获取实际影响金额（存款为正，取款为负）
     */
    fun getEffectiveAmount(): Double = if (isWithdrawal()) -kotlin.math.abs(amount) else kotlin.math.abs(amount)
}
