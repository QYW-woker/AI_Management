package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.TimeRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 时间记录DAO接口
 */
@Dao
interface TimeRecordDao {

    /**
     * 获取指定日期范围的时间记录
     */
    @Query("""
        SELECT * FROM time_records
        WHERE date BETWEEN :startDate AND :endDate
        ORDER BY date DESC, startTime DESC
    """)
    fun getByDateRange(startDate: Int, endDate: Int): Flow<List<TimeRecordEntity>>

    /**
     * 获取指定日期的时间记录
     */
    @Query("""
        SELECT * FROM time_records
        WHERE date = :date
        ORDER BY startTime ASC
    """)
    fun getByDate(date: Int): Flow<List<TimeRecordEntity>>

    /**
     * 获取正在进行的计时
     */
    @Query("SELECT * FROM time_records WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    fun getActiveRecord(): Flow<TimeRecordEntity?>

    /**
     * 检查是否有正在进行的计时
     */
    @Query("SELECT COUNT(*) FROM time_records WHERE endTime IS NULL")
    suspend fun hasActiveRecord(): Int

    /**
     * 获取指定日期范围内各分类的时长汇总
     */
    @Query("""
        SELECT categoryId as fieldId, SUM(durationMinutes) as total
        FROM time_records
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY categoryId
    """)
    fun getCategoryDurations(startDate: Int, endDate: Int): Flow<List<FieldTotal>>

    /**
     * 获取指定日期的总时长（分钟）
     */
    @Query("""
        SELECT COALESCE(SUM(durationMinutes), 0) FROM time_records
        WHERE date = :date
    """)
    suspend fun getTotalDuration(date: Int): Int

    /**
     * 获取指定目标关联的时间记录
     */
    @Query("""
        SELECT * FROM time_records
        WHERE linkedGoalId = :goalId
        ORDER BY date DESC
    """)
    fun getByGoal(goalId: Long): Flow<List<TimeRecordEntity>>

    /**
     * 根据ID获取记录
     */
    @Query("SELECT * FROM time_records WHERE id = :id")
    suspend fun getById(id: Long): TimeRecordEntity?

    /**
     * 插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: TimeRecordEntity): Long

    /**
     * 更新记录
     */
    @Update
    suspend fun update(record: TimeRecordEntity)

    /**
     * 结束计时
     */
    @Query("""
        UPDATE time_records
        SET endTime = :endTime, durationMinutes = :durationMinutes
        WHERE id = :id
    """)
    suspend fun endRecord(id: Long, endTime: Long, durationMinutes: Int)

    /**
     * 删除记录
     */
    @Query("DELETE FROM time_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    /**
     * 获取每日时长趋势
     */
    @Query("""
        SELECT date, SUM(durationMinutes) as total
        FROM time_records
        WHERE date BETWEEN :startDate AND :endDate
        GROUP BY date
        ORDER BY date ASC
    """)
    fun getDailyDurations(startDate: Int, endDate: Int): Flow<List<DateTotal>>
}
