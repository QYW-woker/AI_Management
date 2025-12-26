package com.lifemanager.app.ui.component.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.lifemanager.app.core.util.formatAmount
import com.lifemanager.app.core.util.formatPercent

/**
 * 饼图数据模型
 */
data class PieChartData(
    val label: String,
    val value: Double,
    val color: Color,
    val id: Long = 0
)

/**
 * 饼图组件
 *
 * 显示数据占比的环形图，可选配图例
 *
 * @param data 饼图数据列表
 * @param showLegend 是否显示图例
 * @param onSliceClick 切片点击回调
 * @param modifier 修饰符
 */
@Composable
fun PieChartView(
    data: List<PieChartData>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    onSliceClick: ((PieChartData) -> Unit)? = null
) {
    if (data.isEmpty()) {
        Box(
            modifier = modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val total = data.sumOf { it.value }

    Column(modifier = modifier) {
        // 饼图
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .padding(16.dp)
        ) {
            val canvasSize = size.minDimension
            val radius = canvasSize / 2
            val strokeWidth = radius * 0.3f
            val innerRadius = radius - strokeWidth

            var startAngle = -90f

            data.forEach { item ->
                val sweepAngle = if (total > 0) {
                    (item.value / total * 360).toFloat()
                } else {
                    0f
                }

                // 绘制扇形
                drawArc(
                    color = item.color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    topLeft = Offset(
                        (size.width - canvasSize) / 2 + strokeWidth / 2,
                        (size.height - canvasSize) / 2 + strokeWidth / 2
                    ),
                    size = Size(canvasSize - strokeWidth, canvasSize - strokeWidth),
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )

                startAngle += sweepAngle
            }
        }

        // 图例
        if (showLegend && data.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(data) { item ->
                    PieLegendItem(
                        data = item,
                        total = total,
                        onClick = { onSliceClick?.invoke(item) }
                    )
                }
            }
        }
    }
}

/**
 * 饼图图例项
 */
@Composable
private fun PieLegendItem(
    data: PieChartData,
    total: Double,
    onClick: () -> Unit
) {
    val percentage = if (total > 0) (data.value / total * 100) else 0.0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 颜色标记
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(data.color, CircleShape)
        )

        Spacer(modifier = Modifier.width(12.dp))

        // 标签
        Text(
            text = data.label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // 金额
        Text(
            text = "¥${data.value.formatAmount()}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 百分比
        Text(
            text = "(${percentage.formatPercent()}%)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 进度环组件
 *
 * 显示单一进度的环形图
 *
 * @param progress 进度值（0-1）
 * @param strokeWidth 环的宽度
 * @param backgroundColor 背景色
 * @param progressColor 进度色
 * @param modifier 修饰符
 */
@Composable
fun ProgressRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 100.dp,
    strokeWidth: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    val sweepAngle = progress.coerceIn(0f, 1f) * 360f

    Canvas(modifier = modifier.size(size)) {
        val canvasSize = this.size.minDimension
        val stroke = strokeWidth.toPx()

        // 背景圆环
        drawArc(
            color = backgroundColor,
            startAngle = -90f,
            sweepAngle = 360f,
            useCenter = false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(canvasSize - stroke, canvasSize - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )

        // 进度圆环
        drawArc(
            color = progressColor,
            startAngle = -90f,
            sweepAngle = sweepAngle,
            useCenter = false,
            topLeft = Offset(stroke / 2, stroke / 2),
            size = Size(canvasSize - stroke, canvasSize - stroke),
            style = Stroke(width = stroke, cap = StrokeCap.Round)
        )
    }
}

/**
 * 带中心文字的进度环
 */
@Composable
fun ProgressRingWithLabel(
    progress: Float,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 10.dp,
    progressColor: Color = MaterialTheme.colorScheme.primary
) {
    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        ProgressRing(
            progress = progress,
            size = size,
            strokeWidth = strokeWidth,
            progressColor = progressColor
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = progressColor
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
