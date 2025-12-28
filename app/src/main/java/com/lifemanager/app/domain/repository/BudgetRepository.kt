package com.lifemanager.app.domain.repository

import com.lifemanager.app.core.database.entity.BudgetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 预算仓库接口
 */
interface BudgetRepository {

    /**
     * 获取指定月份的预算
     */
    suspend fun getByYearMonth(yearMonth: Int): BudgetEntity?

    /**
     * 获取指定月份的预算（Flow版本）
     */
    fun getByYearMonthFlow(yearMonth: Int): Flow<BudgetEntity?>

    /**
     * 获取所有预算记录
     */
    fun getAllBudgets(): Flow<List<BudgetEntity>>

    /**
     * 获取最近N个月的预算
     */
    fun getRecentBudgets(limit: Int): Flow<List<BudgetEntity>>

    /**
     * 获取指定日期范围的预算
     */
    fun getBudgetsByRange(startYearMonth: Int, endYearMonth: Int): Flow<List<BudgetEntity>>

    /**
     * 插入或更新预算
     */
    suspend fun insertOrUpdate(budget: BudgetEntity): Long

    /**
     * 更新预算
     */
    suspend fun update(budget: BudgetEntity)

    /**
     * 删除预算
     */
    suspend fun delete(budget: BudgetEntity)

    /**
     * 根据ID删除
     */
    suspend fun deleteById(id: Long)

    /**
     * 检查指定月份是否有预算
     */
    suspend fun hasBudget(yearMonth: Int): Boolean

    /**
     * 获取最新的预算记录
     */
    suspend fun getLatestBudget(): BudgetEntity?
}
