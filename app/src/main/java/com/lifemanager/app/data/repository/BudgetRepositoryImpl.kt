package com.lifemanager.app.data.repository

import com.lifemanager.app.core.database.dao.BudgetDao
import com.lifemanager.app.core.database.entity.BudgetEntity
import com.lifemanager.app.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 预算仓库实现类
 */
@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override suspend fun getByYearMonth(yearMonth: Int): BudgetEntity? {
        return dao.getByYearMonth(yearMonth)
    }

    override fun getByYearMonthFlow(yearMonth: Int): Flow<BudgetEntity?> {
        return dao.getByYearMonthFlow(yearMonth)
    }

    override fun getAllBudgets(): Flow<List<BudgetEntity>> {
        return dao.getAllBudgets()
    }

    override fun getRecentBudgets(limit: Int): Flow<List<BudgetEntity>> {
        return dao.getRecentBudgets(limit)
    }

    override fun getBudgetsByRange(startYearMonth: Int, endYearMonth: Int): Flow<List<BudgetEntity>> {
        return dao.getBudgetsByRange(startYearMonth, endYearMonth)
    }

    override suspend fun insertOrUpdate(budget: BudgetEntity): Long {
        return dao.insertOrUpdate(budget)
    }

    override suspend fun update(budget: BudgetEntity) {
        dao.update(budget)
    }

    override suspend fun delete(budget: BudgetEntity) {
        dao.delete(budget)
    }

    override suspend fun deleteById(id: Long) {
        dao.deleteById(id)
    }

    override suspend fun hasBudget(yearMonth: Int): Boolean {
        return dao.hasBudget(yearMonth)
    }

    override suspend fun getLatestBudget(): BudgetEntity? {
        return dao.getLatestBudget()
    }
}
