package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 存钱记录实体类
 *
 * 记录每次存款的详情
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
        Index(value = ["date"])
    ]
)
data class SavingsRecordEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 所属计划ID
    val planId: Long,

    // 存款金额
    val amount: Double,

    // 日期，epochDay格式
    val date: Int,

    // 备注
    val note: String = "",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
)
