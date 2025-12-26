package com.lifemanager.app.domain.usecase

import com.lifemanager.app.core.database.entity.CustomFieldEntity
import com.lifemanager.app.core.database.entity.ExpenseType
import com.lifemanager.app.core.database.entity.ModuleType
import com.lifemanager.app.core.database.entity.MonthlyExpenseEntity
import com.lifemanager.app.domain.model.ExpenseFieldStats
import com.lifemanager.app.domain.model.ExpenseStats
import com.lifemanager.app.domain.model.ExpenseTrendPoint
import com.lifemanager.app.domain.model.MonthlyExpenseWithField
import com.lifemanager.app.domain.repository.CustomFieldRepository
import com.lifemanager.app.domain.repository.MonthlyExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 月度开销用例类
 */
@Singleton
class MonthlyExpenseUseCase @Inject constructor(
    private val repository: MonthlyExpenseRepository,
    private val fieldRepository: CustomFieldRepository
) {

    /**
     * 获取指定月份的记录（带字段详情）
     */
    fun getRecordsWithFields(yearMonth: Int): Flow<List<MonthlyExpenseWithField>> {
        return repository.getByMonth(yearMonth).map { records ->
            val fieldIds = records.mapNotNull { it.fieldId }.distinct()
            val fields = if (fieldIds.isNotEmpty()) {
                fieldRepository.getFieldsByIds(fieldIds).associateBy { it.id }
            } else {
                emptyMap()
            }

            records.map { record ->
                MonthlyExpenseWithField(
                    record = record,
                    field = record.fieldId?.let { fields[it] }
                )
            }
        }
    }

    /**
     * 获取月度统计数据
     */
    suspend fun getExpenseStats(yearMonth: Int): ExpenseStats {
        val totalExpense = repository.getTotalExpense(yearMonth)
        val fixedExpense = repository.getFixedExpenseTotal(yearMonth)
        val variableExpense = repository.getVariableExpenseTotal(yearMonth)

        return ExpenseStats(
            yearMonth = yearMonth,
            totalExpense = totalExpense,
            fixedExpense = fixedExpense,
            variableExpense = variableExpense
        )
    }

    /**
     * 获取字段统计（带预算信息）
     */
    fun getFieldStats(yearMonth: Int): Flow<List<ExpenseFieldStats>> {
        return combine(
            repository.getFieldTotals(yearMonth),
            repository.getBudgetStatus(yearMonth),
            fieldRepository.getFieldsByModule(ModuleType.MONTHLY_EXPENSE),
            repository.getByMonth(yearMonth)
        ) { totals, budgets, fields, records ->
            val fieldsMap = fields.associateBy { it.id }
            val budgetMap = budgets.associateBy { it.fieldId }
            val recordsMap = records.groupBy { it.fieldId }
            val total = totals.sumOf { it.total }

            totals.mapNotNull { fieldTotal ->
                fieldTotal.fieldId?.let { fieldId ->
                    fieldsMap[fieldId]?.let { field ->
                        val budgetStatus = budgetMap[fieldId]
                        val fieldRecords = recordsMap[fieldId] ?: emptyList()
                        val isFixed = fieldRecords.any { it.expenseType == ExpenseType.FIXED }

                        ExpenseFieldStats(
                            fieldId = field.id,
                            fieldName = field.name,
                            fieldColor = field.color,
                            fieldIcon = field.iconName,
                            amount = fieldTotal.total,
                            budgetAmount = budgetStatus?.budgetAmount,
                            percentage = if (total > 0) (fieldTotal.total / total) * 100 else 0.0,
                            isFixed = isFixed
                        )
                    }
                }
            }.sortedByDescending { it.amount }
        }
    }

    /**
     * 获取开销趋势（过去12个月）
     */
    fun getExpenseTrend(currentYearMonth: Int): Flow<List<ExpenseTrendPoint>> {
        val year = currentYearMonth / 100
        val month = currentYearMonth % 100

        val startYear = if (month <= 12) year - 1 else year
        val startMonth = if (month <= 12) month else month - 12
        val startYearMonth = startYear * 100 + startMonth

        return repository.getMonthlyTrend(startYearMonth, currentYearMonth).map { monthTotals ->
            monthTotals.map { monthTotal ->
                ExpenseTrendPoint(
                    yearMonth = monthTotal.yearMonth,
                    amount = monthTotal.total
                )
            }
        }
    }

    /**
     * 获取可用的月份列表
     */
    fun getAvailableMonths(): Flow<List<Int>> {
        return repository.getAvailableMonths()
    }

    /**
     * 根据ID获取记录
     */
    suspend fun getRecordById(id: Long): MonthlyExpenseEntity? {
        return repository.getById(id)
    }

    /**
     * 添加记录
     */
    suspend fun addRecord(
        yearMonth: Int,
        fieldId: Long,
        amount: Double,
        budgetAmount: Double?,
        isFixed: Boolean,
        note: String
    ): Long {
        val record = MonthlyExpenseEntity(
            yearMonth = yearMonth,
            fieldId = fieldId,
            amount = amount,
            budgetAmount = budgetAmount,
            expenseType = if (isFixed) ExpenseType.FIXED else ExpenseType.VARIABLE,
            note = note
        )
        return repository.insert(record)
    }

    /**
     * 更新记录
     */
    suspend fun updateRecord(
        id: Long,
        yearMonth: Int,
        fieldId: Long,
        amount: Double,
        budgetAmount: Double?,
        isFixed: Boolean,
        note: String
    ) {
        val existingRecord = repository.getById(id) ?: return

        val updatedRecord = existingRecord.copy(
            yearMonth = yearMonth,
            fieldId = fieldId,
            amount = amount,
            budgetAmount = budgetAmount,
            expenseType = if (isFixed) ExpenseType.FIXED else ExpenseType.VARIABLE,
            note = note,
            updatedAt = System.currentTimeMillis()
        )
        repository.update(updatedRecord)
    }

    /**
     * 删除记录
     */
    suspend fun deleteRecord(id: Long) {
        repository.delete(id)
    }

    /**
     * 获取开销类别字段列表
     */
    fun getExpenseFields(): Flow<List<CustomFieldEntity>> {
        return fieldRepository.getFieldsByModule(ModuleType.MONTHLY_EXPENSE)
    }
}
