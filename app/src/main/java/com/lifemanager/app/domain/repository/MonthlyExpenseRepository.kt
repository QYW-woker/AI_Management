package com.lifemanager.app.domain.repository

import com.lifemanager.app.core.database.dao.BudgetStatus
import com.lifemanager.app.core.database.dao.FieldTotal
import com.lifemanager.app.core.database.dao.MonthTotal
import com.lifemanager.app.core.database.entity.MonthlyExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * 月度开销仓库接口
 */
interface MonthlyExpenseRepository {

    /**
     * 获取指定月份的所有记录
     */
    fun getByMonth(yearMonth: Int): Flow<List<MonthlyExpenseEntity>>

    /**
     * 根据ID获取记录
     */
    suspend fun getById(id: Long): MonthlyExpenseEntity?

    /**
     * 获取指定月份的总开销
     */
    suspend fun getTotalExpense(yearMonth: Int): Double

    /**
     * 获取固定开销总额
     */
    suspend fun getFixedExpenseTotal(yearMonth: Int): Double

    /**
     * 获取可变开销总额
     */
    suspend fun getVariableExpenseTotal(yearMonth: Int): Double

    /**
     * 获取各字段的开销汇总
     */
    fun getFieldTotals(yearMonth: Int): Flow<List<FieldTotal>>

    /**
     * 获取月度开销趋势
     */
    fun getMonthlyTrend(startMonth: Int, endMonth: Int): Flow<List<MonthTotal>>

    /**
     * 获取预算执行情况
     */
    fun getBudgetStatus(yearMonth: Int): Flow<List<BudgetStatus>>

    /**
     * 获取有数据的月份列表
     */
    fun getAvailableMonths(): Flow<List<Int>>

    /**
     * 插入记录
     */
    suspend fun insert(record: MonthlyExpenseEntity): Long

    /**
     * 更新记录
     */
    suspend fun update(record: MonthlyExpenseEntity)

    /**
     * 删除记录
     */
    suspend fun delete(id: Long)
}
