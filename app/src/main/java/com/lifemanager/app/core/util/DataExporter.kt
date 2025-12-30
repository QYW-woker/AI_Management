package com.lifemanager.app.core.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.lifemanager.app.core.database.entity.DailyTransactionEntity
import com.lifemanager.app.core.database.entity.FundAccountEntity
import com.lifemanager.app.core.database.entity.TransferEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale

/**
 * 数据导出工具类
 */
object DataExporter {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.CHINA)
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    /**
     * 导出交易记录到CSV
     */
    suspend fun exportTransactionsToCSV(
        context: Context,
        transactions: List<DailyTransactionEntity>,
        categoryMap: Map<Long, String>,
        accountMap: Map<Long, String>,
        startDate: LocalDate? = null,
        endDate: LocalDate? = null
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "交易记录_${dateFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                // 写入UTF-8 BOM以支持Excel正确识别中文
                writer.write("\uFEFF")

                // 写入标题行
                writer.write("日期,时间,类型,金额,分类,账户,备注,来源\n")

                // 写入数据
                transactions.forEach { transaction ->
                    val date = LocalDate.ofEpochDay(transaction.date.toLong())
                    val type = if (transaction.type == "INCOME") "收入" else "支出"
                    val category = transaction.categoryId?.let { categoryMap[it] } ?: "未分类"
                    val account = transaction.accountId?.let { accountMap[it] } ?: "未指定"
                    val source = when (transaction.source) {
                        "VOICE" -> "语音"
                        "SCREENSHOT" -> "截图"
                        "IMPORT" -> "导入"
                        else -> "手动"
                    }

                    // CSV转义处理
                    val note = escapeCSV(transaction.note)

                    writer.write("${date},${transaction.time},$type,${transaction.amount},$category,$account,$note,$source\n")
                }
            }

            // 获取FileProvider URI
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导出资金账户到CSV
     */
    suspend fun exportAccountsToCSV(
        context: Context,
        accounts: List<FundAccountEntity>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "资金账户_${dateFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                writer.write("\uFEFF")
                writer.write("账户名称,账户类型,余额,信用额度,账单日,还款日,备注,是否计入统计\n")

                accounts.forEach { account ->
                    val typeName = when (account.accountType) {
                        "CASH" -> "现金"
                        "BANK_CARD" -> "银行卡"
                        "CREDIT_CARD" -> "信用卡"
                        "ALIPAY" -> "支付宝"
                        "WECHAT" -> "微信"
                        "CREDIT_LOAN" -> "信贷账户"
                        "INVESTMENT" -> "投资账户"
                        else -> "其他"
                    }
                    val note = escapeCSV(account.note)
                    val includeInTotal = if (account.includeInTotal) "是" else "否"

                    writer.write("${account.name},$typeName,${account.balance},${account.creditLimit ?: ""},${account.billDay ?: ""},${account.repaymentDay ?: ""},$note,$includeInTotal\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 导出转账记录到CSV
     */
    suspend fun exportTransfersToCSV(
        context: Context,
        transfers: List<TransferEntity>,
        accountMap: Map<Long, String>
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "转账记录_${dateFormat.format(Date())}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                writer.write("\uFEFF")
                writer.write("日期,时间,转出账户,转入账户,金额,手续费,备注\n")

                transfers.forEach { transfer ->
                    val date = LocalDate.ofEpochDay(transfer.date.toLong())
                    val fromAccount = accountMap[transfer.fromAccountId] ?: "未知"
                    val toAccount = accountMap[transfer.toAccountId] ?: "未知"
                    val note = escapeCSV(transfer.note)

                    writer.write("${date},${transfer.time},$fromAccount,$toAccount,${transfer.amount},${transfer.fee},$note\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 生成月度报告
     */
    suspend fun generateMonthlyReport(
        context: Context,
        transactions: List<DailyTransactionEntity>,
        categoryMap: Map<Long, String>,
        yearMonth: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val fileName = "月度报告_${yearMonth}.csv"
            val file = File(context.cacheDir, fileName)

            // 计算统计数据
            val income = transactions.filter { it.type == "INCOME" }
            val expense = transactions.filter { it.type == "EXPENSE" }
            val totalIncome = income.sumOf { it.amount }
            val totalExpense = expense.sumOf { it.amount }
            val balance = totalIncome - totalExpense

            // 按分类统计
            val categoryStats = expense.groupBy { it.categoryId }
                .map { (categoryId, items) ->
                    val name = categoryId?.let { categoryMap[it] } ?: "未分类"
                    val total = items.sumOf { it.amount }
                    val count = items.size
                    Triple(name, total, count)
                }
                .sortedByDescending { it.second }

            FileWriter(file).use { writer ->
                writer.write("\uFEFF")

                // 概览
                writer.write("${yearMonth}月度财务报告\n\n")
                writer.write("收入总额,¥${String.format("%.2f", totalIncome)}\n")
                writer.write("支出总额,¥${String.format("%.2f", totalExpense)}\n")
                writer.write("结余,¥${String.format("%.2f", balance)}\n")
                writer.write("交易笔数,${transactions.size}\n\n")

                // 分类统计
                writer.write("支出分类统计\n")
                writer.write("分类,金额,笔数,占比\n")
                categoryStats.forEach { (name, total, count) ->
                    val percentage = if (totalExpense > 0) total / totalExpense * 100 else 0.0
                    writer.write("$name,¥${String.format("%.2f", total)},$count,${String.format("%.1f", percentage)}%\n")
                }
            }

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 分享文件
     */
    fun shareFile(context: Context, uri: Uri, mimeType: String = "text/csv") {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "分享导出文件"))
    }

    /**
     * CSV字段转义
     */
    private fun escapeCSV(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"${value.replace("\"", "\"\"")}\""
        } else {
            value
        }
    }
}
