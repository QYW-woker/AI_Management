package com.lifemanager.app.core.database.dao

import androidx.room.*
import com.lifemanager.app.core.database.entity.GoalMilestoneEntity
import kotlinx.coroutines.flow.Flow

/**
 * 目标里程碑DAO接口
 */
@Dao
interface GoalMilestoneDao {

    /**
     * 获取指定目标的所有里程碑
     */
    @Query("""
        SELECT * FROM goal_milestones
        WHERE goalId = :goalId
        ORDER BY sortOrder ASC, targetDate ASC
    """)
    fun getMilestonesByGoal(goalId: Long): Flow<List<GoalMilestoneEntity>>

    /**
     * 根据ID获取里程碑
     */
    @Query("SELECT * FROM goal_milestones WHERE id = :id")
    suspend fun getMilestoneById(id: Long): GoalMilestoneEntity?

    /**
     * 获取未完成的里程碑数量
     */
    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goalId = :goalId AND isCompleted = 0")
    suspend fun countPendingMilestones(goalId: Long): Int

    /**
     * 获取已完成的里程碑数量
     */
    @Query("SELECT COUNT(*) FROM goal_milestones WHERE goalId = :goalId AND isCompleted = 1")
    suspend fun countCompletedMilestones(goalId: Long): Int

    /**
     * 插入里程碑
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(milestone: GoalMilestoneEntity): Long

    /**
     * 批量插入里程碑
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(milestones: List<GoalMilestoneEntity>)

    /**
     * 更新里程碑
     */
    @Update
    suspend fun update(milestone: GoalMilestoneEntity)

    /**
     * 标记里程碑完成
     */
    @Query("""
        UPDATE goal_milestones
        SET isCompleted = :isCompleted, completedAt = :completedAt
        WHERE id = :id
    """)
    suspend fun markCompleted(id: Long, isCompleted: Boolean, completedAt: Long?)

    /**
     * 删除里程碑
     */
    @Query("DELETE FROM goal_milestones WHERE id = :id")
    suspend fun delete(id: Long)

    /**
     * 删除目标的所有里程碑
     */
    @Query("DELETE FROM goal_milestones WHERE goalId = :goalId")
    suspend fun deleteByGoal(goalId: Long)
}
