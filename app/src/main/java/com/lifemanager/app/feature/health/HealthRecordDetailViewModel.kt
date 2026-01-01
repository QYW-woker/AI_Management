package com.lifemanager.app.feature.health

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.database.entity.HealthRecordEntity
import com.lifemanager.app.core.database.entity.HealthRecordType
import com.lifemanager.app.data.repository.HealthRecordRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
 * 健康记录详情 ViewModel
 */
@HiltViewModel
class HealthRecordDetailViewModel @Inject constructor(
    private val repository: HealthRecordRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val recordId: Long = savedStateHandle.get<Long>("id") ?: 0L

    private val _uiState = MutableStateFlow<HealthRecordDetailUiState>(HealthRecordDetailUiState.Loading)
    val uiState: StateFlow<HealthRecordDetailUiState> = _uiState.asStateFlow()

    private val _record = MutableStateFlow<HealthRecordEntity?>(null)
    val record: StateFlow<HealthRecordEntity?> = _record.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    // 编辑状态
    private val _editValue = MutableStateFlow("")
    val editValue: StateFlow<String> = _editValue.asStateFlow()

    private val _editSecondaryValue = MutableStateFlow("")
    val editSecondaryValue: StateFlow<String> = _editSecondaryValue.asStateFlow()

    private val _editRating = MutableStateFlow(3)
    val editRating: StateFlow<Int> = _editRating.asStateFlow()

    private val _editNote = MutableStateFlow("")
    val editNote: StateFlow<String> = _editNote.asStateFlow()

    private val _isOperating = MutableStateFlow(false)
    val isOperating: StateFlow<Boolean> = _isOperating.asStateFlow()

    private val _operationError = MutableStateFlow<String?>(null)
    val operationError: StateFlow<String?> = _operationError.asStateFlow()

    init {
        if (recordId > 0) {
            loadRecord(recordId)
        }
    }

    fun loadRecord(id: Long) {
        viewModelScope.launch {
            _uiState.value = HealthRecordDetailUiState.Loading
            try {
                val recordEntity = repository.getById(id)
                if (recordEntity == null) {
                    _uiState.value = HealthRecordDetailUiState.Error("记录不存在")
                    return@launch
                }
                _record.value = recordEntity
                _uiState.value = HealthRecordDetailUiState.Success
            } catch (e: Exception) {
                _uiState.value = HealthRecordDetailUiState.Error(e.message ?: "加载失败")
            }
        }
    }

    // 编辑操作
    fun showEditDialog() {
        _record.value?.let { record ->
            _editValue.value = when (record.recordType) {
                HealthRecordType.MOOD -> ""
                else -> record.value.toString()
            }
            _editSecondaryValue.value = record.secondaryValue?.toString() ?: ""
            _editRating.value = record.rating ?: 3
            _editNote.value = record.note
            _operationError.value = null
            _showEditDialog.value = true
        }
    }

    fun hideEditDialog() {
        _showEditDialog.value = false
    }

    fun updateEditValue(value: String) {
        _editValue.value = value
        _operationError.value = null
    }

    fun updateEditSecondaryValue(value: String) {
        _editSecondaryValue.value = value
    }

    fun updateEditRating(rating: Int) {
        _editRating.value = rating
    }

    fun updateEditNote(note: String) {
        _editNote.value = note
    }

    fun saveEdit() {
        val currentRecord = _record.value ?: return

        val parsedValue = when (currentRecord.recordType) {
            HealthRecordType.MOOD -> _editRating.value.toDouble()
            else -> _editValue.value.toDoubleOrNull()
        }

        if (parsedValue == null) {
            _operationError.value = "请输入有效数值"
            return
        }

        viewModelScope.launch {
            _isOperating.value = true
            try {
                val updatedRecord = currentRecord.copy(
                    value = parsedValue,
                    secondaryValue = _editSecondaryValue.value.toDoubleOrNull(),
                    rating = when (currentRecord.recordType) {
                        HealthRecordType.MOOD, HealthRecordType.SLEEP -> _editRating.value
                        else -> currentRecord.rating
                    },
                    note = _editNote.value,
                    updatedAt = System.currentTimeMillis()
                )
                repository.update(updatedRecord)
                _record.value = updatedRecord
                hideEditDialog()
            } catch (e: Exception) {
                _operationError.value = e.message ?: "保存失败"
            } finally {
                _isOperating.value = false
            }
        }
    }

    // 删除操作
    fun showDeleteConfirm() {
        _showDeleteDialog.value = true
    }

    fun hideDeleteConfirm() {
        _showDeleteDialog.value = false
    }

    fun confirmDelete(onComplete: () -> Unit) {
        viewModelScope.launch {
            _isOperating.value = true
            try {
                _record.value?.let { record ->
                    repository.delete(record)
                    hideDeleteConfirm()
                    onComplete()
                }
            } catch (e: Exception) {
                _operationError.value = e.message ?: "删除失败"
            } finally {
                _isOperating.value = false
            }
        }
    }

    fun formatDate(epochDay: Int): String {
        return try {
            val date = LocalDate.ofEpochDay(epochDay.toLong())
            date.format(DateTimeFormatter.ofPattern("yyyy年M月d日"))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatWeekday(epochDay: Int): String {
        return try {
            val date = LocalDate.ofEpochDay(epochDay.toLong())
            val weekdays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            weekdays[date.dayOfWeek.value - 1]
        } catch (e: Exception) {
            ""
        }
    }
}

/**
 * UI状态
 */
sealed class HealthRecordDetailUiState {
    object Loading : HealthRecordDetailUiState()
    object Success : HealthRecordDetailUiState()
    data class Error(val message: String) : HealthRecordDetailUiState()
}
