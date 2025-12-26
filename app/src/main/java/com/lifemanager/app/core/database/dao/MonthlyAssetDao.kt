package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.MonthlyAssetEntity
import kotlinx.coroutines.flow.Flow

/**
 * 月度资产DAO接口
 */
@Dao
interface MonthlyAssetDao {

    /**
     * 获取指定月份的所有资产/负债记录
     */
    @Query("""
        SELECT * FROM monthly_assets
        WHERE yearMonth = :yearMonth
        ORDER BY type ASC
    """)
    fun getByMonth(yearMonth: Int): Flow<List<MonthlyAssetEntity>>

    /**
     * 获取指定月份的资产总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM monthly_assets
        WHERE yearMonth = :yearMonth AND type = 'ASSET'
    """)
    suspend fun getTotalAssets(yearMonth: Int): Double

    /**
     * 获取指定月份的负债总额
     */
    @Query("""
        SELECT COALESCE(SUM(amount), 0.0) FROM monthly_assets
        WHERE yearMonth = :yearMonth AND type = 'LIABILITY'
    """)
    suspend fun getTotalLiabilities(yearMonth: Int): Double

    /**
     * 获取指定月份各字段的资产汇总
     */
    @Query("""
        SELECT fieldId, SUM(amount) as total
        FROM monthly_assets
        WHERE yearMonth = :yearMonth AND type = :type
        GROUP BY fieldId
    """)
    fun getFieldTotals(yearMonth: Int, type: String): Flow<List<FieldTotal>>

    /**
     * 根据ID获取记录
     */
    @Query("SELECT * FROM monthly_assets WHERE id = :id")
    suspend fun getById(id: Long): MonthlyAssetEntity?

    /**
     * 获取月份范围内的记录（用于趋势分析）
     */
    @Query("""
        SELECT * FROM monthly_assets
        WHERE yearMonth BETWEEN :startMonth AND :endMonth
        ORDER BY yearMonth ASC
    """)
    fun getByRange(startMonth: Int, endMonth: Int): Flow<List<MonthlyAssetEntity>>

    /**
     * 获取各月份的净资产（资产-负债）
     */
    @Query("""
        SELECT yearMonth,
               SUM(CASE WHEN type = 'ASSET' THEN amount ELSE 0 END) -
               SUM(CASE WHEN type = 'LIABILITY' THEN amount ELSE 0 END) as total
        FROM monthly_assets
        WHERE yearMonth BETWEEN :startMonth AND :endMonth
        GROUP BY yearMonth
        ORDER BY yearMonth ASC
    """)
    fun getNetWorthByRange(startMonth: Int, endMonth: Int): Flow<List<MonthTotal>>

    /**
     * 插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: MonthlyAssetEntity): Long

    /**
     * 批量插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<MonthlyAssetEntity>)

    /**
     * 更新记录
     */
    @Update
    suspend fun update(record: MonthlyAssetEntity)

    /**
     * 删除记录
     */
    @Query("DELETE FROM monthly_assets WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取有数据的月份列表
     */
    @Query("SELECT DISTINCT yearMonth FROM monthly_assets ORDER BY yearMonth DESC")
    fun getAvailableMonths(): Flow<List<Int>>

    /**
     * 复制上月数据到当月（用于快速填充）
     */
    @Query("""
        INSERT INTO monthly_assets (yearMonth, type, fieldId, amount, note, createdAt, updatedAt)
        SELECT :targetMonth, type, fieldId, amount, note, :now, :now
        FROM monthly_assets
        WHERE yearMonth = :sourceMonth
    """)
    suspend fun copyFromMonth(sourceMonth: Int, targetMonth: Int, now: Long = System.currentTimeMillis())
}
