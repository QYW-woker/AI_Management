package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 月度资产/负债记录实体类
 *
 * 用于记录每月的资产和负债状况
 * 包括存款、股票、基金、房产等资产，以及房贷、车贷等负债
 * 每月记录一次，用于追踪净资产变化
 */
@Entity(
    tableName = "monthly_assets",
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
data class MonthlyAssetEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 年月，格式YYYYMM
    val yearMonth: Int,

    // 类型: ASSET(资产) 或 LIABILITY(负债)
    val type: String,

    // 关联的自定义字段ID
    val fieldId: Long?,

    // 金额
    val amount: Double,

    // 备注
    val note: String = "",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 资产类型枚举
 */
object AssetType {
    const val ASSET = "ASSET"           // 资产
    const val LIABILITY = "LIABILITY"   // 负债
}
