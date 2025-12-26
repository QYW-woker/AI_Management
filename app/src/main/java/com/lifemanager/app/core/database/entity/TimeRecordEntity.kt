package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 时间记录实体类
 *
 * 用于记录每个时间段的活动
 * 支持计时功能（开始-结束）
 * 可关联目标进行时间追踪
 */
@Entity(
    tableName = "time_records",
    foreignKeys = [
        ForeignKey(
            entity = TimeCategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["linkedGoalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["date"]),
        Index(value = ["categoryId"]),
        Index(value = ["linkedGoalId"])
    ]
)
data class TimeRecordEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 分类ID
    val categoryId: Long?,

    // 日期，epochDay格式
    val date: Int,

    // 开始时间戳
    val startTime: Long,

    // 结束时间戳，null表示正在进行中
    val endTime: Long? = null,

    // 时长（分钟）
    val durationMinutes: Int = 0,

    // 关联目标ID
    val linkedGoalId: Long? = null,

    // 备注
    val note: String = "",

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
)
