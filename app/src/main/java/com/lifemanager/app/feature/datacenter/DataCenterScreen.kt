package com.lifemanager.app.feature.datacenter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

/**
 * 数据中心页面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataCenterScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataCenterViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val overviewStats by viewModel.overviewStats.collectAsState()
    val selectedPeriod by viewModel.selectedPeriod.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据中心") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is DataCenterUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is DataCenterUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = (uiState as DataCenterUiState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.refresh() }) {
                            Text("重试")
                        }
                    }
                }
            }

            is DataCenterUiState.Success -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // 时间周期选择
                    item {
                        PeriodSelector(
                            selectedPeriod = selectedPeriod,
                            onPeriodChange = { viewModel.selectPeriod(it) }
                        )
                    }

                    // 总览卡片
                    item {
                        OverviewCard(stats = overviewStats)
                    }

                    // 财务统计
                    item {
                        SectionTitle(title = "财务统计")
                    }
                    item {
                        FinanceStatsCard(
                            income = overviewStats.totalIncome,
                            expense = overviewStats.totalExpense,
                            balance = overviewStats.totalIncome - overviewStats.totalExpense
                        )
                    }

                    // 效率统计
                    item {
                        SectionTitle(title = "效率统计")
                    }
                    item {
                        EfficiencyStatsCard(
                            todoCompleted = overviewStats.todoCompleted,
                            todoTotal = overviewStats.todoTotal,
                            habitCheckedIn = overviewStats.habitCheckedIn,
                            habitTotal = overviewStats.habitTotal,
                            focusMinutes = overviewStats.focusMinutes
                        )
                    }

                    // 目标进度
                    item {
                        SectionTitle(title = "目标进度")
                    }
                    item {
                        GoalProgressCard(
                            activeGoals = overviewStats.activeGoals,
                            completedGoals = overviewStats.completedGoals,
                            avgProgress = overviewStats.avgGoalProgress
                        )
                    }

                    // 日记统计
                    item {
                        SectionTitle(title = "日记统计")
                    }
                    item {
                        DiaryStatsCard(
                            diaryCount = overviewStats.diaryCount,
                            avgMood = overviewStats.avgMoodScore
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: String,
    onPeriodChange: (String) -> Unit
) {
    val periods = listOf(
        "WEEK" to "本周",
        "MONTH" to "本月",
        "YEAR" to "本年",
        "ALL" to "全部"
    )

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(periods) { (value, label) ->
            FilterChip(
                selected = selectedPeriod == value,
                onClick = { onPeriodChange(value) },
                label = { Text(label) },
                leadingIcon = if (selectedPeriod == value) {
                    { Icon(Icons.Default.Check, contentDescription = null, Modifier.size(18.dp)) }
                } else null
            )
        }
    }
}

@Composable
private fun OverviewCard(stats: OverviewStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = "数据总览",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                OverviewStatItem(
                    icon = Icons.Default.AccountBalance,
                    label = "储蓄金额",
                    value = "¥${formatNumber(stats.totalSavings)}"
                )
                OverviewStatItem(
                    icon = Icons.Default.CheckCircle,
                    label = "完成任务",
                    value = "${stats.todoCompleted}"
                )
                OverviewStatItem(
                    icon = Icons.Default.Flag,
                    label = "达成目标",
                    value = "${stats.completedGoals}"
                )
            }
        }
    }
}

@Composable
private fun OverviewStatItem(
    icon: ImageVector,
    label: String,
    value: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}

@Composable
private fun FinanceStatsCard(
    income: Double,
    expense: Double,
    balance: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            FinanceStatItem(
                label = "总收入",
                value = "¥${formatNumber(income)}",
                color = Color(0xFF4CAF50)
            )
            FinanceStatItem(
                label = "总支出",
                value = "¥${formatNumber(expense)}",
                color = Color(0xFFF44336)
            )
            FinanceStatItem(
                label = "结余",
                value = "¥${formatNumber(balance)}",
                color = if (balance >= 0) Color(0xFF2196F3) else Color(0xFFF44336)
            )
        }
    }
}

@Composable
private fun FinanceStatItem(
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun EfficiencyStatsCard(
    todoCompleted: Int,
    todoTotal: Int,
    habitCheckedIn: Int,
    habitTotal: Int,
    focusMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 待办完成率
            StatProgressRow(
                icon = Icons.Default.CheckCircle,
                label = "待办完成率",
                current = todoCompleted,
                total = todoTotal,
                color = Color(0xFF4CAF50)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 习惯打卡率
            StatProgressRow(
                icon = Icons.Default.Verified,
                label = "习惯打卡率",
                current = habitCheckedIn,
                total = habitTotal,
                color = Color(0xFF9C27B0)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 专注时长
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = Color(0xFFFF9800),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "专注时长",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                Text(
                    text = formatMinutes(focusMinutes),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
            }
        }
    }
}

@Composable
private fun StatProgressRow(
    icon: ImageVector,
    label: String,
    current: Int,
    total: Int,
    color: Color
) {
    val progress = if (total > 0) current.toFloat() / total else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "$current/$total (${(progress * 100).toInt()}%)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun GoalProgressCard(
    activeGoals: Int,
    completedGoals: Int,
    avgProgress: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$activeGoals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "进行中",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$completedGoals",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "已完成",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "${(avgProgress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFFF9800)
                )
                Text(
                    text = "平均进度",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DiaryStatsCard(
    diaryCount: Int,
    avgMood: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$diaryCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE91E63)
                )
                Text(
                    text = "日记数量",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (avgMood > 0) String.format("%.1f", avgMood) else "-",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = getMoodColor(avgMood)
                )
                Text(
                    text = "平均心情",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatNumber(value: Double): String {
    return if (value >= 10000) {
        String.format("%.1f万", value / 10000)
    } else {
        String.format("%.0f", value)
    }
}

private fun formatMinutes(minutes: Int): String {
    val hours = minutes / 60
    val mins = minutes % 60
    return if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
}

private fun getMoodColor(mood: Float): Color {
    return when {
        mood >= 4 -> Color(0xFF4CAF50)
        mood >= 3 -> Color(0xFF8BC34A)
        mood >= 2 -> Color(0xFFFF9800)
        mood > 0 -> Color(0xFFF44336)
        else -> Color.Gray
    }
}
