package com.lifemanager.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.BuildConfig
import com.lifemanager.app.core.data.repository.CurrencySymbol
import com.lifemanager.app.core.data.repository.DateFormat
import com.lifemanager.app.core.data.repository.WeekStartDay
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 设置页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
    onNavigateToPrivacy: () -> Unit = {},
    onNavigateToTerms: () -> Unit = {},
    onNavigateToAISettings: () -> Unit = {},
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val settings by viewModel.settings.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val showTimePicker by viewModel.showTimePicker.collectAsState()
    val showLanguagePicker by viewModel.showLanguagePicker.collectAsState()
    val showClearDataDialog by viewModel.showClearDataDialog.collectAsState()
    val showBackupSuccessDialog by viewModel.showBackupSuccessDialog.collectAsState()
    val showLogoutDialog by viewModel.showLogoutDialog.collectAsState()
    val currentUser by viewModel.currentUser.collectAsState()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val showExportDialog by viewModel.showExportDialog.collectAsState()
    val showExportSuccessDialog by viewModel.showExportSuccessDialog.collectAsState()
    val exportStartDate by viewModel.exportStartDate.collectAsState()
    val exportEndDate by viewModel.exportEndDate.collectAsState()

    // 新增对话框状态
    val showCurrencyPicker by viewModel.showCurrencyPicker.collectAsState()
    val showDateFormatPicker by viewModel.showDateFormatPicker.collectAsState()
    val showWeekStartPicker by viewModel.showWeekStartPicker.collectAsState()
    val showDecimalPlacesPicker by viewModel.showDecimalPlacesPicker.collectAsState()
    val showHomeCardSettings by viewModel.showHomeCardSettings.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // 处理UI状态变化
    LaunchedEffect(uiState) {
        when (val state = uiState) {
            is SettingsUiState.Success -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearUiState()
            }
            is SettingsUiState.Error -> {
                snackbarHostState.showSnackbar(state.message)
                viewModel.clearUiState()
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("设置") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 外观设置
            item {
                SettingsSection(title = "外观") {
                    SwitchSettingItem(
                        icon = Icons.Outlined.DarkMode,
                        title = "深色模式",
                        subtitle = "使用深色主题",
                        checked = settings.isDarkMode,
                        onCheckedChange = { viewModel.toggleDarkMode(it) }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.Language,
                        title = "语言",
                        value = settings.language,
                        onClick = { viewModel.showLanguagePickerDialog() }
                    )
                }
            }

            // 显示格式设置
            item {
                SettingsSection(title = "显示格式") {
                    ClickableSettingItem(
                        icon = Icons.Outlined.AttachMoney,
                        title = "货币符号",
                        value = settings.currencySymbol.displayName,
                        onClick = { viewModel.showCurrencyPickerDialog() }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.Pin,
                        title = "金额小数位",
                        value = "${settings.decimalPlaces}位",
                        onClick = { viewModel.showDecimalPlacesPickerDialog() }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    SwitchSettingItem(
                        icon = Icons.Outlined.FormatListNumbered,
                        title = "千位分隔符",
                        subtitle = "使用逗号分隔大数字 (如 1,000)",
                        checked = settings.useThousandSeparator,
                        onCheckedChange = { viewModel.toggleThousandSeparator(it) }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.CalendarMonth,
                        title = "日期格式",
                        value = settings.dateFormat.displayName,
                        onClick = { viewModel.showDateFormatPickerDialog() }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.DateRange,
                        title = "周起始日",
                        value = settings.weekStartDay.displayName,
                        onClick = { viewModel.showWeekStartPickerDialog() }
                    )
                }
            }

            // 首页布局设置
            item {
                SettingsSection(title = "首页布局") {
                    ClickableSettingItem(
                        icon = Icons.Outlined.Dashboard,
                        title = "自定义首页卡片",
                        value = "显示/隐藏卡片",
                        onClick = { viewModel.showHomeCardSettingsDialog() }
                    )
                }
            }

            // 通知设置
            item {
                SettingsSection(title = "通知") {
                    SwitchSettingItem(
                        icon = Icons.Outlined.Notifications,
                        title = "开启通知",
                        subtitle = "接收提醒和通知",
                        checked = settings.enableNotification,
                        onCheckedChange = { viewModel.toggleNotification(it) }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.Schedule,
                        title = "每日提醒时间",
                        value = settings.reminderTime,
                        enabled = settings.enableNotification,
                        onClick = { viewModel.showTimePickerDialog() }
                    )
                }
            }

            // AI功能设置
            item {
                SettingsSection(title = "AI功能") {
                    ClickableSettingItem(
                        icon = Icons.Filled.SmartToy,
                        title = "AI设置",
                        value = "",
                        onClick = onNavigateToAISettings
                    )
                }
            }

            // 数据设置
            item {
                SettingsSection(title = "数据") {
                    SwitchSettingItem(
                        icon = Icons.Outlined.CloudSync,
                        title = "自动备份",
                        subtitle = "定期备份数据到云端",
                        checked = settings.autoBackup,
                        onCheckedChange = { viewModel.toggleAutoBackup(it) }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.CloudUpload,
                        title = "立即备份",
                        value = "",
                        onClick = { viewModel.backupNow() }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.CloudDownload,
                        title = "恢复数据",
                        value = "",
                        onClick = { viewModel.restoreData() }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.FileDownload,
                        title = "导出记账数据",
                        value = "",
                        onClick = { viewModel.showExportDialog() }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.Delete,
                        title = "清除所有数据",
                        value = "",
                        isDanger = true,
                        onClick = { viewModel.showClearDataConfirmation() }
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    ClickableSettingItem(
                        icon = Icons.Outlined.Info,
                        title = "版本",
                        value = BuildConfig.VERSION_NAME,
                        onClick = { }
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.Description,
                        title = "隐私政策",
                        value = "",
                        onClick = onNavigateToPrivacy
                    )
                    Divider(modifier = Modifier.padding(start = 56.dp))
                    ClickableSettingItem(
                        icon = Icons.Outlined.Gavel,
                        title = "用户协议",
                        value = "",
                        onClick = onNavigateToTerms
                    )
                }
            }

            // 账户
            item {
                SettingsSection(title = "账户") {
                    if (isLoggedIn && currentUser != null) {
                        // 已登录 - 显示用户信息和退出按钮
                        ClickableSettingItem(
                            icon = Icons.Outlined.Person,
                            title = currentUser?.nickname ?: currentUser?.username ?: "用户",
                            value = currentUser?.email ?: "",
                            onClick = { }
                        )
                        Divider(modifier = Modifier.padding(start = 56.dp))
                        ClickableSettingItem(
                            icon = Icons.Outlined.Logout,
                            title = "退出登录",
                            value = "",
                            isDanger = true,
                            onClick = { viewModel.showLogoutConfirmation() }
                        )
                    } else {
                        // 未登录 - 显示登录按钮
                        ClickableSettingItem(
                            icon = Icons.Outlined.Login,
                            title = "登录/注册",
                            value = "",
                            onClick = onNavigateToLogin
                        )
                    }
                }
            }
        }
    }

    // 语言选择对话框
    if (showLanguagePicker) {
        LanguagePickerDialog(
            currentLanguage = settings.language,
            onSelect = { viewModel.setLanguage(it) },
            onDismiss = { viewModel.hideLanguagePickerDialog() }
        )
    }

    // 时间选择对话框
    if (showTimePicker) {
        TimePickerDialog(
            currentTime = settings.reminderTime,
            onConfirm = { viewModel.setReminderTime(it) },
            onDismiss = { viewModel.hideTimePickerDialog() }
        )
    }

    // 清除数据确认对话框
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideClearDataConfirmation() },
            icon = { Icon(Icons.Default.Warning, contentDescription = null) },
            title = { Text("清除所有数据") },
            text = { Text("确定要清除所有数据吗？此操作不可撤销。") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.clearAllData() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("确定清除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideClearDataConfirmation() }) {
                    Text("取消")
                }
            }
        )
    }

    // 备份成功对话框
    showBackupSuccessDialog?.let { backupPath ->
        AlertDialog(
            onDismissRequest = { viewModel.hideBackupSuccessDialog() },
            icon = { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("备份成功") },
            text = { Text("数据已备份到:\n$backupPath") },
            confirmButton = {
                TextButton(onClick = { viewModel.hideBackupSuccessDialog() }) {
                    Text("确定")
                }
            }
        )
    }

    // 加载指示器
    if (uiState is SettingsUiState.Loading) {
        AlertDialog(
            onDismissRequest = { },
            title = { Text((uiState as SettingsUiState.Loading).message) },
            text = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            },
            confirmButton = { }
        )
    }

    // 退出登录确认对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideLogoutConfirmation() },
            icon = { Icon(Icons.Outlined.Logout, contentDescription = null) },
            title = { Text("退出登录") },
            text = { Text("确定要退出当前账号吗？") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmLogout() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("退出")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideLogoutConfirmation() }) {
                    Text("取消")
                }
            }
        )
    }

    // 数据导出对话框
    if (showExportDialog) {
        ExportDataDialog(
            startDate = exportStartDate,
            endDate = exportEndDate,
            onStartDateChange = { viewModel.setExportStartDate(it) },
            onEndDateChange = { viewModel.setExportEndDate(it) },
            onConfirm = { viewModel.exportFinanceData() },
            onDismiss = { viewModel.hideExportDialog() }
        )
    }

    // 导出成功对话框
    showExportSuccessDialog?.let { exportPath ->
        AlertDialog(
            onDismissRequest = { viewModel.hideExportSuccessDialog() },
            icon = { Icon(Icons.Default.Check, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("导出成功") },
            text = { Text("数据已导出到:\n$exportPath") },
            confirmButton = {
                TextButton(onClick = { viewModel.hideExportSuccessDialog() }) {
                    Text("确定")
                }
            }
        )
    }

    // 货币符号选择对话框
    if (showCurrencyPicker) {
        CurrencyPickerDialog(
            currentSymbol = settings.currencySymbol,
            onSelect = { viewModel.setCurrencySymbol(it) },
            onDismiss = { viewModel.hideCurrencyPickerDialog() }
        )
    }

    // 日期格式选择对话框
    if (showDateFormatPicker) {
        DateFormatPickerDialog(
            currentFormat = settings.dateFormat,
            onSelect = { viewModel.setDateFormat(it) },
            onDismiss = { viewModel.hideDateFormatPickerDialog() }
        )
    }

    // 周起始日选择对话框
    if (showWeekStartPicker) {
        WeekStartPickerDialog(
            currentDay = settings.weekStartDay,
            onSelect = { viewModel.setWeekStartDay(it) },
            onDismiss = { viewModel.hideWeekStartPickerDialog() }
        )
    }

    // 小数位数选择对话框
    if (showDecimalPlacesPicker) {
        DecimalPlacesPickerDialog(
            currentPlaces = settings.decimalPlaces,
            onSelect = { viewModel.setDecimalPlaces(it) },
            onDismiss = { viewModel.hideDecimalPlacesPickerDialog() }
        )
    }

    // 首页卡片设置对话框
    if (showHomeCardSettings) {
        HomeCardSettingsDialog(
            config = settings.homeCardConfig,
            onCardVisibilityChange = { key, visible -> viewModel.setHomeCardVisibility(key, visible) },
            onReset = { viewModel.resetHomeCardConfig() },
            onDismiss = { viewModel.hideHomeCardSettingsDialog() }
        )
    }
}

/**
 * 设置分组
 */
@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
            content()
        }
    }
}

/**
 * 开关设置项
 */
@Composable
private fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * 可点击设置项
 */
@Composable
private fun ClickableSettingItem(
    icon: ImageVector,
    title: String,
    value: String,
    enabled: Boolean = true,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isDanger) {
                MaterialTheme.colorScheme.error
            } else if (enabled) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            color = if (isDanger) {
                MaterialTheme.colorScheme.error
            } else if (enabled) {
                MaterialTheme.colorScheme.onSurface
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            },
            modifier = Modifier.weight(1f)
        )
        if (value.isNotEmpty()) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (enabled) 1f else 0.5f
            )
        )
    }
}

/**
 * 语言选择对话框
 */
@Composable
private fun LanguagePickerDialog(
    currentLanguage: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val languages = listOf("简体中文", "English")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择语言") },
        text = {
            Column {
                languages.forEach { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(language) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = language == currentLanguage,
                            onClick = { onSelect(language) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = language)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 时间选择对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    currentTime: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val parts = currentTime.split(":")
    val initialHour = parts.getOrNull(0)?.toIntOrNull() ?: 9
    val initialMinute = parts.getOrNull(1)?.toIntOrNull() ?: 0

    val timePickerState = rememberTimePickerState(
        initialHour = initialHour,
        initialMinute = initialMinute,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择提醒时间") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hour = String.format("%02d", timePickerState.hour)
                    val minute = String.format("%02d", timePickerState.minute)
                    onConfirm("$hour:$minute")
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 数据导出对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportDataDialog(
    startDate: LocalDate,
    endDate: LocalDate,
    onStartDateChange: (LocalDate) -> Unit,
    onEndDateChange: (LocalDate) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    val dateFormatter = remember { DateTimeFormatter.ofPattern("yyyy-MM-dd") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(Icons.Outlined.FileDownload, contentDescription = null) },
        title = { Text("导出记账数据") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "选择导出的日期范围，数据将导出为CSV格式",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 开始日期
                OutlinedCard(
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "开始日期",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = startDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 结束日期
                OutlinedCard(
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "结束日期",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = endDate.format(dateFormatter),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // 快捷选项
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {
                            val now = LocalDate.now()
                            onStartDateChange(now.withDayOfMonth(1))
                            onEndDateChange(now)
                        },
                        label = { Text("本月") }
                    )
                    AssistChip(
                        onClick = {
                            val now = LocalDate.now()
                            onStartDateChange(now.minusMonths(3))
                            onEndDateChange(now)
                        },
                        label = { Text("近3月") }
                    )
                    AssistChip(
                        onClick = {
                            val now = LocalDate.now()
                            onStartDateChange(now.withDayOfYear(1))
                            onEndDateChange(now)
                        },
                        label = { Text("今年") }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("导出")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 开始日期选择器
    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = startDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            onStartDateChange(selectedDate)
                        }
                        showStartDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // 结束日期选择器
    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = endDate.toEpochDay() * 24 * 60 * 60 * 1000
        )
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            onEndDateChange(selectedDate)
                        }
                        showEndDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 货币符号选择对话框
 */
@Composable
private fun CurrencyPickerDialog(
    currentSymbol: CurrencySymbol,
    onSelect: (CurrencySymbol) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择货币符号") },
        text = {
            Column {
                CurrencySymbol.entries.forEach { symbol ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(symbol) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = symbol == currentSymbol,
                            onClick = { onSelect(symbol) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = symbol.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 日期格式选择对话框
 */
@Composable
private fun DateFormatPickerDialog(
    currentFormat: DateFormat,
    onSelect: (DateFormat) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择日期格式") },
        text = {
            Column {
                DateFormat.entries.forEach { format ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(format) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = format == currentFormat,
                            onClick = { onSelect(format) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = format.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 周起始日选择对话框
 */
@Composable
private fun WeekStartPickerDialog(
    currentDay: WeekStartDay,
    onSelect: (WeekStartDay) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择周起始日") },
        text = {
            Column {
                WeekStartDay.entries.forEach { day ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(day) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = day == currentDay,
                            onClick = { onSelect(day) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = day.displayName)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 小数位数选择对话框
 */
@Composable
private fun DecimalPlacesPickerDialog(
    currentPlaces: Int,
    onSelect: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    val options = listOf(0, 1, 2, 3, 4)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择小数位数") },
        text = {
            Column {
                options.forEach { places ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(places) }
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = places == currentPlaces,
                            onClick = { onSelect(places) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${places}位小数")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * 首页卡片设置对话框
 */
@Composable
private fun HomeCardSettingsDialog(
    config: com.lifemanager.app.core.data.repository.HomeCardConfig,
    onCardVisibilityChange: (String, Boolean) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    val cardNames = mapOf(
        "todayStats" to "今日统计",
        "monthlyFinance" to "月度财务",
        "topGoals" to "目标进度",
        "habitProgress" to "习惯打卡",
        "aiInsight" to "AI 洞察",
        "quickActions" to "快捷操作"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("自定义首页卡片") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "选择要在首页显示的卡片",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // 今日统计
                SwitchSettingRow(
                    title = cardNames["todayStats"] ?: "",
                    checked = config.showTodayStats,
                    onCheckedChange = { onCardVisibilityChange("todayStats", it) }
                )

                // 月度财务
                SwitchSettingRow(
                    title = cardNames["monthlyFinance"] ?: "",
                    checked = config.showMonthlyFinance,
                    onCheckedChange = { onCardVisibilityChange("monthlyFinance", it) }
                )

                // 目标进度
                SwitchSettingRow(
                    title = cardNames["topGoals"] ?: "",
                    checked = config.showTopGoals,
                    onCheckedChange = { onCardVisibilityChange("topGoals", it) }
                )

                // 习惯打卡
                SwitchSettingRow(
                    title = cardNames["habitProgress"] ?: "",
                    checked = config.showHabitProgress,
                    onCheckedChange = { onCardVisibilityChange("habitProgress", it) }
                )

                // AI 洞察
                SwitchSettingRow(
                    title = cardNames["aiInsight"] ?: "",
                    checked = config.showAIInsight,
                    onCheckedChange = { onCardVisibilityChange("aiInsight", it) }
                )

                // 快捷操作
                SwitchSettingRow(
                    title = cardNames["quickActions"] ?: "",
                    checked = config.showQuickActions,
                    onCheckedChange = { onCardVisibilityChange("quickActions", it) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("完成")
            }
        },
        dismissButton = {
            TextButton(onClick = onReset) {
                Text("重置")
            }
        }
    )
}

/**
 * 简单的开关行
 */
@Composable
private fun SwitchSettingRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title)
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
