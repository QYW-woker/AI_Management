package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.MonthlyIncomeExpenseEntity
import kotlinx.coroutines.flow.Flow

/**
 * 字段统计数据类
 */
data class FieldTotal(
    val fieldId: Long?,
    val total: Double
)

/**
 * 月度收支DAO接口
 *
 * 提供对monthly_income_expense表的数据库操作
 */
@Dao
interface MonthlyIncomeExpenseDao {

    /**
     * 获取指定月份的所有记录
     */
    @Query("""
        SELECT * FROM monthly_income_expense
        WHERE yearMonth = :yearMonth
        ORDER BY type ASC, recordDate DESC
    """)
    fun getByMonth(yearMonth: Int): Flow<List<MonthlyIncomeExpenseEntity>>

    /**
     * 获取指定月份范围的记录
     */
    @Query("""
        SELECT * FROM monthly_income_expense
        WHERE yearMonth BETWEEN :startMonth AND :endMonth
        ORDER BY yearMonth DESC, type ASC
    """)
    fun getByRange(startMonth: Int, endMonth: Int): Flow<List<MonthlyIncomeExpenseEntity>>

    /**
     * 获取指定月份指定类型的总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM monthly_income_expense
        WHERE yearMonth = :yearMonth AND type = :type
    """)
    suspend fun getTotalByType(yearMonth: Int, type: String): Double

    /**
     * 获取指定月份各字段的汇总
     */
    @Query("""
        SELECT fieldId, SUM(amount) as total
        FROM monthly_income_expense
        WHERE yearMonth = :yearMonth AND type = :type
        GROUP BY fieldId
    """)
    fun getFieldTotals(yearMonth: Int, type: String): Flow<List<FieldTotal>>

    /**
     * 获取多个月份各字段的汇总
     */
    @Query("""
        SELECT fieldId, SUM(amount) as total
        FROM monthly_income_expense
        WHERE yearMonth BETWEEN :startMonth AND :endMonth AND type = :type
        GROUP BY fieldId
    """)
    fun getFieldTotalsInRange(startMonth: Int, endMonth: Int, type: String): Flow<List<FieldTotal>>

    /**
     * 根据ID获取记录
     */
    @Query("SELECT * FROM monthly_income_expense WHERE id = :id")
    suspend fun getById(id: Long): MonthlyIncomeExpenseEntity?

    /**
     * 获取最近的记录
     */
    @Query("""
        SELECT * FROM monthly_income_expense
        ORDER BY yearMonth DESC, recordDate DESC
        LIMIT :limit
    """)
    fun getRecentRecords(limit: Int = 20): Flow<List<MonthlyIncomeExpenseEntity>>

    /**
     * 插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MonthlyIncomeExpenseEntity): Long

    /**
     * 批量插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MonthlyIncomeExpenseEntity>)

    /**
     * 更新记录
     */
    @Update
    suspend fun update(record: MonthlyIncomeExpenseEntity)

    /**
     * 删除记录
     */
    @Delete
    suspend fun delete(record: MonthlyIncomeExpenseEntity)

    /**
     * 根据ID删除记录
     */
    @Query("DELETE FROM monthly_income_expense WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取有数据的月份列表
     */
    @Query("""
        SELECT DISTINCT yearMonth FROM monthly_income_expense
        ORDER BY yearMonth DESC
    """)
    fun getAvailableMonths(): Flow<List<Int>>

    /**
     * 统计指定月份的记录数
     */
    @Query("SELECT COUNT(*) FROM monthly_income_expense WHERE yearMonth = :yearMonth")
    suspend fun countByMonth(yearMonth: Int): Int

    /**
     * 获取指定年份各月份的总收入
     */
    @Query("""
        SELECT yearMonth, SUM(amount) as total
        FROM monthly_income_expense
        WHERE yearMonth / 100 = :year AND type = 'INCOME'
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
    """)
    fun getMonthlyIncomeTotalsByYear(year: Int): Flow<List<MonthTotal>>

    /**
     * 获取指定年份各月份的总支出
     */
    @Query("""
        SELECT yearMonth, SUM(amount) as total
        FROM monthly_income_expense
        WHERE yearMonth / 100 = :year AND type = 'EXPENSE'
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
    """)
    fun getMonthlyExpenseTotalsByYear(year: Int): Flow<List<MonthTotal>>
}

/**
 * 月度总额数据类
 */
data class MonthTotal(
    val yearMonth: Int,
    val total: Double
)
