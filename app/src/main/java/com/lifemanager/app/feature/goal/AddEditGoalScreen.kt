package com.lifemanager.app.feature.goal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 新建/编辑目标页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditGoalScreen(
    goalId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: GoalViewModel = hiltViewModel()
) {
    val isEditing = goalId != null && goalId > 0

    // 表单状态
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("FINANCE") }
    var goalType by remember { mutableStateOf("SAVINGS") }
    var targetValue by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("元") }
    var progressType by remember { mutableStateOf("NUMERIC") }
    var deadline by remember { mutableStateOf<LocalDate?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showTypeDropdown by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // 加载现有目标数据（编辑模式）
    LaunchedEffect(goalId) {
        if (isEditing && goalId != null) {
            viewModel.getGoalById(goalId).collect { goal ->
                goal?.let {
                    title = it.title
                    description = it.description
                    category = it.category
                    goalType = it.goalType
                    targetValue = it.targetValue?.toString() ?: ""
                    unit = it.unit
                    progressType = it.progressType
                    it.deadline?.let { d ->
                        val year = d / 10000
                        val month = (d % 10000) / 100
                        val day = d % 100
                        deadline = LocalDate.of(year, month, day)
                    }
                }
            }
        }
    }

    val categories = listOf(
        "CAREER" to "职业发展",
        "FINANCE" to "财务目标",
        "HEALTH" to "健康运动",
        "LEARNING" to "学习成长",
        "RELATIONSHIP" to "人际关系",
        "LIFESTYLE" to "生活方式",
        "HOBBY" to "兴趣爱好",
        "OTHER" to "其他"
    )

    val goalTypes = listOf(
        "SAVINGS" to "存钱目标",
        "HABIT" to "习惯养成",
        "ACHIEVEMENT" to "成就达成",
        "LEARNING" to "学习目标",
        "FITNESS" to "健身目标",
        "OTHER" to "其他目标"
    )

    Scaffold(
        topBar = {
            UnifiedTopAppBar(
                title = if (isEditing) "编辑目标" else "新建目标",
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isBlank()) {
                                error = "请输入目标名称"
                                return@TextButton
                            }
                            isLoading = true
                            error = null

                            val deadlineInt = deadline?.let {
                                it.year * 10000 + it.monthValue * 100 + it.dayOfMonth
                            }

                            if (isEditing && goalId != null) {
                                viewModel.updateGoal(
                                    id = goalId,
                                    title = title,
                                    description = description,
                                    category = category,
                                    goalType = goalType,
                                    targetValue = targetValue.toDoubleOrNull(),
                                    unit = unit,
                                    progressType = progressType,
                                    deadline = deadlineInt
                                )
                            } else {
                                viewModel.createGoal(
                                    title = title,
                                    description = description,
                                    category = category,
                                    goalType = goalType,
                                    targetValue = targetValue.toDoubleOrNull(),
                                    unit = unit,
                                    progressType = progressType,
                                    deadline = deadlineInt
                                )
                            }
                            onNavigateBack()
                        },
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text("保存", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(AppDimens.PageHorizontalPadding),
            verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingNormal)
        ) {
            // 错误提示
            error?.let {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    ),
                    shape = AppShapes.Medium
                ) {
                    Row(
                        modifier = Modifier.padding(AppDimens.SpacingMedium),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Error,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(AppDimens.SpacingSmall))
                        Text(
                            text = it,
                            color = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }

            // 目标名称
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("目标名称 *") },
                placeholder = { Text("例如：存款10万元") },
                singleLine = true,
                shape = AppShapes.Medium
            )

            // 目标描述
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("目标描述") },
                placeholder = { Text("描述一下这个目标...") },
                minLines = 2,
                maxLines = 4,
                shape = AppShapes.Medium
            )

            // 分类选择
            ExposedDropdownMenuBox(
                expanded = showCategoryDropdown,
                onExpandedChange = { showCategoryDropdown = it }
            ) {
                OutlinedTextField(
                    value = categories.find { it.first == category }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("分类") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryDropdown) },
                    shape = AppShapes.Medium
                )
                ExposedDropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false }
                ) {
                    categories.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                category = value
                                showCategoryDropdown = false
                            },
                            leadingIcon = {
                                Box(
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clip(CircleShape)
                                        .background(getCategoryColor(value))
                                )
                            }
                        )
                    }
                }
            }

            // 目标类型
            ExposedDropdownMenuBox(
                expanded = showTypeDropdown,
                onExpandedChange = { showTypeDropdown = it }
            ) {
                OutlinedTextField(
                    value = goalTypes.find { it.first == goalType }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("目标类型") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showTypeDropdown) },
                    shape = AppShapes.Medium
                )
                ExposedDropdownMenu(
                    expanded = showTypeDropdown,
                    onDismissRequest = { showTypeDropdown = false }
                ) {
                    goalTypes.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                goalType = value
                                showTypeDropdown = false
                            }
                        )
                    }
                }
            }

            // 目标值和单位
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium)
            ) {
                OutlinedTextField(
                    value = targetValue,
                    onValueChange = { targetValue = it.filter { c -> c.isDigit() || c == '.' } },
                    modifier = Modifier.weight(2f),
                    label = { Text("目标数值") },
                    placeholder = { Text("例如：100000") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = AppShapes.Medium
                )
                OutlinedTextField(
                    value = unit,
                    onValueChange = { unit = it },
                    modifier = Modifier.weight(1f),
                    label = { Text("单位") },
                    placeholder = { Text("元") },
                    singleLine = true,
                    shape = AppShapes.Medium
                )
            }

            // 截止日期
            OutlinedTextField(
                value = deadline?.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                label = { Text("截止日期") },
                placeholder = { Text("选择截止日期（可选）") },
                trailingIcon = {
                    Row {
                        if (deadline != null) {
                            IconButton(onClick = { deadline = null }) {
                                Icon(Icons.Default.Clear, contentDescription = "清除")
                            }
                        }
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
                        }
                    }
                },
                shape = AppShapes.Medium,
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )

            // 进度类型选择
            SectionTitle(title = "进度跟踪方式", centered = false)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium)
            ) {
                FilterChip(
                    selected = progressType == "NUMERIC",
                    onClick = { progressType = "NUMERIC" },
                    label = { Text("数值进度") },
                    leadingIcon = if (progressType == "NUMERIC") {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
                FilterChip(
                    selected = progressType == "PERCENTAGE",
                    onClick = { progressType = "PERCENTAGE" },
                    label = { Text("百分比进度") },
                    leadingIcon = if (progressType == "PERCENTAGE") {
                        { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                    } else null,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingLarge))
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = deadline?.toEpochDay()?.times(86400000)
                ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            deadline = LocalDate.ofEpochDay(it / 86400000)
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

private fun getCategoryColor(category: String): Color {
    return when (category) {
        "CAREER" -> Color(0xFF2196F3)
        "FINANCE" -> Color(0xFF4CAF50)
        "HEALTH" -> Color(0xFFE91E63)
        "LEARNING" -> Color(0xFFFF9800)
        "RELATIONSHIP" -> Color(0xFF9C27B0)
        "LIFESTYLE" -> Color(0xFF00BCD4)
        "HOBBY" -> Color(0xFFFF5722)
        else -> Color.Gray
    }
}
