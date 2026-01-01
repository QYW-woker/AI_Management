package com.lifemanager.app.feature.habit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.domain.model.frequencyOptions
import com.lifemanager.app.domain.model.habitColors
import com.lifemanager.app.ui.component.*
import com.lifemanager.app.ui.theme.*

/**
 * 习惯编辑页 - 全屏编辑页面
 *
 * 页面职责：
 * - 添加新习惯
 * - 编辑现有习惯
 *
 * 设计原则：
 * - 干净、克制、有呼吸感
 * - 表单分组清晰
 * - 操作反馈明确
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanEditHabitScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    viewModel: EditHabitViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val editState by viewModel.editState.collectAsState()
    val saveResult by viewModel.saveResult.collectAsState()

    // 监听保存结果
    LaunchedEffect(saveResult) {
        when (saveResult) {
            is SaveResult.Success -> onNavigateBack()
            else -> {}
        }
    }

    Scaffold(
        containerColor = CleanColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (viewModel.isEditMode) "编辑习惯" else "添加习惯",
                        style = CleanTypography.title,
                        color = CleanColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = "关闭",
                            tint = CleanColors.textPrimary
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = { viewModel.saveHabit() },
                        enabled = !editState.isSaving
                    ) {
                        if (editState.isSaving) {
                            CleanLoadingIndicator(size = 20.dp)
                        } else {
                            Text(
                                text = "保存",
                                style = CleanTypography.button,
                                color = CleanColors.primary
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.background
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is EditHabitUiState.Loading -> {
                PageLoadingState(modifier = Modifier.padding(paddingValues))
            }

            is EditHabitUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as EditHabitUiState.Error).message,
                            style = CleanTypography.body,
                            color = CleanColors.error
                        )
                        Spacer(modifier = Modifier.height(Spacing.lg))
                        CleanSecondaryButton(
                            text = "返回",
                            onClick = onNavigateBack
                        )
                    }
                }
            }

            is EditHabitUiState.Ready -> {
                EditHabitForm(
                    editState = editState,
                    onUpdateName = viewModel::updateName,
                    onUpdateDescription = viewModel::updateDescription,
                    onUpdateColor = viewModel::updateColor,
                    onUpdateFrequency = viewModel::updateFrequency,
                    onUpdateTargetTimes = viewModel::updateTargetTimes,
                    onUpdateIsNumeric = viewModel::updateIsNumeric,
                    onUpdateTargetValue = viewModel::updateTargetValue,
                    onUpdateUnit = viewModel::updateUnit,
                    modifier = Modifier.padding(paddingValues)
                )
            }
        }
    }
}

@Composable
private fun EditHabitForm(
    editState: HabitFormState,
    onUpdateName: (String) -> Unit,
    onUpdateDescription: (String) -> Unit,
    onUpdateColor: (String) -> Unit,
    onUpdateFrequency: (String) -> Unit,
    onUpdateTargetTimes: (Int) -> Unit,
    onUpdateIsNumeric: (Boolean) -> Unit,
    onUpdateTargetValue: (Double?) -> Unit,
    onUpdateUnit: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.pageHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        // 错误提示
        editState.error?.let { error ->
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    color = CleanColors.error.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(Spacing.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Outlined.Error,
                            contentDescription = null,
                            tint = CleanColors.error,
                            modifier = Modifier.size(IconSize.md)
                        )
                        Spacer(modifier = Modifier.width(Spacing.sm))
                        Text(
                            text = error,
                            style = CleanTypography.secondary,
                            color = CleanColors.error
                        )
                    }
                }
            }
        }

        // 基本信息
        item {
            FormSection(title = "基本信息") {
                // 习惯名称
                CleanTextField(
                    value = editState.name,
                    onValueChange = onUpdateName,
                    label = "习惯名称",
                    placeholder = "如：每天阅读30分钟",
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.lg))

                // 描述
                CleanTextField(
                    value = editState.description,
                    onValueChange = onUpdateDescription,
                    label = "描述（可选）",
                    placeholder = "添加描述说明",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // 颜色选择
        item {
            FormSection(title = "习惯颜色") {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    horizontalArrangement = Arrangement.spacedBy(Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md)
                ) {
                    items(habitColors) { (color, _) ->
                        ColorChip(
                            color = color,
                            selected = editState.color == color,
                            onClick = { onUpdateColor(color) }
                        )
                    }
                }
            }
        }

        // 打卡频率
        item {
            FormSection(title = "打卡频率") {
                Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    frequencyOptions.forEach { (value, label) ->
                        FrequencyOption(
                            label = label,
                            selected = editState.frequency == value,
                            onClick = { onUpdateFrequency(value) }
                        )
                    }
                }

                // 如果选择每周X次或每月X次，显示目标次数输入
                if (editState.frequency == "WEEKLY_TIMES" || editState.frequency == "MONTHLY_TIMES") {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    CleanTextField(
                        value = editState.targetTimes.toString(),
                        onValueChange = { value ->
                            value.toIntOrNull()?.let { onUpdateTargetTimes(it) }
                        },
                        label = if (editState.frequency == "WEEKLY_TIMES") "每周目标次数" else "每月目标次数",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // 数值型习惯
        item {
            FormSection(title = "高级设置") {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Radius.md),
                    color = CleanColors.surface
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(Spacing.lg),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "数值型习惯",
                                style = CleanTypography.body,
                                color = CleanColors.textPrimary
                            )
                            Text(
                                text = "如：每天喝8杯水、走10000步",
                                style = CleanTypography.caption,
                                color = CleanColors.textTertiary
                            )
                        }
                        Switch(
                            checked = editState.isNumeric,
                            onCheckedChange = onUpdateIsNumeric,
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CleanColors.onPrimary,
                                checkedTrackColor = CleanColors.primary,
                                uncheckedThumbColor = CleanColors.textTertiary,
                                uncheckedTrackColor = CleanColors.borderLight
                            )
                        )
                    }
                }

                // 数值型习惯设置
                if (editState.isNumeric) {
                    Spacer(modifier = Modifier.height(Spacing.lg))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.lg)
                    ) {
                        CleanTextField(
                            value = editState.targetValue?.toInt()?.toString() ?: "",
                            onValueChange = { value ->
                                onUpdateTargetValue(value.toDoubleOrNull())
                            },
                            label = "目标值",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        CleanTextField(
                            value = editState.unit,
                            onValueChange = onUpdateUnit,
                            label = "单位",
                            placeholder = "如：杯、步",
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        // 底部安全间距
        item {
            Spacer(modifier = Modifier.height(Spacing.bottomSafe))
        }
    }
}

@Composable
private fun FormSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = title,
                style = CleanTypography.title,
                color = CleanColors.textPrimary
            )
            Spacer(modifier = Modifier.height(Spacing.lg))
            content()
        }
    }
}

@Composable
private fun ColorChip(
    color: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val chipColor = try {
        Color(android.graphics.Color.parseColor(color))
    } catch (e: Exception) {
        CleanColors.primary
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(chipColor)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun FrequencyOption(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(Radius.md))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Radius.md),
        color = if (selected) CleanColors.primaryLight else CleanColors.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = selected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(
                    selectedColor = CleanColors.primary,
                    unselectedColor = CleanColors.textTertiary
                )
            )
            Spacer(modifier = Modifier.width(Spacing.sm))
            Text(
                text = label,
                style = CleanTypography.body,
                color = if (selected) CleanColors.primary else CleanColors.textPrimary
            )
        }
    }
}
