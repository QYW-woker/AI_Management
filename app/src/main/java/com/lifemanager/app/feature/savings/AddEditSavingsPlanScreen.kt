package com.lifemanager.app.feature.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import com.lifemanager.app.ui.component.*
import com.lifemanager.app.ui.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * 新建/编辑存钱计划页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSavingsPlanScreen(
    planId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: SavingsPlanViewModel = hiltViewModel()
) {
    val isEditing = planId != null && planId > 0

    // 表单状态
    var name by remember { mutableStateOf("") }
    var targetAmount by remember { mutableStateOf("") }
    var targetDate by remember { mutableStateOf<LocalDate?>(null) }
    var strategy by remember { mutableStateOf("FIXED") }
    var selectedColor by remember { mutableStateOf("#2196F3") }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStrategyDropdown by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // 加载现有计划数据（编辑模式）
    LaunchedEffect(planId) {
        if (isEditing && planId != null) {
            viewModel.getPlanById(planId).collect { planWithDetails ->
                planWithDetails?.let {
                    val plan = it.plan
                    name = plan.name
                    targetAmount = plan.targetAmount.toString()
                    selectedColor = plan.color
                    strategy = plan.strategy

                    val year = plan.targetDate / 10000
                    val month = (plan.targetDate % 10000) / 100
                    val day = plan.targetDate % 100
                    targetDate = LocalDate.of(year, month, day)
                }
            }
        }
    }

    // 策略选项
    val strategies = listOf(
        "FIXED" to "固定金额",
        "PERCENTAGE" to "收入百分比",
        "FLEXIBLE" to "灵活存款"
    )

    // 颜色选项
    val colorOptions = listOf(
        "#2196F3", "#4CAF50", "#FF9800", "#E91E63",
        "#9C27B0", "#00BCD4", "#FF5722", "#795548",
        "#607D8B", "#3F51B5", "#FFC107", "#8BC34A"
    )

    Scaffold(
        topBar = {
            UnifiedTopAppBar(
                title = if (isEditing) "编辑计划" else "新建计划",
                onNavigateBack = onNavigateBack,
                actions = {
                    TextButton(
                        onClick = {
                            // 验证
                            if (name.isBlank()) {
                                error = "请输入计划名称"
                                return@TextButton
                            }
                            val amount = targetAmount.toDoubleOrNull()
                            if (amount == null || amount <= 0) {
                                error = "请输入有效的目标金额"
                                return@TextButton
                            }
                            if (targetDate == null) {
                                error = "请选择目标日期"
                                return@TextButton
                            }

                            isLoading = true
                            error = null

                            val dateInt = targetDate!!.let {
                                it.year * 10000 + it.monthValue * 100 + it.dayOfMonth
                            }

                            if (isEditing && planId != null) {
                                viewModel.updatePlan(
                                    id = planId,
                                    name = name,
                                    targetAmount = amount,
                                    targetDate = dateInt,
                                    strategy = strategy,
                                    color = selectedColor
                                )
                            } else {
                                viewModel.createPlan(
                                    name = name,
                                    targetAmount = amount,
                                    targetDate = dateInt,
                                    strategy = strategy,
                                    color = selectedColor
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

            // 计划名称
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("计划名称 *") },
                placeholder = { Text("例如：旅行基金") },
                singleLine = true,
                shape = AppShapes.Medium
            )

            // 目标金额
            OutlinedTextField(
                value = targetAmount,
                onValueChange = { targetAmount = it.filter { c -> c.isDigit() || c == '.' } },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("目标金额 *") },
                placeholder = { Text("例如：10000") },
                prefix = { Text("¥") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = AppShapes.Medium
            )

            // 目标日期
            OutlinedTextField(
                value = targetDate?.format(DateTimeFormatter.ofPattern("yyyy年MM月dd日")) ?: "",
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showDatePicker = true },
                label = { Text("目标日期 *") },
                placeholder = { Text("选择目标日期") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarMonth, contentDescription = "选择日期")
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

            // 存款策略
            ExposedDropdownMenuBox(
                expanded = showStrategyDropdown,
                onExpandedChange = { showStrategyDropdown = it }
            ) {
                OutlinedTextField(
                    value = strategies.find { it.first == strategy }?.second ?: "",
                    onValueChange = {},
                    readOnly = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    label = { Text("存款策略") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = showStrategyDropdown) },
                    shape = AppShapes.Medium
                )
                ExposedDropdownMenu(
                    expanded = showStrategyDropdown,
                    onDismissRequest = { showStrategyDropdown = false }
                ) {
                    strategies.forEach { (value, label) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = {
                                strategy = value
                                showStrategyDropdown = false
                            }
                        )
                    }
                }
            }

            // 颜色选择
            SectionTitle(title = "选择颜色", centered = false)

            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingSmall),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingSmall)
            ) {
                items(colorOptions) { colorHex ->
                    val color = try {
                        Color(android.graphics.Color.parseColor(colorHex))
                    } catch (e: Exception) {
                        MaterialTheme.colorScheme.primary
                    }
                    val isSelected = selectedColor == colorHex

                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(color)
                            .then(
                                if (isSelected) {
                                    Modifier.border(3.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                } else {
                                    Modifier
                                }
                            )
                            .clickable { selectedColor = colorHex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingLarge))
        }
    }

    // 日期选择器
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = targetDate?.toEpochDay()?.times(86400000)
                ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            targetDate = LocalDate.ofEpochDay(it / 86400000)
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
