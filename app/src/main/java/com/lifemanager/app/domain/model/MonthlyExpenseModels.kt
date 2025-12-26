package com.lifemanager.app.domain.model

import com.lifemanager.app.core.database.entity.CustomFieldEntity
import com.lifemanager.app.core.database.entity.MonthlyExpenseEntity

/**
 * 月度开销记录模型（带字段详情）
 */
data class MonthlyExpenseWithField(
    val record: MonthlyExpenseEntity,
    val field: CustomFieldEntity?
)

/**
 * 月度开销统计模型
 */
data class ExpenseStats(
    val yearMonth: Int,
    val totalExpense: Double,
    val fixedExpense: Double,
    val variableExpense: Double,
    val totalBudget: Double = 0.0
) {
    /**
     * 固定开销占比
     */
    val fixedRatio: Double get() = if (totalExpense > 0) {
        (fixedExpense / totalExpense) * 100
    } else {
        0.0
    }

    /**
     * 可变开销占比
     */
    val variableRatio: Double get() = if (totalExpense > 0) {
        (variableExpense / totalExpense) * 100
    } else {
        0.0
    }

    /**
     * 预算执行率
     */
    val budgetUsageRate: Double get() = if (totalBudget > 0) {
        (totalExpense / totalBudget) * 100
    } else {
        0.0
    }
}

/**
 * 开销字段统计
 */
data class ExpenseFieldStats(
    val fieldId: Long,
    val fieldName: String,
    val fieldColor: String,
    val fieldIcon: String,
    val amount: Double,
    val budgetAmount: Double?,
    val percentage: Double,
    val isFixed: Boolean
) {
    /**
     * 预算执行率
     */
    val budgetUsageRate: Double get() = budgetAmount?.let {
        if (it > 0) (amount / it) * 100 else 0.0
    } ?: 0.0

    /**
     * 是否超预算
     */
    val isOverBudget: Boolean get() = budgetAmount?.let { amount > it } ?: false
}

/**
 * 开销趋势数据点
 */
data class ExpenseTrendPoint(
    val yearMonth: Int,
    val amount: Double
) {
    val year: Int get() = yearMonth / 100
    val month: Int get() = yearMonth % 100
    fun formatMonth(): String = "${month}月"
}

/**
 * 开销UI状态
 */
sealed class ExpenseUiState {
    data object Loading : ExpenseUiState()

    data class Success(
        val records: List<MonthlyExpenseWithField>,
        val stats: ExpenseStats,
        val fieldStats: List<ExpenseFieldStats>
    ) : ExpenseUiState()

    data class Error(val message: String) : ExpenseUiState()
}

/**
 * 添加/编辑开销记录状态
 */
data class EditExpenseState(
    val id: Long = 0,
    val yearMonth: Int = 0,
    val fieldId: Long = 0,
    val amount: Double = 0.0,
    val budgetAmount: Double? = null,
    val isFixed: Boolean = false,
    val note: String = "",
    val isEditing: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)
