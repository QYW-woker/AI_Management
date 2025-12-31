package com.lifemanager.app.domain.usecase

import com.lifemanager.app.core.database.entity.GoalEntity
import com.lifemanager.app.core.database.entity.GoalStatus
import com.lifemanager.app.core.database.entity.ProgressType
import com.lifemanager.app.domain.model.GoalStatistics
import com.lifemanager.app.domain.model.GoalWithChildren
import com.lifemanager.app.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
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
     * 获取顶级目标（用于层级显示）
     */
    fun getTopLevelGoals(): Flow<List<GoalEntity>> {
        return repository.getTopLevelGoals()
    }

    /**
     * 获取子目标
     */
    fun getChildGoals(parentId: Long): Flow<List<GoalEntity>> {
        return repository.getChildGoals(parentId)
    }

    /**
     * 获取目标及其子目标的完整结构
     */
    suspend fun getGoalWithChildren(goalId: Long): GoalWithChildren? {
        val goal = repository.getGoalById(goalId) ?: return null
        val children = repository.getChildGoalsSync(goalId)
        val childCount = children.size
        val completedChildCount = children.count { it.status == GoalStatus.COMPLETED }

        return GoalWithChildren(
            goal = goal,
            children = children,
            childCount = childCount,
            completedChildCount = completedChildCount,
            childProgress = if (childCount > 0) completedChildCount.toFloat() / childCount else 0f
        )
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
        unit: String,
        parentId: Long? = null
    ): Long {
        // 如果有父目标，计算层级
        val level = if (parentId != null) {
            val parentGoal = repository.getGoalById(parentId)
            (parentGoal?.level ?: 0) + 1
        } else 0

        val goal = GoalEntity(
            title = title,
            description = description,
            goalType = goalType,
            category = category,
            startDate = startDate,
            endDate = endDate,
            progressType = progressType,
            targetValue = targetValue,
            unit = unit,
            parentId = parentId,
            level = level
        )
        return repository.insert(goal)
    }

    /**
     * 创建子目标
     */
    suspend fun createSubGoal(
        parentId: Long,
        title: String,
        description: String = ""
    ): Long {
        val parentGoal = repository.getGoalById(parentId)
            ?: throw IllegalArgumentException("Parent goal not found")

        val subGoal = GoalEntity(
            title = title,
            description = description,
            goalType = parentGoal.goalType,
            category = parentGoal.category,
            startDate = parentGoal.startDate,
            endDate = parentGoal.endDate,
            progressType = ProgressType.PERCENTAGE,  // 子目标默认使用百分比进度
            parentId = parentId,
            level = parentGoal.level + 1
        )

        val subGoalId = repository.insert(subGoal)

        // 如果父目标还没有子目标，将其进度类型设置为基于子目标
        // 父目标的进度将由子目标完成情况自动计算
        return subGoalId
    }

    /**
     * 更新目标
     */
    suspend fun updateGoal(goal: GoalEntity) {
        repository.update(goal.copy(updatedAt = System.currentTimeMillis()))

        // 如果有父目标，更新父目标进度
        goal.parentId?.let { parentId ->
            updateParentGoalProgress(parentId)
        }
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

            // 如果有父目标，更新父目标进度
            goal.parentId?.let { parentId ->
                updateParentGoalProgress(parentId)
            }
        }
    }

    /**
     * 完成目标
     */
    suspend fun completeGoal(id: Long) {
        repository.updateStatus(id, GoalStatus.COMPLETED)

        // 如果有父目标，更新父目标进度
        val goal = repository.getGoalById(id)
        goal?.parentId?.let { parentId ->
            updateParentGoalProgress(parentId)
        }
    }

    /**
     * 更新父目标进度（基于子目标完成情况）
     * 当子目标状态变化时调用此方法
     */
    suspend fun updateParentGoalProgress(parentId: Long) {
        val childCount = repository.countChildGoals(parentId)
        if (childCount == 0) return

        val completedCount = repository.countCompletedChildGoals(parentId)
        val progressPercentage = (completedCount.toDouble() / childCount) * 100

        // 更新父目标进度
        repository.updateProgress(parentId, progressPercentage)

        // 如果所有子目标都完成了，自动完成父目标
        if (completedCount >= childCount) {
            repository.updateStatus(parentId, GoalStatus.COMPLETED)

            // 递归检查更上层的父目标
            val parentGoal = repository.getGoalById(parentId)
            parentGoal?.parentId?.let { grandParentId ->
                updateParentGoalProgress(grandParentId)
            }
        }
    }

    /**
     * 获取子目标数量
     */
    suspend fun getChildCount(goalId: Long): Int {
        return repository.countChildGoals(goalId)
    }

    /**
     * 获取已完成的子目标数量
     */
    suspend fun getCompletedChildCount(goalId: Long): Int {
        return repository.countCompletedChildGoals(goalId)
    }

    /**
     * 检查目标是否有子目标
     */
    suspend fun hasChildren(goalId: Long): Boolean {
        return repository.countChildGoals(goalId) > 0
    }

    /**
     * 放弃目标
     */
    suspend fun abandonGoal(id: Long) {
        repository.updateStatus(id, GoalStatus.ABANDONED)

        // 如果有父目标，更新父目标进度（放弃不算完成，但可能影响整体进度显示）
        val goal = repository.getGoalById(id)
        goal?.parentId?.let { parentId ->
            updateParentGoalProgress(parentId)
        }
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

        // 如果有父目标，更新父目标进度
        val goal = repository.getGoalById(id)
        goal?.parentId?.let { parentId ->
            updateParentGoalProgress(parentId)
        }
    }

    /**
     * 删除目标
     */
    suspend fun deleteGoal(id: Long) {
        val goal = repository.getGoalById(id)
        val parentId = goal?.parentId

        // 如果有子目标，一并删除
        val childCount = repository.countChildGoals(id)
        if (childCount > 0) {
            repository.deleteWithChildren(id)
        } else {
            repository.delete(id)
        }

        // 如果有父目标，更新父目标进度
        parentId?.let {
            updateParentGoalProgress(it)
        }
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
     * 计算目标进度（简单版本，用于列表展示）
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
     * 计算目标进度（完整版本，考虑子目标）
     * 如果目标有子目标，进度基于子目标完成情况计算
     */
    suspend fun calculateProgressWithChildren(goal: GoalEntity): Float {
        if (goal.status == GoalStatus.COMPLETED) return 1f

        // 检查是否有子目标
        val childCount = repository.countChildGoals(goal.id)
        if (childCount > 0) {
            val completedCount = repository.countCompletedChildGoals(goal.id)
            return (completedCount.toFloat() / childCount).coerceIn(0f, 1f)
        }

        // 无子目标时使用原有逻辑
        return calculateProgress(goal)
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
