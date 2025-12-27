package com.lifemanager.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 设置项
 */
data class SettingsState(
    val isDarkMode: Boolean = false,
    val enableNotification: Boolean = true,
    val reminderTime: String = "09:00",
    val autoBackup: Boolean = false,
    val language: String = "简体中文"
)

/**
 * 设置ViewModel
 */
@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {

    private val _settings = MutableStateFlow(SettingsState())
    val settings: StateFlow<SettingsState> = _settings.asStateFlow()

    // 显示时间选择器
    private val _showTimePicker = MutableStateFlow(false)
    val showTimePicker: StateFlow<Boolean> = _showTimePicker.asStateFlow()

    // 显示语言选择
    private val _showLanguagePicker = MutableStateFlow(false)
    val showLanguagePicker: StateFlow<Boolean> = _showLanguagePicker.asStateFlow()

    // 显示清除数据确认
    private val _showClearDataDialog = MutableStateFlow(false)
    val showClearDataDialog: StateFlow<Boolean> = _showClearDataDialog.asStateFlow()

    /**
     * 切换深色模式
     */
    fun toggleDarkMode(enabled: Boolean) {
        _settings.value = _settings.value.copy(isDarkMode = enabled)
    }

    /**
     * 切换通知开关
     */
    fun toggleNotification(enabled: Boolean) {
        _settings.value = _settings.value.copy(enableNotification = enabled)
    }

    /**
     * 切换自动备份
     */
    fun toggleAutoBackup(enabled: Boolean) {
        _settings.value = _settings.value.copy(autoBackup = enabled)
    }

    /**
     * 显示时间选择器
     */
    fun showTimePickerDialog() {
        _showTimePicker.value = true
    }

    fun hideTimePickerDialog() {
        _showTimePicker.value = false
    }

    /**
     * 设置提醒时间
     */
    fun setReminderTime(time: String) {
        _settings.value = _settings.value.copy(reminderTime = time)
        hideTimePickerDialog()
    }

    /**
     * 显示语言选择
     */
    fun showLanguagePickerDialog() {
        _showLanguagePicker.value = true
    }

    fun hideLanguagePickerDialog() {
        _showLanguagePicker.value = false
    }

    /**
     * 设置语言
     */
    fun setLanguage(language: String) {
        _settings.value = _settings.value.copy(language = language)
        hideLanguagePickerDialog()
    }

    /**
     * 显示清除数据确认
     */
    fun showClearDataConfirmation() {
        _showClearDataDialog.value = true
    }

    fun hideClearDataConfirmation() {
        _showClearDataDialog.value = false
    }

    /**
     * 清除所有数据
     */
    fun clearAllData() {
        viewModelScope.launch {
            // TODO: 实现数据清除逻辑
            hideClearDataConfirmation()
        }
    }
}
