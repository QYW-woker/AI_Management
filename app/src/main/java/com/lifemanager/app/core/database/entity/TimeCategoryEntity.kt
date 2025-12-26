package com.lifemanager.app.core.database.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 时间分类实体类
 *
 * 用于定义时间统计的分类
 * 如工作、学习、运动、娱乐等
 * 支持层级结构
 */
@Entity(
    tableName = "time_categories",
    indices = [Index(value = ["parentId"])]
)
data class TimeCategoryEntity(
    // 主键ID
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    // 分类名称
    val name: String,

    // 父分类ID，0表示顶级分类
    val parentId: Long = 0,

    // 图标名称
    val iconName: String = "schedule",

    // 颜色
    val color: String = "#2196F3",

    // 排序顺序
    val sortOrder: Int = 0,

    // 是否启用
    val isEnabled: Boolean = true,

    // 创建时间
    val createdAt: Long = System.currentTimeMillis()
)
