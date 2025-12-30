package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 账户转账记录实体
 *
 * 用于记录账户间的资金转移，不计入收入或支出统计
 */
@Entity(
    tableName = "account_transfers",
    foreignKeys = [
        ForeignKey(
            entity = FundAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["fromAccountId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FundAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["toAccountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["fromAccountId"]),
        Index(value = ["toAccountId"]),
        Index(value = ["date", "fromAccountId"])
    ]
)
data class TransferEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 转出账户ID
    val fromAccountId: Long,

    // 转入账户ID
    val toAccountId: Long,

    // 转账金额
    val amount: Double,

    // 手续费（可选）
    val fee: Double = 0.0,

    // 日期（epochDay格式）
    val date: Int,

    // 时间（HH:mm格式）
    val time: String = "",

    // 备注
    val note: String = "",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)
