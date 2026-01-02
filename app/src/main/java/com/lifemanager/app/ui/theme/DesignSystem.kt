package com.lifemanager.app.ui.theme

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * 统一设计规范
 *
 * 定义全应用统一的圆角、间距、尺寸等设计常量
 */
object AppDimens {

    // ==================== 圆角规范 ====================
    /** 小圆角 - 用于标签、小按钮 */
    val CornerSmall: Dp = 8.dp
    /** 中圆角 - 用于卡片、输入框 */
    val CornerMedium: Dp = 12.dp
    /** 大圆角 - 用于大卡片、底部弹窗 */
    val CornerLarge: Dp = 16.dp
    /** 超大圆角 - 用于全屏弹窗 */
    val CornerExtraLarge: Dp = 24.dp

    // ==================== 间距规范 ====================
    /** 极小间距 */
    val SpacingXSmall: Dp = 4.dp
    /** 小间距 */
    val SpacingSmall: Dp = 8.dp
    /** 中间距 */
    val SpacingMedium: Dp = 12.dp
    /** 标准间距 */
    val SpacingNormal: Dp = 16.dp
    /** 大间距 */
    val SpacingLarge: Dp = 20.dp
    /** 超大间距 */
    val SpacingXLarge: Dp = 24.dp
    /** 巨大间距 */
    val SpacingXXLarge: Dp = 32.dp

    // ==================== 页面内边距 ====================
    /** 页面水平内边距 */
    val PageHorizontalPadding: Dp = 16.dp
    /** 页面垂直内边距 */
    val PageVerticalPadding: Dp = 16.dp
    /** 卡片内边距 */
    val CardPadding: Dp = 16.dp

    // ==================== 组件尺寸 ====================
    /** 图标小尺寸 */
    val IconSmall: Dp = 16.dp
    /** 图标中尺寸 */
    val IconMedium: Dp = 24.dp
    /** 图标大尺寸 */
    val IconLarge: Dp = 32.dp
    /** 图标超大尺寸 */
    val IconXLarge: Dp = 48.dp
    /** 图标巨大尺寸 */
    val IconXXLarge: Dp = 64.dp

    /** 头像小尺寸 */
    val AvatarSmall: Dp = 32.dp
    /** 头像中尺寸 */
    val AvatarMedium: Dp = 40.dp
    /** 头像大尺寸 */
    val AvatarLarge: Dp = 56.dp

    /** 按钮最小高度 */
    val ButtonMinHeight: Dp = 48.dp
    /** 快捷按钮尺寸 */
    val QuickButtonSize: Dp = 56.dp

    /** 进度条高度 */
    val ProgressBarHeight: Dp = 8.dp

    /** 分割线厚度 */
    val DividerThickness: Dp = 1.dp

    // ==================== 列表项间距 ====================
    /** 列表项垂直间距 */
    val ListItemSpacing: Dp = 12.dp
    /** 网格项间距 */
    val GridItemSpacing: Dp = 12.dp
}

/**
 * 统一形状规范
 */
object AppShapes {
    val Small = RoundedCornerShape(AppDimens.CornerSmall)
    val Medium = RoundedCornerShape(AppDimens.CornerMedium)
    val Large = RoundedCornerShape(AppDimens.CornerLarge)
    val ExtraLarge = RoundedCornerShape(AppDimens.CornerExtraLarge)
}

/**
 * 统一顶部应用栏
 *
 * 所有页面使用统一的 TopAppBar 样式，标题居中
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedTopAppBar(
    title: String,
    onNavigateBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
        },
        navigationIcon = {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}

/**
 * 统一页面标题（用于模块标题）
 */
@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier,
    centered: Boolean = true
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        textAlign = if (centered) TextAlign.Center else TextAlign.Start,
        modifier = modifier.then(
            if (centered) Modifier.fillMaxWidth() else Modifier
        )
    )
}

/**
 * 统一卡片样式
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnifiedCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    if (onClick != null) {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = AppShapes.Large,
            onClick = onClick
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPadding),
                content = content
            )
        }
    } else {
        Card(
            modifier = modifier.fillMaxWidth(),
            shape = AppShapes.Large
        ) {
            Column(
                modifier = Modifier.padding(AppDimens.CardPadding),
                content = content
            )
        }
    }
}

/**
 * 统一统计卡片
 */
@Composable
fun UnifiedStatsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.CardPadding),
            content = content
        )
    }
}

/**
 * 统一输入框 - Material Design 3 风格
 *
 * 特性：
 * - 动态边框颜色（聚焦/错误/正常状态）
 * - 支持前缀图标和后缀图标
 * - 支持清除按钮
 * - 支持错误提示
 * - 支持帮助文本
 */
@Composable
fun UnifiedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    leadingIcon: ImageVector? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    suffix: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    helperText: String? = null,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    showClearButton: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    // 动画颜色
    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            isFocused -> MaterialTheme.colorScheme.primary
            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "borderColor"
    )

    val backgroundColor by animateColorAsState(
        targetValue = when {
            !enabled -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            isFocused -> MaterialTheme.colorScheme.surface
            else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        },
        animationSpec = tween(durationMillis = 150),
        label = "backgroundColor"
    )

    Column(modifier = modifier) {
        // 标签
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = when {
                    isError -> MaterialTheme.colorScheme.error
                    isFocused -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        // 输入框容器
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(AppShapes.Medium)
                .background(backgroundColor)
                .border(
                    width = if (isFocused || isError) 2.dp else 1.dp,
                    color = borderColor,
                    shape = AppShapes.Medium
                )
                .padding(horizontal = 12.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 前缀图标
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = if (isError) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                // 输入区域
                Box(modifier = Modifier.weight(1f)) {
                    if (value.isEmpty() && placeholder != null) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = enabled,
                        readOnly = readOnly,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        keyboardOptions = keyboardOptions,
                        keyboardActions = keyboardActions,
                        singleLine = singleLine,
                        maxLines = maxLines,
                        visualTransformation = visualTransformation,
                        interactionSource = interactionSource,
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary)
                    )
                }

                // 后缀文本
                if (suffix != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = suffix,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // 清除按钮
                if (showClearButton && value.isNotEmpty() && enabled) {
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { onValueChange("") },
                        modifier = Modifier.size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = "清除",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // 错误图标
                if (isError) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                }

                // 自定义后缀图标
                if (trailingIcon != null) {
                    Spacer(modifier = Modifier.width(8.dp))
                    trailingIcon()
                }
            }
        }

        // 错误信息或帮助文本
        if (isError && errorMessage != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 4.dp)
            )
        } else if (helperText != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = helperText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp)
            )
        }
    }
}

/**
 * 统一数字输入框
 *
 * 专门用于数字输入，带有单位后缀
 */
@Composable
fun UnifiedNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    unit: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    allowDecimal: Boolean = true
) {
    UnifiedTextField(
        value = value,
        onValueChange = { newValue ->
            // 只允许数字和小数点
            val filtered = if (allowDecimal) {
                newValue.filter { it.isDigit() || it == '.' }
            } else {
                newValue.filter { it.isDigit() }
            }
            // 确保只有一个小数点
            val parts = filtered.split('.')
            val finalValue = if (parts.size > 2) {
                parts[0] + "." + parts.drop(1).joinToString("")
            } else {
                filtered
            }
            onValueChange(finalValue)
        },
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        suffix = unit,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = if (allowDecimal)
                androidx.compose.ui.text.input.KeyboardType.Decimal
            else
                androidx.compose.ui.text.input.KeyboardType.Number
        ),
        showClearButton = true
    )
}

/**
 * 统一多行文本输入框
 */
@Composable
fun UnifiedTextArea(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    placeholder: String? = null,
    isError: Boolean = false,
    errorMessage: String? = null,
    enabled: Boolean = true,
    minLines: Int = 3,
    maxLines: Int = 5
) {
    UnifiedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = label,
        placeholder = placeholder,
        isError = isError,
        errorMessage = errorMessage,
        enabled = enabled,
        singleLine = false,
        maxLines = maxLines
    )
}

/**
 * 统一选择器按钮
 *
 * 用于选择日期、分类等场景的统一样式按钮
 */
@Composable
fun UnifiedSelectorButton(
    value: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null,
    leadingIcon: ImageVector? = null,
    placeholder: String = "请选择",
    enabled: Boolean = true
) {
    val isEmpty = value.isBlank()

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }

        Surface(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth(),
            shape = AppShapes.Medium,
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            enabled = enabled
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        shape = AppShapes.Medium
                    )
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (leadingIcon != null) {
                    Icon(
                        imageVector = leadingIcon,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                }

                Text(
                    text = if (isEmpty) placeholder else value,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isEmpty) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )

                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier
                        .size(16.dp)
                        .graphicsLayer(rotationZ = 270f),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 统一选项卡组
 */
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun UnifiedChipGroup(
    options: List<Pair<String, String>>,
    selectedValue: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String? = null
) {
    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            options.forEach { (value, displayName) ->
                val isSelected = value == selectedValue
                FilterChip(
                    selected = isSelected,
                    onClick = { onValueChange(value) },
                    label = {
                        Text(
                            text = displayName,
                            style = MaterialTheme.typography.labelMedium
                        )
                    },
                    shape = AppShapes.Small,
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )
            }
        }
    }
}
