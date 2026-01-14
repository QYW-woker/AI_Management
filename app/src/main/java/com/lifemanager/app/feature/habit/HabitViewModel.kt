package com.lifemanager.app.feature.habit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lifemanager.app.core.ai.service.AIDataAnalysisService
import com.lifemanager.app.core.database.entity.AIAnalysisEntity
import com.lifemanager.app.core.database.entity.HabitEntity
import com.lifemanager.app.domain.model.HabitAchievement
import com.lifemanager.app.domain.model.HabitEditState
import com.lifemanager.app.domain.model.HabitRankItem
import com.lifemanager.app.domain.model.HabitStats
import com.lifemanager.app.domain.model.HabitUiState
import com.lifemanager.app.domain.model.HabitWithStatus
import com.lifemanager.app.domain.model.MonthlyHabitStats
import com.lifemanager.app.domain.model.RetroCheckinState
import com.lifemanager.app.domain.model.WeeklyHabitStats
import com.lifemanager.app.domain.model.getMotivationalMessage
import com.lifemanager.app.domain.usecase.HabitUseCase
import java.time.YearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 习惯打卡ViewModel
 */
@HiltViewModel
class HabitViewModel @Inject constructor(
    private val habitUseCase: HabitUseCase,
    private val aiAnalysisService: AIDataAnalysisService
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow<HabitUiState>(HabitUiState.Loading)
    val uiState: StateFlow<HabitUiState> = _uiState.asStateFlow()

    // 习惯列表（带状态）
    private val _habits = MutableStateFlow<List<HabitWithStatus>>(emptyList())
    val habits: StateFlow<List<HabitWithStatus>> = _habits.asStateFlow()

    // 习惯统计
    private val _stats = MutableStateFlow(HabitStats())
    val stats: StateFlow<HabitStats> = _stats.asStateFlow()

    // 编辑状态
    private val _editState = MutableStateFlow(HabitEditState())
    val editState: StateFlow<HabitEditState> = _editState.asStateFlow()

    // 对话框状态
    private val _showEditDialog = MutableStateFlow(false)
    val showEditDialog: StateFlow<Boolean> = _showEditDialog.asStateFlow()

    private val _showDeleteDialog = MutableStateFlow(false)
    val showDeleteDialog: StateFlow<Boolean> = _showDeleteDialog.asStateFlow()

    private var habitToDelete: Long? = null

    // AI分析状态
    private val _habitAnalysis = MutableStateFlow<AIAnalysisEntity?>(null)
    val habitAnalysis: StateFlow<AIAnalysisEntity?> = _habitAnalysis.asStateFlow()

    private val _isAnalyzing = MutableStateFlow(false)
    val isAnalyzing: StateFlow<Boolean> = _isAnalyzing.asStateFlow()

    // 成就徽章
    private val _achievements = MutableStateFlow<List<HabitAchievement>>(emptyList())
    val achievements: StateFlow<List<HabitAchievement>> = _achievements.asStateFlow()

    // 周度统计
    private val _weeklyStats = MutableStateFlow<WeeklyHabitStats?>(null)
    val weeklyStats: StateFlow<WeeklyHabitStats?> = _weeklyStats.asStateFlow()

    // 月度统计
    private val _monthlyStats = MutableStateFlow<MonthlyHabitStats?>(null)
    val monthlyStats: StateFlow<MonthlyHabitStats?> = _monthlyStats.asStateFlow()

    // 习惯排行
    private val _habitRanking = MutableStateFlow<List<HabitRankItem>>(emptyList())
    val habitRanking: StateFlow<List<HabitRankItem>> = _habitRanking.asStateFlow()

    // 激励语
    private val _motivationalMessage = MutableStateFlow("")
    val motivationalMessage: StateFlow<String> = _motivationalMessage.asStateFlow()

    // 当前月份（用于日历和统计）
    private val _currentYearMonth = MutableStateFlow(
        YearMonth.now().let { it.year * 100 + it.monthValue }
    )
    val currentYearMonth: StateFlow<Int> = _currentYearMonth.asStateFlow()

    // 补打卡状态
    private val _retroCheckinState = MutableStateFlow(RetroCheckinState())
    val retroCheckinState: StateFlow<RetroCheckinState> = _retroCheckinState.asStateFlow()

    init {
        loadHabits()
        loadAIAnalysis()
        loadEnhancedData()
    }

    /**
     * 加载习惯列表
     */
    private fun loadHabits() {
        viewModelScope.launch {
            _uiState.value = HabitUiState.Loading
            habitUseCase.getHabitsWithStatus()
                .catch { e ->
                    _uiState.value = HabitUiState.Error(e.message ?: "加载失败")
                }
                .collect { habits ->
                    _habits.value = habits
                    _uiState.value = HabitUiState.Success()
                    loadStats()
                }
        }
    }

    /**
     * 加载统计数据
     */
    private fun loadStats() {
        viewModelScope.launch {
            try {
                _stats.value = habitUseCase.getHabitStats()
            } catch (e: Exception) {
                // 统计加载失败不影响主界面
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadHabits()
    }

    /**
     * 打卡/取消打卡
     */
    fun toggleCheckIn(habitId: Long) {
        viewModelScope.launch {
            try {
                habitUseCase.toggleCheckIn(habitId)
                loadStats()
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "操作失败")
            }
        }
    }

    /**
     * 更新数值型习惯的值
     */
    fun updateNumericValue(habitId: Long, value: Double) {
        viewModelScope.launch {
            try {
                habitUseCase.updateNumericValue(habitId, value)
                loadStats()
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "操作失败")
            }
        }
    }

    /**
     * 显示添加对话框
     */
    fun showAddDialog() {
        _editState.value = HabitEditState()
        _showEditDialog.value = true
    }

    /**
     * 显示编辑对话框
     */
    fun showEditDialog(habitId: Long) {
        viewModelScope.launch {
            val habit = habitUseCase.getHabitById(habitId)
            if (habit != null) {
                _editState.value = HabitEditState(
                    id = habit.id,
                    name = habit.name,
                    description = habit.description,
                    iconName = habit.iconName,
                    color = habit.color,
                    frequency = habit.frequency,
                    targetTimes = habit.targetTimes,
                    reminderTime = habit.reminderTime,
                    isNumeric = habit.isNumeric,
                    targetValue = habit.targetValue,
                    unit = habit.unit,
                    isEditing = true
                )
                _showEditDialog.value = true
            }
        }
    }

    /**
     * 隐藏编辑对话框
     */
    fun hideEditDialog() {
        _showEditDialog.value = false
        _editState.value = HabitEditState()
    }

    /**
     * 更新编辑状态
     */
    fun updateEditName(name: String) {
        _editState.value = _editState.value.copy(name = name, error = null)
    }

    fun updateEditDescription(description: String) {
        _editState.value = _editState.value.copy(description = description)
    }

    fun updateEditIcon(iconName: String) {
        _editState.value = _editState.value.copy(iconName = iconName)
    }

    fun updateEditColor(color: String) {
        _editState.value = _editState.value.copy(color = color)
    }

    fun updateEditFrequency(frequency: String) {
        _editState.value = _editState.value.copy(frequency = frequency)
    }

    fun updateEditTargetTimes(times: Int) {
        _editState.value = _editState.value.copy(targetTimes = times)
    }

    fun updateEditIsNumeric(isNumeric: Boolean) {
        _editState.value = _editState.value.copy(isNumeric = isNumeric)
    }

    fun updateEditTargetValue(value: Double?) {
        _editState.value = _editState.value.copy(targetValue = value)
    }

    fun updateEditUnit(unit: String) {
        _editState.value = _editState.value.copy(unit = unit)
    }

    /**
     * 保存习惯
     */
    fun saveHabit() {
        val state = _editState.value

        // 验证
        if (state.name.isBlank()) {
            _editState.value = state.copy(error = "请输入习惯名称")
            return
        }

        viewModelScope.launch {
            _editState.value = state.copy(isSaving = true, error = null)
            try {
                val habit = HabitEntity(
                    id = if (state.isEditing) state.id else 0,
                    name = state.name.trim(),
                    description = state.description.trim(),
                    iconName = state.iconName,
                    color = state.color,
                    frequency = state.frequency,
                    targetTimes = state.targetTimes,
                    reminderTime = state.reminderTime,
                    isNumeric = state.isNumeric,
                    targetValue = state.targetValue,
                    unit = state.unit
                )

                if (state.isEditing) {
                    habitUseCase.updateHabit(habit)
                } else {
                    habitUseCase.saveHabit(habit)
                }

                hideEditDialog()
            } catch (e: Exception) {
                _editState.value = _editState.value.copy(
                    isSaving = false,
                    error = e.message ?: "保存失败"
                )
            }
        }
    }

    /**
     * 显示删除确认
     */
    fun showDeleteConfirm(habitId: Long) {
        habitToDelete = habitId
        _showDeleteDialog.value = true
    }

    /**
     * 隐藏删除确认
     */
    fun hideDeleteConfirm() {
        _showDeleteDialog.value = false
        habitToDelete = null
    }

    /**
     * 确认删除
     */
    fun confirmDelete() {
        val id = habitToDelete ?: return
        viewModelScope.launch {
            try {
                habitUseCase.deleteHabit(id)
                hideDeleteConfirm()
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "删除失败")
            }
        }
    }

    /**
     * 暂停习惯
     */
    fun pauseHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                habitUseCase.pauseHabit(habitId)
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "操作失败")
            }
        }
    }

    /**
     * 恢复习惯
     */
    fun resumeHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                habitUseCase.resumeHabit(habitId)
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "操作失败")
            }
        }
    }

    /**
     * 归档习惯
     */
    fun archiveHabit(habitId: Long) {
        viewModelScope.launch {
            try {
                habitUseCase.archiveHabit(habitId)
            } catch (e: Exception) {
                _uiState.value = HabitUiState.Error(e.message ?: "操作失败")
            }
        }
    }

    /**
     * 加载AI分析结果
     */
    private fun loadAIAnalysis() {
        viewModelScope.launch {
            aiAnalysisService.getHabitAnalysis().collectLatest { analyses ->
                _habitAnalysis.value = analyses.firstOrNull()
            }
        }
    }

    /**
     * 刷新AI分析
     */
    fun refreshAIAnalysis() {
        if (_isAnalyzing.value) return

        viewModelScope.launch {
            _isAnalyzing.value = true
            try {
                val result = aiAnalysisService.analyzeHabitData(forceRefresh = true)
                result.onSuccess { analysis ->
                    _habitAnalysis.value = analysis
                }
            } finally {
                _isAnalyzing.value = false
            }
        }
    }

    /**
     * 加载增强数据（成就、统计、排行等）
     */
    private fun loadEnhancedData() {
        viewModelScope.launch {
            try {
                // 加载成就
                _achievements.value = habitUseCase.getAchievements()

                // 加载周度统计
                _weeklyStats.value = habitUseCase.getWeeklyStats()

                // 加载月度统计
                _monthlyStats.value = habitUseCase.getMonthlyStats(_currentYearMonth.value)

                // 加载习惯排行
                _habitRanking.value = habitUseCase.getHabitRanking()

                // 获取最长连续打卡天数并生成激励语
                val longestStreak = habitUseCase.getLongestStreak()
                _motivationalMessage.value = getMotivationalMessage(longestStreak)
            } catch (e: Exception) {
                // 增强数据加载失败不影响主功能
            }
        }
    }

    /**
     * 刷新增强数据
     */
    fun refreshEnhancedData() {
        loadEnhancedData()
    }

    /**
     * 切换到上个月
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
        loadMonthlyData()
    }

    /**
     * 切换到下个月
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
        loadMonthlyData()
    }

    /**
     * 加载月度相关数据
     */
    private fun loadMonthlyData() {
        viewModelScope.launch {
            try {
                _monthlyStats.value = habitUseCase.getMonthlyStats(_currentYearMonth.value)
            } catch (e: Exception) {
                // 忽略错误
            }
        }
    }

    /**
     * 格式化年月显示
     */
    fun formatYearMonth(yearMonth: Int): String {
        val year = yearMonth / 100
        val month = yearMonth % 100
        return "${year}年${month}月"
    }

    /**
     * 显示补打卡对话框
     */
    fun showRetroCheckinDialog(habitId: Long, date: Int) {
        _retroCheckinState.value = RetroCheckinState(
            habitId = habitId,
            selectedDate = date,
            isShowing = true
        )
    }

    /**
     * 隐藏补打卡对话框
     */
    fun hideRetroCheckinDialog() {
        _retroCheckinState.value = RetroCheckinState()
    }

    /**
     * 更新补打卡备注
     */
    fun updateRetroCheckinNote(note: String) {
        _retroCheckinState.value = _retroCheckinState.value.copy(note = note)
    }

    /**
     * 执行补打卡
     */
    fun performRetroCheckin() {
        val state = _retroCheckinState.value
        if (state.habitId == 0L || state.selectedDate == 0) return

        viewModelScope.launch {
            _retroCheckinState.value = state.copy(isSaving = true, error = null)
            try {
                habitUseCase.retroCheckin(state.habitId, state.selectedDate, state.note)
                hideRetroCheckinDialog()
                refresh()
                loadEnhancedData()
            } catch (e: Exception) {
                _retroCheckinState.value = state.copy(
                    isSaving = false,
                    error = e.message ?: "补打卡失败"
                )
            }
        }
    }

    /**
     * 获取已解锁的成就数量
     */
    fun getUnlockedAchievementsCount(): Int {
        return _achievements.value.count { it.isUnlocked }
    }

    /**
     * 获取总成就数量
     */
    fun getTotalAchievementsCount(): Int {
        return _achievements.value.size
    }
}
