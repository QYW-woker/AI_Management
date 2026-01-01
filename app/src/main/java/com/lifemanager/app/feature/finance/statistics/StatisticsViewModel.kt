package com.lifemanager.app.feature.finance.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.domain.model.*
import com.lifemanager.app.domain.usecase.DailyTransactionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 统计分析ViewModel
 */
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val transactionUseCase: DailyTransactionUseCase
) : ViewModel() {

    // UI状态
    sealed class UiState {
        object Loading : UiState()
        object Success : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // 当前统计周期类型
    private val _periodType = MutableStateFlow(StatsPeriodType.MONTHLY)
    val periodType: StateFlow<StatsPeriodType> = _periodType.asStateFlow()

    // 当前选择的年份
    private val _selectedYear = MutableStateFlow(LocalDate.now().year)
    val selectedYear: StateFlow<Int> = _selectedYear.asStateFlow()

    // 当前选择的季度 (1-4)
    private val _selectedQuarter = MutableStateFlow((LocalDate.now().monthValue - 1) / 3 + 1)
    val selectedQuarter: StateFlow<Int> = _selectedQuarter.asStateFlow()

    // 当前选择的月份 (1-12)
    private val _selectedMonth = MutableStateFlow(LocalDate.now().monthValue)
    val selectedMonth: StateFlow<Int> = _selectedMonth.asStateFlow()

    // 月度统计
    private val _monthlyStats = MutableStateFlow<MonthlyStats?>(null)
    val monthlyStats: StateFlow<MonthlyStats?> = _monthlyStats.asStateFlow()

    // 季度统计
    private val _quarterlyStats = MutableStateFlow<QuarterlyStats?>(null)
    val quarterlyStats: StateFlow<QuarterlyStats?> = _quarterlyStats.asStateFlow()

    // 年度统计
    private val _yearlyStats = MutableStateFlow<YearlyStats?>(null)
    val yearlyStats: StateFlow<YearlyStats?> = _yearlyStats.asStateFlow()

    // 月度趋势
    private val _monthlyTrend = MutableStateFlow<List<TrendDataPoint>>(emptyList())
    val monthlyTrend: StateFlow<List<TrendDataPoint>> = _monthlyTrend.asStateFlow()

    // 季度趋势
    private val _quarterlyTrend = MutableStateFlow<List<TrendDataPoint>>(emptyList())
    val quarterlyTrend: StateFlow<List<TrendDataPoint>> = _quarterlyTrend.asStateFlow()

    // 同比分析
    private val _yearOverYearComparison = MutableStateFlow<ComparisonStats?>(null)
    val yearOverYearComparison: StateFlow<ComparisonStats?> = _yearOverYearComparison.asStateFlow()

    // 环比分析
    private val _monthOverMonthComparison = MutableStateFlow<ComparisonStats?>(null)
    val monthOverMonthComparison: StateFlow<ComparisonStats?> = _monthOverMonthComparison.asStateFlow()

    // 分类支出
    private val _categoryStats = MutableStateFlow<List<CategoryExpenseStats>>(emptyList())
    val categoryStats: StateFlow<List<CategoryExpenseStats>> = _categoryStats.asStateFlow()

    init {
        loadData()
    }

    /**
     * 加载数据
     */
    fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = UiState.Loading

                when (_periodType.value) {
                    StatsPeriodType.MONTHLY -> loadMonthlyData()
                    StatsPeriodType.QUARTERLY -> loadQuarterlyData()
                    StatsPeriodType.YEARLY -> loadYearlyData()
                    StatsPeriodType.WEEKLY -> loadMonthlyData() // 暂时用月度数据
                }

                _uiState.value = UiState.Success
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 加载月度数据
     */
    private suspend fun loadMonthlyData() {
        val year = _selectedYear.value
        val month = _selectedMonth.value

        _monthlyStats.value = transactionUseCase.getMonthStats(year, month)
        _monthlyTrend.value = transactionUseCase.getMonthlyTrend(12)
        _yearOverYearComparison.value = transactionUseCase.getMonthYearOverYearComparison(year, month)
        _monthOverMonthComparison.value = transactionUseCase.getMonthMonthOverMonthComparison(year, month)

        val startDate = LocalDate.of(year, month, 1).toEpochDay().toInt()
        val endDate = LocalDate.of(year, month, 1)
            .withDayOfMonth(LocalDate.of(year, month, 1).lengthOfMonth())
            .toEpochDay().toInt()
        transactionUseCase.getCategoryExpenseStats(startDate, endDate).collect {
            _categoryStats.value = it
        }
    }

    /**
     * 加载季度数据
     */
    private suspend fun loadQuarterlyData() {
        val year = _selectedYear.value
        val quarter = _selectedQuarter.value

        _quarterlyStats.value = transactionUseCase.getQuarterStats(year, quarter)
        _quarterlyTrend.value = transactionUseCase.getQuarterlyTrend(4)
        _categoryStats.value = _quarterlyStats.value?.categoryBreakdown ?: emptyList()
    }

    /**
     * 加载年度数据
     */
    private suspend fun loadYearlyData() {
        val year = _selectedYear.value

        _yearlyStats.value = transactionUseCase.getYearStats(year)
        _monthlyTrend.value = transactionUseCase.getMonthlyTrend(12)
        _categoryStats.value = _yearlyStats.value?.categoryBreakdown ?: emptyList()
    }

    /**
     * 切换统计周期
     */
    fun setPeriodType(type: StatsPeriodType) {
        _periodType.value = type
        loadData()
    }

    /**
     * 切换年份
     */
    fun setYear(year: Int) {
        _selectedYear.value = year
        loadData()
    }

    /**
     * 切换季度
     */
    fun setQuarter(quarter: Int) {
        _selectedQuarter.value = quarter
        loadData()
    }

    /**
     * 切换月份
     */
    fun setMonth(month: Int) {
        _selectedMonth.value = month
        loadData()
    }

    /**
     * 上一个周期
     */
    fun previousPeriod() {
        when (_periodType.value) {
            StatsPeriodType.MONTHLY -> {
                if (_selectedMonth.value == 1) {
                    _selectedMonth.value = 12
                    _selectedYear.value -= 1
                } else {
                    _selectedMonth.value -= 1
                }
            }
            StatsPeriodType.QUARTERLY -> {
                if (_selectedQuarter.value == 1) {
                    _selectedQuarter.value = 4
                    _selectedYear.value -= 1
                } else {
                    _selectedQuarter.value -= 1
                }
            }
            StatsPeriodType.YEARLY -> {
                _selectedYear.value -= 1
            }
            else -> {}
        }
        loadData()
    }

    /**
     * 下一个周期
     */
    fun nextPeriod() {
        val today = LocalDate.now()
        when (_periodType.value) {
            StatsPeriodType.MONTHLY -> {
                val nextMonth = if (_selectedMonth.value == 12) 1 else _selectedMonth.value + 1
                val nextYear = if (_selectedMonth.value == 12) _selectedYear.value + 1 else _selectedYear.value

                // 不能超过当前月份
                if (nextYear < today.year || (nextYear == today.year && nextMonth <= today.monthValue)) {
                    _selectedMonth.value = nextMonth
                    _selectedYear.value = nextYear
                    loadData()
                }
            }
            StatsPeriodType.QUARTERLY -> {
                val nextQuarter = if (_selectedQuarter.value == 4) 1 else _selectedQuarter.value + 1
                val nextYear = if (_selectedQuarter.value == 4) _selectedYear.value + 1 else _selectedYear.value
                val currentQuarter = (today.monthValue - 1) / 3 + 1

                if (nextYear < today.year || (nextYear == today.year && nextQuarter <= currentQuarter)) {
                    _selectedQuarter.value = nextQuarter
                    _selectedYear.value = nextYear
                    loadData()
                }
            }
            StatsPeriodType.YEARLY -> {
                if (_selectedYear.value < today.year) {
                    _selectedYear.value += 1
                    loadData()
                }
            }
            else -> {}
        }
    }

    /**
     * 获取当前周期标签
     */
    fun getCurrentPeriodLabel(): String {
        return when (_periodType.value) {
            StatsPeriodType.MONTHLY -> "${_selectedYear.value}年${_selectedMonth.value}月"
            StatsPeriodType.QUARTERLY -> "${_selectedYear.value}年第${_selectedQuarter.value}季度"
            StatsPeriodType.YEARLY -> "${_selectedYear.value}年"
            StatsPeriodType.WEEKLY -> "本周"
        }
    }
}
