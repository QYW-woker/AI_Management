package com.lifemanager.app.feature.goal

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.database.dao.GoalDao
import com.lifemanager.app.core.database.dao.GoalRecordDao
import com.lifemanager.app.core.database.entity.GoalEntity
import com.lifemanager.app.core.database.entity.GoalRecordEntity
import com.lifemanager.app.core.database.entity.GoalRecordType
import com.lifemanager.app.core.database.entity.GoalStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 操作结果状态 - 用于提供即时反馈
 */
sealed class OperationResult {
    object Idle : OperationResult()
    object Loading : OperationResult()
    data class Success(val message: String) : OperationResult()
    data class Error(val message: String) : OperationResult()
}

/**
 * 目标详情ViewModel
 */
@HiltViewModel
class GoalDetailViewModel @Inject constructor(
    private val goalDao: GoalDao,
    private val goalRecordDao: GoalRecordDao
) : ViewModel() {

    // 当前目标
    private val _goal = MutableStateFlow<GoalEntity?>(null)
    val goal: StateFlow<GoalEntity?> = _goal.asStateFlow()

    // 目标记录（时间轴）
    private val _records = MutableStateFlow<List<GoalRecordEntity>>(emptyList())
    val records: StateFlow<List<GoalRecordEntity>> = _records.asStateFlow()

    // 子目标
    private val _childGoals = MutableStateFlow<List<GoalEntity>>(emptyList())
    val childGoals: StateFlow<List<GoalEntity>> = _childGoals.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 操作结果状态 - 用于即时反馈
    private val _operationResult = MutableStateFlow<OperationResult>(OperationResult.Idle)
    val operationResult: StateFlow<OperationResult> = _operationResult.asStateFlow()

    // 标记完成操作中
    private val _isCompleting = MutableStateFlow(false)
    val isCompleting: StateFlow<Boolean> = _isCompleting.asStateFlow()

    private var currentGoalId: Long = 0

    /**
     * 加载目标详情
     */
    fun loadGoal(goalId: Long) {
        currentGoalId = goalId
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // 加载目标
                goalDao.getGoalByIdFlow(goalId).collect { goalEntity ->
                    _goal.value = goalEntity
                }
            } catch (e: Exception) {
                // Handle error
            } finally {
                _isLoading.value = false
            }
        }

        // 加载记录
        viewModelScope.launch {
            goalRecordDao.getRecordsByGoalId(goalId).collect { recordList ->
                _records.value = recordList
            }
        }

        // 加载子目标
        viewModelScope.launch {
            goalDao.getChildGoals(goalId).collect { children ->
                _childGoals.value = children
            }
        }
    }

    /**
     * 计算目标进度
     */
    fun calculateProgress(goal: GoalEntity): Float {
        return when (goal.progressType) {
            "NUMERIC" -> {
                val target = goal.targetValue ?: return 0f
                if (target <= 0) return 0f
                (goal.currentValue / target).coerceIn(0.0, 1.0).toFloat()
            }
            else -> {
                // 百分比类型，currentValue直接是百分比
                (goal.currentValue / 100.0).coerceIn(0.0, 1.0).toFloat()
            }
        }
    }

    /**
     * 清除操作结果状态
     */
    fun clearOperationResult() {
        _operationResult.value = OperationResult.Idle
    }

    /**
     * 添加进度记录 - 带即时反馈
     */
    fun addRecord(title: String, content: String, progressValue: Double?) {
        val currentGoal = _goal.value ?: return
        val today = LocalDate.now().toEpochDay().toInt()

        viewModelScope.launch {
            _operationResult.value = OperationResult.Loading
            try {
                // 创建记录
                val previousValue = currentGoal.currentValue
                val newValue = if (progressValue != null) {
                    previousValue + progressValue
                } else {
                    previousValue
                }

                val record = GoalRecordEntity(
                    goalId = currentGoalId,
                    recordType = GoalRecordType.PROGRESS,
                    title = title,
                    content = content,
                    progressValue = progressValue,
                    previousValue = previousValue,
                    recordDate = today
                )
                goalRecordDao.insert(record)

                // 更新目标进度
                if (progressValue != null) {
                    goalDao.updateProgress(currentGoalId, newValue)

                    // 检查是否完成
                    val target = currentGoal.targetValue
                    if (target != null && newValue >= target) {
                        goalDao.completeGoal(currentGoalId)
                        // 添加完成记录
                        val completeRecord = GoalRecordEntity(
                            goalId = currentGoalId,
                            recordType = GoalRecordType.COMPLETE,
                            title = "目标完成",
                            content = "恭喜！目标已达成！",
                            recordDate = today
                        )
                        goalRecordDao.insert(completeRecord)
                        _operationResult.value = OperationResult.Success("🎉 目标已完成！恭喜！")
                    } else {
                        val progressPercent = if (target != null && target > 0) {
                            ((newValue / target) * 100).toInt()
                        } else {
                            newValue.toInt()
                        }
                        _operationResult.value = OperationResult.Success("进度已更新至 $progressPercent%")
                    }
                } else {
                    _operationResult.value = OperationResult.Success("记录已添加")
                }

                // 自动清除成功状态（3秒后）
                delay(3000)
                if (_operationResult.value is OperationResult.Success) {
                    _operationResult.value = OperationResult.Idle
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.Error(e.message ?: "更新失败，请重试")
            }
        }
    }

    /**
     * 标记目标完成 - 带即时反馈和动画
     */
    fun completeGoal() {
        val currentGoal = _goal.value ?: return
        if (currentGoal.status != GoalStatus.ACTIVE) return

        val today = LocalDate.now().toEpochDay().toInt()

        viewModelScope.launch {
            _isCompleting.value = true
            _operationResult.value = OperationResult.Loading
            try {
                // 检查是否有未完成的子目标
                val childCount = goalDao.countChildGoals(currentGoalId)
                if (childCount > 0) {
                    val completedChildCount = goalDao.countCompletedChildGoals(currentGoalId)
                    if (completedChildCount < childCount) {
                        _operationResult.value = OperationResult.Error(
                            "请先完成所有子目标（$completedChildCount/$childCount）"
                        )
                        _isCompleting.value = false
                        return@launch
                    }
                }

                // 更新目标状态为已完成
                goalDao.completeGoal(currentGoalId)

                // 如果是数值型目标，将进度设置为目标值
                if (currentGoal.progressType == "NUMERIC" && currentGoal.targetValue != null) {
                    goalDao.updateProgress(currentGoalId, currentGoal.targetValue)
                } else {
                    // 百分比类型设置为100
                    goalDao.updateProgress(currentGoalId, 100.0)
                }

                // 添加完成记录
                val completeRecord = GoalRecordEntity(
                    goalId = currentGoalId,
                    recordType = GoalRecordType.COMPLETE,
                    title = "目标完成",
                    content = "恭喜！目标「${currentGoal.title}」已成功达成！",
                    recordDate = today
                )
                goalRecordDao.insert(completeRecord)

                // 显示成功反馈
                _operationResult.value = OperationResult.Success("🎉 目标已完成！恭喜你！")
                _isCompleting.value = false

                // 5秒后自动清除成功状态
                delay(5000)
                if (_operationResult.value is OperationResult.Success) {
                    _operationResult.value = OperationResult.Idle
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.Error(e.message ?: "操作失败，请重试")
                _isCompleting.value = false
            }
        }
    }

    /**
     * 更新百分比进度 - 快捷更新
     */
    fun updatePercentageProgress(percentage: Int) {
        val currentGoal = _goal.value ?: return
        if (currentGoal.progressType != "PERCENTAGE") return

        val today = LocalDate.now().toEpochDay().toInt()

        viewModelScope.launch {
            _operationResult.value = OperationResult.Loading
            try {
                val previousValue = currentGoal.currentValue
                val newValue = percentage.toDouble().coerceIn(0.0, 100.0)

                // 更新进度
                goalDao.updateProgress(currentGoalId, newValue)

                // 添加记录
                val record = GoalRecordEntity(
                    goalId = currentGoalId,
                    recordType = GoalRecordType.PROGRESS,
                    title = "进度更新",
                    content = "进度从 ${previousValue.toInt()}% 更新至 ${newValue.toInt()}%",
                    progressValue = newValue - previousValue,
                    previousValue = previousValue,
                    recordDate = today
                )
                goalRecordDao.insert(record)

                // 检查是否完成
                if (newValue >= 100.0) {
                    goalDao.completeGoal(currentGoalId)
                    val completeRecord = GoalRecordEntity(
                        goalId = currentGoalId,
                        recordType = GoalRecordType.COMPLETE,
                        title = "目标完成",
                        content = "恭喜！目标已达成！",
                        recordDate = today
                    )
                    goalRecordDao.insert(completeRecord)
                    _operationResult.value = OperationResult.Success("🎉 目标已完成！恭喜！")
                } else {
                    _operationResult.value = OperationResult.Success("进度已更新至 ${newValue.toInt()}%")
                }

                delay(3000)
                if (_operationResult.value is OperationResult.Success) {
                    _operationResult.value = OperationResult.Idle
                }
            } catch (e: Exception) {
                _operationResult.value = OperationResult.Error(e.message ?: "更新失败")
            }
        }
    }

    /**
     * 放弃目标
     */
    fun abandonGoal(reason: String) {
        val today = LocalDate.now().toEpochDay().toInt()

        viewModelScope.launch {
            try {
                // 更新目标状态
                goalDao.abandonGoal(currentGoalId, reason)

                // 添加放弃记录
                val record = GoalRecordEntity(
                    goalId = currentGoalId,
                    recordType = GoalRecordType.ABANDON,
                    title = "放弃目标",
                    content = reason,
                    recordDate = today
                )
                goalRecordDao.insert(record)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * 添加子目标
     */
    fun addSubGoal(title: String, description: String, targetValue: Double?) {
        val parentGoal = _goal.value ?: return
        val today = LocalDate.now().toEpochDay().toInt()

        viewModelScope.launch {
            try {
                val subGoal = GoalEntity(
                    parentId = currentGoalId,
                    title = title,
                    description = description,
                    goalType = parentGoal.goalType,
                    category = parentGoal.category,
                    startDate = today,
                    endDate = parentGoal.endDate,
                    progressType = if (targetValue != null) "NUMERIC" else "PERCENTAGE",
                    targetValue = targetValue,
                    unit = parentGoal.unit,
                    level = parentGoal.level + 1
                )
                val subGoalId = goalDao.insert(subGoal)

                // 添加开始记录
                val startRecord = GoalRecordEntity(
                    goalId = subGoalId,
                    recordType = GoalRecordType.START,
                    title = "子目标创建",
                    content = "作为「${parentGoal.title}」的子目标创建",
                    recordDate = today
                )
                goalRecordDao.insert(startRecord)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * 完成子目标
     */
    fun completeSubGoal(subGoalId: Long) {
        val today = LocalDate.now().toEpochDay().toInt()

        viewModelScope.launch {
            try {
                goalDao.completeGoal(subGoalId)

                // 添加完成记录
                val record = GoalRecordEntity(
                    goalId = subGoalId,
                    recordType = GoalRecordType.COMPLETE,
                    title = "子目标完成",
                    content = "",
                    recordDate = today
                )
                goalRecordDao.insert(record)

                // 更新父目标进度（如果是百分比类型）
                val parentGoal = _goal.value
                if (parentGoal?.progressType == "PERCENTAGE") {
                    val totalChildren = goalDao.countChildGoals(currentGoalId)
                    val completedChildren = goalDao.countCompletedChildGoals(currentGoalId)
                    val newProgress = if (totalChildren > 0) {
                        (completedChildren.toDouble() / totalChildren) * 100
                    } else {
                        0.0
                    }
                    goalDao.updateProgress(currentGoalId, newProgress)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * 删除目标（包括子目标）
     */
    fun deleteGoal() {
        viewModelScope.launch {
            try {
                goalDao.deleteWithChildren(currentGoalId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
