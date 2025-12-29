package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.GoalRecordEntity
import kotlinx.coroutines.flow.Flow

/**
 * 目标记录DAO接口
 *
 * 提供对goal_records表的数据库操作
 */
@Dao
interface GoalRecordDao {

    /**
     * 获取目标的所有记录（按时间倒序）
     */
    @Query("""
        SELECT * FROM goal_records
        WHERE goalId = :goalId
        ORDER BY createdAt DESC
    """)
    fun getRecordsByGoalId(goalId: Long): Flow<List<GoalRecordEntity>>

    /**
     * 获取目标的所有记录（同步版本）
     */
    @Query("""
        SELECT * FROM goal_records
        WHERE goalId = :goalId
        ORDER BY createdAt DESC
    """)
    suspend fun getRecordsByGoalIdSync(goalId: Long): List<GoalRecordEntity>

    /**
     * 获取目标记录按时间正序（用于时间轴展示）
     */
    @Query("""
        SELECT * FROM goal_records
        WHERE goalId = :goalId
        ORDER BY createdAt ASC
    """)
    fun getRecordsTimeline(goalId: Long): Flow<List<GoalRecordEntity>>

    /**
     * 获取最新的进度记录
     */
    @Query("""
        SELECT * FROM goal_records
        WHERE goalId = :goalId AND recordType = 'PROGRESS'
        ORDER BY createdAt DESC
        LIMIT 1
    """)
    suspend fun getLatestProgressRecord(goalId: Long): GoalRecordEntity?

    /**
     * 插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: GoalRecordEntity): Long

    /**
     * 批量插入记录
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(records: List<GoalRecordEntity>)

    /**
     * 更新记录
     */
    @Update
    suspend fun update(record: GoalRecordEntity)

    /**
     * 删除记录
     */
    @Query("DELETE FROM goal_records WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * 删除目标的所有记录
     */
    @Query("DELETE FROM goal_records WHERE goalId = :goalId")
    suspend fun deleteByGoalId(goalId: Long)

    /**
     * 统计目标记录数量
     */
    @Query("SELECT COUNT(*) FROM goal_records WHERE goalId = :goalId")
    suspend fun countRecords(goalId: Long): Int

    /**
     * 获取里程碑记录
     */
    @Query("""
        SELECT * FROM goal_records
        WHERE goalId = :goalId AND recordType = 'MILESTONE'
        ORDER BY createdAt DESC
    """)
    fun getMilestones(goalId: Long): Flow<List<GoalRecordEntity>>
}
