package com.lifemanager.app.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.domain.model.*
import com.lifemanager.app.domain.usecase.BudgetUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.YearMonth
import javax.inject.Inject

/**
 * 预算管理ViewModel
 */
@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetUseCase: BudgetUseCase
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow<BudgetUiState>(BudgetUiState.Loading)
    val uiState: StateFlow<BudgetUiState> = _uiState.asStateFlow()

    // 当前年月
    private val _currentYearMonth = MutableStateFlow(
        YearMonth.now().let { it.year * 100 + it.monthValue }
    )
    val currentYearMonth: StateFlow<Int> = _currentYearMonth.asStateFlow()

    // 当前预算及花销
    private val _budgetWithSpending = MutableStateFlow<BudgetWithSpending?>(null)
    val budgetWithSpending: StateFlow<BudgetWithSpending?> = _budgetWithSpending.asStateFlow()

    // 月度分析数据（用于图表）
    private val _monthlyAnalysis = MutableStateFlow<List<MonthlyBudgetAnalysis>>(emptyList())
    val monthlyAnalysis: StateFlow<List<MonthlyBudgetAnalysis>> = _monthlyAnalysis.asStateFlow()

    // AI建议
    private val _aiAdvice = MutableStateFlow<String>("")
    val aiAdvice: StateFlow<String> = _aiAdvice.asStateFlow()

    // 显示编辑对话框
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    // 编辑状态
    private val _editState = MutableStateFlow(BudgetEditState())
    val editState: StateFlow<BudgetEditState> = _editState.asStateFlow()

    init {
        loadData()
        observeBudget()
    }

    /**
     * 加载数据
     */
    private fun loadData() {
        viewModelScope.launch {
            try {
                _uiState.value = BudgetUiState.Loading

                // 加载月度分析
                _monthlyAnalysis.value = budgetUseCase.getMonthlyBudgetAnalysis(6)

                // 加载AI建议
                _aiAdvice.value = budgetUseCase.generateAIBudgetAdvice(_currentYearMonth.value)

                _uiState.value = BudgetUiState.Success
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    /**
     * 观察当前月份预算
     */
    private fun observeBudget() {
        viewModelScope.launch {
            _currentYearMonth.flatMapLatest { yearMonth ->
                budgetUseCase.getBudgetWithSpending(yearMonth)
            }.catch { e ->
                _uiState.value = BudgetUiState.Error(e.message ?: "加载失败")
            }.collect { budget ->
                _budgetWithSpending.value = budget
                if (_uiState.value is BudgetUiState.Loading) {
                    _uiState.value = BudgetUiState.Success
                }
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
     * 上个月
     */
    fun previousMonth() {
        val current = _currentYearMonth.value
        val year = current / 100
        val month = current % 100
        _currentYearMonth.value = if (month == 1) {
            (year - 1) * 100 + 12
        } else {
            year * 100 + (month - 1)
        }
        refreshAIAdvice()
    }

    /**
     * 下个月
     */
    fun nextMonth() {
        val current = _currentYearMonth.value
        val year = current / 100
        val month = current % 100
        _currentYearMonth.value = if (month == 12) {
            (year + 1) * 100 + 1
        } else {
            year * 100 + (month + 1)
        }
        refreshAIAdvice()
    }

    /**
     * 刷新AI建议
     */
    private fun refreshAIAdvice() {
        viewModelScope.launch {
            _aiAdvice.value = budgetUseCase.generateAIBudgetAdvice(_currentYearMonth.value)
        }
    }

    /**
     * 格式化年月
     */
    fun formatYearMonth(yearMonth: Int): String {
        return budgetUseCase.formatYearMonth(yearMonth)
    }

    /**
     * 显示编辑对话框
     */
    fun showEditDialog() {
        val current = _budgetWithSpending.value

        _editState.value = if (current != null) {
            BudgetEditState(
                yearMonth = current.budget.yearMonth,
                totalBudget = current.budget.totalBudget.toString(),
                alertThreshold = current.budget.alertThreshold,
                alertEnabled = current.budget.alertEnabled,
                note = current.budget.note,
                isEditing = true
            )
        } else {
            BudgetEditState(
                yearMonth = _currentYearMonth.value,
                totalBudget = "",
                alertThreshold = 80,
                alertEnabled = true
            )
        }
        _showEditDialog.value = true
    }

    /**
     * 隐藏编辑对话框
     */
    fun hideEditDialog() {
        _showEditDialog.value = false
        _editState.value = BudgetEditState()
    }

    /**
     * 更新预算金额
     */
    fun updateBudgetAmount(amount: String) {
        _editState.value = _editState.value.copy(totalBudget = amount)
    }

    /**
     * 更新提醒阈值
     */
    fun updateAlertThreshold(threshold: Int) {
        _editState.value = _editState.value.copy(alertThreshold = threshold)
    }

    /**
     * 更新提醒开关
     */
    fun updateAlertEnabled(enabled: Boolean) {
        _editState.value = _editState.value.copy(alertEnabled = enabled)
    }

    /**
     * 更新备注
     */
    fun updateNote(note: String) {
        _editState.value = _editState.value.copy(note = note)
    }

    /**
     * 保存预算
     */
    fun saveBudget() {
        val state = _editState.value
        val amount = state.totalBudget.toDoubleOrNull()

        if (amount == null || amount <= 0) {
            _editState.value = state.copy(error = "请输入有效的预算金额")
            return
        }

        viewModelScope.launch {
            try {
                _editState.value = state.copy(isSaving = true, error = null)

                budgetUseCase.setBudget(
                    yearMonth = state.yearMonth.takeIf { it > 0 } ?: _currentYearMonth.value,
                    totalBudget = amount,
                    alertThreshold = state.alertThreshold,
                    alertEnabled = state.alertEnabled,
                    note = state.note
                )

                hideEditDialog()
                refresh()
            } catch (e: Exception) {
                _editState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    /**
     * 从上月复制预算
     */
    fun copyFromPreviousMonth() {
        viewModelScope.launch {
            try {
                val success = budgetUseCase.copyBudgetFromPreviousMonth(_currentYearMonth.value)
                if (success) {
                    refresh()
                } else {
                    _uiState.value = BudgetUiState.Error("没有找到可复制的预算记录")
                }
            } catch (e: Exception) {
                _uiState.value = BudgetUiState.Error(e.message ?: "复制失败")
            }
        }
    }
}
