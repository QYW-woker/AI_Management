package com.lifemanager.app.feature.finance.transaction.import

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader
import java.nio.charset.Charset

/**
 * 账单解析器
 *
 * 支持解析微信和支付宝的CSV账单文件
 */
class BillParser(private val context: Context) {

    companion object {
        // 微信账单关键字段
        private val WECHAT_HEADERS = listOf("交易时间", "交易类型", "交易对方", "商品", "收/支", "金额(元)")

        // 支付宝账单关键字段
        private val ALIPAY_HEADERS = listOf("交易创建时间", "交易对方", "商品名称", "金额（元）", "收/支")

        // 需要跳过的交易状态
        private val SKIP_STATUS = listOf(
            "已退款", "退款成功", "交易关闭", "已关闭", "对方已退还",
            "已全额退款", "已转账到零钱", "朋友已收钱"
        )

        // 需要跳过的交易类型
        private val SKIP_TYPES = listOf(
            "零钱提现", "零钱通转出", "信用卡还款",
            "转入零钱通", "零钱通收益", "理财通"
        )
    }

    /**
     * 解析CSV文件
     */
    fun parseFile(uri: Uri): BillParseResult {
        return try {
            val content = readFileContent(uri)
            if (content.isBlank()) {
                return BillParseResult.Error("文件内容为空")
            }

            // 检测账单类型
            val source = detectBillSource(content)

            when (source) {
                BillSource.WECHAT -> parseWechatBill(content)
                BillSource.ALIPAY -> parseAlipayBill(content)
                BillSource.UNKNOWN -> BillParseResult.Error("无法识别账单格式，请确保是微信或支付宝导出的CSV账单")
            }
        } catch (e: Exception) {
            BillParseResult.Error("解析失败: ${e.message}")
        }
    }

    /**
     * 读取文件内容，自动检测编码
     */
    private fun readFileContent(uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw Exception("无法打开文件")

        // 尝试不同编码
        val charsets = listOf(
            Charset.forName("UTF-8"),
            Charset.forName("GBK"),
            Charset.forName("GB2312"),
            Charset.forName("GB18030")
        )

        val bytes = inputStream.readBytes()
        inputStream.close()

        // 优先尝试UTF-8
        for (charset in charsets) {
            try {
                val content = String(bytes, charset)
                // 检查是否包含中文，如果是乱码则尝试下一个编码
                if (content.contains("交易") || content.contains("时间") || content.contains("金额")) {
                    return content
                }
            } catch (e: Exception) {
                continue
            }
        }

        // 默认使用GBK（微信/支付宝常用）
        return String(bytes, Charset.forName("GBK"))
    }

    /**
     * 检测账单来源
     */
    private fun detectBillSource(content: String): BillSource {
        val firstLines = content.lines().take(30).joinToString("\n")

        return when {
            firstLines.contains("微信支付账单") ||
            WECHAT_HEADERS.all { header -> firstLines.contains(header) } -> BillSource.WECHAT

            firstLines.contains("支付宝") ||
            firstLines.contains("账单明细") ||
            ALIPAY_HEADERS.count { header -> firstLines.contains(header) } >= 3 -> BillSource.ALIPAY

            else -> BillSource.UNKNOWN
        }
    }

    /**
     * 解析微信账单
     *
     * 微信账单格式：
     * 交易时间,交易类型,交易对方,商品,收/支,金额(元),支付方式,当前状态,交易单号,商户单号,备注
     */
    private fun parseWechatBill(content: String): BillParseResult {
        val lines = content.lines()

        // 找到数据开始行（跳过账单统计信息）
        var headerIndex = -1
        for (i in lines.indices) {
            if (lines[i].startsWith("交易时间") && lines[i].contains("金额")) {
                headerIndex = i
                break
            }
        }

        if (headerIndex == -1) {
            return BillParseResult.Error("无法找到微信账单数据表头")
        }

        val records = mutableListOf<ParsedBillRecord>()
        var totalIncome = 0.0
        var totalExpense = 0.0
        var skippedCount = 0

        for (i in (headerIndex + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank()) continue

            val fields = parseCSVLine(line)
            if (fields.size < 8) continue

            val datetime = fields.getOrNull(0)?.trim() ?: continue
            val transactionType = fields.getOrNull(1)?.trim() ?: ""
            val counterparty = fields.getOrNull(2)?.trim() ?: ""
            val goods = fields.getOrNull(3)?.trim() ?: ""
            val incomeExpense = fields.getOrNull(4)?.trim() ?: ""
            val amountStr = fields.getOrNull(5)?.trim()?.replace("¥", "")?.replace(",", "") ?: continue
            val paymentMethod = fields.getOrNull(6)?.trim() ?: ""
            val status = fields.getOrNull(7)?.trim() ?: ""
            val orderNo = fields.getOrNull(8)?.trim() ?: ""
            val merchantNo = fields.getOrNull(9)?.trim() ?: ""
            val note = fields.getOrNull(10)?.trim() ?: ""

            // 跳过不需要的交易
            if (shouldSkipTransaction(status, transactionType)) {
                skippedCount++
                continue
            }

            val amount = amountStr.toDoubleOrNull() ?: continue
            if (amount <= 0) continue

            val type = when {
                incomeExpense.contains("收入") -> "收入"
                incomeExpense.contains("支出") -> "支出"
                else -> continue  // 跳过不收不支的记录
            }

            if (type == "收入") totalIncome += amount else totalExpense += amount

            records.add(ParsedBillRecord(
                datetime = datetime,
                type = type,
                counterparty = counterparty,
                goods = goods.ifBlank { transactionType },
                amount = amount,
                paymentMethod = paymentMethod,
                status = status,
                orderNo = orderNo,
                merchantNo = merchantNo,
                note = note,
                source = BillSource.WECHAT
            ))
        }

        if (records.isEmpty()) {
            return BillParseResult.Error("未找到有效的交易记录")
        }

        return BillParseResult.Success(
            records = records,
            source = BillSource.WECHAT,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            skippedCount = skippedCount
        )
    }

    /**
     * 解析支付宝账单
     *
     * 支付宝账单格式：
     * 交易创建时间,交易来源,交易类型,交易对方,商品名称,金额（元）,收/支,交易状态,...
     */
    private fun parseAlipayBill(content: String): BillParseResult {
        val lines = content.lines()

        // 找到数据开始行
        var headerIndex = -1
        for (i in lines.indices) {
            val line = lines[i]
            if ((line.contains("交易创建时间") || line.contains("交易时间")) &&
                line.contains("金额")) {
                headerIndex = i
                break
            }
        }

        if (headerIndex == -1) {
            return BillParseResult.Error("无法找到支付宝账单数据表头")
        }

        // 解析表头，确定各字段位置
        val headerFields = parseCSVLine(lines[headerIndex])
        val timeIndex = headerFields.indexOfFirst { it.contains("时间") }
        val counterpartyIndex = headerFields.indexOfFirst { it.contains("交易对方") || it.contains("对方") }
        val goodsIndex = headerFields.indexOfFirst { it.contains("商品") || it.contains("名称") }
        val amountIndex = headerFields.indexOfFirst { it.contains("金额") }
        val typeIndex = headerFields.indexOfFirst { it == "收/支" || it.contains("收/支") }
        val statusIndex = headerFields.indexOfFirst { it.contains("状态") }
        val orderIndex = headerFields.indexOfFirst { it.contains("订单号") || it.contains("交易号") }

        val records = mutableListOf<ParsedBillRecord>()
        var totalIncome = 0.0
        var totalExpense = 0.0
        var skippedCount = 0

        for (i in (headerIndex + 1) until lines.size) {
            val line = lines[i].trim()
            if (line.isBlank() || line.startsWith("-") || line.startsWith("=")) continue

            val fields = parseCSVLine(line)
            if (fields.size < 5) continue

            val datetime = fields.getOrNull(timeIndex)?.trim() ?: continue
            val counterparty = fields.getOrNull(counterpartyIndex)?.trim() ?: ""
            val goods = fields.getOrNull(goodsIndex)?.trim() ?: ""
            val amountStr = fields.getOrNull(amountIndex)?.trim()
                ?.replace("¥", "")?.replace(",", "")?.replace(" ", "") ?: continue
            val incomeExpense = fields.getOrNull(typeIndex)?.trim() ?: ""
            val status = if (statusIndex >= 0) fields.getOrNull(statusIndex)?.trim() ?: "" else ""
            val orderNo = if (orderIndex >= 0) fields.getOrNull(orderIndex)?.trim() ?: "" else ""

            // 跳过不需要的交易
            if (shouldSkipTransaction(status, goods)) {
                skippedCount++
                continue
            }

            val amount = amountStr.toDoubleOrNull() ?: continue
            if (amount <= 0) continue

            val type = when {
                incomeExpense.contains("收入") -> "收入"
                incomeExpense.contains("支出") -> "支出"
                incomeExpense.isBlank() && status.contains("退款") -> continue  // 跳过退款
                else -> continue
            }

            if (type == "收入") totalIncome += amount else totalExpense += amount

            records.add(ParsedBillRecord(
                datetime = datetime,
                type = type,
                counterparty = counterparty,
                goods = goods,
                amount = amount,
                paymentMethod = "",
                status = status,
                orderNo = orderNo,
                source = BillSource.ALIPAY
            ))
        }

        if (records.isEmpty()) {
            return BillParseResult.Error("未找到有效的交易记录")
        }

        return BillParseResult.Success(
            records = records,
            source = BillSource.ALIPAY,
            totalIncome = totalIncome,
            totalExpense = totalExpense,
            skippedCount = skippedCount
        )
    }

    /**
     * 解析CSV行（处理引号包裹的逗号）
     */
    private fun parseCSVLine(line: String): List<String> {
        val result = mutableListOf<String>()
        var current = StringBuilder()
        var inQuotes = false

        for (char in line) {
            when {
                char == '"' -> inQuotes = !inQuotes
                char == ',' && !inQuotes -> {
                    result.add(current.toString())
                    current = StringBuilder()
                }
                else -> current.append(char)
            }
        }
        result.add(current.toString())

        return result
    }

    /**
     * 判断是否应该跳过该交易
     */
    private fun shouldSkipTransaction(status: String, type: String): Boolean {
        return SKIP_STATUS.any { status.contains(it) } ||
               SKIP_TYPES.any { type.contains(it) }
    }
}
