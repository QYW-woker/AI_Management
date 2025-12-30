package com.lifemanager.app.feature.finance.accounting

import androidx.compose.animation.*
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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.domain.model.*
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.time.LocalDate
import java.util.Locale
import kotlin.math.abs

/**
 * 记账主界面
 *
 * 参考"时光序"APP的设计：
 * - 左侧抽屉侧边栏，包含功能和管理模块入口
 * - 主界面显示统计数据和快捷功能入口
 * - 支持快速记账
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountingMainScreen(
    onNavigateBack: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToLedgerManagement: () -> Unit,
    onNavigateToAssetManagement: () -> Unit,
    onNavigateToRecurringTransaction: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDailyTransaction: () -> Unit,
    onNavigateToFundAccount: () -> Unit = {},
    viewModel: AccountingMainViewModel = hiltViewModel()
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val uiState by viewModel.uiState.collectAsState()
    val todayStats by viewModel.todayStats.collectAsState()
    val monthStats by viewModel.monthStats.collectAsState()
    val recentTransactions by viewModel.recentTransactions.collectAsState()
    val currentLedger by viewModel.currentLedger.collectAsState()
    val showQuickAddDialog by viewModel.showQuickAddDialog.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val editingTransaction by viewModel.editingTransaction.collectAsState()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AccountingSidebar(
                currentLedger = currentLedger,
                onNavigateToCalendar = {
                    scope.launch { drawerState.close() }
                    onNavigateToCalendar()
                },
                onNavigateToSearch = {
                    scope.launch { drawerState.close() }
                    onNavigateToSearch()
                },
                onNavigateToStatistics = {
                    scope.launch { drawerState.close() }
                    onNavigateToStatistics()
                },
                onNavigateToLedgerManagement = {
                    scope.launch { drawerState.close() }
                    onNavigateToLedgerManagement()
                },
                onNavigateToAssetManagement = {
                    scope.launch { drawerState.close() }
                    onNavigateToAssetManagement()
                },
                onNavigateToRecurringTransaction = {
                    scope.launch { drawerState.close() }
                    onNavigateToRecurringTransaction()
                },
                onNavigateToCategoryManagement = {
                    scope.launch { drawerState.close() }
                    onNavigateToCategoryManagement()
                },
                onNavigateToBudget = {
                    scope.launch { drawerState.close() }
                    onNavigateToBudget()
                },
                onNavigateToImport = {
                    scope.launch { drawerState.close() }
                    onNavigateToImport()
                },
                onNavigateToSettings = {
                    scope.launch { drawerState.close() }
                    onNavigateToSettings()
                },
                onNavigateToFundAccount = {
                    scope.launch { drawerState.close() }
                    onNavigateToFundAccount()
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = currentLedger?.name ?: "默认账本",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "菜单")
                        }
                    },
                    actions = {
                        IconButton(onClick = onNavigateToSearch) {
                            Icon(Icons.Default.Search, contentDescription = "搜索")
                        }
                        IconButton(onClick = onNavigateToCalendar) {
                            Icon(Icons.Default.CalendarMonth, contentDescription = "日历")
                        }
                    }
                )
            },
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    onClick = { viewModel.showQuickAdd() },
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("记一笔") }
                )
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 月度统计卡片
                item {
                    MonthlyStatisticsCard(
                        monthStats = monthStats,
                        onClick = onNavigateToStatistics
                    )
                }

                // 今日统计
                item {
                    TodayStatisticsCard(todayStats = todayStats)
                }

                // 快捷功能入口
                item {
                    QuickActionsSection(
                        onNavigateToDailyTransaction = onNavigateToDailyTransaction,
                        onNavigateToCalendar = onNavigateToCalendar,
                        onNavigateToBudget = onNavigateToBudget,
                        onNavigateToStatistics = onNavigateToStatistics
                    )
                }

                // 最近交易
                item {
                    RecentTransactionsSection(
                        transactions = recentTransactions,
                        onViewAll = onNavigateToDailyTransaction,
                        onTransactionClick = { transactionId ->
                            viewModel.showEditTransaction(transactionId)
                        }
                    )
                }
            }
        }
    }

    // 快速记账对话框
    if (showQuickAddDialog) {
        QuickAddTransactionDialog(
            onDismiss = { viewModel.hideQuickAdd() },
            onConfirm = { type, amount, categoryId, note, date, time, accountId ->
                viewModel.quickAddTransaction(type, amount, categoryId, note, date, time, accountId)
            },
            categories = viewModel.categories.collectAsState().value,
            accounts = viewModel.accounts.collectAsState().value
        )
    }

    // 编辑交易对话框
    if (showEditDialog && editingTransaction != null) {
        EditTransactionDialog(
            transaction = editingTransaction!!,
            onDismiss = { viewModel.hideEditDialog() },
            onConfirm = { id, type, amount, categoryId, note, date, time ->
                viewModel.updateTransaction(id, type, amount, categoryId, note, date, time)
            },
            onDelete = { id ->
                viewModel.deleteTransaction(id)
            },
            categories = viewModel.categories.collectAsState().value
        )
    }
}

/**
 * 侧边栏
 */
@Composable
private fun AccountingSidebar(
    currentLedger: LedgerInfo?,
    onNavigateToCalendar: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToLedgerManagement: () -> Unit,
    onNavigateToAssetManagement: () -> Unit,
    onNavigateToRecurringTransaction: () -> Unit,
    onNavigateToCategoryManagement: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToImport: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToFundAccount: () -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier.width(280.dp)
    ) {
        // 顶部账本信息
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = currentLedger?.name ?: "默认账本",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "记录生活每一笔",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 功能模块
        Text(
            text = "功能",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        SidebarItem(
            icon = Icons.Outlined.CalendarMonth,
            label = "记账日历",
            description = "按日期查看收支",
            onClick = onNavigateToCalendar
        )

        SidebarItem(
            icon = Icons.Outlined.Search,
            label = "搜索",
            description = "查找交易记录",
            onClick = onNavigateToSearch
        )

        SidebarItem(
            icon = Icons.Outlined.Analytics,
            label = "统计",
            description = "收支分析报表",
            onClick = onNavigateToStatistics
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // 管理模块
        Text(
            text = "管理",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        SidebarItem(
            icon = Icons.Outlined.LibraryBooks,
            label = "多账本管理",
            description = "管理多个账本",
            onClick = onNavigateToLedgerManagement
        )

        SidebarItem(
            icon = Icons.Outlined.AccountBalanceWallet,
            label = "资金账户",
            description = "管理资金账户余额",
            onClick = onNavigateToFundAccount
        )

        SidebarItem(
            icon = Icons.Outlined.AccountBalance,
            label = "资产管理",
            description = "管理资产账户",
            onClick = onNavigateToAssetManagement
        )

        SidebarItem(
            icon = Icons.Outlined.Repeat,
            label = "周期记账",
            description = "定期自动记账",
            onClick = onNavigateToRecurringTransaction
        )

        SidebarItem(
            icon = Icons.Outlined.Category,
            label = "分类管理",
            description = "管理收支分类",
            onClick = onNavigateToCategoryManagement
        )

        SidebarItem(
            icon = Icons.Outlined.PieChart,
            label = "预算管理",
            description = "设置月度预算",
            onClick = onNavigateToBudget
        )

        SidebarItem(
            icon = Icons.Outlined.FileUpload,
            label = "记账导入",
            description = "导入账单数据",
            onClick = onNavigateToImport
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        SidebarItem(
            icon = Icons.Outlined.Settings,
            label = "记账设置",
            description = "个性化设置",
            onClick = onNavigateToSettings
        )
    }
}

/**
 * 侧边栏项
 */
@Composable
private fun SidebarItem(
    icon: ImageVector,
    label: String,
    description: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
    }
}

/**
 * 月度统计卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MonthlyStatisticsCard(
    monthStats: PeriodStats,
    onClick: () -> Unit
) {
    val today = remember { LocalDate.now() }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${today.year}年${today.monthValue}月",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "查看详情",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 收入
                MonthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "收入",
                    amount = monthStats.totalIncome,
                    color = Color(0xFF4CAF50)
                )

                // 支出
                MonthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "支出",
                    amount = monthStats.totalExpense,
                    color = Color(0xFFF44336)
                )

                // 结余
                val balance = monthStats.totalIncome - monthStats.totalExpense
                MonthStatItem(
                    modifier = Modifier.weight(1f),
                    label = "结余",
                    amount = balance,
                    color = if (balance >= 0) MaterialTheme.colorScheme.primary else Color(0xFFF44336)
                )
            }
        }
    }
}

/**
 * 月度统计项
 */
@Composable
private fun MonthStatItem(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double,
    color: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = formatAmount(amount),
            style = MaterialTheme.typography.titleMedium,
            color = color,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 今日统计卡片
 */
@Composable
private fun TodayStatisticsCard(todayStats: DailyStats) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 今日支出
            StatItem(
                modifier = Modifier.weight(1f),
                label = "今日支出",
                amount = todayStats.totalExpense,
                valueColor = Color(0xFFF44336)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // 今日收入
            StatItem(
                modifier = Modifier.weight(1f),
                label = "今日收入",
                amount = todayStats.totalIncome,
                valueColor = Color(0xFF4CAF50)
            )

            Box(
                modifier = Modifier
                    .width(1.dp)
                    .height(48.dp)
                    .background(MaterialTheme.colorScheme.outlineVariant)
            )

            // 今日笔数
            StatItem(
                modifier = Modifier.weight(1f),
                label = "今日笔数",
                value = "${todayStats.transactionCount}笔",
                valueColor = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    label: String,
    amount: Double? = null,
    value: String? = null,
    valueColor: Color
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value ?: formatAmount(amount ?: 0.0),
            style = MaterialTheme.typography.titleMedium,
            color = valueColor,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * 快捷功能入口
 */
@Composable
private fun QuickActionsSection(
    onNavigateToDailyTransaction: () -> Unit,
    onNavigateToCalendar: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    Column {
        Text(
            text = "快捷入口",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            QuickActionButton(
                icon = Icons.Default.ReceiptLong,
                label = "明细",
                color = Color(0xFF2196F3),
                onClick = onNavigateToDailyTransaction
            )
            QuickActionButton(
                icon = Icons.Default.CalendarMonth,
                label = "日历",
                color = Color(0xFF4CAF50),
                onClick = onNavigateToCalendar
            )
            QuickActionButton(
                icon = Icons.Default.PieChart,
                label = "预算",
                color = Color(0xFFFF9800),
                onClick = onNavigateToBudget
            )
            QuickActionButton(
                icon = Icons.Default.Analytics,
                label = "统计",
                color = Color(0xFF9C27B0),
                onClick = onNavigateToStatistics
            )
        }
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.1f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

/**
 * 最近交易部分
 */
@Composable
private fun RecentTransactionsSection(
    transactions: List<DailyTransactionWithCategory>,
    onViewAll: () -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "最近记录",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onViewAll) {
                Text("查看全部")
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (transactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ReceiptLong,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "暂无记录",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column {
                    transactions.forEachIndexed { index, transaction ->
                        RecentTransactionItem(
                            transaction = transaction,
                            onClick = { onTransactionClick(transaction.transaction.id) }
                        )
                        if (index < transactions.size - 1) {
                            Divider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.outlineVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentTransactionItem(
    transaction: DailyTransactionWithCategory,
    onClick: () -> Unit
) {
    val isExpense = transaction.transaction.type == TransactionType.EXPENSE

    // 获取卡通图标
    val emoji = transaction.category?.let {
        com.lifemanager.app.ui.component.CategoryIcons.getIcon(
            name = it.name,
            iconName = it.iconName,
            moduleType = it.moduleType
        )
    } ?: if (isExpense) "💸" else "💰"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 分类图标 - 使用卡通emoji
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(
                    transaction.category?.let { parseColor(it.color) }
                        ?: MaterialTheme.colorScheme.primary
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = emoji,
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // 信息
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.category?.name ?: if (isExpense) "支出" else "收入",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (transaction.transaction.note.isNotBlank()) {
                Text(
                    text = transaction.transaction.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 金额 - 使用智能格式化
        Text(
            text = "${if (isExpense) "-" else "+"}${formatAmount(transaction.transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (isExpense) Color(0xFFF44336) else Color(0xFF4CAF50),
            maxLines = 1
        )
    }
}

/**
 * 快速记账对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun QuickAddTransactionDialog(
    onDismiss: () -> Unit,
    onConfirm: (type: String, amount: Double, categoryId: Long?, note: String, date: LocalDate, time: String?, accountId: Long?) -> Unit,
    categories: List<com.lifemanager.app.core.database.entity.CustomFieldEntity>,
    accounts: List<com.lifemanager.app.core.database.entity.FundAccountEntity> = emptyList()
) {
    var selectedType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var amount by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Long?>(null) }
    var selectedAccountId by remember { mutableStateOf<Long?>(null) }
    var note by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 日期选择器状态
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("快速记账") },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 类型选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedType == TransactionType.EXPENSE) {
                        Button(
                            onClick = { selectedType = TransactionType.EXPENSE },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("支出")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedType = TransactionType.EXPENSE },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("支出")
                        }
                    }

                    if (selectedType == TransactionType.INCOME) {
                        Button(
                            onClick = { selectedType = TransactionType.INCOME },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("收入")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedType = TransactionType.INCOME },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("收入")
                        }
                    }
                }

                // 金额输入
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("金额") },
                    leadingIcon = { Text("¥") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 日期和时间选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 日期选择
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "日期",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    // 时间选择
                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "时间",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedTime ?: "现在",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 分类选择
                val filteredCategories = categories.filter {
                    it.moduleType == if (selectedType == TransactionType.EXPENSE) "EXPENSE" else "INCOME"
                }

                if (filteredCategories.isNotEmpty()) {
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCategories) { category ->
                            val isSelected = selectedCategoryId == category.id
                            val emoji = com.lifemanager.app.ui.component.CategoryIcons.getIcon(
                                name = category.name,
                                iconName = category.iconName,
                                moduleType = category.moduleType
                            )
                            if (isSelected) {
                                Button(
                                    onClick = { selectedCategoryId = category.id },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("$emoji ${category.name}", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedCategoryId = category.id },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("$emoji ${category.name}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // 资金账户选择
                if (accounts.isNotEmpty()) {
                    Text(
                        text = "资金账户（选填）",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(accounts) { account ->
                            val isSelected = selectedAccountId == account.id
                            val icon = com.lifemanager.app.core.database.entity.AccountType.getIcon(account.accountType)
                            if (isSelected) {
                                Button(
                                    onClick = { selectedAccountId = null }, // 点击已选中的取消选择
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("$icon ${account.name}", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedAccountId = account.id },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("$icon ${account.name}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // 备注
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        onConfirm(selectedType, amountValue, selectedCategoryId, note, selectedDate, selectedTime, selectedAccountId)
                        onDismiss()
                    }
                }
            ) {
                Text("确认")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
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

    // 时间选择器对话框
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            }
        )
    }
}

/**
 * 时间选择器对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimePickerDialog(
    onDismiss: () -> Unit,
    onConfirm: (hour: Int, minute: Int) -> Unit
) {
    val currentTime = java.time.LocalTime.now()
    val timePickerState = rememberTimePickerState(
        initialHour = currentTime.hour,
        initialMinute = currentTime.minute
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("选择时间") },
        text = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm(timePickerState.hour, timePickerState.minute)
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

private fun parseColor(colorString: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorString))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * 智能格式化金额
 * - 小于1万：显示完整金额（如 ¥1,234.56）
 * - 1万-1亿：显示万为单位（如 ¥1.23万）
 * - 大于1亿：显示亿为单位（如 ¥1.23亿）
 */
private fun formatAmount(amount: Double): String {
    val absAmount = abs(amount)
    return when {
        absAmount >= 100_000_000 -> {
            val value = absAmount / 100_000_000
            "¥${String.format("%.2f", value)}亿"
        }
        absAmount >= 10_000 -> {
            val value = absAmount / 10_000
            "¥${String.format("%.2f", value)}万"
        }
        else -> {
            "¥${String.format("%,.2f", absAmount)}"
        }
    }
}

/**
 * 编辑交易对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditTransactionDialog(
    transaction: DailyTransactionWithCategory,
    onDismiss: () -> Unit,
    onConfirm: (id: Long, type: String, amount: Double, categoryId: Long?, note: String, date: LocalDate, time: String?) -> Unit,
    onDelete: (Long) -> Unit,
    categories: List<com.lifemanager.app.core.database.entity.CustomFieldEntity>
) {
    val entity = transaction.transaction
    var selectedType by remember { mutableStateOf(entity.type) }
    var amount by remember { mutableStateOf(entity.amount.toString()) }
    var selectedCategoryId by remember { mutableStateOf(entity.categoryId) }
    var note by remember { mutableStateOf(entity.note) }
    var selectedDate by remember { mutableStateOf(LocalDate.ofEpochDay(entity.date.toLong())) }
    var selectedTime by remember { mutableStateOf(entity.time) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate.toEpochDay() * 24 * 60 * 60 * 1000
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("编辑记录")
                IconButton(onClick = { showDeleteConfirm = true }) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "删除",
                        tint = Color(0xFFF44336)
                    )
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 类型选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedType == "EXPENSE") {
                        Button(
                            onClick = { selectedType = "EXPENSE" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("支出")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedType = "EXPENSE" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("支出")
                        }
                    }

                    if (selectedType == "INCOME") {
                        Button(
                            onClick = { selectedType = "INCOME" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("收入")
                        }
                    } else {
                        OutlinedButton(
                            onClick = { selectedType = "INCOME" },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("收入")
                        }
                    }
                }

                // 金额输入
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("金额") },
                    leadingIcon = { Text("¥") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                // 日期和时间选择
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedCard(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "日期",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "${selectedDate.monthValue}月${selectedDate.dayOfMonth}日",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    OutlinedCard(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "时间",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = selectedTime ?: "未设置",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }

                // 分类选择
                val filteredCategories = categories.filter {
                    it.moduleType == if (selectedType == "EXPENSE") "EXPENSE" else "INCOME"
                }

                if (filteredCategories.isNotEmpty()) {
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredCategories) { category ->
                            val isSelected = selectedCategoryId == category.id
                            val emoji = com.lifemanager.app.ui.component.CategoryIcons.getIcon(
                                name = category.name,
                                iconName = category.iconName,
                                moduleType = category.moduleType
                            )
                            if (isSelected) {
                                Button(
                                    onClick = { selectedCategoryId = category.id },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("$emoji ${category.name}", style = MaterialTheme.typography.bodySmall)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = { selectedCategoryId = category.id },
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text("$emoji ${category.name}", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }

                // 备注
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("备注（选填）") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amountValue = amount.toDoubleOrNull()
                    if (amountValue != null && amountValue > 0) {
                        onConfirm(entity.id, selectedType, amountValue, selectedCategoryId, note, selectedDate, selectedTime)
                    }
                }
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )

    // 日期选择器对话框
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            selectedDate = LocalDate.ofEpochDay(millis / (24 * 60 * 60 * 1000))
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

    // 时间选择器
    if (showTimePicker) {
        TimePickerDialog(
            onDismiss = { showTimePicker = false },
            onConfirm = { hour, minute ->
                selectedTime = String.format("%02d:%02d", hour, minute)
                showTimePicker = false
            }
        )
    }

    // 删除确认对话框
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条记录吗？此操作不可恢复。") },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete(entity.id)
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 账本信息
 */
data class LedgerInfo(
    val id: Long,
    val name: String,
    val icon: String? = null,
    val isDefault: Boolean = false
)
