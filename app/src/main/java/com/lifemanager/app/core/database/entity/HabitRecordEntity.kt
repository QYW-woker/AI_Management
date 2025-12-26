package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 习惯打卡记录实体类
 *
 * 记录每天的习惯完成情况
 * 每个习惯每天只能有一条记录
 */
@Entity(
    tableName = "habit_records",
    foreignKeys = [
        ForeignKey(
            entity = HabitEntity::class,
            parentColumns = ["id"],
            childColumns = ["habitId"],
            onDelete = ForeignKey.CASCADE  // 习惯删除时级联删除记录
        )
    ],
    indices = [
        Index(value = ["habitId", "date"], unique = true),  // 每个习惯每天唯一
        Index(value = ["habitId"]),
        Index(value = ["date"])
    ]
)
data class HabitRecordEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 习惯ID
    val habitId: Long,

    // 日期，epochDay格式
    val date: Int,

    // 是否完成
    val isCompleted: Boolean = true,

    // 完成数值（数值型习惯使用）
    val value: Double? = null,

    // 备注
    val note: String = "",

    // 打卡时间
    val createdAt: Long = System.currentTimeMillis()
)
