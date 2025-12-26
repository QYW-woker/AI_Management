package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.SavingsRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 存钱记录DAO接口
 */
@Dao
interface SavingsRecordDao {

    /**
     * 获取指定计划的所有记录
     */
    @Query("""
        SELECT * FROM savings_records
        WHERE planId = :planId
        ORDER BY date DESC, createdAt DESC
    """)
    fun getByPlan(planId: Long): Flow<List<SavingsRecordEntity>>

    /**
     * 获取指定计划指定日期范围的记录
     */
    @Query("""
        SELECT * FROM savings_records
        WHERE planId = :planId AND date BETWEEN :startDate AND :endDate
        ORDER BY date ASC
    """)
    fun getByPlanAndDateRange(planId: Long, startDate: Int, endDate: Int): Flow<List<SavingsRecordEntity>>

    /**
     * 获取指定计划的总存款金额
     */
    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM savings_records WHERE planId = :planId")
    suspend fun getTotalByPlan(planId: Long): Double

    /**
     * 根据ID获取记录
     */
    @Query("SELECT * FROM savings_records WHERE id = :id")
    suspend fun getById(id: Long): SavingsRecordEntity?

    /**
     * 插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: SavingsRecordEntity): Long

    /**
     * 更新记录
     */
    @Update
    suspend fun update(record: SavingsRecordEntity)

    /**
     * 删除记录
     */
    @Query("DELETE FROM savings_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 删除计划的所有记录
     */
    @Query("DELETE FROM savings_records WHERE planId = :planId")
    suspend fun deleteByPlan(planId: Long)

    /**
     * 获取最近的存款记录
     */
    @Query("""
        SELECT * FROM savings_records
        ORDER BY date DESC, createdAt DESC
        LIMIT :limit
    """)
    fun getRecentRecords(limit: Int = 20): Flow<List<SavingsRecordEntity>>

    /**
     * 统计指定计划的记录数
     */
    @Query("SELECT COUNT(*) FROM savings_records WHERE planId = :planId")
    suspend fun countByPlan(planId: Long): Int
}
