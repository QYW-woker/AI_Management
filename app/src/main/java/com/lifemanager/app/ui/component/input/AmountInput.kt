package com.lifemanager.app.ui.component.input

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.lifemanager.app.ui.theme.AppColors

/**
 * 金额输入组件
 *
 * 专用于金额输入的文本框，支持：
 * - 数字和小数点输入
 * - 最多两位小数
 * - 收入/支出颜色区分
 *
 * @param value 当前输入值
 * @param onValueChange 值变化回调
 * @param label 标签文本
 * @param isExpense 是否为支出（决定颜色）
 * @param modifier 修饰符
 */
@Composable
fun AmountInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String = "金额",
    isExpense: Boolean = true,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            // 验证输入：只允许数字和小数点，最多两位小数
            if (newValue.isEmpty() ||
                newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))
            ) {
                onValueChange(newValue)
            }
        },
        label = { Text(label) },
        leadingIcon = {
            Text(
                text = "¥",
                style = MaterialTheme.typography.titleLarge,
                color = if (isExpense) AppColors.Expense else AppColors.Income
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * 简洁金额输入
 *
 * 没有边框的金额输入，适用于卡片内部
 */
@Composable
fun SimpleAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "0.00",
    isExpense: Boolean = true,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() ||
                newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))
            ) {
                onValueChange(newValue)
            }
        },
        placeholder = {
            Text(
                text = placeholder,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        },
        leadingIcon = {
            Text(
                text = "¥",
                style = MaterialTheme.typography.titleMedium,
                color = if (isExpense) AppColors.Expense else AppColors.Income
            )
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline
        ),
        modifier = modifier.fillMaxWidth()
    )
}

/**
 * 大号金额显示输入
 *
 * 用于主要金额输入场景
 */
@Composable
fun LargeAmountInput(
    value: String,
    onValueChange: (String) -> Unit,
    isExpense: Boolean = true,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = { newValue ->
            if (newValue.isEmpty() ||
                newValue.matches(Regex("^\\d*\\.?\\d{0,2}$"))
            ) {
                onValueChange(newValue)
            }
        },
        placeholder = {
            Text(
                text = "0.00",
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
            )
        },
        leadingIcon = {
            Text(
                text = "¥",
                style = MaterialTheme.typography.displaySmall,
                color = if (isExpense) AppColors.Expense else AppColors.Income
            )
        },
        textStyle = MaterialTheme.typography.displaySmall.copy(
            color = if (isExpense) AppColors.Expense else AppColors.Income
        ),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = ImeAction.Done
        ),
        singleLine = true,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surface,
            unfocusedContainerColor = MaterialTheme.colorScheme.surface,
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    )
}
