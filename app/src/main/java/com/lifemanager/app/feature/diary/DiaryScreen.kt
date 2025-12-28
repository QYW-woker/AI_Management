package com.lifemanager.app.feature.diary

import android.net.Uri
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lifemanager.app.core.database.entity.DiaryEntity
import com.lifemanager.app.domain.model.*

/**
 * 日记主界面
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    onNavigateBack: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val diaries by viewModel.diaries.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val currentYearMonth by viewModel.currentYearMonth.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()
    val showEditDialog by viewModel.showEditDialog.collectAsState()
    val showDeleteDialog by viewModel.showDeleteDialog.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日记") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.toggleViewMode() }) {
                        Icon(
                            imageVector = if (viewMode == "LIST") Icons.Filled.CalendarMonth else Icons.Filled.List,
                            contentDescription = if (viewMode == "LIST") "日历视图" else "列表视图"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.showEditDialog() }
            ) {
                Icon(Icons.Filled.Edit, contentDescription = "写日记")
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

            // 月份选择器
            MonthSelector(
                yearMonth = currentYearMonth,
                onPreviousMonth = { viewModel.previousMonth() },
                onNextMonth = { viewModel.nextMonth() },
                formatYearMonth = { viewModel.formatYearMonth(it) }
            )

            when (uiState) {
                is DiaryUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is DiaryUiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = (uiState as DiaryUiState.Error).message,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = { viewModel.refresh() }) {
                                Text("重试")
                            }
                        }
                    }
                }

                is DiaryUiState.Success -> {
                    if (diaries.isEmpty()) {
                        EmptyState()
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(diaries, key = { it.id }) { diary ->
                                DiaryItem(
                                    diary = diary,
                                    formatDate = { viewModel.formatDate(it) },
                                    getDayOfWeek = { viewModel.getDayOfWeek(it) },
                                    onClick = { viewModel.showEditDialog(diary.date) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 编辑对话框
    if (showEditDialog) {
        EditDiaryDialog(
            viewModel = viewModel,
            onDismiss = { viewModel.hideEditDialog() }
        )
    }

    // 删除确认对话框
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.hideDeleteConfirm() },
            title = { Text("确认删除") },
            text = { Text("确定要删除这篇日记吗？") },
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
private fun StatisticsCard(statistics: DiaryStatistics) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(
                label = "总篇数",
                value = statistics.totalCount.toString(),
                icon = "📝"
            )
            StatItem(
                label = "连续",
                value = "${statistics.currentStreak}天",
                icon = "🔥"
            )
            StatItem(
                label = "平均心情",
                value = String.format("%.1f", statistics.averageMood),
                icon = getMoodEmoji(statistics.averageMood.toInt())
            )
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium
        )
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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
private fun MonthSelector(
    yearMonth: Int,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    formatYearMonth: (Int) -> String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "上个月")
            }

            Text(
                text = formatYearMonth(yearMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = onNextMonth) {
                Icon(Icons.Default.KeyboardArrowRight, contentDescription = "下个月")
            }
        }
    }
}

@Composable
private fun DiaryItem(
    diary: DiaryEntity,
    formatDate: (Int) -> String,
    getDayOfWeek: (Int) -> String,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val attachments = remember(diary.attachments) {
        parseAttachments(diary.attachments)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 日期和心情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formatDate(diary.date),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = getDayOfWeek(diary.date),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 附件数量指示
                    if (attachments.isNotEmpty()) {
                        Icon(
                            imageVector = Icons.Filled.Image,
                            contentDescription = "有附件",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = attachments.size.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    diary.weather?.let { weather ->
                        Text(
                            text = getWeatherEmoji(weather),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    diary.moodScore?.let { score ->
                        Text(
                            text = getMoodEmoji(score),
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }

            // 附件预览
            if (attachments.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(attachments.take(4), key = { it }) { attachment ->
                        val isVideo = attachment.contains("video") || attachment.endsWith(".mp4") || attachment.endsWith(".mov")
                        Box(modifier = Modifier.size(60.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(Uri.parse(attachment))
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "附件",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            if (isVideo) {
                                Icon(
                                    imageVector = Icons.Filled.PlayCircle,
                                    contentDescription = "视频",
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .size(24.dp),
                                    tint = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    if (attachments.size > 4) {
                        item {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "+${attachments.size - 4}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内容预览
            Text(
                text = diary.content,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * 解析附件JSON
 */
private fun parseAttachments(attachmentsJson: String): List<String> {
    if (attachmentsJson.isBlank() || attachmentsJson == "[]") return emptyList()
    return try {
        attachmentsJson
            .removeSurrounding("[", "]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotBlank() }
    } catch (e: Exception) {
        emptyList()
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
            Text(
                text = "📖",
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "本月还没有日记",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "点击右下角按钮开始记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

private fun getMoodEmoji(score: Int): String {
    return moodList.find { it.score == score }?.emoji ?: "😐"
}

private fun getWeatherEmoji(weather: String): String {
    return weatherList.find { it.code == weather }?.emoji ?: ""
}
