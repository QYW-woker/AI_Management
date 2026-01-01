package com.lifemanager.app.feature.health

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.core.database.entity.*
import com.lifemanager.app.ui.component.*
import com.lifemanager.app.ui.theme.*

/**
 * 健康记录详情页 - 产品级设计
 *
 * 页面职责：
 * - 展示单条健康记录的完整信息
 * - 提供编辑和删除操作
 *
 * 设计原则：
 * - 干净、克制、有呼吸感
 * - 信息层次清晰
 * - 操作便捷
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanHealthRecordDetailScreen(
    recordId: Long,
    onNavigateBack: () -> Unit,
    viewModel: HealthRecordDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val record by viewModel.record.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()

    LaunchedEffect(recordId) {
        viewModel.loadRecord(recordId)
    }

    Scaffold(
        containerColor = CleanColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "记录详情",
                        style = CleanTypography.title,
                        color = CleanColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = CleanColors.textPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.showEditDialog() }) {
                        Icon(
                            Icons.Outlined.Edit,
                            contentDescription = "编辑",
                            tint = CleanColors.textSecondary
                        )
                    }
                    IconButton(onClick = { viewModel.showDeleteConfirm() }) {
                        Icon(
                            Icons.Outlined.Delete,
                            contentDescription = "删除",
                            tint = CleanColors.error
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.background
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is HealthRecordDetailUiState.Loading -> {
                PageLoadingState(modifier = Modifier.padding(paddingValues))
            }

            is HealthRecordDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as HealthRecordDetailUiState.Error).message,
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

            is HealthRecordDetailUiState.Success -> {
                record?.let { recordEntity ->
                    HealthRecordDetailContent(
                        record = recordEntity,
                        formatDate = { viewModel.formatDate(it) },
                        formatWeekday = { viewModel.formatWeekday(it) },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
    }

    // 编辑对话框
    if (showEditDialog) {
        EditRecordDialog(
            viewModel = viewModel,
            record = record,
            onDismiss = { viewModel.hideEditDialog() }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            icon = {
                Icon(
                    Icons.Outlined.Warning,
                    contentDescription = null,
                    tint = CleanColors.error,
                    modifier = Modifier.size(IconSize.lg)
                )
            },
            title = {
                Text(
                    text = "确认删除",
                    style = CleanTypography.title,
                    color = CleanColors.textPrimary
                )
            },
            text = {
                Text(
                    text = "确定要删除这条记录吗？此操作不可恢复。",
                    style = CleanTypography.body,
                    color = CleanColors.textSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.confirmDelete(onNavigateBack) }) {
                    Text(
                        text = "删除",
                        style = CleanTypography.button,
                        color = CleanColors.error
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) {
                    Text(
                        text = "取消",
                        style = CleanTypography.button,
                        color = CleanColors.textSecondary
                    )
                }
            },
            containerColor = CleanColors.surface,
            shape = RoundedCornerShape(Radius.lg)
        )
    }
}

@Composable
private fun HealthRecordDetailContent(
    record: HealthRecordEntity,
    formatDate: (Int) -> String,
    formatWeekday: (Int) -> String,
    modifier: Modifier = Modifier
) {
    val typeColor = getTypeColor(record.recordType)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.pageHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        item {
            Spacer(modifier = Modifier.height(Spacing.sm))
        }

        // 头部卡片
        item {
            RecordHeaderCard(
                record = record,
                typeColor = typeColor,
                formatDate = formatDate,
                formatWeekday = formatWeekday
            )
        }

        // 详细信息卡片
        item {
            RecordDetailsCard(record = record)
        }

        // 备注卡片（如果有）
        if (record.note.isNotBlank()) {
            item {
                RecordNoteCard(note = record.note)
            }
        }

        // 底部间距
        item {
            Spacer(modifier = Modifier.height(Spacing.bottomSafe))
        }
    }
}

@Composable
private fun RecordHeaderCard(
    record: HealthRecordEntity,
    typeColor: Color,
    formatDate: (Int) -> String,
    formatWeekday: (Int) -> String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        color = typeColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 类型图标
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = HealthRecordType.getIcon(record.recordType),
                    style = CleanTypography.amountLarge
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // 类型名称
            Text(
                text = HealthRecordType.getDisplayName(record.recordType),
                style = CleanTypography.title,
                color = CleanColors.textPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.md))

            // 主要数值
            when (record.recordType) {
                HealthRecordType.MOOD -> {
                    Text(
                        text = MoodRating.getIcon(record.value.toInt()),
                        style = CleanTypography.amountLarge
                    )
                    Spacer(modifier = Modifier.height(Spacing.sm))
                    Text(
                        text = MoodRating.getDisplayName(record.value.toInt()),
                        style = CleanTypography.headline,
                        color = typeColor
                    )
                }
                HealthRecordType.BLOOD_PRESSURE -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${record.value.toInt()}",
                            style = CleanTypography.amountLarge,
                            color = typeColor
                        )
                        Text(
                            text = " / ${record.secondaryValue?.toInt() ?: 0}",
                            style = CleanTypography.headline,
                            color = typeColor
                        )
                        Text(
                            text = " mmHg",
                            style = CleanTypography.secondary,
                            color = CleanColors.textSecondary
                        )
                    }
                }
                else -> {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = formatValue(record),
                            style = CleanTypography.amountLarge,
                            color = typeColor
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = HealthRecordType.getUnit(record.recordType),
                            style = CleanTypography.body,
                            color = CleanColors.textSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            // 日期时间
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = CleanColors.textTertiary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = "${formatDate(record.date)} ${formatWeekday(record.date)}",
                    style = CleanTypography.secondary,
                    color = CleanColors.textSecondary
                )
                record.time?.let { time ->
                    Spacer(modifier = Modifier.width(Spacing.md))
                    Icon(
                        Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = CleanColors.textTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.xs))
                    Text(
                        text = time,
                        style = CleanTypography.secondary,
                        color = CleanColors.textSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RecordDetailsCard(record: HealthRecordEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "详细信息",
                style = CleanTypography.title,
                color = CleanColors.textPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            // 根据类型显示不同的详细信息
            when (record.recordType) {
                HealthRecordType.SLEEP -> {
                    DetailRow(
                        icon = Icons.Outlined.Bedtime,
                        label = "睡眠时长",
                        value = "${String.format("%.1f", record.value)} 小时"
                    )
                    record.rating?.let { rating ->
                        CleanDivider(modifier = Modifier.padding(vertical = Spacing.md))
                        DetailRow(
                            icon = Icons.Outlined.Star,
                            label = "睡眠质量",
                            value = "${SleepQuality.getIcon(rating)} ${SleepQuality.getDisplayName(rating)}"
                        )
                    }
                }
                HealthRecordType.EXERCISE -> {
                    DetailRow(
                        icon = Icons.Outlined.Timer,
                        label = "运动时长",
                        value = "${record.value.toInt()} 分钟"
                    )
                    record.category?.let { category ->
                        CleanDivider(modifier = Modifier.padding(vertical = Spacing.md))
                        DetailRow(
                            icon = Icons.Outlined.FitnessCenter,
                            label = "运动类型",
                            value = "${ExerciseCategory.getIcon(category)} ${ExerciseCategory.getDisplayName(category)}"
                        )
                    }
                    record.secondaryValue?.let { calories ->
                        if (calories > 0) {
                            CleanDivider(modifier = Modifier.padding(vertical = Spacing.md))
                            DetailRow(
                                icon = Icons.Outlined.LocalFireDepartment,
                                label = "消耗热量",
                                value = "${calories.toInt()} kcal"
                            )
                        }
                    }
                }
                HealthRecordType.MOOD -> {
                    DetailRow(
                        icon = Icons.Outlined.SentimentSatisfied,
                        label = "心情评分",
                        value = "${MoodRating.getIcon(record.value.toInt())} ${MoodRating.getDisplayName(record.value.toInt())}"
                    )
                    record.category?.let { source ->
                        CleanDivider(modifier = Modifier.padding(vertical = Spacing.md))
                        DetailRow(
                            icon = Icons.Outlined.Category,
                            label = "心情来源",
                            value = MoodSource.getDisplayName(source)
                        )
                    }
                }
                HealthRecordType.WATER -> {
                    DetailRow(
                        icon = Icons.Outlined.WaterDrop,
                        label = "饮水量",
                        value = "${record.value.toInt()} ml"
                    )
                }
                HealthRecordType.STEPS -> {
                    DetailRow(
                        icon = Icons.Outlined.DirectionsWalk,
                        label = "步数",
                        value = "${record.value.toInt()} 步"
                    )
                }
                HealthRecordType.WEIGHT -> {
                    DetailRow(
                        icon = Icons.Outlined.MonitorWeight,
                        label = "体重",
                        value = "${String.format("%.1f", record.value)} kg"
                    )
                }
                HealthRecordType.HEART_RATE -> {
                    DetailRow(
                        icon = Icons.Outlined.Favorite,
                        label = "心率",
                        value = "${record.value.toInt()} bpm"
                    )
                }
                else -> {
                    DetailRow(
                        icon = Icons.Outlined.Analytics,
                        label = "数值",
                        value = "${record.value} ${record.unit}"
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailRow(
    icon: ImageVector,
    label: String,
    value: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CleanColors.textTertiary,
            modifier = Modifier.size(IconSize.md)
        )
        Spacer(modifier = Modifier.width(Spacing.lg))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = CleanTypography.caption,
                color = CleanColors.textTertiary
            )
            Text(
                text = value,
                style = CleanTypography.body,
                color = CleanColors.textPrimary
            )
        }
    }
}

@Composable
private fun RecordNoteCard(note: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Outlined.Notes,
                    contentDescription = null,
                    tint = CleanColors.textTertiary,
                    modifier = Modifier.size(IconSize.md)
                )
                Spacer(modifier = Modifier.width(Spacing.sm))
                Text(
                    text = "备注",
                    style = CleanTypography.title,
                    color = CleanColors.textPrimary
                )
            }
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = note,
                style = CleanTypography.body,
                color = CleanColors.textSecondary
            )
        }
    }
}

@Composable
private fun EditRecordDialog(
    viewModel: HealthRecordDetailViewModel,
    record: HealthRecordEntity?,
    onDismiss: () -> Unit
) {
    if (record == null) return

    val editValue by viewModel.editValue.collectAsState()
    val editSecondaryValue by viewModel.editSecondaryValue.collectAsState()
    val editRating by viewModel.editRating.collectAsState()
    val editNote by viewModel.editNote.collectAsState()
    val error by viewModel.operationError.collectAsState()
    val isOperating by viewModel.isOperating.collectAsState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "编辑${HealthRecordType.getDisplayName(record.recordType)}",
                style = CleanTypography.title,
                color = CleanColors.textPrimary
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Spacing.md)) {
                when (record.recordType) {
                    HealthRecordType.MOOD -> {
                        Text(
                            text = "选择心情",
                            style = CleanTypography.secondary,
                            color = CleanColors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (1..5).forEach { mood ->
                                MoodButton(
                                    icon = MoodRating.getIcon(mood),
                                    selected = editRating == mood,
                                    onClick = { viewModel.updateEditRating(mood) }
                                )
                            }
                        }
                    }
                    HealthRecordType.SLEEP -> {
                        CleanTextField(
                            value = editValue,
                            onValueChange = { viewModel.updateEditValue(it) },
                            label = "睡眠时长（小时）",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "睡眠质量",
                            style = CleanTypography.secondary,
                            color = CleanColors.textSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            (1..5).forEach { quality ->
                                MoodButton(
                                    icon = SleepQuality.getIcon(quality),
                                    selected = editRating == quality,
                                    onClick = { viewModel.updateEditRating(quality) }
                                )
                            }
                        }
                    }
                    else -> {
                        CleanTextField(
                            value = editValue,
                            onValueChange = { viewModel.updateEditValue(it) },
                            label = "${HealthRecordType.getDisplayName(record.recordType)}（${HealthRecordType.getUnit(record.recordType)}）",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                CleanTextField(
                    value = editNote,
                    onValueChange = { viewModel.updateEditNote(it) },
                    label = "备注（可选）",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )

                error?.let { errorMsg ->
                    Text(
                        text = errorMsg,
                        style = CleanTypography.caption,
                        color = CleanColors.error
                    )
                }
            }
        },
        confirmButton = {
            if (isOperating) {
                CleanLoadingIndicator(size = 20.dp)
            } else {
                CleanPrimaryButton(
                    text = "保存",
                    onClick = { viewModel.saveEdit() }
                )
            }
        },
        dismissButton = {
            CleanTextButton(
                text = "取消",
                onClick = onDismiss,
                color = CleanColors.textSecondary
            )
        },
        containerColor = CleanColors.surface,
        shape = RoundedCornerShape(Radius.lg)
    )
}

@Composable
private fun MoodButton(
    icon: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(Radius.md),
        color = if (selected) CleanColors.primary.copy(alpha = 0.15f) else CleanColors.surface,
        border = if (selected) androidx.compose.foundation.BorderStroke(2.dp, CleanColors.primary) else null
    ) {
        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, style = CleanTypography.headline)
        }
    }
}

private fun formatValue(record: HealthRecordEntity): String {
    return when (record.recordType) {
        HealthRecordType.WEIGHT -> String.format("%.1f", record.value)
        HealthRecordType.SLEEP -> String.format("%.1f", record.value)
        else -> record.value.toInt().toString()
    }
}

private fun getTypeColor(type: String): Color {
    return when (type) {
        HealthRecordType.WEIGHT -> Color(0xFF4CAF50)
        HealthRecordType.SLEEP -> Color(0xFF9C27B0)
        HealthRecordType.EXERCISE -> Color(0xFFFF5722)
        HealthRecordType.MOOD -> Color(0xFFFFEB3B)
        HealthRecordType.WATER -> Color(0xFF2196F3)
        HealthRecordType.BLOOD_PRESSURE -> Color(0xFFE91E63)
        HealthRecordType.HEART_RATE -> Color(0xFFF44336)
        HealthRecordType.STEPS -> Color(0xFF00BCD4)
        else -> CleanColors.primary
    }
}
