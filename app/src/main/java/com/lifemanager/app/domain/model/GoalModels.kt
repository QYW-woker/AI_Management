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

/**
 * 目标树节点（用于展示多级目标）
 */
data class GoalTreeNode(
    val goal: GoalEntity,
    val level: Int = 0,
    val children: List<GoalTreeNode> = emptyList(),
    var isExpanded: Boolean = false,
    val childCount: Int = 0,
    val progress: Float = 0f
)

/**
 * 目标类型（单级/多级）
 */
enum class GoalStructureType {
    SINGLE,     // 单级目标
    MULTI_LEVEL // 多级目标（带子目标）
}

/**
 * 子目标编辑状态（用于新建多级目标时的临时子目标）
 */
data class SubGoalEditState(
    val tempId: Long = System.currentTimeMillis(), // 临时ID，保存后会被替换
    val title: String = "",
    val description: String = "",
    val targetValue: Double? = null,
    val unit: String = "",
    val progressType: String = "PERCENTAGE"
)

/**
 * 操作结果状态（用于UI反馈）
 */
sealed class OperationResult {
    object Idle : OperationResult()
    object Loading : OperationResult()
    data class Success(val message: String) : OperationResult()
    data class Error(val message: String) : OperationResult()
}

/**
 * 目标详情状态（用于详情页）
 */
data class GoalDetailState(
    val goal: GoalEntity? = null,
    val isLoading: Boolean = false,
    val progress: Float = 0f,
    val remainingDays: Int? = null,
    val operationResult: OperationResult = OperationResult.Idle
)

/**
 * 分类与目标类型的推荐映射
 */
val categoryToTypeMapping = mapOf(
    "HEALTH" to "MONTHLY",      // 健康 → 月度目标（持续习惯）
    "CAREER" to "YEARLY",       // 事业 → 年度目标
    "FINANCE" to "YEARLY",      // 财务 → 年度目标
    "LEARNING" to "QUARTERLY",  // 学习 → 季度目标
    "RELATIONSHIP" to "LONG_TERM", // 人际关系 → 长期目标
    "LIFESTYLE" to "MONTHLY",   // 生活方式 → 月度目标
    "HOBBY" to "CUSTOM"         // 兴趣爱好 → 自定义
)

/**
 * AI分析状态
 */
sealed class AIAnalysisState {
    object Idle : AIAnalysisState()
    object Loading : AIAnalysisState()
    data class Success(val analysis: String) : AIAnalysisState()
    data class Error(val message: String) : AIAnalysisState()
}
