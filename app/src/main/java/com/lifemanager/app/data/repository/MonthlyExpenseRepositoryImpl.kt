package com.lifemanager.app.data.repository

import com.lifemanager.app.core.database.dao.BudgetStatus
import com.lifemanager.app.core.database.dao.FieldTotal
import com.lifemanager.app.core.database.dao.MonthTotal
import com.lifemanager.app.core.database.dao.MonthlyExpenseDao
import com.lifemanager.app.core.database.entity.MonthlyExpenseEntity
import com.lifemanager.app.domain.repository.MonthlyExpenseRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 月度开销仓库实现类
 */
@Singleton
class MonthlyExpenseRepositoryImpl @Inject constructor(
    private val dao: MonthlyExpenseDao
) : MonthlyExpenseRepository {

    override fun getByMonth(yearMonth: Int): Flow<List<MonthlyExpenseEntity>> {
        return dao.getByMonth(yearMonth)
    }

    override suspend fun getById(id: Long): MonthlyExpenseEntity? {
        return dao.getById(id)
    }

    override suspend fun getTotalExpense(yearMonth: Int): Double {
        return dao.getTotalExpense(yearMonth)
    }

    override suspend fun getFixedExpenseTotal(yearMonth: Int): Double {
        return dao.getFixedExpenseTotal(yearMonth)
    }

    override suspend fun getVariableExpenseTotal(yearMonth: Int): Double {
        return dao.getVariableExpenseTotal(yearMonth)
    }

    override fun getFieldTotals(yearMonth: Int): Flow<List<FieldTotal>> {
        return dao.getFieldTotals(yearMonth)
    }

    override fun getMonthlyTrend(startMonth: Int, endMonth: Int): Flow<List<MonthTotal>> {
        return dao.getMonthlyTotalsByRange(startMonth, endMonth)
    }

    override fun getBudgetStatus(yearMonth: Int): Flow<List<BudgetStatus>> {
        return dao.getBudgetStatus(yearMonth)
    }

    override fun getAvailableMonths(): Flow<List<Int>> {
        return dao.getAvailableMonths()
    }

    override suspend fun insert(record: MonthlyExpenseEntity): Long {
        return dao.insert(record)
    }

    override suspend fun update(record: MonthlyExpenseEntity) {
        dao.update(record)
    }

    override suspend fun delete(id: Long) {
        dao.deleteById(id)
    }
}
