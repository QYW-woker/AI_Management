package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 目标实体类
 *
 * 用于存储用户设定的各类目标
 * 支持年度、季度、月度、长期和自定义周期目标
 * 可关联财务字段实现自动进度更新
 */
@Entity(tableName = "goals")
data class GoalEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 目标标题
    val title: String,

    // 目标详细描述
    val description: String = "",

    // 目标类型
    // 可选值: YEARLY(年度), QUARTERLY(季度), MONTHLY(月度), CUSTOM(自定义), LONG_TERM(长期)
    val goalType: String,

    // 目标分类
    // 可选值: CAREER(事业), FINANCE(财务), HEALTH(健康), LEARNING(学习),
    //        RELATIONSHIP(人际关系), LIFESTYLE(生活方式), HOBBY(兴趣爱好)
    val category: String,

    // 开始日期，使用epochDay格式（从1970-01-01起的天数）
    val startDate: Int,

    // 结束日期，长期目标可为null
    val endDate: Int?,

    // 进度类型
    // PERCENTAGE: 百分比进度（0-100）
    // NUMERIC: 数值进度（有具体目标数值）
    val progressType: String = "PERCENTAGE",

    // 目标数值（仅数值型目标使用）
    // 如：存款10万元，跑步100公里等
    val targetValue: Double? = null,

    // 当前进度数值
    val currentValue: Double = 0.0,

    // 数值单位（如"元"、"公里"、"本"、"个"等）
    val unit: String = "",

    // 目标状态
    // 可选值: ACTIVE(进行中), COMPLETED(已完成), ABANDONED(已放弃), ARCHIVED(已归档)
    val status: String = "ACTIVE",

    // 关联的财务字段ID
    // 设置后可自动根据该字段的数据更新目标进度
    val linkedFieldId: Long? = null,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis(),

    // 最后更新时间
    val updatedAt: Long = System.currentTimeMillis()
)

/**
 * 目标类型枚举
 */
object GoalType {
    const val YEARLY = "YEARLY"         // 年度目标
    const val QUARTERLY = "QUARTERLY"   // 季度目标
    const val MONTHLY = "MONTHLY"       // 月度目标
    const val CUSTOM = "CUSTOM"         // 自定义周期
    const val LONG_TERM = "LONG_TERM"   // 长期目标（无明确结束日期）
}

/**
 * 目标分类枚举
 */
object GoalCategory {
    const val CAREER = "CAREER"             // 事业
    const val FINANCE = "FINANCE"           // 财务
    const val HEALTH = "HEALTH"             // 健康
    const val LEARNING = "LEARNING"         // 学习
    const val RELATIONSHIP = "RELATIONSHIP" // 人际关系
    const val LIFESTYLE = "LIFESTYLE"       // 生活方式
    const val HOBBY = "HOBBY"               // 兴趣爱好
}

/**
 * 目标状态枚举
 */
object GoalStatus {
    const val ACTIVE = "ACTIVE"         // 进行中
    const val COMPLETED = "COMPLETED"   // 已完成
    const val ABANDONED = "ABANDONED"   // 已放弃
    const val ARCHIVED = "ARCHIVED"     // 已归档
}

/**
 * 进度类型枚举
 */
object ProgressType {
    const val PERCENTAGE = "PERCENTAGE" // 百分比
    const val NUMERIC = "NUMERIC"       // 数值型
}
