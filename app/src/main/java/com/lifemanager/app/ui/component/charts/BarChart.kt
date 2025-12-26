package com.lifemanager.app.ui.component.charts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.lifemanager.app.core.util.formatAmount
import com.lifemanager.app.ui.theme.AppColors

/**
 * 柱状图数据模型
 */
data class BarChartData(
    val label: String,
    val value: Double,
    val color: Color? = null
)

/**
 * 水平柱状图组件
 *
 * 显示多项数据的横向柱状图
 *
 * @param data 数据列表
 * @param barColor 默认柱子颜色
 * @param modifier 修饰符
 */
@Composable
fun HorizontalBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
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

    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        data.forEach { item ->
            HorizontalBarItem(
                data = item,
                maxValue = maxValue,
                barColor = item.color ?: barColor
            )
        }
    }
}

/**
 * 单个水平柱状项
 */
@Composable
private fun HorizontalBarItem(
    data: BarChartData,
    maxValue: Double,
    barColor: Color
) {
    val ratio = if (maxValue > 0) (data.value / maxValue).toFloat() else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 标签
        Text(
            text = data.label,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(60.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.width(8.dp))

        // 柱子
        Box(
            modifier = Modifier
                .weight(1f)
                .height(24.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio)
                    .background(barColor)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // 数值
        Text(
            text = "¥${data.value.formatAmount()}",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.width(80.dp)
        )
    }
}

/**
 * 收支对比柱状图
 *
 * 专门用于显示收入和支出的对比
 *
 * @param income 收入金额
 * @param expense 支出金额
 * @param modifier 修饰符
 */
@Composable
fun IncomeExpenseComparisonBar(
    income: Double,
    expense: Double,
    modifier: Modifier = Modifier
) {
    val maxValue = maxOf(income, expense)
    val balance = income - expense

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 收入条
        ComparisonBarRow(
            label = "收入",
            value = income,
            maxValue = maxValue,
            color = AppColors.Income
        )

        // 支出条
        ComparisonBarRow(
            label = "支出",
            value = expense,
            maxValue = maxValue,
            color = AppColors.Expense
        )

        // 结余
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "结余：",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "¥${balance.formatAmount()}",
                style = MaterialTheme.typography.titleMedium,
                color = if (balance >= 0) AppColors.Income else AppColors.Expense,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * 对比柱状行
 */
@Composable
private fun ComparisonBarRow(
    label: String,
    value: Double,
    maxValue: Double,
    color: Color
) {
    val ratio = if (maxValue > 0) (value / maxValue).toFloat() else 0f

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.width(50.dp)
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .height(32.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(ratio)
                    .background(color)
            )
        }

        Text(
            text = "¥${value.formatAmount()}",
            style = MaterialTheme.typography.bodyMedium,
            color = color,
            modifier = Modifier
                .padding(start = 8.dp)
                .width(100.dp)
        )
    }
}

/**
 * 垂直柱状图（用于趋势展示）
 */
@Composable
fun VerticalBarChart(
    data: List<BarChartData>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary
) {
    if (data.isEmpty()) return

    val maxValue = data.maxOfOrNull { it.value } ?: 1.0

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(150.dp)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { item ->
            val ratio = if (maxValue > 0) (item.value / maxValue).toFloat() else 0f

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                // 柱子
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(0.6f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(ratio)
                            .background(item.color ?: barColor)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 标签
                Text(
                    text = item.label,
                    style = MaterialTheme.typography.labelSmall,
                    maxLines = 1
                )
            }
        }
    }
}
