package com.lifemanager.app.domain.usecase

import com.lifemanager.app.core.database.entity.BudgetEntity
import com.lifemanager.app.domain.model.BudgetStatus
import com.lifemanager.app.domain.model.BudgetWithSpending
import com.lifemanager.app.domain.model.CategoryBudgetStatus
import com.lifemanager.app.domain.model.MonthlyBudgetAnalysis
import com.lifemanager.app.domain.repository.BudgetRepository
import com.lifemanager.app.domain.repository.DailyTransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 预算用例
 */
class BudgetUseCase @Inject constructor(
    private val budgetRepository: BudgetRepository,
    private val transactionRepository: DailyTransactionRepository
) {

    /**
     * 获取当前月份的预算及花销情况
     */
    fun getCurrentMonthBudgetWithSpending(): Flow<BudgetWithSpending?> {
        val currentYearMonth = YearMonth.now().let { it.year * 100 + it.monthValue }
        return getBudgetWithSpending(currentYearMonth)
    }

    /**
     * 获取指定月份的预算及花销情况
     */
    fun getBudgetWithSpending(yearMonth: Int): Flow<BudgetWithSpending?> {
        val year = yearMonth / 100
        val month = yearMonth % 100
        val ym = YearMonth.of(year, month)
        val startDate = ym.atDay(1).toEpochDay().toInt()
        val endDate = ym.atEndOfMonth().toEpochDay().toInt()

        return budgetRepository.getByYearMonthFlow(yearMonth).map { budget ->
            if (budget == null) return@map null

            // 获取本月支出总额
            val totalSpent = transactionRepository.getTotalByTypeInRange(startDate, endDate, "EXPENSE")

            // 解析分类预算
            val categoryBudgets = parseCategoryBudgets(budget.categoryBudgets)

            // 计算状态
            val usagePercentage = if (budget.totalBudget > 0) {
                (totalSpent / budget.totalBudget * 100).toInt()
            } else 0

            val status = when {
                usagePercentage >= 100 -> BudgetStatus.EXCEEDED
                usagePercentage >= budget.alertThreshold -> BudgetStatus.WARNING
                else -> BudgetStatus.NORMAL
            }

            BudgetWithSpending(
                budget = budget,
                totalSpent = totalSpent,
                remaining = budget.totalBudget - totalSpent,
                usagePercentage = usagePercentage,
                status = status,
                categoryBudgets = categoryBudgets,
                daysRemaining = calculateDaysRemaining(yearMonth)
            )
        }
    }

    /**
     * 获取分类支出详情
     */
    suspend fun getCategorySpending(yearMonth: Int): List<CategoryBudgetStatus> {
        val year = yearMonth / 100
        val month = yearMonth % 100
        val ym = YearMonth.of(year, month)
        val startDate = ym.atDay(1).toEpochDay().toInt()
        val endDate = ym.atEndOfMonth().toEpochDay().toInt()

        val budget = budgetRepository.getByYearMonth(yearMonth) ?: return emptyList()
        val categoryBudgets = parseCategoryBudgets(budget.categoryBudgets)

        // 这里简化处理，实际应该按分类汇总
        return categoryBudgets.map { (category, budgetAmount) ->
            // TODO: 实际应该从交易记录中按分类汇总
            CategoryBudgetStatus(
                categoryName = category,
                budgetAmount = budgetAmount,
                spentAmount = 0.0, // 需要按分类汇总
                remaining = budgetAmount,
                usagePercentage = 0,
                status = BudgetStatus.NORMAL
            )
        }
    }

    /**
     * 设置月度预算
     */
    suspend fun setBudget(
        yearMonth: Int,
        totalBudget: Double,
        categoryBudgets: Map<String, Double> = emptyMap(),
        alertThreshold: Int = 80,
        alertEnabled: Boolean = true,
        note: String = ""
    ): Long {
        val existing = budgetRepository.getByYearMonth(yearMonth)

        val budget = if (existing != null) {
            existing.copy(
                totalBudget = totalBudget,
                categoryBudgets = convertCategoryBudgetsToJson(categoryBudgets),
                alertThreshold = alertThreshold,
                alertEnabled = alertEnabled,
                note = note,
                updatedAt = System.currentTimeMillis()
            )
        } else {
            BudgetEntity(
                yearMonth = yearMonth,
                totalBudget = totalBudget,
                categoryBudgets = convertCategoryBudgetsToJson(categoryBudgets),
                alertThreshold = alertThreshold,
                alertEnabled = alertEnabled,
                note = note
            )
        }

        return budgetRepository.insertOrUpdate(budget)
    }

    /**
     * 从上月复制预算到本月
     */
    suspend fun copyBudgetFromPreviousMonth(targetYearMonth: Int): Boolean {
        val latest = budgetRepository.getLatestBudget() ?: return false

        val newBudget = latest.copy(
            id = 0,
            yearMonth = targetYearMonth,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )

        budgetRepository.insertOrUpdate(newBudget)
        return true
    }

    /**
     * 获取预算历史记录
     */
    fun getBudgetHistory(limit: Int = 12): Flow<List<BudgetEntity>> {
        return budgetRepository.getRecentBudgets(limit)
    }

    /**
     * 获取多月预算分析（用于数据中心）
     */
    suspend fun getMonthlyBudgetAnalysis(months: Int = 6): List<MonthlyBudgetAnalysis> {
        val currentYearMonth = YearMonth.now()
        val result = mutableListOf<MonthlyBudgetAnalysis>()

        for (i in 0 until months) {
            val ym = currentYearMonth.minusMonths(i.toLong())
            val yearMonth = ym.year * 100 + ym.monthValue
            val startDate = ym.atDay(1).toEpochDay().toInt()
            val endDate = ym.atEndOfMonth().toEpochDay().toInt()

            val budget = budgetRepository.getByYearMonth(yearMonth)
            val totalSpent = transactionRepository.getTotalByTypeInRange(startDate, endDate, "EXPENSE")

            result.add(
                MonthlyBudgetAnalysis(
                    yearMonth = yearMonth,
                    budgetAmount = budget?.totalBudget ?: 0.0,
                    spentAmount = totalSpent,
                    hasBudget = budget != null
                )
            )
        }

        return result.reversed() // 按时间顺序排列
    }

    /**
     * 生成AI预算建议
     */
    suspend fun generateAIBudgetAdvice(yearMonth: Int): String {
        val year = yearMonth / 100
        val month = yearMonth % 100
        val ym = YearMonth.of(year, month)
        val startDate = ym.atDay(1).toEpochDay().toInt()
        val endDate = ym.atEndOfMonth().toEpochDay().toInt()

        val budget = budgetRepository.getByYearMonth(yearMonth)
        val totalSpent = transactionRepository.getTotalByTypeInRange(startDate, endDate, "EXPENSE")

        if (budget == null) {
            return buildString {
                appendLine("📊 本月尚未设置预算")
                appendLine()
                appendLine("建议：")
                appendLine("1. 根据您的收入水平设置合理的月度预算")
                appendLine("2. 建议将预算控制在月收入的60-70%")
                appendLine("3. 为紧急情况留有一定余地")
            }
        }

        val usagePercentage = if (budget.totalBudget > 0) {
            (totalSpent / budget.totalBudget * 100).toInt()
        } else 0

        val remaining = budget.totalBudget - totalSpent
        val daysRemaining = calculateDaysRemaining(yearMonth)
        val dailyBudget = if (daysRemaining > 0) remaining / daysRemaining else 0.0

        return buildString {
            appendLine("📊 ${month}月预算执行分析")
            appendLine()
            appendLine("预算总额：¥${String.format("%.2f", budget.totalBudget)}")
            appendLine("已支出：¥${String.format("%.2f", totalSpent)} (${usagePercentage}%)")
            appendLine("剩余预算：¥${String.format("%.2f", remaining)}")
            appendLine()

            when {
                usagePercentage >= 100 -> {
                    appendLine("⚠️ 本月预算已超支！")
                    appendLine()
                    appendLine("建议：")
                    appendLine("1. 暂停非必要消费")
                    appendLine("2. 检查是否有可削减的开支")
                    appendLine("3. 考虑是否需要调整下月预算")
                }
                usagePercentage >= 80 -> {
                    appendLine("⚡ 预算使用接近警戒线")
                    appendLine()
                    appendLine("剩余${daysRemaining}天，日均可用：¥${String.format("%.2f", dailyBudget)}")
                    appendLine()
                    appendLine("建议：")
                    appendLine("1. 控制剩余天数的支出")
                    appendLine("2. 优先保障必需品消费")
                    appendLine("3. 延迟非紧急购物计划")
                }
                usagePercentage >= 50 -> {
                    appendLine("✅ 预算执行良好")
                    appendLine()
                    appendLine("剩余${daysRemaining}天，日均可用：¥${String.format("%.2f", dailyBudget)}")
                    appendLine()
                    appendLine("建议：")
                    appendLine("1. 保持当前消费节奏")
                    appendLine("2. 考虑将节省的资金用于储蓄")
                }
                else -> {
                    appendLine("💰 预算富余较多")
                    appendLine()
                    appendLine("剩余${daysRemaining}天，日均可用：¥${String.format("%.2f", dailyBudget)}")
                    appendLine()
                    appendLine("建议：")
                    appendLine("1. 可适当增加生活品质支出")
                    appendLine("2. 考虑增加储蓄或投资")
                    appendLine("3. 检查预算是否设置过高")
                }
            }
        }
    }

    /**
     * 计算剩余天数
     */
    private fun calculateDaysRemaining(yearMonth: Int): Int {
        val year = yearMonth / 100
        val month = yearMonth % 100
        val ym = YearMonth.of(year, month)
        val today = LocalDate.now()

        return if (today.year == year && today.monthValue == month) {
            ym.lengthOfMonth() - today.dayOfMonth + 1
        } else if (today.isBefore(ym.atDay(1))) {
            ym.lengthOfMonth()
        } else {
            0
        }
    }

    /**
     * 解析分类预算JSON
     */
    private fun parseCategoryBudgets(json: String): Map<String, Double> {
        if (json.isBlank() || json == "{}") return emptyMap()
        return try {
            val content = json.removeSurrounding("{", "}")
            if (content.isBlank()) return emptyMap()

            content.split(",").associate { pair ->
                val (key, value) = pair.split(":").map { it.trim().removeSurrounding("\"") }
                key to value.toDouble()
            }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    /**
     * 转换分类预算为JSON
     */
    private fun convertCategoryBudgetsToJson(budgets: Map<String, Double>): String {
        if (budgets.isEmpty()) return "{}"
        return budgets.entries.joinToString(",", "{", "}") { (key, value) ->
            "\"$key\":$value"
        }
    }

    /**
     * 格式化年月
     */
    fun formatYearMonth(yearMonth: Int): String {
        val year = yearMonth / 100
        val month = yearMonth % 100
        return "${year}年${month}月"
    }
}
