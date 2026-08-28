package com.moneyminder.app.ui.screens.insights

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.R
import com.moneyminder.app.data.dao.CategorySum
import com.moneyminder.app.data.entity.TransactionType
import com.moneyminder.app.ui.components.MonthSelector
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.util.DateUtils
import com.moneyminder.app.viewmodel.MoneyMinderViewModel

private val chartColors = listOf(
    Color(0xFF78909C), Color(0xFF90A4AE), Color(0xFFB0BEC5),
    Color(0xFF546E7A), Color(0xFF607D8B), Color(0xFF455A64),
    Color(0xFFCFD8DC), Color(0xFF37474F), Color(0xFF263238),
    Color(0xFFECEFF1)
)

@Composable
fun InsightsScreen(viewModel: MoneyMinderViewModel) {
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val income by viewModel.monthlyIncome.collectAsState()
    val expense by viewModel.monthlyExpense.collectAsState()
    val bank by viewModel.bankBalance.collectAsState()
    val wallet by viewModel.walletBalance.collectAsState()
    val cash by viewModel.cashBalance.collectAsState()
    val overall by viewModel.overallBalance.collectAsState()
    val categorySums by viewModel.categorySums.collectAsState()
    val transferTotal by viewModel.monthlyTransferTotal.collectAsState()
    val transactions by viewModel.monthlyTransactions.collectAsState()

    val daysInMonth = DateUtils.getDaysInMonth(year, month)
    val avgDaily = if (daysInMonth > 0 && expense > 0) expense / daysInMonth else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .verticalScroll(rememberScrollState())
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
            Text("Insights", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            MonthSelector(year, month) { y, m -> viewModel.setSelectedMonth(y, m) }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Monthly Balance", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Overall: ${CurrencyUtils.formatAmount(overall)}",
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BalanceItem("Bank", bank)
                    BalanceItem("Wallet", wallet)
                    BalanceItem("Cash", cash)
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = CardBorder)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Income", color = IncomeGreen, fontSize = 12.sp)
                        Text(CurrencyUtils.formatAmount(income), color = IncomeGreen, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Expense", color = ExpenseRed, fontSize = 12.sp)
                        Text(CurrencyUtils.formatAmount(expense), color = ExpenseRed, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("Net", color = TextSecondary, fontSize = 12.sp)
                        Text(
                            CurrencyUtils.formatAmount(income - expense),
                            color = if (income >= expense) IncomeGreen else ExpenseRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text("Transfers", color = TransferBlue, fontSize = 12.sp)
                        Text(
                            "${CurrencyUtils.formatAmount(transferTotal)} moved",
                            color = TransferBlue,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }

        if (categorySums.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Spending by Category", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    DonutChart(categorySums, expense)
                    Spacer(modifier = Modifier.height(16.dp))
                    categorySums.forEachIndexed { index, cat ->
                        val color = chartColors[index % chartColors.size]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(color)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                cat.category,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                modifier = Modifier.weight(1f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                CurrencyUtils.formatAmount(cat.total),
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            if (expense > 0) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "${(cat.total / expense * 100).toInt()}%",
                                    color = TextTertiary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        if (categorySums.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Top Spending Category", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    val topCategory = categorySums.first()
                    Text(topCategory.category, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                    Text(CurrencyUtils.formatAmount(topCategory.total), color = ExpenseRed, fontSize = 16.sp)
                }
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Average Daily Spending", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(CurrencyUtils.formatAmount(avgDaily), color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("per day this month", color = TextTertiary, fontSize = 12.sp)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Income vs Expense", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                val maxVal = maxOf(income, expense, 1.0)
                BarRow("Income", income, maxVal, IncomeGreen)
                Spacer(modifier = Modifier.height(8.dp))
                BarRow("Expense", expense, maxVal, ExpenseRed)
            }
        }

        if (transferTotal > 0) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Account Movements", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    val transfers = transactions.filter { it.type == TransactionType.TRANSFER }
                    transfers.take(5).forEach { txn ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "${txn.fromAccount?.name ?: ""} → ${txn.toAccount?.name ?: ""}",
                                color = TextPrimary,
                                fontSize = 13.sp
                            )
                            Text(CurrencyUtils.formatAmount(txn.amount), color = TransferBlue, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}

@Composable
private fun BalanceItem(label: String, balance: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, color = TextTertiary, fontSize = 11.sp)
        Text(CurrencyUtils.formatAmount(balance), color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun BarRow(label: String, value: Double, maxValue: Double, color: Color) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, color = TextSecondary, fontSize = 12.sp)
            Text(CurrencyUtils.formatAmount(value), color = color, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        val fraction = if (maxValue > 0) (value / maxValue).toFloat().coerceIn(0f, 1f) else 0f
        val animatedFraction by animateFloatAsState(
            targetValue = fraction,
            animationSpec = tween(800, easing = EaseOutCubic),
            label = "bar"
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(CardBorder)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedFraction)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color)
            )
        }
    }
}

@Composable
private fun DonutChart(categorySums: List<CategorySum>, totalExpense: Double) {
    val animProgress by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(1200, easing = EaseOutCubic),
        label = "donut"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val strokeWidth = 32.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val topLeft = Offset(
                (size.width - radius * 2) / 2,
                (size.height - radius * 2) / 2
            )
            val arcSize = Size(radius * 2, radius * 2)

            var startAngle = -90f
            categorySums.forEachIndexed { index, cat ->
                val sweep = if (totalExpense > 0) (cat.total / totalExpense * 360f).toFloat() else 0f
                drawArc(
                    color = chartColors[index % chartColors.size],
                    startAngle = startAngle,
                    sweepAngle = sweep * animProgress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
                )
                startAngle += sweep * animProgress
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Total", color = TextTertiary, fontSize = 11.sp)
            Text(
                CurrencyUtils.formatAmount(totalExpense),
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
