package com.moneyminder.app.util

import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*

object CurrencyUtils {
    private val wholeFormat = DecimalFormat("#,##,###")
    private val decimalFormat = DecimalFormat("#,##,##0.##")

    fun formatAmount(amount: Double): String {
        return if (amount == amount.toLong().toDouble()) {
            "₹${wholeFormat.format(amount.toLong())}"
        } else {
            "₹${decimalFormat.format(amount)}"
        }
    }

    fun formatAmountSigned(amount: Double, isExpense: Boolean): String {
        val prefix = if (isExpense) "− " else "+ "
        return prefix + formatAmount(kotlin.math.abs(amount))
    }
}

object DateUtils {
    private val dateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
    private val monthYearFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
    private val monthYearUpperFormat = SimpleDateFormat("MMM yyyy", Locale.ENGLISH)
    private val dayFormat = SimpleDateFormat("dd", Locale.ENGLISH)
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

    fun formatDateTime(timestamp: Long): String = dateTimeFormat.format(Date(timestamp))
    fun formatDate(timestamp: Long): String = dateFormat.format(Date(timestamp))
    fun formatMonthYear(timestamp: Long): String = monthYearUpperFormat.format(Date(timestamp)).uppercase()
    fun formatDay(timestamp: Long): String = dayFormat.format(Date(timestamp))
    fun formatTime(timestamp: Long): String = timeFormat.format(Date(timestamp))

    fun getMonthStart(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getMonthEnd(year: Int, month: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.MILLISECOND, -1)
        return cal.timeInMillis
    }

    fun getDayStart(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun getDayEnd(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.set(year, month, day, 23, 59, 59)
        cal.set(Calendar.MILLISECOND, 999)
        return cal.timeInMillis
    }

    fun getCurrentYear(): Int = Calendar.getInstance().get(Calendar.YEAR)
    fun getCurrentMonth(): Int = Calendar.getInstance().get(Calendar.MONTH)
    fun getCurrentDay(): Int = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    fun getDaysInMonth(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeek(year: Int, month: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(year, month, 1)
        return cal.get(Calendar.DAY_OF_WEEK)
    }
}
