package com.lifemanager.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.database.dao.DailyTransactionDao
import com.lifemanager.app.core.database.dao.GoalDao
import com.lifemanager.app.core.database.dao.HabitDao
import com.lifemanager.app.core.database.dao.TodoDao
import com.lifemanager.app.core.database.entity.GoalEntity
import com.lifemanager.app.core.database.entity.GoalStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * 首页ViewModel - 提供真实数据并优化性能
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionDao: DailyTransactionDao,
    private val todoDao: TodoDao,
    private val goalDao: GoalDao,
    private val habitDao: HabitDao
) : ViewModel() {

    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 今日统计
    private val _todayStats = MutableStateFlow(TodayStatsData())
    val todayStats: StateFlow<TodayStatsData> = _todayStats.asStateFlow()

    // 本月财务
    private val _monthlyFinance = MutableStateFlow(MonthlyFinanceData())
    val monthlyFinance: StateFlow<MonthlyFinanceData> = _monthlyFinance.asStateFlow()

    // 目标进度
    private val _topGoals = MutableStateFlow<List<GoalProgressData>>(emptyList())
    val topGoals: StateFlow<List<GoalProgressData>> = _topGoals.asStateFlow()

    init {
        loadAllData()
    }

    /**
     * 并行加载所有数据
     */
    private fun loadAllData() {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true

            // 并行加载各项数据
            launch { loadTodayStats() }
            launch { loadMonthlyFinance() }
            launch { loadTopGoals() }

            _isLoading.value = false
        }
    }

    /**
     * 加载今日统计
     */
    private suspend fun loadTodayStats() {
        try {
            val today = LocalDate.now().toEpochDay().toInt()

            // 获取今日交易数据
            transactionDao.getTransactionsByDate(today).collectLatest { transactions ->
                val todayExpense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
                val todayIncome = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }

                // 获取待办完成情况
                val todoStats = todoDao.getTodayStats(today)

                // 获取活跃习惯数量
                val totalHabits = habitDao.countActive()

                _todayStats.value = TodayStatsData(
                    completedTodos = todoStats.completed,
                    totalTodos = todoStats.total,
                    todayExpense = todayExpense,
                    todayIncome = todayIncome,
                    completedHabits = 0, // 需要 HabitCheckInDao
                    totalHabits = totalHabits,
                    focusMinutes = 0 // 暂无计时功能
                )
            }
        } catch (e: Exception) {
            // 静默处理错误
        }
    }

    /**
     * 加载本月财务
     */
    private suspend fun loadMonthlyFinance() {
        try {
            val yearMonth = YearMonth.now()
            val startDate = yearMonth.atDay(1).toEpochDay().toInt()
            val endDate = yearMonth.atEndOfMonth().toEpochDay().toInt()

            transactionDao.getTransactionsInRange(startDate, endDate).collectLatest { transactions ->
                val income = transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
                val expense = transactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }

                _monthlyFinance.value = MonthlyFinanceData(
                    totalIncome = income,
                    totalExpense = expense,
                    balance = income - expense
                )
            }
        } catch (e: Exception) {
            // 静默处理错误
        }
    }

    /**
     * 加载目标进度
     */
    private suspend fun loadTopGoals() {
        try {
            goalDao.getGoalsByStatus(GoalStatus.ACTIVE).collectLatest { goals ->
                val topGoals = goals.take(3).map { goal ->
                    val progress = calculateGoalProgress(goal)
                    GoalProgressData(
                        id = goal.id,
                        title = goal.title,
                        progress = progress,
                        progressText = formatGoalProgress(goal, progress)
                    )
                }
                _topGoals.value = topGoals
            }
        } catch (e: Exception) {
            // 静默处理错误
        }
    }

    /**
     * 计算目标进度
     */
    private fun calculateGoalProgress(goal: GoalEntity): Float {
        return when (goal.progressType) {
            "NUMERIC" -> {
                val target = goal.targetValue ?: 100.0
                if (target > 0) (goal.currentValue / target).toFloat().coerceIn(0f, 1f) else 0f
            }
            else -> (goal.currentValue / 100.0).toFloat().coerceIn(0f, 1f)
        }
    }

    /**
     * 格式化目标进度文本
     */
    private fun formatGoalProgress(goal: GoalEntity, progress: Float): String {
        return when (goal.progressType) {
            "NUMERIC" -> {
                val current = goal.currentValue.toInt()
                val target = goal.targetValue?.toInt() ?: 100
                "$current${goal.unit} / $target${goal.unit}"
            }
            else -> "${(progress * 100).toInt()}%"
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadAllData()
    }
}

/**
 * 今日统计数据
 */
data class TodayStatsData(
    val completedTodos: Int = 0,
    val totalTodos: Int = 0,
    val todayExpense: Double = 0.0,
    val todayIncome: Double = 0.0,
    val completedHabits: Int = 0,
    val totalHabits: Int = 0,
    val focusMinutes: Int = 0
)

/**
 * 本月财务数据
 */
data class MonthlyFinanceData(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0
)

/**
 * 目标进度数据
 */
data class GoalProgressData(
    val id: Long,
    val title: String,
    val progress: Float,
    val progressText: String
)
