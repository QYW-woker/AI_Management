package com.lifemanager.app.domain.usecase

import com.lifemanager.app.core.database.entity.GoalEntity
import com.lifemanager.app.core.database.entity.GoalStatus
import com.lifemanager.app.domain.model.GoalStatistics
import com.lifemanager.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.time.LocalDate
import javax.inject.Inject

/**
 * 目标用例
 */
class GoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {

    /**
     * 获取活跃目标
     */
    fun getActiveGoals(): Flow<List<GoalEntity>> {
        return repository.getActiveGoals()
    }

    /**
     * 获取所有目标
     */
    fun getAllGoals(): Flow<List<GoalEntity>> {
        return repository.getAllGoals()
    }

    /**
     * 根据类型获取目标
     */
    fun getGoalsByType(type: String): Flow<List<GoalEntity>> {
        return repository.getGoalsByType(type)
    }

    /**
     * 根据分类获取目标
     */
    fun getGoalsByCategory(category: String): Flow<List<GoalEntity>> {
        return repository.getGoalsByCategory(category)
    }

    /**
     * 获取目标详情
     */
    suspend fun getGoalById(id: Long): GoalEntity? {
        return repository.getGoalById(id)
    }

    /**
     * 创建目标
     */
    suspend fun createGoal(
        title: String,
        description: String,
        goalType: String,
        category: String,
        startDate: Int,
        endDate: Int?,
        progressType: String,
        targetValue: Double?,
        unit: String
    ): Long {
        val goal = GoalEntity(
            title = title,
            description = description,
            goalType = goalType,
            category = category,
            startDate = startDate,
            endDate = endDate,
            progressType = progressType,
            targetValue = targetValue,
            unit = unit
        )
        return repository.insert(goal)
    }

    /**
     * 更新目标
     */
    suspend fun updateGoal(goal: GoalEntity) {
        repository.update(goal.copy(updatedAt = System.currentTimeMillis()))
    }

    /**
     * 更新目标进度
     */
    suspend fun updateProgress(id: Long, value: Double) {
        repository.updateProgress(id, value)

        // 检查是否达成目标
        val goal = repository.getGoalById(id)
        if (goal != null && goal.targetValue != null && value >= goal.targetValue) {
            repository.updateStatus(id, GoalStatus.COMPLETED)
        }
    }

    /**
     * 完成目标
     */
    suspend fun completeGoal(id: Long) {
        repository.updateStatus(id, GoalStatus.COMPLETED)
    }

    /**
     * 放弃目标
     */
    suspend fun abandonGoal(id: Long) {
        repository.updateStatus(id, GoalStatus.ABANDONED)
    }

    /**
     * 归档目标
     */
    suspend fun archiveGoal(id: Long) {
        repository.updateStatus(id, GoalStatus.ARCHIVED)
    }

    /**
     * 重新激活目标
     */
    suspend fun reactivateGoal(id: Long) {
        repository.updateStatus(id, GoalStatus.ACTIVE)
    }

    /**
     * 删除目标
     */
    suspend fun deleteGoal(id: Long) {
        repository.delete(id)
    }

    /**
     * 获取目标统计
     */
    suspend fun getStatistics(): GoalStatistics {
        val allGoals = repository.getAllGoals().first()
        val activeGoals = allGoals.filter { it.status == GoalStatus.ACTIVE }
        val completedGoals = allGoals.filter { it.status == GoalStatus.COMPLETED }

        val totalProgress = if (activeGoals.isNotEmpty()) {
            activeGoals.map { calculateProgress(it) }.average().toFloat()
        } else 0f

        return GoalStatistics(
            activeCount = activeGoals.size,
            completedCount = completedGoals.size,
            totalProgress = totalProgress
        )
    }

    /**
     * 计算目标进度
     */
    fun calculateProgress(goal: GoalEntity): Float {
        return when {
            goal.status == GoalStatus.COMPLETED -> 1f
            goal.progressType == "NUMERIC" && goal.targetValue != null && goal.targetValue > 0 -> {
                (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
            }
            goal.progressType == "PERCENTAGE" -> {
                (goal.currentValue / 100.0).toFloat().coerceIn(0f, 1f)
            }
            else -> 0f
        }
    }

    /**
     * 获取当前日期
     */
    fun getToday(): Int {
        return LocalDate.now().toEpochDay().toInt()
    }

    /**
     * 格式化日期
     */
    fun formatDate(epochDay: Int): String {
        val date = LocalDate.ofEpochDay(epochDay.toLong())
        return "${date.year}年${date.monthValue}月${date.dayOfMonth}日"
    }

    /**
     * 计算剩余天数
     */
    fun getRemainingDays(endDate: Int?): Int? {
        if (endDate == null) return null
        val today = getToday()
        return endDate - today
    }
}
