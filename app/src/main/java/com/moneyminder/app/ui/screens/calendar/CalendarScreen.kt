package com.moneyminder.app.ui.screens.calendar

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.R
import com.moneyminder.app.data.entity.Transaction
import com.moneyminder.app.data.entity.TransactionType
import com.moneyminder.app.ui.components.TransactionCard
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.util.DateUtils
import com.moneyminder.app.viewmodel.MoneyMinderViewModel
import java.util.*

@Composable
fun CalendarScreen(
    viewModel: MoneyMinderViewModel,
    onTransactionClick: (Long) -> Unit,
    onAddTransaction: () -> Unit
) {
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val transactions by viewModel.monthlyTransactions.collectAsState()
    val runningBalances by viewModel.runningBalances.collectAsState()

    var selectedDay by remember { mutableStateOf<Int?>(null) }
    val dayTransactions = remember(selectedDay, transactions) {
        if (selectedDay != null) {
            val cal = Calendar.getInstance()
            transactions.filter {
                cal.timeInMillis = it.dateTime
                cal.get(Calendar.DAY_OF_MONTH) == selectedDay
            }
        } else emptyList()
    }

    val dayTotals = remember(transactions) {
        val map = mutableMapOf<Int, Triple<Double, Double, Double>>()
        val cal = Calendar.getInstance()
        transactions.forEach { txn ->
            cal.timeInMillis = txn.dateTime
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val current = map[day] ?: Triple(0.0, 0.0, 0.0)
            map[day] = when (txn.type) {
                TransactionType.INCOME -> Triple(current.first + txn.amount, current.second, current.third)
                TransactionType.EXPENSE -> Triple(current.first, current.second + txn.amount, current.third)
                TransactionType.TRANSFER -> Triple(current.first, current.second, current.third + txn.amount)
            }
        }
        map
    }

    val monthIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
    val monthExpense = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
    val monthTransfer = transactions.filter { it.type == TransactionType.TRANSFER }.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                painter = painterResource(id = R.drawable.money_minder_logo),
                contentDescription = "Money Minder",
                modifier = Modifier.height(32.dp),
                contentScale = ContentScale.FillHeight
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Calendar", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (month == 0) viewModel.setSelectedMonth(year - 1, 11)
                else viewModel.setSelectedMonth(year, month - 1)
            }) {
                Icon(Icons.Filled.ChevronLeft, "Previous", tint = TextPrimary)
            }

            val months = listOf("January","February","March","April","May","June","July","August","September","October","November","December")
            Text("${months[month]} $year", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)

            IconButton(onClick = {
                if (month == 11) viewModel.setSelectedMonth(year + 1, 0)
                else viewModel.setSelectedMonth(year, month + 1)
            }) {
                Icon(Icons.Filled.ChevronRight, "Next", tint = TextPrimary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            SummaryChip("Income", monthIncome, IncomeGreen)
            SummaryChip("Expense", monthExpense, ExpenseRed)
            SummaryChip("Transfer", monthTransfer, TransferBlue)
        }

        Spacer(modifier = Modifier.height(8.dp))

        CalendarGrid(
            year = year,
            month = month,
            selectedDay = selectedDay,
            dayTotals = dayTotals,
            onDayClick = { day ->
                selectedDay = if (selectedDay == day) null else day
            }
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (selectedDay != null) {
            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
            Text(
                "$selectedDay ${months[month]} $year",
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (dayTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No transactions", color = TextTertiary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onAddTransaction) {
                            Text("+ Add Transaction", color = TextSecondary)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
                ) {
                    items(dayTransactions, key = { it.id }) { txn ->
                        TransactionCard(
                            transaction = txn,
                            runningBalances = runningBalances[txn.id],
                            onClick = { onTransactionClick(txn.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryChip(label: String, amount: Double, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = color, fontSize = 11.sp)
        Text(
            CurrencyUtils.formatAmount(amount),
            color = color,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun CalendarGrid(
    year: Int,
    month: Int,
    selectedDay: Int?,
    dayTotals: Map<Int, Triple<Double, Double, Double>>,
    onDayClick: (Int) -> Unit
) {
    val daysInMonth = DateUtils.getDaysInMonth(year, month)
    val firstDayOfWeek = DateUtils.getFirstDayOfWeek(year, month)
    val today = Calendar.getInstance()
    val isCurrentMonth = today.get(Calendar.YEAR) == year && today.get(Calendar.MONTH) == month
    val currentDay = today.get(Calendar.DAY_OF_MONTH)

    Column(modifier = Modifier.padding(horizontal = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))

        var dayCounter = 1
        val offset = firstDayOfWeek - 1

        for (week in 0..5) {
            if (dayCounter > daysInMonth) break
            Row(modifier = Modifier.fillMaxWidth()) {
                for (dayOfWeek in 0..6) {
                    val cellIndex = week * 7 + dayOfWeek
                    if (cellIndex < offset || dayCounter > daysInMonth) {
                        Spacer(modifier = Modifier.weight(1f))
                    } else {
                        val day = dayCounter
                        val isSelected = day == selectedDay
                        val isToday = isCurrentMonth && day == currentDay
                        val totals = dayTotals[day]

                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(0.85f)
                                .padding(1.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    when {
                                        isSelected -> GlassHighlight
                                        else -> Color.Transparent
                                    }
                                )
                                .border(
                                    width = if (isToday) 1.dp else 0.dp,
                                    color = if (isToday) TextSecondary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onDayClick(day) },
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(2.dp)
                            ) {
                                Text(
                                    "$day",
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                                )
                                if (totals != null) {
                                    if (totals.first > 0) {
                                        Text(
                                            "+${formatCompact(totals.first)}",
                                            color = IncomeGreen,
                                            fontSize = 7.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                    if (totals.second > 0) {
                                        Text(
                                            "-${formatCompact(totals.second)}",
                                            color = ExpenseRed,
                                            fontSize = 7.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Clip
                                        )
                                    }
                                    if (totals.third > 0) {
                                        Box(
                                            modifier = Modifier
                                                .size(4.dp)
                                                .clip(CircleShape)
                                                .background(TransferBlue)
                                        )
                                    }
                                }
                            }
                        }
                        dayCounter++
                    }
                }
            }
        }
    }
}

private fun formatCompact(amount: Double): String {
    return when {
        amount >= 100000 -> "${(amount / 100000).toInt()}L"
        amount >= 1000 -> "${(amount / 1000).toInt()}K"
        amount == amount.toLong().toDouble() -> amount.toLong().toString()
        else -> String.format("%.0f", amount)
    }
}
