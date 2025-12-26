package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.MonthlyExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * 月度开销DAO接口
 */
@Dao
interface MonthlyExpenseDao {

    /**
     * 获取指定月份的所有开销记录
     */
    @Query("""
        SELECT * FROM monthly_expenses
        WHERE yearMonth = :yearMonth
        ORDER BY expenseType ASC
    """)
    fun getByMonth(yearMonth: Int): Flow<List<MonthlyExpenseEntity>>

    /**
     * 获取指定月份的总开销
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM monthly_expenses
        WHERE yearMonth = :yearMonth
    """)
    suspend fun getTotalExpense(yearMonth: Int): Double

    /**
     * 获取指定月份的固定开销总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM monthly_expenses
        WHERE yearMonth = :yearMonth AND expenseType = 'FIXED'
    """)
    suspend fun getFixedExpenseTotal(yearMonth: Int): Double

    /**
     * 获取指定月份的可变开销总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM monthly_expenses
        WHERE yearMonth = :yearMonth AND expenseType = 'VARIABLE'
    """)
    suspend fun getVariableExpenseTotal(yearMonth: Int): Double

    /**
     * 获取指定月份各字段的开销汇总
     */
    @Query("""
        SELECT fieldId, SUM(amount) as total
        FROM monthly_expenses
        WHERE yearMonth = :yearMonth
        GROUP BY fieldId
    """)
    fun getFieldTotals(yearMonth: Int): Flow<List<FieldTotal>>

    /**
     * 根据ID获取记录
     */
    @Query("SELECT * FROM monthly_expenses WHERE id = :id")
    suspend fun getById(id: Long): MonthlyExpenseEntity?

    /**
     * 获取月份范围内的开销记录
     */
    @Query("""
        SELECT * FROM monthly_expenses
        WHERE yearMonth BETWEEN :startMonth AND :endMonth
        ORDER BY yearMonth DESC
    """)
    fun getByRange(startMonth: Int, endMonth: Int): Flow<List<MonthlyExpenseEntity>>

    /**
     * 获取各月份的总开销趋势
     */
    @Query("""
        SELECT yearMonth, SUM(amount) as total
        FROM monthly_expenses
        WHERE yearMonth BETWEEN :startMonth AND :endMonth
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
    """)
    fun getMonthlyTotalsByRange(startMonth: Int, endMonth: Int): Flow<List<MonthTotal>>

    /**
     * 插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MonthlyExpenseEntity): Long

    /**
     * 批量插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MonthlyExpenseEntity>)

    /**
     * 更新记录
     */
    @Update
    suspend fun update(record: MonthlyExpenseEntity)

    /**
     * 删除记录
     */
    @Query("DELETE FROM monthly_expenses WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取有数据的月份列表
     */
    @Query("SELECT DISTINCT yearMonth FROM monthly_expenses ORDER BY yearMonth DESC")
    fun getAvailableMonths(): Flow<List<Int>>

    /**
     * 获取预算执行情况
     */
    @Query("""
        SELECT fieldId, amount, budgetAmount
        FROM monthly_expenses
        WHERE yearMonth = :yearMonth AND budgetAmount IS NOT NULL
    """)
    fun getBudgetStatus(yearMonth: Int): Flow<List<BudgetStatus>>
}

/**
 * 预算执行状态数据类
 */
data class BudgetStatus(
    val fieldId: Long?,
    val amount: Double,
    val budgetAmount: Double?
)
