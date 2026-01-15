package com.lifemanager.app.feature.finance.asset

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.domain.model.AssetFieldStats
import com.lifemanager.app.domain.model.AssetStats
import com.lifemanager.app.domain.model.AssetUiState
import com.lifemanager.app.domain.model.MonthlyAssetWithField
import com.lifemanager.app.ui.component.charts.PieChartView
import com.lifemanager.app.ui.component.charts.PieChartData
import com.lifemanager.app.ui.component.charts.TrendLineChart
import com.lifemanager.app.ui.component.charts.LineChartSeries
import com.lifemanager.app.domain.model.NetWorthTrendPoint
import com.lifemanager.app.ui.theme.*
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext
import java.text.NumberFormat
import java.util.Locale

/**
 * 月度资产主界面 - 简洁设计版本
 *
 * 设计原则:
 * - 与首页保持一致的CleanColors设计系统
 * - 干净、克制、有呼吸感
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonthlyAssetScreen(
    onNavigateBack: () -> Unit,
    viewModel: MonthlyAssetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val records by viewModel.records.collectAsState()
    val assetStats by viewModel.assetStats.collectAsState()
    val assetFieldStats by viewModel.assetFieldStats.collectAsState()
    val liabilityFieldStats by viewModel.liabilityFieldStats.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()
    val showCopyDialog by viewModel.showCopyDialog.collectAsState()
    val netWorthTrend by viewModel.netWorthTrend.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    val context = LocalContext.current

    Scaffold(
        containerColor = CleanColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "月度资产",
                        style = CleanTypography.title,
                        color = CleanColors.textPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "返回",
                            tint = CleanColors.textSecondary
                        )
                    }
                },
                actions = {
                    // 导出报表按钮
                    IconButton(
                        onClick = {
                            val success = AssetExportUtil.exportAndShare(
                                context = context,
                                yearMonth = currentYearMonth,
                                stats = assetStats,
                                records = records,
                                trend = netWorthTrend
                            )
                            if (!success) {
                                Toast.makeText(context, "导出失败", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(
                            Icons.Outlined.FileDownload,
                            contentDescription = "导出报表",
                            tint = CleanColors.textSecondary
                        )
                    }
                    // 复制上月数据按钮
                    IconButton(onClick = { viewModel.showCopyFromPreviousMonth() }) {
                        Icon(
                            Icons.Outlined.ContentCopy,
                            contentDescription = "复制上月数据",
                            tint = CleanColors.textSecondary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.background
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog(isAsset = selectedTab == 0) },
                containerColor = CleanColors.primary,
                contentColor = CleanColors.onPrimary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加记录")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 月份选择器
            CleanMonthSelector(
                yearMonth = currentYearMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                formatYearMonth = { viewModel.formatYearMonth(it) }
            )

            when (uiState) {
                is AssetUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = CleanColors.primary)
                    }
                }

                is AssetUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (uiState as AssetUiState.Error).message,
                                style = CleanTypography.body,
                                color = CleanColors.error
                            )
                            Spacer(modifier = Modifier.height(Spacing.lg))
                            Button(
                                onClick = { viewModel.refresh() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CleanColors.primary,
                                    contentColor = CleanColors.onPrimary
                                ),
                                shape = RoundedCornerShape(Radius.sm)
                            ) {
                                Text("重试")
                            }
                        }
                    }
                }

                is AssetUiState.Success -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            horizontal = Spacing.pageHorizontal,
                            vertical = Spacing.pageVertical
                        ),
                        verticalArrangement = Arrangement.spacedBy(Spacing.sectionGap)
                    ) {
                        // 统计卡片
                        item {
                            CleanAssetStatsCard(stats = assetStats)
                        }

                        // 净资产趋势图
                        if (netWorthTrend.isNotEmpty()) {
                            item {
                                CleanNetWorthTrendCard(trendData = netWorthTrend)
                            }
                        }

                        // 标签页切换
                        item {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(Radius.md),
                                color = CleanColors.surface,
                                shadowElevation = Elevation.xs
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(Spacing.xs)
                                ) {
                                    CleanTabButton(
                                        text = "资产",
                                        selected = selectedTab == 0,
                                        onClick = { selectedTab = 0 },
                                        modifier = Modifier.weight(1f)
                                    )
                                    CleanTabButton(
                                        text = "负债",
                                        selected = selectedTab == 1,
                                        onClick = { selectedTab = 1 },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }

                        // 分类统计图表
                        item {
                            val chartData = if (selectedTab == 0) assetFieldStats else liabilityFieldStats
                            if (chartData.isNotEmpty()) {
                                CleanFieldStatsChart(
                                    title = if (selectedTab == 0) "资产分类" else "负债分类",
                                    stats = chartData
                                )
                            }
                        }

                        // 记录列表标题
                        item {
                            Text(
                                text = "详细记录",
                                style = CleanTypography.title,
                                color = CleanColors.textPrimary
                            )
                        }

                        // 筛选当前类型的记录
                        val filteredRecords = records.filter {
                            if (selectedTab == 0) {
                                it.record.type == "ASSET"
                            } else {
                                it.record.type == "LIABILITY"
                            }
                        }

                        if (filteredRecords.isEmpty()) {
                            item {
                                CleanEmptyState(
                                    message = if (selectedTab == 0) "暂无资产记录" else "暂无负债记录",
                                    hint = "点击右下角添加"
                                )
                            }
                        } else {
                            items(filteredRecords, key = { it.record.id }) { record ->
                                CleanRecordItem(
                                    record = record,
                                    onClick = { viewModel.showEditDialog(record.record.id) },
                                    onDelete = { viewModel.showDeleteConfirm(record.record.id) }
                                )
                            }
                        }

                        // 底部安全间距
                        item {
                            Spacer(modifier = Modifier.height(Spacing.bottomSafe))
                        }
                    }
                }
            }
        }
    }

    // 添加/编辑对话框
    if (showEditDialog) {
        AddEditAssetDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideEditDialog() }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("确认删除", style = CleanTypography.title) },
            text = { Text("确定要删除这条记录吗？", style = CleanTypography.body) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(contentColor = CleanColors.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) {
                    Text("取消", color = CleanColors.textSecondary)
                }
            },
            containerColor = CleanColors.surface
        )
    }

    // 复制上月数据对话框
    if (showCopyDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideCopyDialog() },
            title = { Text("复制上月数据", style = CleanTypography.title) },
            text = { Text("将上月的资产负债数据复制到本月，方便快速填写。是否继续？", style = CleanTypography.body) },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmCopyFromPreviousMonth() },
                    colors = ButtonDefaults.textButtonColors(contentColor = CleanColors.primary)
                ) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideCopyDialog() }) {
                    Text("取消", color = CleanColors.textSecondary)
                }
            },
            containerColor = CleanColors.surface
        )
    }
}

/**
 * 简洁月份选择器
 */
@Composable
private fun CleanMonthSelector(
    yearMonth: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    formatYearMonth: (Int) -> String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = Spacing.sm),
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
                text = formatYearMonth(yearMonth),
                style = CleanTypography.title,
                color = CleanColors.textPrimary,
                fontWeight = FontWeight.SemiBold
            )

            IconButton(onClick = onNextMonth) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "下个月",
                    tint = CleanColors.textSecondary
                )
            }
        }
    }
}

/**
 * 简洁标签按钮
 */
@Composable
private fun CleanTabButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(Radius.sm))
            .clickable(onClick = onClick),
        color = if (selected) CleanColors.primary else Color.Transparent,
        shape = RoundedCornerShape(Radius.sm)
    ) {
        Box(
            modifier = Modifier.padding(vertical = Spacing.md),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = CleanTypography.button,
                color = if (selected) CleanColors.onPrimary else CleanColors.textSecondary
            )
        }
    }
}

/**
 * 简洁资产统计卡片
 */
@Composable
private fun CleanAssetStatsCard(stats: AssetStats) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            // 净资产
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "净资产",
                    style = CleanTypography.caption,
                    color = CleanColors.textTertiary
                )
                Spacer(modifier = Modifier.height(Spacing.xs))
                Text(
                    text = "¥${numberFormat.format(stats.netWorth.toLong())}",
                    style = CleanTypography.amountLarge,
                    color = if (stats.netWorth >= 0) CleanColors.primary else CleanColors.error,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))
            Divider(color = CleanColors.divider)
            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // 资产
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "总资产",
                        style = CleanTypography.caption,
                        color = CleanColors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "¥${numberFormat.format(stats.totalAssets.toLong())}",
                        style = CleanTypography.amountMedium,
                        color = CleanColors.success
                    )
                }

                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(CleanColors.borderLight)
                )

                // 负债
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "总负债",
                        style = CleanTypography.caption,
                        color = CleanColors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = "¥${numberFormat.format(stats.totalLiabilities.toLong())}",
                        style = CleanTypography.amountMedium,
                        color = CleanColors.error
                    )
                }

                // 分隔线
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(36.dp)
                        .background(CleanColors.borderLight)
                )

                // 负债率
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "负债率",
                        style = CleanTypography.caption,
                        color = CleanColors.textTertiary
                    )
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = String.format("%.1f%%", stats.debtRatio),
                        style = CleanTypography.amountMedium,
                        color = when {
                            stats.debtRatio < 30 -> CleanColors.success
                            stats.debtRatio < 50 -> CleanColors.warning
                            else -> CleanColors.error
                        }
                    )
                }
            }
        }
    }
}

/**
 * 简洁净资产趋势图卡片
 */
@Composable
private fun CleanNetWorthTrendCard(trendData: List<NetWorthTrendPoint>) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "净资产趋势",
                    style = CleanTypography.title,
                    color = CleanColors.textPrimary
                )
                Text(
                    text = "近${trendData.size}个月",
                    style = CleanTypography.caption,
                    color = CleanColors.textTertiary
                )
            }

            Spacer(modifier = Modifier.height(Spacing.lg))

            if (trendData.size >= 2) {
                val firstValue = trendData.firstOrNull()?.netWorth ?: 0.0
                val lastValue = trendData.lastOrNull()?.netWorth ?: 0.0
                val change = lastValue - firstValue
                val changePercent = if (firstValue != 0.0) {
                    (change / kotlin.math.abs(firstValue)) * 100
                } else 0.0

                // 趋势指示
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = when {
                                change > 0 -> Icons.Default.TrendingUp
                                change < 0 -> Icons.Default.TrendingDown
                                else -> Icons.Default.TrendingFlat
                            },
                            contentDescription = null,
                            tint = when {
                                change > 0 -> CleanColors.success
                                change < 0 -> CleanColors.error
                                else -> CleanColors.textTertiary
                            },
                            modifier = Modifier.size(IconSize.sm)
                        )
                        Spacer(modifier = Modifier.width(Spacing.xs))
                        Text(
                            text = when {
                                change > 0 -> "+¥${numberFormat.format(change.toLong())}"
                                change < 0 -> "-¥${numberFormat.format((-change).toLong())}"
                                else -> "¥0"
                            },
                            style = CleanTypography.secondary,
                            fontWeight = FontWeight.Medium,
                            color = when {
                                change > 0 -> CleanColors.success
                                change < 0 -> CleanColors.error
                                else -> CleanColors.textTertiary
                            }
                        )
                    }
                    Text(
                        text = String.format("%+.1f%%", changePercent),
                        style = CleanTypography.caption,
                        color = when {
                            change > 0 -> CleanColors.success
                            change < 0 -> CleanColors.error
                            else -> CleanColors.textTertiary
                        }
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.md))

                // 折线图
                TrendLineChart(
                    series = listOf(
                        LineChartSeries(
                            label = "净资产",
                            values = trendData.map { it.netWorth.toFloat() },
                            color = CleanColors.primary
                        )
                    ),
                    xLabels = trendData.map { it.formatMonth() },
                    modifier = Modifier.height(180.dp),
                    showLegend = false
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "数据不足，至少需要2个月的记录",
                        style = CleanTypography.secondary,
                        color = CleanColors.textTertiary
                    )
                }
            }
        }
    }
}

/**
 * 简洁字段统计图表
 */
@Composable
private fun CleanFieldStatsChart(
    title: String,
    stats: List<AssetFieldStats>
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg)
        ) {
            Text(
                text = title,
                style = CleanTypography.title,
                color = CleanColors.textPrimary
            )

            Spacer(modifier = Modifier.height(Spacing.lg))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .padding(Spacing.sm)
                ) {
                    PieChartView(
                        data = stats.map {
                            PieChartData(
                                label = it.fieldName,
                                value = it.amount,
                                color = parseColor(it.fieldColor)
                            )
                        },
                        modifier = Modifier.fillMaxSize(),
                        showLegend = false
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = Spacing.lg),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm)
                ) {
                    stats.take(5).forEach { stat ->
                        CleanLegendItem(stat = stat)
                    }
                    if (stats.size > 5) {
                        Text(
                            text = "...还有${stats.size - 5}项",
                            style = CleanTypography.caption,
                            color = CleanColors.textTertiary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 简洁图例项
 */
@Composable
private fun CleanLegendItem(stat: AssetFieldStats) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(parseColor(stat.fieldColor))
        )
        Spacer(modifier = Modifier.width(Spacing.sm))
        Text(
            text = stat.fieldName,
            style = CleanTypography.caption,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = CleanColors.textSecondary
        )
        Text(
            text = String.format("%.1f%%", stat.percentage),
            style = CleanTypography.caption,
            color = CleanColors.textTertiary
        )
    }
}

/**
 * 简洁记录项
 */
@Composable
private fun CleanRecordItem(
    record: MonthlyAssetWithField,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surface,
        shadowElevation = Elevation.xs
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 图标
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(Radius.sm))
                    .background(
                        record.field?.let { parseColor(it.color).copy(alpha = 0.15f) }
                            ?: CleanColors.primaryLight
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (record.record.type == "ASSET") {
                        Icons.Outlined.AccountBalance
                    } else {
                        Icons.Outlined.CreditCard
                    },
                    contentDescription = null,
                    tint = record.field?.let { parseColor(it.color) } ?: CleanColors.primary,
                    modifier = Modifier.size(IconSize.md)
                )
            }

            Spacer(modifier = Modifier.width(Spacing.md))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = record.field?.name ?: "未分类",
                    style = CleanTypography.body,
                    color = CleanColors.textPrimary,
                    fontWeight = FontWeight.Medium
                )
                if (record.record.note.isNotBlank()) {
                    Spacer(modifier = Modifier.height(Spacing.xs))
                    Text(
                        text = record.record.note,
                        style = CleanTypography.caption,
                        color = CleanColors.textTertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Text(
                text = "¥${numberFormat.format(record.record.amount.toLong())}",
                style = CleanTypography.amountMedium,
                color = if (record.record.type == "ASSET") CleanColors.success else CleanColors.error
            )

            Spacer(modifier = Modifier.width(Spacing.sm))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = "删除",
                    tint = CleanColors.textTertiary,
                    modifier = Modifier.size(IconSize.sm)
                )
            }
        }
    }
}

/**
 * 简洁空状态提示
 */
@Composable
private fun CleanEmptyState(message: String, hint: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.xxl),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Outlined.AccountBalance,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = CleanColors.textPlaceholder
            )
            Spacer(modifier = Modifier.height(Spacing.md))
            Text(
                text = message,
                style = CleanTypography.body,
                color = CleanColors.textTertiary
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = hint,
                style = CleanTypography.caption,
                color = CleanColors.textPlaceholder
            )
        }
    }
}

/**
 * 解析颜色字符串
 */
private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        CleanColors.primary
    }
}
