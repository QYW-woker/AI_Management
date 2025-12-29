package com.lifemanager.app.feature.todo

import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.core.database.entity.Priority
import com.lifemanager.app.core.database.entity.TodoEntity
import com.lifemanager.app.core.database.entity.TodoStatus
import com.lifemanager.app.domain.model.*

/**
 * 待办记事主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(
    onNavigateBack: () -> Unit,
    viewModel: TodoViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val todoGroups by viewModel.todoGroups.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val currentFilter by viewModel.currentFilter.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val quadrantData by viewModel.quadrantData.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("待办记事") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (viewMode == "LIST") Icons.Filled.GridView else Icons.Filled.List,
                            contentDescription = if (viewMode == "LIST") "四象限视图" else "列表视图"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showAddDialog() }
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加待办")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 统计卡片
            StatisticsCard(statistics = statistics)

            // 筛选栏
            FilterBar(
                currentFilter = currentFilter,
                onFilterChange = { viewModel.setFilter(it) }
            )

            when (uiState) {
                is TodoUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is TodoUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (uiState as TodoUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("重试")
                            }
                        }
                    }
                }

                is TodoUiState.Success -> {
                    if (viewMode == "LIST") {
                        TodoListView(
                            groups = todoGroups,
                            viewModel = viewModel
                        )
                    } else {
                        QuadrantView(
                            data = quadrantData,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }

    // 添加/编辑对话框
    if (showEditDialog) {
        AddEditTodoDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideEditDialog() }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条待办吗？") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.hideDeleteConfirm() }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun StatisticsCard(statistics: TodoStatistics) {
    val completionRate = if (statistics.todayTotal > 0)
        statistics.todayCompleted.toFloat() / statistics.todayTotal else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = completionRate,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(20.dp),
                spotColor = Color(0xFFF59E0B).copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFFF59E0B),
                            Color(0xFFEF4444)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = "任务概览",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        icon = Icons.Default.Assignment,
                        label = "待完成",
                        value = statistics.totalPending.toString(),
                        color = Color(0xFFFCD34D)
                    )
                    StatItem(
                        icon = Icons.Default.CheckCircle,
                        label = "今日完成",
                        value = "${statistics.todayCompleted}/${statistics.todayTotal}",
                        color = Color(0xFF4ADE80)
                    )
                    StatItem(
                        icon = Icons.Default.TrendingUp,
                        label = "完成率",
                        value = "${(completionRate * 100).toInt()}%",
                        color = Color(0xFF60A5FA)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 进度条
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.White.copy(alpha = 0.3f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress)
                            .background(Color.White, RoundedCornerShape(3.dp))
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    icon: ImageVector,
    label: String,
    value: String,
    color: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(40.dp)
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
                modifier = Modifier.size(20.dp)
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

@Composable
private fun FilterBar(
    currentFilter: TodoFilter,
    onFilterChange: (TodoFilter) -> Unit
) {
    data class FilterItem(
        val filter: TodoFilter,
        val label: String,
        val icon: ImageVector,
        val color: Color
    )

    val filters = remember {
        listOf(
            FilterItem(TodoFilter.ALL, "全部", Icons.Default.ViewList, Color(0xFF6366F1)),
            FilterItem(TodoFilter.TODAY, "今日", Icons.Default.Today, Color(0xFF10B981)),
            FilterItem(TodoFilter.UPCOMING, "计划", Icons.Default.Event, Color(0xFF3B82F6)),
            FilterItem(TodoFilter.OVERDUE, "逾期", Icons.Default.Warning, Color(0xFFEF4444)),
            FilterItem(TodoFilter.COMPLETED, "完成", Icons.Default.Done, Color(0xFF8B5CF6))
        )
    }

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(filters, key = { it.filter }) { item ->
            val isSelected = currentFilter == item.filter
            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) item.color else item.color.copy(alpha = 0.1f),
                label = "bgColor"
            )
            val contentColor by animateColorAsState(
                targetValue = if (isSelected) Color.White else item.color,
                label = "contentColor"
            )

            Surface(
                onClick = { onFilterChange(item.filter) },
                shape = RoundedCornerShape(12.dp),
                color = backgroundColor,
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        tint = contentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun TodoListView(
    groups: List<TodoGroup>,
    viewModel: TodoViewModel
) {
    if (groups.isEmpty()) {
        EmptyState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            groups.forEach { group ->
                item(key = "header_${group.title}") {
                    Text(
                        text = group.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(group.todos, key = { it.id }) { todo ->
                    TodoItem(
                        todo = todo,
                        isOverdue = viewModel.isOverdue(todo),
                        onToggleComplete = { viewModel.toggleComplete(todo.id) },
                        onClick = { viewModel.showEditDialog(todo.id) },
                        onDelete = { viewModel.showDeleteConfirm(todo.id) },
                        formatDueDate = { viewModel.formatDueDate(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuadrantView(
    data: QuadrantData,
    viewModel: TodoViewModel
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuadrantCard(
                title = "重要且紧急",
                color = Color(0xFFF44336),
                todos = data.importantUrgent,
                modifier = Modifier.weight(1f),
                onAddClick = { viewModel.showAddDialogWithQuadrant("IMPORTANT_URGENT") },
                onTodoClick = { viewModel.showEditDialog(it) },
                onToggleComplete = { viewModel.toggleComplete(it) }
            )
            QuadrantCard(
                title = "重要不紧急",
                color = Color(0xFF2196F3),
                todos = data.importantNotUrgent,
                modifier = Modifier.weight(1f),
                onAddClick = { viewModel.showAddDialogWithQuadrant("IMPORTANT_NOT_URGENT") },
                onTodoClick = { viewModel.showEditDialog(it) },
                onToggleComplete = { viewModel.toggleComplete(it) }
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuadrantCard(
                title = "不重要但紧急",
                color = Color(0xFFFF9800),
                todos = data.notImportantUrgent,
                modifier = Modifier.weight(1f),
                onAddClick = { viewModel.showAddDialogWithQuadrant("NOT_IMPORTANT_URGENT") },
                onTodoClick = { viewModel.showEditDialog(it) },
                onToggleComplete = { viewModel.toggleComplete(it) }
            )
            QuadrantCard(
                title = "不重要不紧急",
                color = Color(0xFF9E9E9E),
                todos = data.notImportantNotUrgent,
                modifier = Modifier.weight(1f),
                onAddClick = { viewModel.showAddDialogWithQuadrant("NOT_IMPORTANT_NOT_URGENT") },
                onTodoClick = { viewModel.showEditDialog(it) },
                onToggleComplete = { viewModel.toggleComplete(it) }
            )
        }
    }
}

@Composable
private fun QuadrantCard(
    title: String,
    color: Color,
    todos: List<TodoEntity>,
    modifier: Modifier = Modifier,
    onAddClick: () -> Unit,
    onTodoClick: (Long) -> Unit,
    onToggleComplete: (Long) -> Unit
) {
    Card(
        modifier = modifier.fillMaxHeight(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Add,
                        contentDescription = "添加",
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(todos, key = { it.id }) { todo ->
                    QuadrantTodoItem(
                        todo = todo,
                        onClick = { onTodoClick(todo.id) },
                        onToggle = { onToggleComplete(todo.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun QuadrantTodoItem(
    todo: TodoEntity,
    onClick: () -> Unit,
    onToggle: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = todo.status == TodoStatus.COMPLETED,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = todo.title,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textDecoration = if (todo.status == TodoStatus.COMPLETED)
                TextDecoration.LineThrough else TextDecoration.None
        )
    }
}

@Composable
private fun TodoItem(
    todo: TodoEntity,
    isOverdue: Boolean,
    onToggleComplete: () -> Unit,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    formatDueDate: (Int?) -> String
) {
    val isCompleted = todo.status == TodoStatus.COMPLETED
    val priorityColor = when (todo.priority) {
        Priority.HIGH -> Color(0xFFEF4444)
        Priority.MEDIUM -> Color(0xFFF59E0B)
        Priority.LOW -> Color(0xFF10B981)
        else -> MaterialTheme.colorScheme.outline
    }

    val cardBackground by animateColorAsState(
        targetValue = if (isCompleted)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        else MaterialTheme.colorScheme.surface,
        label = "cardBg"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isCompleted) 0.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = priorityColor.copy(alpha = 0.15f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackground)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 自定义复选框
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (isCompleted) priorityColor
                        else priorityColor.copy(alpha = 0.15f)
                    )
                    .clickable(onClick = onToggleComplete),
                contentAlignment = Alignment.Center
            ) {
                if (isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // 标题和描述
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = todo.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                    color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    else MaterialTheme.colorScheme.onSurface
                )

                if (todo.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = todo.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                todo.dueDate?.let { date ->
                    Spacer(modifier = Modifier.height(6.dp))
                    Surface(
                        color = if (isOverdue) Color(0xFFEF4444).copy(alpha = 0.1f)
                        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isOverdue) Icons.Default.Warning else Icons.Filled.Schedule,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = if (isOverdue) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = formatDueDate(date),
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isOverdue) Color(0xFFEF4444) else MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            // 优先级徽章
            if (todo.priority != Priority.NONE) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(priorityColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(8.dp))
            }

            // 删除按钮
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "暂无待办事项",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角按钮添加待办",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
