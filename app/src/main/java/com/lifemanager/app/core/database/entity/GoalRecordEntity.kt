package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 目标记录实体类
 *
 * 用于记录目标的进度更新、里程碑和事件
 * 支持时间轴展示
 */
@Entity(
    tableName = "goal_records",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("goalId")]
)
data class GoalRecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 关联的目标ID
    val goalId: Long,

    // 记录类型
    // PROGRESS: 进度更新
    // MILESTONE: 里程碑
    // NOTE: 备注/日志
    // START: 开始
    // COMPLETE: 完成
    // ABANDON: 放弃
    val recordType: String,

    // 记录标题
    val title: String,

    // 记录内容/描述
    val content: String = "",

    // 进度值变化（如有）
    val progressValue: Double? = null,

    // 之前的进度值
    val previousValue: Double? = null,

    // 记录日期（epochDay）
    val recordDate: Int,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * 记录类型枚举
 */
object GoalRecordType {
    const val PROGRESS = "PROGRESS"     // 进度更新
    const val MILESTONE = "MILESTONE"   // 里程碑
    const val NOTE = "NOTE"             // 备注
    const val START = "START"           // 开始
    const val COMPLETE = "COMPLETE"     // 完成
    const val ABANDON = "ABANDON"       // 放弃
}
