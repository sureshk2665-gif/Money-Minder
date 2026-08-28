package com.moneyminder.app.ui.screens.home

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.moneyminder.app.R
import com.moneyminder.app.data.entity.*
import com.moneyminder.app.ui.components.*
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.util.DateUtils
import com.moneyminder.app.util.ExportUtils
import com.moneyminder.app.viewmodel.MoneyMinderViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MoneyMinderViewModel,
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit,
    onTransfer: () -> Unit,
    onImportSms: () -> Unit,
    onTransactionClick: (Long) -> Unit,
    onSettingsClick: () -> Unit
) {
    val year by viewModel.selectedYear.collectAsState()
    val month by viewModel.selectedMonth.collectAsState()
    val overall by viewModel.overallBalance.collectAsState()
    val bank by viewModel.bankBalance.collectAsState()
    val wallet by viewModel.walletBalance.collectAsState()
    val cash by viewModel.cashBalance.collectAsState()
    val income by viewModel.monthlyIncome.collectAsState()
    val expense by viewModel.monthlyExpense.collectAsState()
    val transferTotal by viewModel.monthlyTransferTotal.collectAsState()
    val transactions by viewModel.monthlyTransactions.collectAsState()
    val runningBalances by viewModel.runningBalances.collectAsState()

    var showAccountPopup by remember { mutableStateOf<AccountType?>(null) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Black),
        contentPadding = PaddingValues(bottom = 100.dp)
    ) {
        item {
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
                Spacer(modifier = Modifier.weight(1f))
                MonthSelector(year = year, month = month) { y, m ->
                    viewModel.setSelectedMonth(y, m)
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSettingsClick) {
                    Icon(Icons.Filled.Settings, "Settings", tint = TextSecondary)
                }
            }
        }

        item {
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
                    Text("Overall Balance", color = TextSecondary, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        CurrencyUtils.formatAmount(overall),
                        color = TextPrimary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Income", color = IncomeGreen, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                CurrencyUtils.formatAmount(income),
                                color = IncomeGreen,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Expense", color = ExpenseRed, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                CurrencyUtils.formatAmount(expense),
                                color = ExpenseRed,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    if (transferTotal > 0) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Transfers this month: ${CurrencyUtils.formatAmount(transferTotal)} moved",
                            color = TextTertiary,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AccountBalanceCard(AccountType.BANK, bank) { showAccountPopup = AccountType.BANK }
                AccountBalanceCard(AccountType.WALLET, wallet) { showAccountPopup = AccountType.WALLET }
                AccountBalanceCard(AccountType.CASH, cash) { showAccountPopup = AccountType.CASH }
            }
        }

        item {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                Text("Quick Actions", color = TextSecondary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    QuickActionButton(Icons.Filled.RemoveCircleOutline, "Add\nExpense", ExpenseRed, onAddExpense)
                    QuickActionButton(Icons.Filled.AddCircleOutline, "Add\nIncome", IncomeGreen, onAddIncome)
                    QuickActionButton(Icons.Filled.SwapHoriz, "Transfer\nMoney", TransferBlue, onTransfer)
                    QuickActionButton(Icons.Filled.Sms, "Import\nSMS", TextSecondary, onImportSms)
                    QuickActionButton(Icons.Filled.FileDownload, "Export\nReport", TextSecondary) {
                        scope.launch {
                            val txns = viewModel.getTransactionsForExportSuspend()
                            val months = listOf("Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec")
                            val label = "${months[month]}_$year"
                            val file = ExportUtils.exportToExcel(context, txns, label)
                            file?.let { ExportUtils.shareFile(context, it) }
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Overall Activity",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

        if (transactions.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.ReceiptLong, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("No transactions yet", color = TextTertiary, fontSize = 14.sp)
                        Text("Tap + to add your first transaction", color = TextTertiary, fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(transactions, key = { it.id }) { transaction ->
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically()
                    ) {
                        TransactionCard(
                            transaction = transaction,
                            runningBalances = runningBalances[transaction.id],
                            onClick = { onTransactionClick(transaction.id) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    showAccountPopup?.let { account ->
        AccountActivityPopup(
            account = account,
            viewModel = viewModel,
            runningBalances = runningBalances,
            onDismiss = { showAccountPopup = null },
            onTransactionClick = onTransactionClick
        )
    }
}

@Composable
fun AccountActivityPopup(
    account: AccountType,
    viewModel: MoneyMinderViewModel,
    runningBalances: Map<Long, Map<AccountType, Double>>,
    onDismiss: () -> Unit,
    onTransactionClick: (Long) -> Unit
) {
    var filterTypes by remember { mutableStateOf<List<TransactionType>>(listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)) }
    val transactions by viewModel.getTransactionsByAccountAndTypes(account, filterTypes).collectAsState(emptyList())

    val balance = when (account) {
        AccountType.BANK -> viewModel.bankBalance.collectAsState().value
        AccountType.WALLET -> viewModel.walletBalance.collectAsState().value
        AccountType.CASH -> viewModel.cashBalance.collectAsState().value
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Charcoal)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "${account.name.lowercase().replaceFirstChar { it.uppercase() }} Activity",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Balance: ${CurrencyUtils.formatAmount(balance)}",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Filled.Close, "Close", tint = TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = TransactionType.INCOME in filterTypes,
                        onClick = {
                            filterTypes = if (TransactionType.INCOME in filterTypes)
                                filterTypes - TransactionType.INCOME
                            else filterTypes + TransactionType.INCOME
                            if (filterTypes.isEmpty()) filterTypes = listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
                        },
                        label = { Text("Income", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = IncomeGreen.copy(alpha = 0.2f),
                            selectedLabelColor = IncomeGreen
                        )
                    )
                    FilterChip(
                        selected = TransactionType.EXPENSE in filterTypes,
                        onClick = {
                            filterTypes = if (TransactionType.EXPENSE in filterTypes)
                                filterTypes - TransactionType.EXPENSE
                            else filterTypes + TransactionType.EXPENSE
                            if (filterTypes.isEmpty()) filterTypes = listOf(TransactionType.INCOME, TransactionType.EXPENSE, TransactionType.TRANSFER)
                        },
                        label = { Text("Expense", fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                            selectedLabelColor = ExpenseRed
                        )
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                LazyColumn {
                    items(transactions, key = { it.id }) { txn ->
                        TransactionCard(
                            transaction = txn,
                            runningBalances = runningBalances[txn.id],
                            onClick = {
                                onTransactionClick(txn.id)
                                onDismiss()
                            }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                    if (transactions.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No transactions for this account", color = TextTertiary)
                            }
                        }
                    }
                }
            }
        }
    }
}
