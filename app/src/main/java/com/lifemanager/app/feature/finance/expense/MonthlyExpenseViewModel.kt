package com.lifemanager.app.feature.finance.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.database.entity.CustomFieldEntity
import com.lifemanager.app.domain.model.EditExpenseState
import com.lifemanager.app.domain.model.ExpenseFieldStats
import com.lifemanager.app.domain.model.ExpenseStats
import com.lifemanager.app.domain.model.ExpenseTrendPoint
import com.lifemanager.app.domain.model.ExpenseUiState
import com.lifemanager.app.domain.model.MonthlyExpenseWithField
import com.lifemanager.app.domain.usecase.MonthlyExpenseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

/**
 * 月度开销ViewModel
 */
@HiltViewModel
class MonthlyExpenseViewModel @Inject constructor(
    private val useCase: MonthlyExpenseUseCase
) : ViewModel() {

    private val _currentYearMonth = MutableStateFlow(getCurrentYearMonth())
    val currentYearMonth: StateFlow<Int> = _currentYearMonth.asStateFlow()

    private val _uiState = MutableStateFlow<ExpenseUiState>(ExpenseUiState.Loading)
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    private val _records = MutableStateFlow<List<MonthlyExpenseWithField>>(emptyList())
    val records: StateFlow<List<MonthlyExpenseWithField>> = _records.asStateFlow()

    private val _expenseStats = MutableStateFlow(ExpenseStats(0, 0.0, 0.0, 0.0))
    val expenseStats: StateFlow<ExpenseStats> = _expenseStats.asStateFlow()

    private val _fieldStats = MutableStateFlow<List<ExpenseFieldStats>>(emptyList())
    val fieldStats: StateFlow<List<ExpenseFieldStats>> = _fieldStats.asStateFlow()

    private val _expenseTrend = MutableStateFlow<List<ExpenseTrendPoint>>(emptyList())
    val expenseTrend: StateFlow<List<ExpenseTrendPoint>> = _expenseTrend.asStateFlow()

    val expenseFields: StateFlow<List<CustomFieldEntity>> = useCase.getExpenseFields()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _editState = MutableStateFlow(EditExpenseState())
    val editState: StateFlow<EditExpenseState> = _editState.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _deleteRecordId = MutableStateFlow<Long?>(null)

    init {
        loadMonthData(_currentYearMonth.value)
    }

    private fun getCurrentYearMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR) * 100 + (calendar.get(Calendar.MONTH) + 1)
    }

    fun selectMonth(yearMonth: Int) {
        _currentYearMonth.value = yearMonth
        loadMonthData(yearMonth)
    }

    fun previousMonth() {
        val current = _currentYearMonth.value
        val year = current / 100
        val month = current % 100

        val newYearMonth = if (month == 1) {
            (year - 1) * 100 + 12
        } else {
            year * 100 + (month - 1)
        }

        selectMonth(newYearMonth)
    }

    fun nextMonth() {
        val current = _currentYearMonth.value
        val year = current / 100
        val month = current % 100

        val newYearMonth = if (month == 12) {
            (year + 1) * 100 + 1
        } else {
            year * 100 + (month + 1)
        }

        selectMonth(newYearMonth)
    }

    private fun loadMonthData(yearMonth: Int) {
        _uiState.value = ExpenseUiState.Loading

        viewModelScope.launch {
            try {
                launch {
                    useCase.getRecordsWithFields(yearMonth).collect { records ->
                        _records.value = records
                    }
                }

                launch {
                    useCase.getFieldStats(yearMonth).collect { stats ->
                        _fieldStats.value = stats
                    }
                }

                val stats = useCase.getExpenseStats(yearMonth)
                _expenseStats.value = stats

                launch {
                    useCase.getExpenseTrend(yearMonth).collect { trend ->
                        _expenseTrend.value = trend
                    }
                }

                _uiState.value = ExpenseUiState.Success(
                    records = _records.value,
                    stats = stats,
                    fieldStats = _fieldStats.value
                )

            } catch (e: Exception) {
                _uiState.value = ExpenseUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    fun refresh() {
        loadMonthData(_currentYearMonth.value)
    }

    fun showAddDialog() {
        _editState.value = EditExpenseState(
            yearMonth = _currentYearMonth.value,
            isEditing = false
        )
        _showEditDialog.value = true
    }

    fun showEditDialog(recordId: Long) {
        viewModelScope.launch {
            val record = useCase.getRecordById(recordId)
            if (record != null) {
                _editState.value = EditExpenseState(
                    id = record.id,
                    yearMonth = record.yearMonth,
                    fieldId = record.fieldId ?: 0,
                    amount = record.amount,
                    budgetAmount = record.budgetAmount,
                    isFixed = record.expenseType == "FIXED",
                    note = record.note,
                    isEditing = true
                )
                _showEditDialog.value = true
            }
        }
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
        _editState.value = EditExpenseState()
    }

    fun updateEditField(fieldId: Long) {
        _editState.value = _editState.value.copy(fieldId = fieldId)
    }

    fun updateEditAmount(amount: Double) {
        _editState.value = _editState.value.copy(amount = amount)
    }

    fun updateEditBudget(budget: Double?) {
        _editState.value = _editState.value.copy(budgetAmount = budget)
    }

    fun updateEditIsFixed(isFixed: Boolean) {
        _editState.value = _editState.value.copy(isFixed = isFixed)
    }

    fun updateEditNote(note: String) {
        _editState.value = _editState.value.copy(note = note)
    }

    fun saveRecord() {
        val state = _editState.value

        if (state.fieldId == 0L) {
            _editState.value = state.copy(error = "请选择类别")
            return
        }
        if (state.amount <= 0) {
            _editState.value = state.copy(error = "请输入有效金额")
            return
        }

        _editState.value = state.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                if (state.isEditing) {
                    useCase.updateRecord(
                        id = state.id,
                        yearMonth = state.yearMonth,
                        fieldId = state.fieldId,
                        amount = state.amount,
                        budgetAmount = state.budgetAmount,
                        isFixed = state.isFixed,
                        note = state.note
                    )
                } else {
                    useCase.addRecord(
                        yearMonth = state.yearMonth,
                        fieldId = state.fieldId,
                        amount = state.amount,
                        budgetAmount = state.budgetAmount,
                        isFixed = state.isFixed,
                        note = state.note
                    )
                }

                hideEditDialog()
                refresh()
            } catch (e: Exception) {
                _editState.value = _editState.value.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    fun showDeleteConfirm(recordId: Long) {
        _deleteRecordId.value = recordId
        _showDeleteDialog.value = true
    }

    fun hideDeleteConfirm() {
        _showDeleteDialog.value = false
        _deleteRecordId.value = null
    }

    fun confirmDelete() {
        val recordId = _deleteRecordId.value ?: return

        viewModelScope.launch {
            try {
                useCase.deleteRecord(recordId)
                hideDeleteConfirm()
                refresh()
            } catch (e: Exception) {
                // 处理错误
            }
        }
    }

    fun formatYearMonth(yearMonth: Int): String {
        val year = yearMonth / 100
        val month = yearMonth % 100
        return "${year}年${month}月"
    }
}
