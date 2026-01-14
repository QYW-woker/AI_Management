package com.lifemanager.app.feature.habit

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.core.database.entity.HabitEntity
import com.lifemanager.app.core.database.entity.HabitRecordEntity
import com.lifemanager.app.domain.model.getFrequencyDisplayText
import com.lifemanager.app.ui.component.*
import com.lifemanager.app.ui.theme.*
import java.time.LocalDate
import java.time.YearMonth

/**
 * 习惯详情页 - 产品级设计
 *
 * 页面职责：
 * - 展示单个习惯的完整信息
 * - 显示打卡日历和历史记录
 * - 显示统计数据（连续天数、完成率）
 * - 提供编辑、删除、暂停操作入口
 *
 * 设计原则：
 * - 信息层级清晰
 * - 操作可达但不突兀
 * - 数据可视化直观
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanHabitDetailScreen(
    habitId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
    viewModel: HabitDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val habit by viewModel.habit.collectAsState()
    val records by viewModel.records.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val currentMonth by viewModel.currentMonth.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    LaunchedEffect(habitId) {
        viewModel.loadHabit(habitId)
    }

    Scaffold(
        containerColor = CleanColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "习惯详情",
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
                    IconButton(onClick = { onNavigateToEdit(habitId) }) {
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
            is HabitDetailUiState.Loading -> {
                PageLoadingState(modifier = Modifier.padding(paddingValues))
            }

            is HabitDetailUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as HabitDetailUiState.Error).message,
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

            is HabitDetailUiState.Success -> {
                habit?.let { habitEntity ->
                    HabitDetailContent(
                        habit = habitEntity,
                        records = records,
                        statistics = statistics,
                        currentMonth = currentMonth,
                        onCheckIn = { viewModel.checkIn() },
                        onPreviousMonth = { viewModel.previousMonth() },
                        onNextMonth = { viewModel.nextMonth() },
                        onTogglePause = { viewModel.togglePause() },
                        modifier = Modifier.padding(paddingValues)
                    )
                }
            }
        }
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
                    text = "删除习惯将同时删除所有打卡记录，此操作不可恢复。确定要删除吗？",
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
private fun HabitDetailContent(
    habit: HabitEntity,
    records: List<HabitRecordEntity>,
    statistics: HabitDetailStatistics,
    currentMonth: YearMonth,
    onCheckIn: () -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onTogglePause: () -> Unit,
    modifier: Modifier = Modifier
) {
    val habitColor = try {
        Color(android.graphics.Color.parseColor(habit.color))
    } catch (e: Exception) {
        CleanColors.primary
    }

    val isPaused = habit.status == "PAUSED"
    val isCheckedToday = statistics.isCheckedToday

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.pageHorizontal),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // 习惯头部卡片
        item {
            Spacer(modifier = Modifier.height(Spacing.sm))
            HabitHeaderCard(
                habit = habit,
                habitColor = habitColor,
                isCheckedToday = isCheckedToday,
                isPaused = isPaused,
                onCheckIn = onCheckIn,
                onTogglePause = onTogglePause
            )
        }

        // 统计数据卡片
        item {
            HabitStatisticsCard(statistics = statistics, habitColor = habitColor)
        }

        // 打卡日历
        item {
            HabitCalendarCard(
                currentMonth = currentMonth,
                records = records,
                habitColor = habitColor,
                onPreviousMonth = onPreviousMonth,
                onNextMonth = onNextMonth
            )
        }

        // 习惯详情信息
        item {
            HabitInfoCard(habit = habit)
        }

        // 底部间距
        item {
            Spacer(modifier = Modifier.height(Spacing.bottomSafe))
        }
    }
}

/**
 * 习惯头部卡片 - 展示核心信息和打卡按钮
 */
@Composable
private fun HabitHeaderCard(
    habit: HabitEntity,
    habitColor: Color,
    isCheckedToday: Boolean,
    isPaused: Boolean,
    onCheckIn: () -> Unit,
    onTogglePause: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.lg),
        color = habitColor.copy(alpha = 0.08f)
    ) {
        Column(
            modifier = Modifier.padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 习惯名称
            Text(
                text = habit.name,
                style = CleanTypography.headline,
                color = CleanColors.textPrimary,
                textAlign = TextAlign.Center
            )

            if (habit.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(Spacing.sm))
                Text(
                    text = habit.description,
                    style = CleanTypography.secondary,
                    color = CleanColors.textSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(Spacing.xl))

            // 打卡按钮
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCheckedToday) habitColor else habitColor.copy(alpha = 0.15f)
                    )
                    .clickable(enabled = !isPaused, onClick = onCheckIn),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCheckedToday) Icons.Filled.Check else Icons.Filled.Add,
                    contentDescription = if (isCheckedToday) "已打卡" else "打卡",
                    tint = if (isCheckedToday) Color.White else habitColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            Text(
                text = when {
                    isPaused -> "已暂停"
                    isCheckedToday -> "今日已完成"
                    else -> "点击打卡"
                },
                style = CleanTypography.secondary,
                color = when {
                    isPaused -> CleanColors.textTertiary
                    isCheckedToday -> habitColor
                    else -> CleanColors.textSecondary
                }
            )

            // 暂停/恢复按钮
            Spacer(modifier = Modifier.height(Spacing.lg))
            TextButton(onClick = onTogglePause) {
                Icon(
                    imageVector = if (isPaused) Icons.Outlined.PlayArrow else Icons.Outlined.Pause,
                    contentDescription = null,
                    tint = CleanColors.textSecondary,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(Spacing.xs))
                Text(
                    text = if (isPaused) "恢复习惯" else "暂停习惯",
                    style = CleanTypography.button,
                    color = CleanColors.textSecondary
                )
            }
        }
    }
}

/**
 * 统计数据卡片
 */
@Composable
private fun HabitStatisticsCard(
    statistics: HabitDetailStatistics,
    habitColor: Color
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "统计数据",
                style = CleanTypography.title,
                color = CleanColors.textPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatisticItem(
                    value = statistics.currentStreak.toString(),
                    label = "当前连续",
                    unit = "天",
                    icon = Icons.Outlined.LocalFireDepartment,
                    color = if (statistics.currentStreak > 0) CleanColors.warning else CleanColors.textTertiary
                )
                StatisticItem(
                    value = statistics.longestStreak.toString(),
                    label = "最长连续",
                    unit = "天",
                    icon = Icons.Outlined.EmojiEvents,
                    color = habitColor
                )
                StatisticItem(
                    value = statistics.totalCheckins.toString(),
                    label = "累计打卡",
                    unit = "次",
                    icon = Icons.Outlined.CheckCircle,
                    color = CleanColors.success
                )
                StatisticItem(
                    value = "${(statistics.completionRate * 100).toInt()}",
                    label = "完成率",
                    unit = "%",
                    icon = Icons.Outlined.TrendingUp,
                    color = CleanColors.primary
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    value: String,
    label: String,
    unit: String,
    icon: ImageVector,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(IconSize.md)
        )
        Spacer(modifier = Modifier.height(Spacing.sm))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = value,
                style = CleanTypography.amountMedium,
                color = color
            )
            Text(
                text = unit,
                style = CleanTypography.caption,
                color = CleanColors.textTertiary
            )
        }
        Text(
            text = label,
            style = CleanTypography.caption,
            color = CleanColors.textTertiary
        )
    }
}

/**
 * 打卡日历卡片
 */
@Composable
private fun HabitCalendarCard(
    currentMonth: YearMonth,
    records: List<HabitRecordEntity>,
    habitColor: Color,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val checkedDays = remember(records) {
        records.map { it.date }.toSet()
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            // 月份导航
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(
                        Icons.Default.ChevronLeft,
                        contentDescription = "上个月",
                        tint = CleanColors.textSecondary
                    )
                }

                Text(
                    text = "${currentMonth.year}年${currentMonth.monthValue}月",
                    style = CleanTypography.title,
                    color = CleanColors.textPrimary
                )

                IconButton(onClick = onNextMonth) {
                    Icon(
                        Icons.Default.ChevronRight,
                        contentDescription = "下个月",
                        tint = CleanColors.textSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.md))

            // 星期标题
            val daysOfWeek = listOf("日", "一", "二", "三", "四", "五", "六")
            Row(modifier = Modifier.fillMaxWidth()) {
                daysOfWeek.forEach { day ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .padding(Spacing.xs),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = day,
                            style = CleanTypography.caption,
                            color = CleanColors.textTertiary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Spacing.sm))

            // 日期网格
            val firstDayOfMonth = currentMonth.atDay(1)
            val lastDayOfMonth = currentMonth.atEndOfMonth()
            val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7
            val totalDays = lastDayOfMonth.dayOfMonth
            val weeks = (startDayOfWeek + totalDays + 6) / 7
            val today = LocalDate.now()

            repeat(weeks) { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { dayOfWeek ->
                        val cellIndex = week * 7 + dayOfWeek
                        val dayNumber = cellIndex - startDayOfWeek + 1

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .padding(2.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNumber in 1..totalDays) {
                                val date = currentMonth.atDay(dayNumber)
                                val epochDay = date.toEpochDay().toInt()
                                val isChecked = checkedDays.contains(epochDay)
                                val isToday = date == today

                                CalendarDay(
                                    day = dayNumber,
                                    isChecked = isChecked,
                                    isToday = isToday,
                                    habitColor = habitColor
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    day: Int,
    isChecked: Boolean,
    isToday: Boolean,
    habitColor: Color
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isChecked -> habitColor
            isToday -> CleanColors.primaryLight
            else -> Color.Transparent
        },
        label = "calendarDayBg"
    )

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (isChecked) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(16.dp)
            )
        } else {
            Text(
                text = day.toString(),
                style = CleanTypography.secondary,
                color = when {
                    isToday -> CleanColors.primary
                    else -> CleanColors.textPrimary
                }
            )
        }
    }
}

/**
 * 习惯信息卡片
 */
@Composable
private fun HabitInfoCard(habit: HabitEntity) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Text(
                text = "习惯信息",
                style = CleanTypography.title,
                color = CleanColors.textPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            InfoRow(
                icon = Icons.Outlined.Repeat,
                label = "打卡频率",
                value = getFrequencyDisplayText(habit.frequency, habit.targetTimes)
            )

            if (habit.isNumeric && habit.targetValue != null) {
                CleanDivider(modifier = Modifier.padding(vertical = Spacing.md))
                InfoRow(
                    icon = Icons.Outlined.Flag,
                    label = "目标数值",
                    value = "${habit.targetValue?.toInt()} ${habit.unit}"
                )
            }

            habit.reminderTime?.let { time ->
                CleanDivider(modifier = Modifier.padding(vertical = Spacing.md))
                InfoRow(
                    icon = Icons.Outlined.Notifications,
                    label = "提醒时间",
                    value = time
                )
            }

            CleanDivider(modifier = Modifier.padding(vertical = Spacing.md))
            InfoRow(
                icon = Icons.Outlined.CalendarToday,
                label = "创建时间",
                value = formatTimestamp(habit.createdAt)
            )
        }
    }
}

@Composable
private fun InfoRow(
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

/**
 * 格式化时间戳
 */
private fun formatTimestamp(timestamp: Long): String {
    return try {
        val dateTime = java.time.Instant.ofEpochMilli(timestamp)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDateTime()
        dateTime.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"))
    } catch (e: Exception) {
        ""
    }
}

/**
 * 习惯详情UI状态
 */
sealed class HabitDetailUiState {
    object Loading : HabitDetailUiState()
    object Success : HabitDetailUiState()
    data class Error(val message: String) : HabitDetailUiState()
}

/**
 * 习惯详情统计数据
 */
data class HabitDetailStatistics(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val totalCheckins: Int = 0,
    val completionRate: Float = 0f,
    val isCheckedToday: Boolean = false
)
