package com.lifemanager.app.feature.savings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.lifemanager.app.domain.model.SavingsPlanWithDetails
import com.lifemanager.app.ui.component.*
import com.lifemanager.app.ui.theme.*
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * 快速存钱页面
 *
 * 目标：2次点击完成存钱
 * 1. 选择计划（如果预选则跳过）
 * 2. 输入金额并确认
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickSavingsScreen(
    preSelectedPlanId: Long? = null,
    onNavigateBack: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: SavingsPlanViewModel = hiltViewModel()
) {
    val plans by viewModel.plans.collectAsState()
    val activePlans = plans.filter { it.plan.status == "ACTIVE" }

    var selectedPlanId by remember { mutableStateOf(preSelectedPlanId) }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }

    // 如果有预选计划且有效，直接使用
    LaunchedEffect(preSelectedPlanId, activePlans) {
        if (preSelectedPlanId != null && preSelectedPlanId > 0) {
            val planExists = activePlans.any { it.plan.id == preSelectedPlanId }
            if (planExists) {
                selectedPlanId = preSelectedPlanId
            }
        } else if (activePlans.size == 1) {
            // 只有一个计划时自动选中
            selectedPlanId = activePlans.first().plan.id
        }
    }

    val selectedPlan = activePlans.find { it.plan.id == selectedPlanId }
    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    // 快捷金额选项
    val quickAmounts = listOf(100.0, 200.0, 500.0, 1000.0, 2000.0, 5000.0)

    if (showSuccess) {
        // 成功页面
        SuccessScreen(
            amount = amount,
            planName = selectedPlan?.plan?.name ?: "",
            onDone = onSuccess
        )
    } else {
        Scaffold(
            topBar = {
                UnifiedTopAppBar(
                    title = "快速存钱",
                    onNavigateBack = onNavigateBack
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(AppDimens.PageHorizontalPadding),
                verticalArrangement = Arrangement.spacedBy(AppDimens.SpacingNormal)
            ) {
                // 选择存钱计划
                SectionTitle(title = "选择存钱计划", centered = true)

                if (activePlans.isEmpty()) {
                    // 没有活跃计划
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = AppShapes.Large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(AppDimens.CardPadding),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Warning,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(AppDimens.SpacingMedium))
                            Text(
                                text = "暂无进行中的存钱计划，请先创建一个计划",
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                } else {
                    // 计划选择列表
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingMedium)
                    ) {
                        items(activePlans, key = { it.plan.id }) { planWithDetails ->
                            PlanSelectionCard(
                                planWithDetails = planWithDetails,
                                isSelected = selectedPlanId == planWithDetails.plan.id,
                                onClick = { selectedPlanId = planWithDetails.plan.id }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingSmall))

                // 输入金额
                SectionTitle(title = "存款金额", centered = true)

                // 大数字输入框
                OutlinedTextField(
                    value = amount,
                    onValueChange = { newValue ->
                        // 只允许数字和小数点
                        val filtered = newValue.filter { it.isDigit() || it == '.' }
                        // 确保只有一个小数点
                        val parts = filtered.split('.')
                        amount = if (parts.size > 2) {
                            parts[0] + "." + parts.drop(1).joinToString("")
                        } else {
                            filtered
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    ),
                    placeholder = {
                        Text(
                            text = "0.00",
                            style = LocalTextStyle.current.copy(
                                fontSize = 32.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    prefix = {
                        Text(
                            text = "¥",
                            style = LocalTextStyle.current.copy(
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = AppShapes.Large
                )

                // 快捷金额按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingSmall)
                ) {
                    quickAmounts.take(3).forEach { quickAmount ->
                        QuickAmountButton(
                            amount = quickAmount,
                            onClick = { amount = quickAmount.toInt().toString() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimens.SpacingSmall)
                ) {
                    quickAmounts.drop(3).forEach { quickAmount ->
                        QuickAmountButton(
                            amount = quickAmount,
                            onClick = { amount = quickAmount.toInt().toString() },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // 备注（可选）
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("备注（可选）") },
                    placeholder = { Text("添加备注...") },
                    singleLine = true,
                    shape = AppShapes.Medium
                )

                Spacer(modifier = Modifier.weight(1f))

                // 确认存钱按钮
                Button(
                    onClick = {
                        val depositAmount = amount.toDoubleOrNull()
                        if (depositAmount != null && depositAmount > 0 && selectedPlanId != null) {
                            isLoading = true
                            viewModel.deposit(
                                planId = selectedPlanId!!,
                                amount = depositAmount,
                                note = note.takeIf { it.isNotBlank() },
                                date = LocalDate.now()
                            )
                            showSuccess = true
                            isLoading = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedPlanId != null && amount.toDoubleOrNull()?.let { it > 0 } == true && !isLoading,
                    shape = AppShapes.Large,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF4CAF50)
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Icon(
                            Icons.Default.Savings,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(AppDimens.SpacingSmall))
                        Text(
                            text = "确认存钱",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(AppDimens.SpacingNormal))
            }
        }
    }
}

@Composable
private fun PlanSelectionCard(
    planWithDetails: SavingsPlanWithDetails,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val plan = planWithDetails.plan
    val planColor = try {
        Color(android.graphics.Color.parseColor(plan.color))
    } catch (e: Exception) {
        MaterialTheme.colorScheme.primary
    }

    val numberFormat = remember { NumberFormat.getNumberInstance(Locale.CHINA) }

    Card(
        modifier = Modifier
            .width(160.dp)
            .clickable(onClick = onClick)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, planColor, AppShapes.Large)
                } else {
                    Modifier
                }
            ),
        shape = AppShapes.Large,
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) planColor.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(AppDimens.SpacingMedium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 颜色圆点
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(planColor),
                contentAlignment = Alignment.Center
            ) {
                if (isSelected) {
                    Icon(
                        Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Savings,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(AppDimens.SpacingSmall))

            Text(
                text = plan.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "¥${numberFormat.format(plan.currentAmount)}",
                style = MaterialTheme.typography.bodyMedium,
                color = planColor,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "/ ¥${numberFormat.format(plan.targetAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun QuickAmountButton(
    amount: Double,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = AppShapes.Medium,
        contentPadding = PaddingValues(vertical = 12.dp)
    ) {
        Text(
            text = "¥${amount.toInt()}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun SuccessScreen(
    amount: String,
    planName: String,
    onDone: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimens.PageHorizontalPadding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // 成功图标
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFF4CAF50)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(AppDimens.SpacingXLarge))

        Text(
            text = "存钱成功！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(AppDimens.SpacingMedium))

        Text(
            text = "¥$amount",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4CAF50)
        )

        Spacer(modifier = Modifier.height(AppDimens.SpacingSmall))

        Text(
            text = "已存入「$planName」",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(AppDimens.SpacingXXLarge))

        Button(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = AppShapes.Large
        ) {
            Text(
                text = "完成",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
