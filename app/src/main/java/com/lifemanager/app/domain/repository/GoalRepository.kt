package com.lifemanager.app.domain.repository

import com.lifemanager.app.core.database.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * 目标仓库接口
 */
interface GoalRepository {
    fun getActiveGoals(): Flow<List<GoalEntity>>
    fun getAllGoals(): Flow<List<GoalEntity>>
    fun getGoalsByType(type: String): Flow<List<GoalEntity>>
    fun getGoalsByCategory(category: String): Flow<List<GoalEntity>>
    suspend fun getGoalById(id: Long): GoalEntity?
    suspend fun insert(goal: GoalEntity): Long
    suspend fun update(goal: GoalEntity)
    suspend fun updateProgress(id: Long, value: Double)
    suspend fun updateStatus(id: Long, status: String)
    suspend fun delete(id: Long)
    suspend fun countActiveGoals(): Int
}
