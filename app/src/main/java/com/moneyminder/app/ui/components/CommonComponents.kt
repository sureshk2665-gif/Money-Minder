package com.moneyminder.app.ui.components

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.R
import com.moneyminder.app.data.entity.*
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.util.DateUtils

@Composable
fun AppHeader(title: String, showSettings: Boolean = false, onSettingsClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.money_minder_logo),
            contentDescription = "Money Minder",
            modifier = Modifier.height(36.dp),
            contentScale = ContentScale.FillHeight
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = title,
            color = TextPrimary,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )
        if (showSettings) {
            IconButton(onClick = onSettingsClick) {
                Icon(Icons.Filled.Settings, "Settings", tint = TextSecondary)
            }
        }
    }
}

@Composable
fun MonthSelector(
    year: Int,
    month: Int,
    onMonthChange: (Int, Int) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val months = listOf("JAN", "FEB", "MAR", "APR", "MAY", "JUN", "JUL", "AUG", "SEP", "OCT", "NOV", "DEC")

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(DarkCard)
            .border(1.dp, CardBorder, RoundedCornerShape(20.dp))
            .clickable { showPicker = true }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = "${months[month]} $year",
            color = TextPrimary,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.width(4.dp))
        Icon(Icons.Filled.KeyboardArrowDown, "Select month", tint = TextSecondary, modifier = Modifier.size(18.dp))
    }

    if (showPicker) {
        MonthPickerDialog(
            currentYear = year,
            currentMonth = month,
            onDismiss = { showPicker = false },
            onMonthSelected = { y, m ->
                onMonthChange(y, m)
                showPicker = false
            }
        )
    }
}

@Composable
fun MonthPickerDialog(
    currentYear: Int,
    currentMonth: Int,
    onDismiss: () -> Unit,
    onMonthSelected: (Int, Int) -> Unit
) {
    var selectedYear by remember { mutableIntStateOf(currentYear) }
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { selectedYear-- }) {
                    Icon(Icons.Filled.ChevronLeft, "Previous year", tint = TextPrimary)
                }
                Text("$selectedYear", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                IconButton(onClick = { selectedYear++ }) {
                    Icon(Icons.Filled.ChevronRight, "Next year", tint = TextPrimary)
                }
            }
        },
        text = {
            Column {
                for (row in 0..3) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        for (col in 0..2) {
                            val monthIndex = row * 3 + col
                            val isSelected = monthIndex == currentMonth && selectedYear == currentYear
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isSelected) GlassHighlight else Color.Transparent)
                                    .border(
                                        if (isSelected) 1.dp else 0.dp,
                                        if (isSelected) TextSecondary else Color.Transparent,
                                        RoundedCornerShape(12.dp)
                                    )
                                    .clickable { onMonthSelected(selectedYear, monthIndex) }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    months[monthIndex],
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AccountBalanceCard(
    account: AccountType,
    balance: Double,
    onClick: () -> Unit
) {
    val icon = when (account) {
        AccountType.BANK -> Icons.Filled.AccountBalance
        AccountType.WALLET -> Icons.Filled.AccountBalanceWallet
        AccountType.CASH -> Icons.Filled.Payments
    }

    Card(
        modifier = Modifier
            .clickable(onClick = onClick)
            .width(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, account.name, tint = TextSecondary, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                account.name.lowercase().replaceFirstChar { it.uppercase() },
                color = TextTertiary,
                fontSize = 11.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                CurrencyUtils.formatAmount(balance),
                color = TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun TransactionCard(
    transaction: Transaction,
    runningBalances: Map<AccountType, Double>?,
    onClick: () -> Unit = {}
) {
    val bgColor = when (transaction.type) {
        TransactionType.INCOME -> IncomeGreenBg
        TransactionType.EXPENSE -> ExpenseRedBg
        TransactionType.TRANSFER -> DarkCard
    }
    val accentColor = when (transaction.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }
    val icon = when (transaction.type) {
        TransactionType.INCOME -> Icons.Filled.ArrowUpward
        TransactionType.EXPENSE -> Icons.Filled.ArrowDownward
        TransactionType.TRANSFER -> Icons.Filled.SwapHoriz
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = accentColor, modifier = Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.category.ifBlank {
                        if (transaction.type == TransactionType.TRANSFER) "Transfer" else "Transaction"
                    },
                    color = TextPrimary,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))

                val contextText = when (transaction.type) {
                    TransactionType.INCOME -> "Received in ${transaction.toAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""}"
                    TransactionType.EXPENSE -> "Paid from ${transaction.fromAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""}"
                    TransactionType.TRANSFER -> "${transaction.fromAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""} → ${transaction.toAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: ""}"
                }
                Text(
                    text = "$contextText · ${DateUtils.formatDateTime(transaction.dateTime)}",
                    color = TextTertiary,
                    fontSize = 12.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (runningBalances != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    when (transaction.type) {
                        TransactionType.INCOME -> {
                            transaction.toAccount?.let { acc ->
                                runningBalances[acc]?.let { bal ->
                                    Text(
                                        "${acc.name.lowercase().replaceFirstChar { it.uppercase() }} Balance: ${CurrencyUtils.formatAmount(bal)}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        TransactionType.EXPENSE -> {
                            transaction.fromAccount?.let { acc ->
                                runningBalances[acc]?.let { bal ->
                                    Text(
                                        "${acc.name.lowercase().replaceFirstChar { it.uppercase() }} Balance: ${CurrencyUtils.formatAmount(bal)}",
                                        color = TextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                        TransactionType.TRANSFER -> {
                            val fromBal = transaction.fromAccount?.let { runningBalances[it] }
                            val toBal = transaction.toAccount?.let { runningBalances[it] }
                            if (fromBal != null) {
                                Text(
                                    "${transaction.fromAccount.name.lowercase().replaceFirstChar { it.uppercase() }} Balance: ${CurrencyUtils.formatAmount(fromBal)}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                            if (toBal != null) {
                                Text(
                                    "${transaction.toAccount!!.name.lowercase().replaceFirstChar { it.uppercase() }} Balance: ${CurrencyUtils.formatAmount(toBal)}",
                                    color = TextSecondary,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = when (transaction.type) {
                        TransactionType.INCOME -> "+ ${CurrencyUtils.formatAmount(transaction.amount)}"
                        TransactionType.EXPENSE -> "− ${CurrencyUtils.formatAmount(transaction.amount)}"
                        TransactionType.TRANSFER -> CurrencyUtils.formatAmount(transaction.amount)
                    },
                    color = accentColor,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    color: Color = TextPrimary,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(8.dp)
            .width(72.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, label, tint = color, modifier = Modifier.size(24.dp))
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            label,
            color = TextSecondary,
            fontSize = 11.sp,
            maxLines = 2,
            lineHeight = 14.sp,
            modifier = Modifier.widthIn(max = 72.dp),
            overflow = TextOverflow.Ellipsis
        )
    }
}
