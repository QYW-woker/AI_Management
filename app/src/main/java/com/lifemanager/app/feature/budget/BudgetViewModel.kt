package com.lifemanager.app.feature.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.database.dao.CustomFieldDao
import com.lifemanager.app.core.database.dao.DailyTransactionDao
import com.lifemanager.app.core.database.entity.CustomFieldEntity
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
    private val budgetUseCase: BudgetUseCase,
    private val customFieldDao: CustomFieldDao,
    private val dailyTransactionDao: DailyTransactionDao
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

    // 支出分类列表
    private val _expenseCategories = MutableStateFlow<List<CustomFieldEntity>>(emptyList())
    val expenseCategories: StateFlow<List<CustomFieldEntity>> = _expenseCategories.asStateFlow()

    // 分类预算状态列表（带实际支出）
    private val _categoryBudgetStatuses = MutableStateFlow<List<CategoryBudgetItem>>(emptyList())
    val categoryBudgetStatuses: StateFlow<List<CategoryBudgetItem>> = _categoryBudgetStatuses.asStateFlow()

    // 是否显示添加分类预算对话框
    private val _showAddCategoryBudgetDialog = MutableStateFlow(false)
    val showAddCategoryBudgetDialog: StateFlow<Boolean> = _showAddCategoryBudgetDialog.asStateFlow()

    init {
        loadData()
        observeBudget()
        loadExpenseCategories()
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
     * 加载支出分类
     */
    private fun loadExpenseCategories() {
        viewModelScope.launch {
            customFieldDao.getFieldsByModule("EXPENSE_CATEGORY")
                .collectLatest { categories ->
                    _expenseCategories.value = categories
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
                // 更新分类预算状态
                updateCategoryBudgetStatuses(budget)
            }
        }
    }

    /**
     * 更新分类预算状态
     */
    private fun updateCategoryBudgetStatuses(budget: BudgetWithSpending?) {
        viewModelScope.launch {
            val yearMonth = _currentYearMonth.value
            val year = yearMonth / 100
            val month = yearMonth % 100
            val ym = YearMonth.of(year, month)
            val startDate = ym.atDay(1).toEpochDay().toInt()
            val endDate = ym.atEndOfMonth().toEpochDay().toInt()

            val categoryBudgets = budget?.categoryBudgets ?: emptyMap()
            val categories = _expenseCategories.value

            val statuses = categories.mapNotNull { category ->
                val budgetAmount = categoryBudgets[category.name]
                if (budgetAmount != null && budgetAmount > 0) {
                    // 获取该分类的实际支出
                    val spentAmount = dailyTransactionDao.getTotalByCategoryInRange(
                        startDate, endDate, category.id
                    )
                    CategoryBudgetItem(
                        categoryId = category.id,
                        categoryName = category.name,
                        categoryColor = category.color ?: "#2196F3",
                        budgetAmount = budgetAmount.toString(),
                        spentAmount = spentAmount
                    )
                } else null
            }

            _categoryBudgetStatuses.value = statuses
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

        viewModelScope.launch {
            // 加载现有分类预算
            val categoryBudgetItems = if (current != null) {
                current.categoryBudgets.map { (name, amount) ->
                    val category = _expenseCategories.value.find { it.name == name }
                    CategoryBudgetItem(
                        categoryId = category?.id ?: 0,
                        categoryName = name,
                        categoryColor = category?.color ?: "#2196F3",
                        budgetAmount = amount.toString()
                    )
                }
            } else {
                emptyList()
            }

            _editState.value = if (current != null) {
                BudgetEditState(
                    yearMonth = current.budget.yearMonth,
                    totalBudget = current.budget.totalBudget.toString(),
                    alertThreshold = current.budget.alertThreshold,
                    alertEnabled = current.budget.alertEnabled,
                    note = current.budget.note,
                    isEditing = true,
                    categoryBudgets = categoryBudgetItems
                )
            } else {
                BudgetEditState(
                    yearMonth = _currentYearMonth.value,
                    totalBudget = "",
                    alertThreshold = 80,
                    alertEnabled = true,
                    categoryBudgets = emptyList()
                )
            }
            _showEditDialog.value = true
        }
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
     * 添加分类预算
     */
    fun addCategoryBudget(categoryId: Long, categoryName: String, categoryColor: String, amount: String) {
        val current = _editState.value
        val existing = current.categoryBudgets.find { it.categoryId == categoryId }

        val updatedList = if (existing != null) {
            current.categoryBudgets.map {
                if (it.categoryId == categoryId) it.copy(budgetAmount = amount) else it
            }
        } else {
            current.categoryBudgets + CategoryBudgetItem(
                categoryId = categoryId,
                categoryName = categoryName,
                categoryColor = categoryColor,
                budgetAmount = amount
            )
        }

        _editState.value = current.copy(categoryBudgets = updatedList)
    }

    /**
     * 更新分类预算金额
     */
    fun updateCategoryBudgetAmount(categoryId: Long, amount: String) {
        val current = _editState.value
        val updatedList = current.categoryBudgets.map {
            if (it.categoryId == categoryId) it.copy(budgetAmount = amount) else it
        }
        _editState.value = current.copy(categoryBudgets = updatedList)
    }

    /**
     * 删除分类预算
     */
    fun removeCategoryBudget(categoryId: Long) {
        val current = _editState.value
        val updatedList = current.categoryBudgets.filter { it.categoryId != categoryId }
        _editState.value = current.copy(categoryBudgets = updatedList)
    }

    /**
     * 显示添加分类预算对话框
     */
    fun showAddCategoryBudgetDialog() {
        _showAddCategoryBudgetDialog.value = true
    }

    /**
     * 隐藏添加分类预算对话框
     */
    fun hideAddCategoryBudgetDialog() {
        _showAddCategoryBudgetDialog.value = false
    }

    /**
     * 获取未添加预算的分类
     */
    fun getAvailableCategories(): List<CustomFieldEntity> {
        val existingCategoryIds = _editState.value.categoryBudgets.map { it.categoryId }.toSet()
        return _expenseCategories.value.filter { it.id !in existingCategoryIds }
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

        // 构建分类预算Map
        val categoryBudgets = state.categoryBudgets
            .filter { it.budgetAmount.toDoubleOrNull() != null && it.budgetAmount.toDoubleOrNull()!! > 0 }
            .associate { it.categoryName to it.budgetAmount.toDouble() }

        viewModelScope.launch {
            try {
                _editState.value = state.copy(isSaving = true, error = null)

                budgetUseCase.setBudget(
                    yearMonth = state.yearMonth.takeIf { it > 0 } ?: _currentYearMonth.value,
                    totalBudget = amount,
                    categoryBudgets = categoryBudgets,
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
