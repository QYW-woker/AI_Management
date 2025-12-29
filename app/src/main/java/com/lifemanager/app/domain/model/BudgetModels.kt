package com.lifemanager.app.domain.model

import com.lifemanager.app.core.database.entity.BudgetEntity

/**
 * 预算状态枚举
 */
enum class BudgetStatus {
    NORMAL,     // 正常
    WARNING,    // 警告（接近阈值）
    EXCEEDED    // 超支
}

/**
 * 预算UI状态
 */
sealed class BudgetUiState {
    object Loading : BudgetUiState()
    object Success : BudgetUiState()
    data class Error(val message: String) : BudgetUiState()
}

/**
 * 预算及花销情况
 */
data class BudgetWithSpending(
    val budget: BudgetEntity,
    val totalSpent: Double,
    val remaining: Double,
    val usagePercentage: Int,
    val status: BudgetStatus,
    val categoryBudgets: Map<String, Double>,
    val daysRemaining: Int
)

/**
 * 分类预算状态
 */
data class CategoryBudgetStatus(
    val categoryName: String,
    val budgetAmount: Double,
    val spentAmount: Double,
    val remaining: Double,
    val usagePercentage: Int,
    val status: BudgetStatus
)

/**
 * 月度预算分析（用于图表）
 */
data class MonthlyBudgetAnalysis(
    val yearMonth: Int,
    val budgetAmount: Double,
    val spentAmount: Double,
    val hasBudget: Boolean
) {
    val usagePercentage: Int
        get() = if (budgetAmount > 0) ((spentAmount / budgetAmount) * 100).toInt() else 0

    val remaining: Double
        get() = budgetAmount - spentAmount

    val status: BudgetStatus
        get() = when {
            usagePercentage >= 100 -> BudgetStatus.EXCEEDED
            usagePercentage >= 80 -> BudgetStatus.WARNING
            else -> BudgetStatus.NORMAL
        }

    val monthLabel: String
        get() = "${yearMonth % 100}月"
}

/**
 * 预算编辑状态
 */
data class BudgetEditState(
    val yearMonth: Int = 0,
    val totalBudget: String = "",
    val alertThreshold: Int = 80,
    val alertEnabled: Boolean = true,
    val note: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    // 分类预算列表
    val categoryBudgets: List<CategoryBudgetItem> = emptyList()
)

/**
 * 分类预算项（用于编辑）
 */
data class CategoryBudgetItem(
    val categoryId: Long,
    val categoryName: String,
    val categoryColor: String = "#2196F3",
    val budgetAmount: String = "",
    val spentAmount: Double = 0.0
) {
    val remaining: Double
        get() {
            val budget = budgetAmount.toDoubleOrNull() ?: 0.0
            return budget - spentAmount
        }

    val usagePercentage: Int
        get() {
            val budget = budgetAmount.toDoubleOrNull() ?: 0.0
            return if (budget > 0) ((spentAmount / budget) * 100).toInt() else 0
        }

    val status: BudgetStatus
        get() = when {
            usagePercentage >= 100 -> BudgetStatus.EXCEEDED
            usagePercentage >= 80 -> BudgetStatus.WARNING
            else -> BudgetStatus.NORMAL
        }
}

/**
 * 预算图表数据点
 */
data class BudgetChartData(
    val label: String,
    val budgetValue: Float,
    val spentValue: Float,
    val percentage: Int
)
