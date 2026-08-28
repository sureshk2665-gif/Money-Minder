package com.moneyminder.app.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.Typeface
import androidx.core.content.FileProvider
import com.moneyminder.app.data.entity.Transaction
import com.moneyminder.app.data.entity.TransactionType
import org.apache.poi.ss.usermodel.FillPatternType
import org.apache.poi.ss.usermodel.HorizontalAlignment
import org.apache.poi.ss.usermodel.IndexedColors
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object ExportUtils {
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)
    private val fileNameFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.ENGLISH)

    fun exportToExcel(context: Context, transactions: List<Transaction>, monthLabel: String): File? {
        return try {
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet("Transactions")

            val headerStyle = workbook.createCellStyle().apply {
                fillForegroundColor = IndexedColors.DARK_TEAL.index
                fillPattern = FillPatternType.SOLID_FOREGROUND
                alignment = HorizontalAlignment.CENTER
                val font = workbook.createFont()
                font.bold = true
                font.color = IndexedColors.WHITE.index
                setFont(font)
            }

            val headers = listOf("Date", "Time", "Type", "Category", "Amount", "Paid From", "Received In", "Transfer From", "Transfer To", "Note", "Reference")
            val headerRow = sheet.createRow(0)
            headers.forEachIndexed { index, header ->
                headerRow.createCell(index).apply {
                    setCellValue(header)
                    cellStyle = headerStyle
                }
            }

            transactions.sortedBy { it.dateTime }.forEachIndexed { index, txn ->
                val row = sheet.createRow(index + 1)
                val date = Date(txn.dateTime)
                row.createCell(0).setCellValue(dateFormat.format(date))
                row.createCell(1).setCellValue(timeFormat.format(date))
                row.createCell(2).setCellValue(txn.type.name)
                row.createCell(3).setCellValue(txn.category)
                row.createCell(4).setCellValue(txn.amount)
                row.createCell(5).setCellValue(if (txn.type == TransactionType.EXPENSE) txn.fromAccount?.name ?: "" else "")
                row.createCell(6).setCellValue(if (txn.type == TransactionType.INCOME) txn.toAccount?.name ?: "" else "")
                row.createCell(7).setCellValue(if (txn.type == TransactionType.TRANSFER) txn.fromAccount?.name ?: "" else "")
                row.createCell(8).setCellValue(if (txn.type == TransactionType.TRANSFER) txn.toAccount?.name ?: "" else "")
                row.createCell(9).setCellValue(txn.note)
                row.createCell(10).setCellValue(txn.referenceNumber)
            }

            for (i in headers.indices) {
                sheet.setColumnWidth(i, 4000)
            }

            val dir = File(context.cacheDir, "exports")
            dir.mkdirs()
            val file = File(dir, "MoneyMinder_${monthLabel}_${fileNameFormat.format(Date())}.xlsx")
            FileOutputStream(file).use { workbook.write(it) }
            workbook.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun exportToPdf(context: Context, transactions: List<Transaction>, monthLabel: String,
                    bankBalance: Double, walletBalance: Double, cashBalance: Double): File? {
        return try {
            val dir = File(context.cacheDir, "exports")
            dir.mkdirs()
            val file = File(dir, "MoneyMinder_${monthLabel}_${fileNameFormat.format(Date())}.pdf")

            val document = PdfDocument()
            val pageWidth = 595
            val pageHeight = 842
            var pageNumber = 1
            var yPos = 60f

            val titlePaint = Paint().apply {
                textSize = 20f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val headerPaint = Paint().apply {
                textSize = 14f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                color = Color.BLACK
            }
            val normalPaint = Paint().apply {
                textSize = 11f
                color = Color.DKGRAY
            }
            val smallPaint = Paint().apply {
                textSize = 9f
                color = Color.DKGRAY
            }
            val linePaint = Paint().apply {
                color = Color.LTGRAY
                strokeWidth = 1f
            }

            var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            var page = document.startPage(pageInfo)
            var canvas = page.canvas

            fun checkNewPage(): Canvas {
                if (yPos > pageHeight - 60) {
                    document.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = document.startPage(pageInfo)
                    yPos = 60f
                    return page.canvas
                }
                return canvas
            }

            canvas.drawText("Money Minder Report", (pageWidth - titlePaint.measureText("Money Minder Report")) / 2, yPos, titlePaint)
            yPos += 24f
            canvas.drawText(monthLabel, (pageWidth - normalPaint.measureText(monthLabel)) / 2, yPos, normalPaint)
            yPos += 30f

            canvas.drawText("Account Balances", 40f, yPos, headerPaint)
            yPos += 20f
            val overall = bankBalance + walletBalance + cashBalance
            canvas.drawText("Bank: ${CurrencyUtils.formatAmount(bankBalance)}", 40f, yPos, normalPaint)
            yPos += 16f
            canvas.drawText("Wallet: ${CurrencyUtils.formatAmount(walletBalance)}", 40f, yPos, normalPaint)
            yPos += 16f
            canvas.drawText("Cash: ${CurrencyUtils.formatAmount(cashBalance)}", 40f, yPos, normalPaint)
            yPos += 16f
            canvas.drawText("Overall: ${CurrencyUtils.formatAmount(overall)}", 40f, yPos, headerPaint)
            yPos += 24f

            val incomeTotal = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            val expenseTotal = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
            canvas.drawText("Summary", 40f, yPos, headerPaint)
            yPos += 20f
            canvas.drawText("Total Income: ${CurrencyUtils.formatAmount(incomeTotal)}", 40f, yPos, normalPaint)
            yPos += 16f
            canvas.drawText("Total Expense: ${CurrencyUtils.formatAmount(expenseTotal)}", 40f, yPos, normalPaint)
            yPos += 16f
            canvas.drawText("Net: ${CurrencyUtils.formatAmount(incomeTotal - expenseTotal)}", 40f, yPos, normalPaint)
            yPos += 30f

            if (transactions.isNotEmpty()) {
                canvas.drawText("Transactions", 40f, yPos, headerPaint)
                yPos += 20f

                canvas.drawLine(40f, yPos, pageWidth - 40f, yPos, linePaint)
                yPos += 4f

                val colDate = 40f
                val colType = 140f
                val colCategory = 200f
                val colAmount = 340f
                val colAccount = 430f

                canvas.drawText("Date", colDate, yPos, smallPaint.apply { typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD) })
                canvas.drawText("Type", colType, yPos, smallPaint)
                canvas.drawText("Category", colCategory, yPos, smallPaint)
                canvas.drawText("Amount", colAmount, yPos, smallPaint)
                canvas.drawText("Account", colAccount, yPos, smallPaint)
                smallPaint.typeface = Typeface.DEFAULT
                yPos += 14f
                canvas.drawLine(40f, yPos, pageWidth - 40f, yPos, linePaint)
                yPos += 4f

                transactions.sortedBy { it.dateTime }.forEach { txn ->
                    canvas = checkNewPage()
                    val dateStr = dateFormat.format(Date(txn.dateTime))
                    canvas.drawText(dateStr, colDate, yPos, smallPaint)
                    canvas.drawText(txn.type.name, colType, yPos, smallPaint)
                    val catText = txn.category.take(20)
                    canvas.drawText(catText, colCategory, yPos, smallPaint)
                    canvas.drawText(CurrencyUtils.formatAmount(txn.amount), colAmount, yPos, smallPaint)
                    val accountText = when (txn.type) {
                        TransactionType.EXPENSE -> "From ${txn.fromAccount?.name ?: ""}"
                        TransactionType.INCOME -> "To ${txn.toAccount?.name ?: ""}"
                        TransactionType.TRANSFER -> "${txn.fromAccount?.name ?: ""} > ${txn.toAccount?.name ?: ""}"
                    }
                    canvas.drawText(accountText, colAccount, yPos, smallPaint)
                    yPos += 14f
                }
            }

            document.finishPage(page)
            FileOutputStream(file).use { document.writeTo(it) }
            document.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(context: Context, file: File) {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = when {
                file.name.endsWith(".xlsx") -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                file.name.endsWith(".pdf") -> "application/pdf"
                else -> "*/*"
            }
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Report"))
    }
}
