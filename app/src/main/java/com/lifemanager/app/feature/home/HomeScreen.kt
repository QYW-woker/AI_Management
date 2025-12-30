package com.lifemanager.app.feature.home

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.ui.navigation.Screen
import com.lifemanager.app.ui.theme.AppColors
import com.lifemanager.app.ui.theme.CartoonShape
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/**
 * 首页屏幕 - 卡通可爱风格
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
            in 5..11 -> "早上好 ☀️"
            in 12..13 -> "中午好 🌤️"
            in 14..17 -> "下午好 🌸"
            else -> "晚上好 🌙"
        }
    }

    // 动画背景的偏移
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "offset"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        // 可爱背景装饰
        CuteBackground(animatedOffset)

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = greeting,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${today.monthValue}月${today.dayOfMonth}日 ${today.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.CHINA)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    actions = {
                        // AI助手按钮 - 卡通样式
                        CuteIconButton(
                            onClick = { onNavigateToModule(Screen.AIAssistant.route) },
                            emoji = "🤖",
                            backgroundColor = AppColors.CandyLavender
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // 设置按钮
                        CuteIconButton(
                            onClick = { onNavigateToModule(Screen.Settings.route) },
                            emoji = "⚙️",
                            backgroundColor = AppColors.CandyMint.copy(alpha = 0.5f)
                        )
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
                    // 可爱加载动画
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("🌀", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "加载中...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 今日概览卡片 - 卡通版
                    item(key = "today_overview") {
                        CuteTodayCard(todayStats = todayStats)
                    }

                    // 快捷功能入口 - 卡通版
                    item(key = "quick_access") {
                        CuteQuickAccessSection(onNavigateToModule = onNavigateToModule)
                    }

                    // 本月财务概览 - 卡通版
                    item(key = "monthly_finance") {
                        CuteFinanceCard(
                            finance = monthlyFinance,
                            onClick = { onNavigateToModule(Screen.AccountingMain.route) }
                        )
                    }

                    // 目标进度 - 卡通版
                    if (topGoals.isNotEmpty()) {
                        item(key = "goals") {
                            CuteGoalSection(
                                goals = topGoals,
                                onClick = { onNavigateToModule(Screen.Goal.route) }
                            )
                        }
                    }

                    // AI建议卡片 - 卡通版
                    item(key = "ai_card") {
                        CuteAICard(onClick = { onNavigateToModule(Screen.AIAssistant.route) })
                    }

                    // 底部间距
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

/**
 * 可爱背景装饰 - 浮动的圆点和星星
 */
@Composable
private fun CuteBackground(animatedOffset: Float) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height

        // 绘制浮动的装饰圆点
        drawFloatingCircle(
            center = Offset(width * 0.1f, height * 0.15f + animatedOffset % 50),
            radius = 30f,
            color = AppColors.CandyPink.copy(alpha = 0.3f)
        )
        drawFloatingCircle(
            center = Offset(width * 0.85f, height * 0.1f - animatedOffset % 40),
            radius = 25f,
            color = AppColors.CandyBlue.copy(alpha = 0.3f)
        )
        drawFloatingCircle(
            center = Offset(width * 0.7f, height * 0.3f + animatedOffset % 60),
            radius = 20f,
            color = AppColors.CandyMint.copy(alpha = 0.3f)
        )
        drawFloatingCircle(
            center = Offset(width * 0.15f, height * 0.5f - animatedOffset % 45),
            radius = 35f,
            color = AppColors.CandyLavender.copy(alpha = 0.25f)
        )
        drawFloatingCircle(
            center = Offset(width * 0.9f, height * 0.6f + animatedOffset % 55),
            radius = 28f,
            color = AppColors.CandyPeach.copy(alpha = 0.3f)
        )
        drawFloatingCircle(
            center = Offset(width * 0.3f, height * 0.8f - animatedOffset % 35),
            radius = 22f,
            color = AppColors.CandyYellow.copy(alpha = 0.35f)
        )
    }
}

private fun DrawScope.drawFloatingCircle(
    center: Offset,
    radius: Float,
    color: Color
) {
    drawCircle(
        color = color,
        radius = radius,
        center = center
    )
}

/**
 * 可爱图标按钮
 */
@Composable
private fun CuteIconButton(
    onClick: () -> Unit,
    emoji: String,
    backgroundColor: Color
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Surface(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        shape = CartoonShape.Circle,
        color = backgroundColor
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(emoji, fontSize = 20.sp)
        }
    }
}

/**
 * 今日概览卡片 - 卡通版
 */
@Composable
private fun CuteTodayCard(todayStats: TodayStatsData) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = CartoonShape.CuteCard,
                spotColor = AppColors.Primary.copy(alpha = 0.3f)
            ),
        shape = CartoonShape.CuteCard,
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = AppColors.GradientDream
                    )
                )
                .padding(24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "今日概览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    CuteTodayStatItem(
                        emoji = "✅",
                        label = "待办",
                        value = "${todayStats.completedTodos}/${todayStats.totalTodos}",
                        backgroundColor = AppColors.CandyMint.copy(alpha = 0.9f)
                    )
                    CuteTodayStatItem(
                        emoji = "💰",
                        label = "消费",
                        value = "¥${numberFormat.format(todayStats.todayExpense.toInt())}",
                        backgroundColor = AppColors.CandyYellow.copy(alpha = 0.9f)
                    )
                    CuteTodayStatItem(
                        emoji = "🎯",
                        label = "习惯",
                        value = "${todayStats.completedHabits}/${todayStats.totalHabits}",
                        backgroundColor = AppColors.CandyBlue.copy(alpha = 0.9f)
                    )
                }
            }
        }
    }
}

/**
 * 可爱今日统计项
 */
@Composable
private fun CuteTodayStatItem(
    emoji: String,
    label: String,
    value: String,
    backgroundColor: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            modifier = Modifier.size(52.dp),
            shape = CartoonShape.Circle,
            color = backgroundColor,
            shadowElevation = 4.dp
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(emoji, fontSize = 24.sp)
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

/**
 * 可爱快捷功能入口
 */
@Composable
private fun CuteQuickAccessSection(onNavigateToModule: (String) -> Unit) {
    val quickAccessItems = remember {
        listOf(
            CuteQuickItem("🤖", "AI助手", AppColors.CandyBlue, Screen.AIAssistant.route),
            CuteQuickItem("💵", "记账", AppColors.CandyMint, Screen.AccountingMain.route),
            CuteQuickItem("📝", "待办", AppColors.CandyYellow, Screen.Todo.route),
            CuteQuickItem("🎯", "目标", AppColors.CandyLavender, Screen.Goal.route),
            CuteQuickItem("⭐", "打卡", AppColors.CandyPink, Screen.Habit.route),
            CuteQuickItem("📔", "日记", AppColors.CandyCoral, Screen.Diary.route),
            CuteQuickItem("🐷", "存钱", AppColors.CandyPeach, Screen.SavingsPlan.route),
            CuteQuickItem("📊", "预算", AppColors.CandyLilac, Screen.Budget.route)
        )
    }

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⚡", fontSize = 20.sp)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "快捷入口",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(quickAccessItems, key = { it.route }) { item ->
                CuteQuickButton(
                    item = item,
                    onClick = { onNavigateToModule(item.route) }
                )
            }
        }
    }
}

/**
 * 可爱快捷入口按钮
 */
@Composable
private fun CuteQuickButton(
    item: CuteQuickItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .scale(scale)
            .clip(CartoonShape.CuteButton)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(8.dp)
    ) {
        Surface(
            shape = CartoonShape.QuickIcon,
            color = item.color.copy(alpha = 0.25f),
            modifier = Modifier
                .size(60.dp)
                .shadow(6.dp, CartoonShape.QuickIcon, spotColor = item.color.copy(alpha = 0.3f))
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(item.emoji, fontSize = 28.sp)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = item.label,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

/**
 * 可爱本月财务卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuteFinanceCard(
    finance: MonthlyFinanceData,
    onClick: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }
    val today = remember { LocalDate.now() }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CartoonShape.CuteCard, spotColor = AppColors.Secondary.copy(alpha = 0.2f)),
        onClick = onClick,
        shape = CartoonShape.CuteCard,
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
                    Text("💳", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${today.monthValue}月财务",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = CartoonShape.Capsule,
                    color = AppColors.CandyMint.copy(alpha = 0.3f)
                ) {
                    Text(
                        "查看 →",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                CuteFinanceItem(
                    emoji = "📈",
                    label = "收入",
                    value = "¥${numberFormat.format(finance.totalIncome.toLong())}",
                    color = AppColors.Income
                )
                CuteFinanceItem(
                    emoji = "📉",
                    label = "支出",
                    value = "¥${numberFormat.format(finance.totalExpense.toLong())}",
                    color = AppColors.Expense
                )
                CuteFinanceItem(
                    emoji = "💎",
                    label = "结余",
                    value = "¥${numberFormat.format(finance.balance.toLong())}",
                    color = if (finance.balance >= 0) AppColors.Primary else AppColors.Expense
                )
            }
        }
    }
}

@Composable
private fun CuteFinanceItem(
    emoji: String,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(emoji, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

/**
 * 可爱目标进度部分
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuteGoalSection(
    goals: List<GoalProgressData>,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(8.dp, CartoonShape.CuteCard, spotColor = AppColors.CandyLavender.copy(alpha = 0.3f)),
        onClick = onClick,
        shape = CartoonShape.CuteCard
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
                    Text("🚀", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "目标进度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    shape = CartoonShape.Capsule,
                    color = AppColors.CandyLavender.copy(alpha = 0.3f)
                ) {
                    Text(
                        "查看 →",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = AppColors.Primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            goals.forEachIndexed { index, goal ->
                if (index > 0) {
                    Spacer(modifier = Modifier.height(14.dp))
                }
                CuteGoalItem(
                    title = goal.title,
                    progress = goal.progress,
                    progressText = goal.progressText,
                    emoji = when (index) {
                        0 -> "🥇"
                        1 -> "🥈"
                        else -> "🥉"
                    }
                )
            }
        }
    }
}

/**
 * 可爱目标进度项
 */
@Composable
private fun CuteGoalItem(
    title: String,
    progress: Float,
    progressText: String,
    emoji: String
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = progressText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(12.dp)
                .clip(CartoonShape.Capsule)
                .background(AppColors.CandyLavender.copy(alpha = 0.3f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .background(
                        brush = Brush.horizontalGradient(colors = AppColors.GradientDream),
                        shape = CartoonShape.Capsule
                    )
            )
        }
    }
}

/**
 * 可爱AI助手卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CuteAICard(onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 10.dp,
                shape = CartoonShape.CuteCard,
                spotColor = AppColors.CandyBlue.copy(alpha = 0.3f)
            ),
        onClick = onClick,
        shape = CartoonShape.CuteCard,
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        interactionSource = interactionSource
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            AppColors.CandyBlue.copy(alpha = 0.2f),
                            AppColors.CandyLavender.copy(alpha = 0.2f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 可爱的AI图标
                Surface(
                    modifier = Modifier.size(56.dp),
                    shape = CartoonShape.QuickIcon,
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(colors = AppColors.GradientCandy)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🤖", fontSize = 28.sp)
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "AI智能助手",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("✨", fontSize = 14.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "语音或文字命令，快速记账、添加待办",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = CartoonShape.Capsule,
                    color = AppColors.Primary.copy(alpha = 0.2f)
                ) {
                    Text(
                        "开始 →",
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = AppColors.Primary
                    )
                }
            }
        }
    }
}

/**
 * 可爱快捷入口数据类
 */
private data class CuteQuickItem(
    val emoji: String,
    val label: String,
    val color: Color,
    val route: String
)
