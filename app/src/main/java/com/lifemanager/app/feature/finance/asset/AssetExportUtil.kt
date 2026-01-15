package com.lifemanager.app.feature.finance.asset

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.lifemanager.app.domain.model.AssetStats
import com.lifemanager.app.domain.model.MonthlyAssetWithField
import com.lifemanager.app.domain.model.NetWorthTrendPoint
import java.io.File
import java.io.FileWriter
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 资产报表导出工具
 * 支持导出为CSV格式（可在Excel中打开）
 */
object AssetExportUtil {

    private val numberFormat = NumberFormat.getNumberInstance(Locale.CHINA)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)

    /**
     * 导出月度资产报表为CSV
     */
    fun exportToCSV(
        context: Context,
        yearMonth: Int,
        stats: AssetStats,
        records: List<MonthlyAssetWithField>,
        trend: List<NetWorthTrendPoint>
    ): Uri? {
        try {
            val year = yearMonth / 100
            val month = yearMonth % 100
            val fileName = "资产报表_${year}年${month}月_${System.currentTimeMillis()}.csv"
            val file = File(context.cacheDir, fileName)

            FileWriter(file).use { writer ->
                // 写入BOM以支持中文
                writer.write("\uFEFF")

                // 报表标题
                writer.write("月度资产统计报表\n")
                writer.write("报表月份,${year}年${month}月\n")
                writer.write("导出时间,${dateFormat.format(Date())}\n")
                writer.write("\n")

                // 资产概览
                writer.write("=== 资产概览 ===\n")
                writer.write("项目,金额(元)\n")
                writer.write("总资产,${numberFormat.format(stats.totalAssets)}\n")
                writer.write("总负债,${numberFormat.format(stats.totalLiabilities)}\n")
                writer.write("净资产,${numberFormat.format(stats.netWorth)}\n")
                writer.write("负债率,${String.format("%.2f", stats.debtRatio)}%\n")
                writer.write("\n")

                // 资产明细
                val assetRecords = records.filter { it.record.type == "ASSET" }
                if (assetRecords.isNotEmpty()) {
                    writer.write("=== 资产明细 ===\n")
                    writer.write("类别,金额(元),备注\n")
                    assetRecords.forEach { record ->
                        val fieldName = record.field?.name ?: "未分类"
                        val amount = numberFormat.format(record.record.amount)
                        val note = record.record.note.replace(",", "，") // 替换逗号避免CSV解析问题
                        writer.write("$fieldName,$amount,$note\n")
                    }
                    writer.write("资产合计,${numberFormat.format(stats.totalAssets)},\n")
                    writer.write("\n")
                }

                // 负债明细
                val liabilityRecords = records.filter { it.record.type == "LIABILITY" }
                if (liabilityRecords.isNotEmpty()) {
                    writer.write("=== 负债明细 ===\n")
                    writer.write("类别,金额(元),备注/欠款说明\n")
                    liabilityRecords.forEach { record ->
                        val fieldName = record.field?.name ?: "未分类"
                        val amount = numberFormat.format(record.record.amount)
                        val note = record.record.note.replace(",", "，")
                        writer.write("$fieldName,$amount,$note\n")
                    }
                    writer.write("负债合计,${numberFormat.format(stats.totalLiabilities)},\n")
                    writer.write("\n")
                }

                // 净资产趋势
                if (trend.isNotEmpty()) {
                    writer.write("=== 净资产趋势（近${trend.size}个月）===\n")
                    writer.write("月份,净资产(元)\n")
                    trend.forEach { point ->
                        val trendYear = point.yearMonth / 100
                        val trendMonth = point.yearMonth % 100
                        writer.write("${trendYear}年${trendMonth}月,${numberFormat.format(point.netWorth)}\n")
                    }
                }
            }

            // 返回文件URI
            return FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    /**
     * 分享导出的文件
     */
    fun shareFile(context: Context, uri: Uri, mimeType: String = "text/csv") {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "分享资产报表"))
    }

    /**
     * 导出并分享
     */
    fun exportAndShare(
        context: Context,
        yearMonth: Int,
        stats: AssetStats,
        records: List<MonthlyAssetWithField>,
        trend: List<NetWorthTrendPoint>
    ): Boolean {
        val uri = exportToCSV(context, yearMonth, stats, records, trend)
        return if (uri != null) {
            shareFile(context, uri)
            true
        } else {
            false
        }
    }
}
