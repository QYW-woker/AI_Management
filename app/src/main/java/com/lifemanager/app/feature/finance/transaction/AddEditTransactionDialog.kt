package com.lifemanager.app.feature.finance.transaction

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.lifemanager.app.core.database.entity.CustomFieldEntity
import com.lifemanager.app.domain.model.TransactionType

/**
 * 添加/编辑交易对话框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionDialog(
    viewModel: DailyTransactionViewModel,
    onDismiss: () -> Unit
) {
    val editState by viewModel.editState.collectAsState()
    val categories by viewModel.categories.collectAsState()

    var amountText by remember(editState.amount) {
        mutableStateOf(if (editState.amount > 0) editState.amount.toString() else "")
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        Text(if (editState.isEditing) "编辑记录" else "记一笔")
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Filled.Close, contentDescription = "关闭")
                        }
                    },
                    actions = {
                        TextButton(
                            onClick = { viewModel.saveTransaction() },
                            enabled = !editState.isSaving
                        ) {
                            if (editState.isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text("保存")
                            }
                        }
                    }
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // 错误提示
                    editState.error?.let { error ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // 收入/支出切换
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        FilterChip(
                            selected = editState.type == TransactionType.EXPENSE,
                            onClick = { viewModel.updateEditType(TransactionType.EXPENSE) },
                            label = { Text("支出") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF44336).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFFF44336)
                            )
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        FilterChip(
                            selected = editState.type == TransactionType.INCOME,
                            onClick = { viewModel.updateEditType(TransactionType.INCOME) },
                            label = { Text("收入") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF4CAF50).copy(alpha = 0.2f),
                                selectedLabelColor = Color(0xFF4CAF50)
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 金额输入
                    Text(
                        text = "金额",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = amountText,
                        onValueChange = { value ->
                            val filtered = value.filter { it.isDigit() || it == '.' }
                            val parts = filtered.split(".")
                            val newValue = when {
                                parts.size <= 1 -> filtered
                                parts.size == 2 -> "${parts[0]}.${parts[1].take(2)}"
                                else -> amountText
                            }
                            amountText = newValue
                            newValue.toDoubleOrNull()?.let { viewModel.updateEditAmount(it) }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("0.00") },
                        prefix = { Text("¥ ") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = if (editState.type == TransactionType.EXPENSE)
                                Color(0xFFF44336) else Color(0xFF4CAF50)
                        )
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // 分类选择
                    Text(
                        text = "分类",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (categories.isEmpty()) {
                        Text(
                            text = "暂无分类，请先添加分类",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(categories, key = { it.id }) { category ->
                                CategoryChip(
                                    category = category,
                                    selected = editState.categoryId == category.id,
                                    onClick = { viewModel.updateEditCategory(category.id) }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // 备注输入
                    Text(
                        text = "备注",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = editState.note,
                        onValueChange = { viewModel.updateEditNote(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("添加备注（可选）") },
                        maxLines = 2,
                        minLines = 2
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // 时间输入
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedTextField(
                            value = editState.time,
                            onValueChange = { viewModel.updateEditTime(it) },
                            modifier = Modifier.weight(1f),
                            label = { Text("时间") },
                            placeholder = { Text("HH:mm") },
                            singleLine = true
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChip(
    category: CustomFieldEntity,
    selected: Boolean,
    onClick: () -> Unit
) {
    val categoryColor = try {
        Color(android.graphics.Color.parseColor(category.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(
                if (selected) categoryColor.copy(alpha = 0.2f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (selected) categoryColor else categoryColor.copy(alpha = 0.6f)),
            contentAlignment = Alignment.Center
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = category.name,
            style = MaterialTheme.typography.labelSmall,
            maxLines = 1,
            color = if (selected) categoryColor else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
