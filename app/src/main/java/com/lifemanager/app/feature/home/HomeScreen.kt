package com.lifemanager.app.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.ui.navigation.Screen
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 首页屏幕 - 现代化设计
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToModule: (String) -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val monthlyFinance by viewModel.monthlyFinance.collectAsState()
    val topGoals by viewModel.topGoals.collectAsState()

    val today = remember { LocalDate.now() }
    val greeting = remember {
        when (java.time.LocalTime.now().hour) {
            in 5..11 -> "早上好"
            in 12..13 -> "中午好"
            in 14..17 -> "下午好"
            else -> "晚上好"
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = greeting,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${today.monthValue}月${today.dayOfMonth}日 ${today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigateToModule(Screen.AIAssistant.route) }) {
                        Icon(
                            Icons.Default.AutoAwesome,
                            contentDescription = "AI助手",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    IconButton(onClick = { onNavigateToModule(Screen.Settings.route) }) {
                        Icon(Icons.Default.Settings, contentDescription = "设置")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // 今日概览卡片
                item(key = "today_overview") {
                    TodayOverviewCard(todayStats = todayStats)
                }

                // 快捷功能入口
                item(key = "quick_access") {
                    QuickAccessSection(onNavigateToModule = onNavigateToModule)
                }

                // 本月财务概览
                item(key = "monthly_finance") {
                    MonthlyFinanceCard(
                        finance = monthlyFinance,
                        onClick = { onNavigateToModule(Screen.AccountingMain.route) }
                    )
                }

                // 目标进度
                if (topGoals.isNotEmpty()) {
                    item(key = "goals") {
                        GoalProgressSection(
                            goals = topGoals,
                            onClick = { onNavigateToModule(Screen.Goal.route) }
                        )
                    }
                }

                // AI建议卡片
                item(key = "ai_card") {
                    AIAssistantCard(onClick = { onNavigateToModule(Screen.AIAssistant.route) })
                }

                // 底部间距
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

/**
 * 今日概览卡片
 */
@Composable
private fun TodayOverviewCard(todayStats: TodayStatsData) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                spotColor = Color(0xFF6366F1).copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF6366F1),
                            Color(0xFF8B5CF6)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "今日概览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TodayStatItem(
                        icon = Icons.Default.CheckCircle,
                        label = "待办",
                        value = "${todayStats.completedTodos}/${todayStats.totalTodos}",
                        color = Color(0xFF4ADE80)
                    )
                    TodayStatItem(
                        icon = Icons.Default.ShoppingCart,
                        label = "消费",
                        value = "¥${numberFormat.format(todayStats.todayExpense.toInt())}",
                        color = Color(0xFFFBBF24)
                    )
                    TodayStatItem(
                        icon = Icons.Default.Verified,
                        label = "习惯",
                        value = "${todayStats.completedHabits}/${todayStats.totalHabits}",
                        color = Color(0xFF60A5FA)
                    )
                }
            }
        }
    }
}

/**
 * 今日统计项
 */
@Composable
private fun TodayStatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .background(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.8f)
        )
    }
}

/**
 * 快捷功能入口
 */
@Composable
private fun QuickAccessSection(onNavigateToModule: (String) -> Unit) {
    val quickAccessItems = remember {
        listOf(
            QuickAccessItem(Icons.Default.AutoAwesome, "AI助手", Color(0xFF3B82F6), Screen.AIAssistant.route),
            QuickAccessItem(Icons.Default.AccountBalance, "记账", Color(0xFF10B981), Screen.AccountingMain.route),
            QuickAccessItem(Icons.Default.Assignment, "待办", Color(0xFFF59E0B), Screen.Todo.route),
            QuickAccessItem(Icons.Default.Flag, "目标", Color(0xFF8B5CF6), Screen.Goal.route),
            QuickAccessItem(Icons.Default.CheckCircle, "打卡", Color(0xFFEC4899), Screen.Habit.route),
            QuickAccessItem(Icons.Default.Book, "日记", Color(0xFFEF4444), Screen.Diary.route),
            QuickAccessItem(Icons.Default.Savings, "存钱", Color(0xFF06B6D4), Screen.SavingsPlan.route),
            QuickAccessItem(Icons.Default.PieChart, "预算", Color(0xFF6366F1), Screen.Budget.route)
        )
    }

    Column {
        Text(
            text = "快捷入口",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(quickAccessItems, key = { it.route }) { item ->
                QuickAccessButton(
                    item = item,
                    onClick = { onNavigateToModule(item.route) }
                )
            }
        }
    }
}

/**
 * 快捷入口按钮
 */
@Composable
private fun QuickAccessButton(
    item: QuickAccessItem,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = item.color.copy(alpha = 0.12f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = item.color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 本月财务卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthlyFinanceCard(
    finance: MonthlyFinanceData,
    onClick: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }
    val today = remember { LocalDate.now() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBalanceWallet,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${today.monthValue}月财务",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "查看详情",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FinanceStatItem(
                    label = "收入",
                    value = "¥${numberFormat.format(finance.totalIncome.toLong())}",
                    color = Color(0xFF4CAF50)
                )
                FinanceStatItem(
                    label = "支出",
                    value = "¥${numberFormat.format(finance.totalExpense.toLong())}",
                    color = Color(0xFFF44336)
                )
                FinanceStatItem(
                    label = "结余",
                    value = "¥${numberFormat.format(finance.balance.toLong())}",
                    color = if (finance.balance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFF44336)
                )
            }
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
            style = MaterialTheme.typography.titleLarge,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 目标进度部分
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GoalProgressSection(
    goals: List<GoalProgressData>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Flag,
                        contentDescription = null,
                        tint = Color(0xFF8B5CF6),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "目标进度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "查看详情",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            goals.forEachIndexed { index, goal ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(12.dp))
                }
                GoalProgressItem(
                    title = goal.title,
                    progress = goal.progress,
                    progressText = goal.progressText
                )
            }
        }
    }
}

/**
 * 目标进度项
 */
@Composable
private fun GoalProgressItem(
    title: String,
    progress: Float,
    progressText: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF8B5CF6).copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFF8B5CF6),
                                Color(0xFFA855F7)
                            )
                        ),
                        shape = RoundedCornerShape(4.dp)
                    )
            )
        }
    }
}

/**
 * AI助手卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AIAssistantCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFF3B82F6).copy(alpha = 0.2f)
            ),
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF3B82F6).copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF3B82F6),
                                Color(0xFF8B5CF6)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "AI智能助手",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "语音或文字命令，快速记账、添加待办、查询数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "进入",
                tint = Color(0xFF3B82F6)
            )
        }
    }
}

/**
 * 快捷入口数据类
 */
private data class QuickAccessItem(
    val icon: ImageVector,
    val label: String,
    val color: Color,
    val route: String
)
