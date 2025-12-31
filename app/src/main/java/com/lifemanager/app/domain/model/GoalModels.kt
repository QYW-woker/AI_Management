package com.lifemanager.app.domain.model

import com.lifemanager.app.core.database.entity.GoalEntity

/**
 * 目标UI状态
 */
sealed class GoalUiState {
    object Loading : GoalUiState()
    data class Success(val goals: List<GoalEntity> = emptyList()) : GoalUiState()
    data class Error(val message: String) : GoalUiState()
}

/**
 * 目标编辑状态
 */
data class GoalEditState(
    val id: Long = 0,
    val title: String = "",
    val description: String = "",
    val goalType: String = "YEARLY",
    val category: String = "CAREER",
    val startDate: Int = 0,
    val endDate: Int? = null,
    val progressType: String = "PERCENTAGE",
    val targetValue: Double? = null,
    val currentValue: Double = 0.0,
    val unit: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * 目标统计数据
 */
data class GoalStatistics(
    val activeCount: Int = 0,
    val completedCount: Int = 0,
    val totalProgress: Float = 0f
)

/**
 * 目标及其子目标的完整结构
 */
data class GoalWithChildren(
    val goal: GoalEntity,
    val children: List<GoalEntity> = emptyList(),
    val childCount: Int = 0,
    val completedChildCount: Int = 0,
    val childProgress: Float = 0f  // 子目标完成进度 0-1
) {
    /**
     * 是否有子目标
     */
    fun hasChildren(): Boolean = childCount > 0

    /**
     * 是否所有子目标都已完成
     */
    fun allChildrenCompleted(): Boolean = childCount > 0 && completedChildCount >= childCount

    /**
     * 获取子目标进度百分比文本
     */
    fun getChildProgressText(): String {
        return if (childCount > 0) {
            "$completedChildCount/$childCount"
        } else {
            ""
        }
    }
}

/**
 * 子目标编辑状态
 */
data class SubGoalEditState(
    val parentId: Long = 0,
    val title: String = "",
    val description: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * 目标类型选项
 */
val goalTypeOptions = listOf(
    "YEARLY" to "年度目标",
    "QUARTERLY" to "季度目标",
    "MONTHLY" to "月度目标",
    "LONG_TERM" to "长期目标",
    "CUSTOM" to "自定义"
)

/**
 * 目标分类选项
 */
val goalCategoryOptions = listOf(
    "CAREER" to "事业",
    "FINANCE" to "财务",
    "HEALTH" to "健康",
    "LEARNING" to "学习",
    "RELATIONSHIP" to "人际关系",
    "LIFESTYLE" to "生活方式",
    "HOBBY" to "兴趣爱好"
)

/**
 * 获取目标分类显示名称
 */
fun getCategoryDisplayName(category: String): String {
    return goalCategoryOptions.find { it.first == category }?.second ?: category
}

/**
 * 获取目标类型显示名称
 */
fun getGoalTypeDisplayName(type: String): String {
    return goalTypeOptions.find { it.first == type }?.second ?: type
}
