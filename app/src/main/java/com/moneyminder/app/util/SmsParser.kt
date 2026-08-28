package com.moneyminder.app.util

import com.moneyminder.app.data.entity.AccountType
import com.moneyminder.app.data.entity.TransactionType
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

data class ParsedSmsTransaction(
    val type: TransactionType,
    val amount: Double,
    val transactionName: String,
    val account: AccountType,
    val dateTime: Long?,
    val referenceNumber: String,
    val balanceAfter: Double?,
    val accountReference: String,
    val isPending: Boolean = false,
    val confidence: Float = 1.0f
)

object SmsParser {
    private val amountPattern = Pattern.compile(
        "(?:Rs\\.?|INR|₹)\\s*:?\\s*([\\d,]+\\.?\\d*)", Pattern.CASE_INSENSITIVE
    )
    private val balancePattern = Pattern.compile(
        "(?:Avl\\s*Bal|Available\\s*Balance|Remaining\\s*balance|balance)\\s*(?:Rs\\.?|INR|₹)?\\s*:?\\s*([\\d,]+\\.?\\d*)",
        Pattern.CASE_INSENSITIVE
    )
    private val refPattern = Pattern.compile(
        "(?:ref\\s*(?:no\\.?|number)?|Ref\\s*No)\\s*:?\\s*([A-Za-z0-9]+)", Pattern.CASE_INSENSITIVE
    )
    private val datePattern1 = Pattern.compile(
        "(\\d{2})-(\\d{2})-(\\d{4})\\s+(\\d{2}:\\d{2}:\\d{2})"
    )
    private val datePattern2 = Pattern.compile(
        "(\\d{2})-(\\d{2})-(\\d{4})"
    )
    private val accountPattern = Pattern.compile(
        "(?:A/c|account)\\s*\\*?(\\w+)", Pattern.CASE_INSENSITIVE
    )

    fun parseMultipleSms(text: String): List<ParsedSmsTransaction> {
        val messages = splitMessages(text)
        return messages.mapNotNull { parseSingleSms(it) }
    }

    private fun splitMessages(text: String): List<String> {
        val lines = text.split("\n")
        val messages = mutableListOf<String>()
        val current = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) {
                if (current.isNotEmpty()) {
                    messages.add(current.toString().trim())
                    current.clear()
                }
            } else {
                if (current.isNotEmpty()) current.append(" ")
                current.append(trimmed)
            }
        }
        if (current.isNotEmpty()) {
            messages.add(current.toString().trim())
        }
        if (messages.isEmpty() && text.isNotBlank()) {
            messages.add(text.trim())
        }
        return messages
    }

    private fun parseSingleSms(sms: String): ParsedSmsTransaction? {
        val amounts = mutableListOf<Double>()
        val amountMatcher = amountPattern.matcher(sms)
        while (amountMatcher.find()) {
            val amountStr = amountMatcher.group(1)?.replace(",", "") ?: continue
            amountStr.toDoubleOrNull()?.let { amounts.add(it) }
        }

        if (amounts.isEmpty()) return null

        val balanceMatcher = balancePattern.matcher(sms)
        var balanceAfter: Double? = null
        if (balanceMatcher.find()) {
            balanceAfter = balanceMatcher.group(1)?.replace(",", "")?.toDoubleOrNull()
        }

        val transactionAmount = amounts.firstOrNull { it != balanceAfter } ?: amounts.first()

        val isDebit = sms.contains("Debit", true) || sms.contains("paid", true) ||
                sms.contains("spent", true) || sms.contains("withdrawn", true) ||
                sms.contains("purchase", true) || sms.contains("debited", true)
        val isCredit = sms.contains("Credit", true) || sms.contains("received", true) ||
                sms.contains("deposited", true) || sms.contains("credited", true) ||
                sms.contains("NEFT Inward", true) || sms.contains("IMPS", true) ||
                sms.contains("refund", true)

        val type = when {
            isDebit -> TransactionType.EXPENSE
            isCredit -> TransactionType.INCOME
            else -> return null
        }

        val refMatcher = refPattern.matcher(sms)
        val referenceNumber = if (refMatcher.find()) refMatcher.group(1) ?: "" else ""

        val accountMatcher = accountPattern.matcher(sms)
        val accountRef = if (accountMatcher.find()) accountMatcher.group(1) ?: "" else ""

        val account = when {
            sms.contains("wallet", true) || sms.contains("PhonePe", true) ||
                    sms.contains("Paytm", true) -> AccountType.WALLET
            sms.contains("bank", true) || sms.contains("A/c", true) ||
                    sms.contains("NEFT", true) || sms.contains("IMPS", true) ||
                    accountRef.isNotEmpty() -> AccountType.BANK
            else -> AccountType.BANK
        }

        var dateTime: Long? = null
        val dateMatcher1 = datePattern1.matcher(sms)
        if (dateMatcher1.find()) {
            try {
                val dateStr = "${dateMatcher1.group(1)}-${dateMatcher1.group(2)}-${dateMatcher1.group(3)} ${dateMatcher1.group(4)}"
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.ENGLISH)
                dateTime = sdf.parse(dateStr)?.time
            } catch (_: Exception) {}
        }
        if (dateTime == null) {
            val dateMatcher2 = datePattern2.matcher(sms)
            if (dateMatcher2.find()) {
                try {
                    val dateStr = "${dateMatcher2.group(1)}-${dateMatcher2.group(2)}-${dateMatcher2.group(3)}"
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.ENGLISH)
                    dateTime = sdf.parse(dateStr)?.time
                } catch (_: Exception) {}
            }
        }

        val transactionName = extractTransactionName(sms, type)
        val isPending = sms.contains("Pending", true) || sms.contains("verification", true)

        return ParsedSmsTransaction(
            type = type,
            amount = transactionAmount,
            transactionName = transactionName,
            account = account,
            dateTime = dateTime,
            referenceNumber = referenceNumber,
            balanceAfter = balanceAfter,
            accountReference = accountRef,
            isPending = isPending
        )
    }

    private fun extractTransactionName(sms: String, type: TransactionType): String {
        val fvgPattern = Pattern.compile("Fvg:\\s*(.+?)(?:\\s+Avl|\\s+Bal|\\.|$)", Pattern.CASE_INSENSITIVE)
        val fvgMatcher = fvgPattern.matcher(sms)
        if (fvgMatcher.find()) {
            return fvgMatcher.group(1)?.trim() ?: ""
        }

        val remPattern = Pattern.compile("Rem(?:itter)?:\\s*(.+?)(?:\\s*,|\\s+Avl|\\s+Bal|$)", Pattern.CASE_INSENSITIVE)
        val remMatcher = remPattern.matcher(sms)
        if (remMatcher.find()) {
            return remMatcher.group(1)?.trim() ?: ""
        }

        val viaPattern = Pattern.compile("via\\s+(.+?)(?:\\.|\\s+Not|\\s+Remaining|$)", Pattern.CASE_INSENSITIVE)
        val viaMatcher = viaPattern.matcher(sms)
        if (viaMatcher.find()) {
            val via = viaMatcher.group(1)?.trim() ?: ""
            return if (type == TransactionType.EXPENSE) "$via payment" else via
        }

        val toPattern = Pattern.compile("(?:to|at|for)\\s+(.+?)(?:\\s+on|\\s+ref|\\.|$)", Pattern.CASE_INSENSITIVE)
        val toMatcher = toPattern.matcher(sms)
        if (toMatcher.find()) {
            return toMatcher.group(1)?.trim()?.take(50) ?: ""
        }

        return if (type == TransactionType.INCOME) "Bank Credit" else "Payment"
    }
}
