package com.lifemanager.app.feature.diary

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.lifemanager.app.domain.model.moodList
import com.lifemanager.app.domain.model.weatherList
import com.lifemanager.app.ui.component.*
import com.lifemanager.app.ui.theme.*

/**
 * 日记编辑全屏页面 - 简洁设计版本
 *
 * 设计原则:
 * - 干净、克制、有呼吸感
 * - 更大的内容编辑区域
 * - 使用Material图标代替emoji
 * - 沉浸式编辑体验
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CleanEditDiaryScreen(
    diaryDate: Int? = null,
    onNavigateBack: () -> Unit,
    viewModel: DiaryViewModel = hiltViewModel()
) {
    val editState by viewModel.editState.collectAsState()

    // 如果传入日期，初始化编辑状态
    LaunchedEffect(diaryDate) {
        diaryDate?.let { viewModel.initEditState(it) }
    }

    Scaffold(
        containerColor = CleanColors.background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = viewModel.formatDate(editState.date),
                            style = CleanTypography.title,
                            color = CleanColors.textPrimary
                        )
                        Text(
                            text = viewModel.getDayOfWeek(editState.date),
                            style = CleanTypography.caption,
                            color = CleanColors.textTertiary
                        )
                    }
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
                    if (editState.isSaving) {
                        CleanLoadingIndicator(
                            size = 20.dp,
                            modifier = Modifier.padding(end = Spacing.lg)
                        )
                    } else {
                        CleanTextButton(
                            text = "保存",
                            onClick = {
                                viewModel.saveDiary()
                                onNavigateBack()
                            },
                            color = CleanColors.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CleanColors.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // 错误提示
            editState.error?.let { error ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = Spacing.pageHorizontal),
                    shape = RoundedCornerShape(Radius.sm),
                    color = CleanColors.error.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(Spacing.md),
                        style = CleanTypography.secondary,
                        color = CleanColors.error
                    )
                }
                Spacer(modifier = Modifier.height(Spacing.lg))
            }

            // 心情选择
            CleanMoodSection(
                selectedMood = editState.moodScore,
                onMoodSelect = { score ->
                    viewModel.updateEditMood(
                        if (editState.moodScore == score) null else score
                    )
                }
            )

            Spacer(modifier = Modifier.height(Spacing.sectionGap))

            // 天气选择
            CleanWeatherSection(
                selectedWeather = editState.weather,
                onWeatherSelect = { code ->
                    viewModel.updateEditWeather(
                        if (editState.weather == code) null else code
                    )
                }
            )

            Spacer(modifier = Modifier.height(Spacing.sectionGap))

            // 附件区域
            CleanAttachmentSection(
                attachments = editState.attachments,
                onAddAttachment = { uri -> viewModel.addAttachment(uri) },
                onRemoveAttachment = { uri -> viewModel.removeAttachment(uri) }
            )

            Spacer(modifier = Modifier.height(Spacing.sectionGap))

            // 日记内容 - 占据更大空间
            CleanContentSection(
                content = editState.content,
                onContentChange = { viewModel.updateEditContent(it) }
            )

            Spacer(modifier = Modifier.height(Spacing.bottomSafe))
        }
    }
}

/**
 * 心情选择区域 - 使用图标代替emoji
 */
@Composable
private fun CleanMoodSection(
    selectedMood: Int?,
    onMoodSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.pageHorizontal)
    ) {
        Text(
            text = "今天的心情",
            style = CleanTypography.title,
            color = CleanColors.textPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            MoodIconItem(
                icon = Icons.Outlined.SentimentVeryDissatisfied,
                label = "很差",
                score = 1,
                color = CleanColors.textTertiary,
                selected = selectedMood == 1,
                onClick = { onMoodSelect(1) }
            )
            MoodIconItem(
                icon = Icons.Outlined.SentimentDissatisfied,
                label = "不好",
                score = 2,
                color = CleanColors.textSecondary,
                selected = selectedMood == 2,
                onClick = { onMoodSelect(2) }
            )
            MoodIconItem(
                icon = Icons.Outlined.SentimentNeutral,
                label = "一般",
                score = 3,
                color = CleanColors.warning,
                selected = selectedMood == 3,
                onClick = { onMoodSelect(3) }
            )
            MoodIconItem(
                icon = Icons.Outlined.SentimentSatisfied,
                label = "不错",
                score = 4,
                color = CleanColors.success,
                selected = selectedMood == 4,
                onClick = { onMoodSelect(4) }
            )
            MoodIconItem(
                icon = Icons.Outlined.SentimentVerySatisfied,
                label = "很棒",
                score = 5,
                color = CleanColors.primary,
                selected = selectedMood == 5,
                onClick = { onMoodSelect(5) }
            )
        }
    }
}

@Composable
private fun MoodIconItem(
    icon: ImageVector,
    label: String,
    score: Int,
    color: Color,
    selected: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(Radius.md))
            .clickable(onClick = onClick)
            .background(
                if (selected) color.copy(alpha = 0.12f) else Color.Transparent
            )
            .padding(Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) color else CleanColors.textTertiary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(modifier = Modifier.height(Spacing.xs))
        Text(
            text = label,
            style = CleanTypography.caption,
            color = if (selected) color else CleanColors.textTertiary
        )
    }
}

/**
 * 天气选择区域 - 使用图标代替emoji
 */
@Composable
private fun CleanWeatherSection(
    selectedWeather: String?,
    onWeatherSelect: (String) -> Unit
) {
    data class WeatherItem(
        val code: String,
        val icon: ImageVector,
        val label: String
    )

    val weatherItems = remember {
        listOf(
            WeatherItem("SUNNY", Icons.Outlined.WbSunny, "晴"),
            WeatherItem("CLOUDY", Icons.Outlined.Cloud, "多云"),
            WeatherItem("OVERCAST", Icons.Outlined.CloudQueue, "阴"),
            WeatherItem("RAINY", Icons.Outlined.Umbrella, "雨"),
            WeatherItem("SNOWY", Icons.Outlined.AcUnit, "雪"),
            WeatherItem("WINDY", Icons.Outlined.Air, "风")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.pageHorizontal)
    ) {
        Text(
            text = "今天的天气",
            style = CleanTypography.title,
            color = CleanColors.textPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            items(weatherItems, key = { it.code }) { weather ->
                val isSelected = selectedWeather == weather.code

                Surface(
                    onClick = { onWeatherSelect(weather.code) },
                    shape = RoundedCornerShape(Radius.sm),
                    color = if (isSelected) CleanColors.primary else CleanColors.surface,
                    shadowElevation = if (isSelected) Elevation.none else Elevation.xs
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.sm),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        Icon(
                            imageVector = weather.icon,
                            contentDescription = weather.label,
                            tint = if (isSelected) CleanColors.onPrimary else CleanColors.textSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = weather.label,
                            style = CleanTypography.button,
                            color = if (isSelected) CleanColors.onPrimary else CleanColors.textSecondary
                        )
                    }
                }
            }
        }
    }
}

/**
 * 附件区域
 */
@Composable
private fun CleanAttachmentSection(
    attachments: List<String>,
    onAddAttachment: (String) -> Unit,
    onRemoveAttachment: (String) -> Unit
) {
    val context = LocalContext.current

    // 图片选择器
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 9)
    ) { uris ->
        uris.forEach { uri ->
            context.contentResolver.takePersistableUriPermission(
                uri,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onAddAttachment(uri.toString())
        }
    }

    // 视频选择器
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            context.contentResolver.takePersistableUriPermission(
                it,
                android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            onAddAttachment(it.toString())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.pageHorizontal)
    ) {
        Text(
            text = "图片/视频",
            style = CleanTypography.title,
            color = CleanColors.textPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
        ) {
            // 添加图片按钮
            item {
                CleanAddMediaButton(
                    icon = Icons.Outlined.Image,
                    label = "图片",
                    onClick = {
                        imagePickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    }
                )
            }

            // 添加视频按钮
            item {
                CleanAddMediaButton(
                    icon = Icons.Outlined.Videocam,
                    label = "视频",
                    onClick = {
                        videoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        )
                    }
                )
            }

            // 已添加的附件
            items(attachments, key = { it }) { attachment ->
                CleanAttachmentItem(
                    uri = attachment,
                    onRemove = { onRemoveAttachment(attachment) }
                )
            }
        }
    }
}

@Composable
private fun CleanAddMediaButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .size(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(Radius.md),
        color = CleanColors.surfaceVariant
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                modifier = Modifier.size(IconSize.md),
                tint = CleanColors.textTertiary
            )
            Spacer(modifier = Modifier.height(Spacing.xs))
            Text(
                text = label,
                style = CleanTypography.caption,
                color = CleanColors.textTertiary
            )
        }
    }
}

@Composable
private fun CleanAttachmentItem(
    uri: String,
    onRemove: () -> Unit
) {
    val context = LocalContext.current
    val isVideo = uri.contains("video") || uri.endsWith(".mp4") || uri.endsWith(".mov")

    Box(modifier = Modifier.size(72.dp)) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(Radius.md),
            color = CleanColors.surfaceVariant
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(Uri.parse(uri))
                        .crossfade(true)
                        .build(),
                    contentDescription = "附件",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // 视频标识
                if (isVideo) {
                    Icon(
                        imageVector = Icons.Filled.PlayCircle,
                        contentDescription = "视频",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(28.dp),
                        tint = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
        }

        // 删除按钮
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = 4.dp, y = (-4).dp)
                .size(18.dp)
                .clip(CircleShape)
                .background(CleanColors.error)
                .clickable(onClick = onRemove),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Close,
                contentDescription = "删除",
                modifier = Modifier.size(12.dp),
                tint = Color.White
            )
        }
    }
}

/**
 * 内容编辑区域 - 更大的编辑空间
 */
@Composable
private fun CleanContentSection(
    content: String,
    onContentChange: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.pageHorizontal)
    ) {
        Text(
            text = "日记内容",
            style = CleanTypography.title,
            color = CleanColors.textPrimary
        )

        Spacer(modifier = Modifier.height(Spacing.md))

        // 大的内容输入区域
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 300.dp),
            shape = RoundedCornerShape(Radius.md),
            color = CleanColors.surface,
            shadowElevation = Elevation.xs
        ) {
            OutlinedTextField(
                value = content,
                onValueChange = onContentChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 300.dp),
                placeholder = {
                    Text(
                        text = "记录今天的点滴...",
                        style = CleanTypography.body,
                        color = CleanColors.textTertiary
                    )
                },
                textStyle = CleanTypography.body.copy(color = CleanColors.textPrimary),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    cursorColor = CleanColors.primary,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                singleLine = false
            )
        }

        Spacer(modifier = Modifier.height(Spacing.sm))

        // 字数统计
        Text(
            text = "${content.length} 字",
            style = CleanTypography.caption,
            color = CleanColors.textTertiary,
            modifier = Modifier.align(Alignment.End)
        )
    }
}
