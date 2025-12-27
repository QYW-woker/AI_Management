package com.lifemanager.app.feature.datacenter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 数据中心UI状态
 */
sealed class DataCenterUiState {
    object Loading : DataCenterUiState()
    object Success : DataCenterUiState()
    data class Error(val message: String) : DataCenterUiState()
}

/**
 * 总览统计数据
 */
data class OverviewStats(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val totalSavings: Double = 0.0,
    val todoCompleted: Int = 0,
    val todoTotal: Int = 0,
    val habitCheckedIn: Int = 0,
    val habitTotal: Int = 0,
    val focusMinutes: Int = 0,
    val activeGoals: Int = 0,
    val completedGoals: Int = 0,
    val avgGoalProgress: Float = 0f,
    val diaryCount: Int = 0,
    val avgMoodScore: Float = 0f
)

/**
 * 数据中心ViewModel
 */
@HiltViewModel
class DataCenterViewModel @Inject constructor(
    private val todoRepository: TodoRepository,
    private val habitRepository: HabitRepository,
    private val diaryRepository: DiaryRepository,
    private val timeTrackRepository: TimeTrackRepository,
    private val savingsPlanRepository: SavingsPlanRepository,
    private val goalRepository: GoalRepository,
    private val monthlyIncomeExpenseRepository: MonthlyIncomeExpenseRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow<DataCenterUiState>(DataCenterUiState.Loading)
    val uiState: StateFlow<DataCenterUiState> = _uiState.asStateFlow()

    // 选中的时间周期
    private val _selectedPeriod = MutableStateFlow("MONTH")
    val selectedPeriod: StateFlow<String> = _selectedPeriod.asStateFlow()

    // 总览统计
    private val _overviewStats = MutableStateFlow(OverviewStats())
    val overviewStats: StateFlow<OverviewStats> = _overviewStats.asStateFlow()

    init {
        loadData()
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        viewModelScope.launch {
            _uiState.value = DataCenterUiState.Loading
            try {
                val stats = calculateStats()
                _overviewStats.value = stats
                _uiState.value = DataCenterUiState.Success
            } catch (e: Exception) {
                _uiState.value = DataCenterUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadData()
    }

    /**
     * 选择时间周期
     */
    fun selectPeriod(period: String) {
        _selectedPeriod.value = period
        loadData()
    }

    /**
     * 计算统计数据
     */
    private suspend fun calculateStats(): OverviewStats {
        val today = LocalDate.now().toEpochDay().toInt()
        val (startDate, endDate) = getDateRange(_selectedPeriod.value, today)

        // 待办统计
        val todoStats = todoRepository.getTodayStats(today)

        // 习惯统计
        val habits = habitRepository.getActiveHabits().first()
        val habitCheckedIn = habits.count { habit ->
            habitRepository.getRecordByHabitAndDate(habit.id, today) != null
        }

        // 日记统计
        val diaries = diaryRepository.getByDateRange(startDate, endDate).first()
        val avgMood = if (diaries.isNotEmpty()) {
            diaries.mapNotNull { it.moodScore }.average().toFloat()
        } else 0f

        // 时间统计
        val timeRecords = timeTrackRepository.getRecordsByDateRange(startDate, endDate).first()
        val focusMinutes = timeRecords.sumOf { it.durationMinutes }

        // 存钱统计
        val savingsPlans = savingsPlanRepository.getActivePlans().first()
        val totalSavings = savingsPlans.sumOf { it.currentAmount }

        // 目标统计
        val goals = goalRepository.getAllGoals().first()
        val activeGoals = goals.count { it.status == "ACTIVE" }
        val completedGoals = goals.count { it.status == "COMPLETED" }
        val avgProgress = if (goals.isNotEmpty()) {
            goals.filter { it.status == "ACTIVE" }.map { goal ->
                when {
                    goal.targetValue != null && goal.targetValue > 0 ->
                        (goal.currentValue / goal.targetValue).toFloat().coerceIn(0f, 1f)
                    else -> (goal.currentValue / 100.0).toFloat().coerceIn(0f, 1f)
                }
            }.average().toFloat()
        } else 0f

        // 财务统计
        val yearMonth = LocalDate.now().let { it.year * 100 + it.monthValue }
        val totalIncome = monthlyIncomeExpenseRepository.getTotalIncome(yearMonth)
        val totalExpense = monthlyIncomeExpenseRepository.getTotalExpense(yearMonth)

        return OverviewStats(
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            totalSavings = totalSavings,
            todoCompleted = todoStats.completed,
            todoTotal = todoStats.total,
            habitCheckedIn = habitCheckedIn,
            habitTotal = habits.size,
            focusMinutes = focusMinutes,
            activeGoals = activeGoals,
            completedGoals = completedGoals,
            avgGoalProgress = avgProgress,
            diaryCount = diaries.size,
            avgMoodScore = avgMood
        )
    }

    /**
     * 获取日期范围
     */
    private fun getDateRange(period: String, today: Int): Pair<Int, Int> {
        val todayDate = LocalDate.ofEpochDay(today.toLong())

        return when (period) {
            "WEEK" -> {
                val startOfWeek = todayDate.minusDays(todayDate.dayOfWeek.value.toLong() - 1)
                Pair(startOfWeek.toEpochDay().toInt(), today)
            }
            "MONTH" -> {
                val startOfMonth = todayDate.withDayOfMonth(1)
                Pair(startOfMonth.toEpochDay().toInt(), today)
            }
            "YEAR" -> {
                val startOfYear = todayDate.withDayOfYear(1)
                Pair(startOfYear.toEpochDay().toInt(), today)
            }
            else -> Pair(0, today)
        }
    }
}
