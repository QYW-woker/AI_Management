package com.lifemanager.app.feature.finance.ledger

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.database.dao.DailyTransactionDao
import com.lifemanager.app.core.database.dao.LedgerDao
import com.lifemanager.app.core.database.entity.LedgerEntity
import com.lifemanager.app.core.database.entity.LedgerType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 账本管理ViewModel
 */
@HiltViewModel
class LedgerManagementViewModel @Inject constructor(
    private val ledgerDao: LedgerDao,
    private val transactionDao: DailyTransactionDao
) : ViewModel() {

    // 账本列表
    private val _ledgers = MutableStateFlow<List<LedgerWithStats>>(emptyList())
    val ledgers: StateFlow<List<LedgerWithStats>> = _ledgers.asStateFlow()

    // 加载状态
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // 显示编辑对话框
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    // 当前编辑的账本
    private val _editingLedger = MutableStateFlow<LedgerEntity?>(null)
    val editingLedger: StateFlow<LedgerEntity?> = _editingLedger.asStateFlow()

    // 编辑状态
    private val _editState = MutableStateFlow(LedgerEditState())
    val editState: StateFlow<LedgerEditState> = _editState.asStateFlow()

    // 显示删除确认对话框
    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    // 待删除的账本
    private val _deletingLedger = MutableStateFlow<LedgerEntity?>(null)
    val deletingLedger: StateFlow<LedgerEntity?> = _deletingLedger.asStateFlow()

    init {
        loadLedgers()
    }

    /**
     * 加载账本列表
     */
    private fun loadLedgers() {
        viewModelScope.launch {
            _isLoading.value = true
            ledgerDao.getAllLedgersIncludingArchived().collectLatest { ledgerList ->
                // 计算每个账本的统计信息
                val ledgersWithStats = ledgerList.map { ledger ->
                    // TODO: 按账本ID统计交易数和金额
                    LedgerWithStats(
                        ledger = ledger,
                        transactionCount = 0,
                        totalIncome = 0.0,
                        totalExpense = 0.0
                    )
                }
                _ledgers.value = ledgersWithStats
                _isLoading.value = false
            }
        }
    }

    /**
     * 显示创建账本对话框
     */
    fun showCreateDialog() {
        _editingLedger.value = null
        _editState.value = LedgerEditState()
        _showEditDialog.value = true
    }

    /**
     * 显示编辑账本对话框
     */
    fun showEditDialog(ledger: LedgerEntity) {
        _editingLedger.value = ledger
        _editState.value = LedgerEditState(
            name = ledger.name,
            description = ledger.description,
            icon = ledger.icon,
            color = ledger.color,
            ledgerType = ledger.ledgerType,
            budgetAmount = ledger.budgetAmount?.toString() ?: ""
        )
        _showEditDialog.value = true
    }

    /**
     * 隐藏编辑对话框
     */
    fun hideEditDialog() {
        _showEditDialog.value = false
        _editingLedger.value = null
        _editState.value = LedgerEditState()
    }

    /**
     * 更新账本名称
     */
    fun updateName(name: String) {
        _editState.value = _editState.value.copy(name = name)
    }

    /**
     * 更新账本描述
     */
    fun updateDescription(description: String) {
        _editState.value = _editState.value.copy(description = description)
    }

    /**
     * 更新账本图标
     */
    fun updateIcon(icon: String) {
        _editState.value = _editState.value.copy(icon = icon)
    }

    /**
     * 更新账本颜色
     */
    fun updateColor(color: String) {
        _editState.value = _editState.value.copy(color = color)
    }

    /**
     * 更新账本类型
     */
    fun updateLedgerType(type: String) {
        _editState.value = _editState.value.copy(ledgerType = type)
    }

    /**
     * 更新预算金额
     */
    fun updateBudgetAmount(amount: String) {
        _editState.value = _editState.value.copy(budgetAmount = amount)
    }

    /**
     * 保存账本
     */
    fun saveLedger() {
        val state = _editState.value

        if (state.name.isBlank()) {
            _editState.value = state.copy(error = "请输入账本名称")
            return
        }

        viewModelScope.launch {
            try {
                _editState.value = state.copy(isSaving = true, error = null)

                val budgetAmount = state.budgetAmount.toDoubleOrNull()

                val existingLedger = _editingLedger.value
                val ledger = if (existingLedger != null) {
                    existingLedger.copy(
                        name = state.name,
                        description = state.description,
                        icon = state.icon,
                        color = state.color,
                        ledgerType = state.ledgerType,
                        budgetAmount = budgetAmount,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    // 获取最大排序顺序
                    val maxSortOrder = ledgerDao.getMaxSortOrder() ?: 0
                    LedgerEntity(
                        name = state.name,
                        description = state.description,
                        icon = state.icon,
                        color = state.color,
                        ledgerType = state.ledgerType,
                        budgetAmount = budgetAmount,
                        sortOrder = maxSortOrder + 1
                    )
                }

                if (existingLedger != null) {
                    ledgerDao.update(ledger)
                } else {
                    ledgerDao.insert(ledger)
                }

                hideEditDialog()
            } catch (e: Exception) {
                _editState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    /**
     * 设置默认账本
     */
    fun setDefaultLedger(ledgerId: Long) {
        viewModelScope.launch {
            try {
                ledgerDao.clearDefaultLedger()
                ledgerDao.setDefaultLedger(ledgerId)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * 显示删除确认对话框
     */
    fun showDeleteConfirmDialog(ledger: LedgerEntity) {
        _deletingLedger.value = ledger
        _showDeleteDialog.value = true
    }

    /**
     * 隐藏删除确认对话框
     */
    fun hideDeleteDialog() {
        _showDeleteDialog.value = false
        _deletingLedger.value = null
    }

    /**
     * 删除账本
     */
    fun deleteLedger() {
        val ledger = _deletingLedger.value ?: return

        viewModelScope.launch {
            try {
                ledgerDao.delete(ledger.id)
                hideDeleteDialog()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    /**
     * 归档/取消归档账本
     */
    fun toggleArchive(ledger: LedgerEntity) {
        viewModelScope.launch {
            try {
                ledgerDao.setArchived(ledger.id, !ledger.isArchived)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}

/**
 * 账本及统计信息
 */
data class LedgerWithStats(
    val ledger: LedgerEntity,
    val transactionCount: Int,
    val totalIncome: Double,
    val totalExpense: Double
) {
    val balance: Double
        get() = totalIncome - totalExpense
}

/**
 * 账本编辑状态
 */
data class LedgerEditState(
    val name: String = "",
    val description: String = "",
    val icon: String = "book",
    val color: String = "#2196F3",
    val ledgerType: String = LedgerType.PERSONAL,
    val budgetAmount: String = "",
    val isSaving: Boolean = false,
    val error: String? = null
)

/**
 * 可选图标列表
 */
val LEDGER_ICONS = listOf(
    "book", "wallet", "card", "cash", "savings",
    "home", "work", "travel", "food", "shopping",
    "health", "education", "entertainment", "gift", "other"
)

/**
 * 可选颜色列表
 */
val LEDGER_COLORS = listOf(
    "#2196F3", "#4CAF50", "#FF9800", "#F44336", "#9C27B0",
    "#00BCD4", "#795548", "#607D8B", "#E91E63", "#3F51B5"
)
