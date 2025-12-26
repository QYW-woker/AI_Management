package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 目标里程碑实体类
 *
 * 用于将大目标拆解为多个可执行的小步骤
 * 每个里程碑关联一个目标，有独立的完成状态
 */
@Entity(
    tableName = "goal_milestones",
    foreignKeys = [
        ForeignKey(
            entity = GoalEntity::class,
            parentColumns = ["id"],
            childColumns = ["goalId"],
            onDelete = ForeignKey.CASCADE  // 目标删除时级联删除里程碑
        )
    ],
    indices = [Index(value = ["goalId"])]
)
data class GoalMilestoneEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 关联的目标ID
    val goalId: Long,

    // 里程碑标题
    val title: String,

    // 里程碑详细描述
    val description: String = "",

    // 计划完成日期，epochDay格式
    val targetDate: Int,

    // 是否已完成
    val isCompleted: Boolean = false,

    // 完成时间戳
    val completedAt: Long? = null,

    // 排序顺序
    val sortOrder: Int = 0,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
)
