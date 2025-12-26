package com.lifemanager.app.ui.component.input

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.lifemanager.app.core.util.DateUtils
import java.time.LocalDate

/**
 * 日期选择器组件
 *
 * 包含文本框显示和日期选择对话框
 *
 * @param selectedDate 当前选中的日期
 * @param onDateSelected 日期选择回调
 * @param label 标签文本
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerField(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    label: String = "日期",
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }

    // 日期选择器状态
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    // 显示日期的文本框
    OutlinedTextField(
        value = DateUtils.formatDate(selectedDate),
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showDatePicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择日期")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showDatePicker = true }
    )

    // 日期选择对话框
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            onDateSelected(newDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 月份选择器组件
 *
 * 用于选择年月（如 2024年12月）
 *
 * @param selectedYearMonth 当前选中的年月（格式YYYYMM）
 * @param onYearMonthSelected 年月选择回调
 * @param label 标签文本
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthPickerField(
    selectedYearMonth: Int,
    onYearMonthSelected: (Int) -> Unit,
    label: String = "月份",
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    // 解析年月
    val year = selectedYearMonth / 100
    val month = selectedYearMonth % 100

    // 显示月份的文本框
    OutlinedTextField(
        value = "${year}年${month}月",
        onValueChange = {},
        label = { Text(label) },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择月份")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    )

    // 月份选择对话框
    if (showPicker) {
        val date = LocalDate.of(year, month, 1)
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.toEpochDay() * 24 * 60 * 60 * 1000,
            initialDisplayMode = DisplayMode.Picker
        )

        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val newDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
                            val newYearMonth = newDate.year * 100 + newDate.monthValue
                            onYearMonthSelected(newYearMonth)
                        }
                        showPicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

/**
 * 日期范围选择器
 *
 * @param startDate 开始日期
 * @param endDate 结束日期
 * @param onRangeSelected 日期范围选择回调
 * @param modifier 修饰符
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateRangePickerField(
    startDate: LocalDate,
    endDate: LocalDate,
    onRangeSelected: (LocalDate, LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    var showPicker by remember { mutableStateOf(false) }

    val dateRangePickerState = rememberDateRangePickerState(
        initialSelectedStartDateMillis = startDate.toEpochDay() * 24 * 60 * 60 * 1000,
        initialSelectedEndDateMillis = endDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    // 显示日期范围的文本框
    OutlinedTextField(
        value = "${DateUtils.formatDate(startDate)} 至 ${DateUtils.formatDate(endDate)}",
        onValueChange = {},
        label = { Text("日期范围") },
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { showPicker = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "选择日期范围")
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .clickable { showPicker = true }
    )

    // 日期范围选择对话框
    if (showPicker) {
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val startMillis = dateRangePickerState.selectedStartDateMillis
                        val endMillis = dateRangePickerState.selectedEndDateMillis
                        if (startMillis != null && endMillis != null) {
                            val newStart = LocalDate.ofEpochDay(startMillis / (24 * 60 * 60 * 1000))
                            val newEnd = LocalDate.ofEpochDay(endMillis / (24 * 60 * 60 * 1000))
                            onRangeSelected(newStart, newEnd)
                        }
                        showPicker = false
                    }
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text("取消")
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState)
        }
    }
}
