package com.moneyminder.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.moneyminder.app.data.entity.AccountType
import com.moneyminder.app.data.entity.TransactionType
import com.moneyminder.app.ui.navigation.BottomNavBar
import com.moneyminder.app.ui.navigation.Screen
import com.moneyminder.app.ui.screens.add.AddTransactionScreen
import com.moneyminder.app.ui.screens.calendar.CalendarScreen
import com.moneyminder.app.ui.screens.home.HomeScreen
import com.moneyminder.app.ui.screens.insights.InsightsScreen
import com.moneyminder.app.ui.screens.settings.SettingsScreen
import com.moneyminder.app.ui.screens.sms.SmsScreen
import com.moneyminder.app.ui.screens.mpin.ForgotMpinScreen
import com.moneyminder.app.ui.screens.mpin.MpinLockScreen
import com.moneyminder.app.ui.screens.mpin.MpinSetupScreen
import com.moneyminder.app.ui.screens.mpin.ResetMpinScreen
import com.moneyminder.app.ui.screens.heldmoney.HeldMoneyDetailScreen
import com.moneyminder.app.ui.screens.splash.SplashScreen
import com.moneyminder.app.ui.screens.welcome.WelcomeScreen
import com.moneyminder.app.util.MpinManager
import com.moneyminder.app.ui.theme.Black
import com.moneyminder.app.ui.theme.MoneyMinderTheme
import com.moneyminder.app.util.ParsedSmsTransaction
import com.moneyminder.app.viewmodel.MoneyMinderViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MoneyMinderTheme {
                MoneyMinderAppContent()
            }
        }
    }
}

enum class AppScreen {
    SPLASH, WELCOME, MPIN_SETUP, MPIN_LOCK, MPIN_FORGOT, MAIN, ADD_TRANSACTION, EDIT_TRANSACTION, SETTINGS, RESET_MPIN, HELD_MONEY_DETAIL
}

@Composable
fun MoneyMinderAppContent() {
    val viewModel: MoneyMinderViewModel = viewModel()
    val systemUiController = rememberSystemUiController()
    val isFirstLaunch by viewModel.isFirstLaunch.collectAsState()

    LaunchedEffect(Unit) {
        systemUiController.setSystemBarsColor(Color.Black, darkIcons = false)
    }

    var appScreen by remember { mutableStateOf(AppScreen.SPLASH) }
    var currentTab by remember { mutableStateOf(Screen.HOME) }
    var addTransactionType by remember { mutableStateOf(TransactionType.EXPENSE) }
    var editTransactionId by remember { mutableStateOf<Long?>(null) }
    var smsPreFill by remember { mutableStateOf<ParsedSmsTransaction?>(null) }

    var transactionDetailId by remember { mutableStateOf<Long?>(null) }
    var showTransactionDetail by remember { mutableStateOf(false) }
    var heldMoneyDetailId by remember { mutableStateOf<Long>(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding()
    ) {
        AnimatedContent(
            targetState = appScreen,
            transitionSpec = {
                fadeIn(tween(300)) togetherWith fadeOut(tween(300))
            },
            label = "screenTransition"
        ) { screen ->
            when (screen) {
                AppScreen.SPLASH -> {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    SplashScreen {
                        appScreen = when {
                            isFirstLaunch -> AppScreen.WELCOME
                            MpinManager.isMpinSet(context) -> AppScreen.MPIN_LOCK
                            else -> AppScreen.MAIN
                        }
                    }
                }

                AppScreen.WELCOME -> {
                    WelcomeScreen {
                        viewModel.completeOnboarding()
                        appScreen = AppScreen.MPIN_SETUP
                    }
                }

                AppScreen.MPIN_SETUP -> {
                    MpinSetupScreen {
                        appScreen = AppScreen.MAIN
                    }
                }

                AppScreen.MPIN_LOCK -> {
                    MpinLockScreen(
                        onUnlocked = { appScreen = AppScreen.MAIN },
                        onForgotPin = { appScreen = AppScreen.MPIN_FORGOT }
                    )
                }

                AppScreen.MPIN_FORGOT -> {
                    ForgotMpinScreen(
                        onReset = { appScreen = AppScreen.MAIN },
                        onBack = { appScreen = AppScreen.MPIN_LOCK }
                    )
                }

                AppScreen.MAIN -> {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AnimatedContent(
                            targetState = currentTab,
                            transitionSpec = {
                                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
                            },
                            label = "tabTransition"
                        ) { tab ->
                            when (tab) {
                                Screen.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    onAddExpense = {
                                        addTransactionType = TransactionType.EXPENSE
                                        smsPreFill = null
                                        editTransactionId = null
                                        appScreen = AppScreen.ADD_TRANSACTION
                                    },
                                    onAddIncome = {
                                        addTransactionType = TransactionType.INCOME
                                        smsPreFill = null
                                        editTransactionId = null
                                        appScreen = AppScreen.ADD_TRANSACTION
                                    },
                                    onTransfer = {
                                        addTransactionType = TransactionType.TRANSFER
                                        smsPreFill = null
                                        editTransactionId = null
                                        appScreen = AppScreen.ADD_TRANSACTION
                                    },
                                    onImportSms = {
                                        currentTab = Screen.SMS
                                    },
                                    onTransactionClick = { id ->
                                        transactionDetailId = id
                                        showTransactionDetail = true
                                    },
                                    onSettingsClick = {
                                        appScreen = AppScreen.SETTINGS
                                    },
                                    onHeldMoneyClick = { id ->
                                        heldMoneyDetailId = id
                                        appScreen = AppScreen.HELD_MONEY_DETAIL
                                    }
                                )

                                Screen.INSIGHTS -> InsightsScreen(viewModel = viewModel)

                                Screen.ADD -> {
                                    addTransactionType = TransactionType.EXPENSE
                                    smsPreFill = null
                                    editTransactionId = null
                                    appScreen = AppScreen.ADD_TRANSACTION
                                    currentTab = Screen.HOME
                                }

                                Screen.CALENDAR -> CalendarScreen(
                                    viewModel = viewModel,
                                    onTransactionClick = { id ->
                                        transactionDetailId = id
                                        showTransactionDetail = true
                                    },
                                    onAddTransaction = {
                                        addTransactionType = TransactionType.EXPENSE
                                        smsPreFill = null
                                        editTransactionId = null
                                        appScreen = AppScreen.ADD_TRANSACTION
                                    }
                                )

                                Screen.SMS -> SmsScreen(
                                    viewModel = viewModel,
                                    onAddFromSms = { parsed ->
                                        smsPreFill = parsed
                                        addTransactionType = parsed.type
                                        editTransactionId = null
                                        appScreen = AppScreen.ADD_TRANSACTION
                                    }
                                )
                            }
                        }

                        BottomNavBar(
                            currentScreen = currentTab,
                            onScreenSelected = { selected ->
                                if (selected == Screen.ADD) {
                                    addTransactionType = TransactionType.EXPENSE
                                    smsPreFill = null
                                    editTransactionId = null
                                    appScreen = AppScreen.ADD_TRANSACTION
                                } else {
                                    currentTab = selected
                                }
                            },
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }

                AppScreen.ADD_TRANSACTION -> {
                    val prefill = smsPreFill
                    AddTransactionScreen(
                        viewModel = viewModel,
                        initialType = addTransactionType,
                        prefillAmount = prefill?.amount?.let {
                            if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
                        } ?: "",
                        prefillCategory = prefill?.transactionName ?: "",
                        prefillAccount = prefill?.account,
                        prefillDateTime = prefill?.dateTime,
                        prefillNote = prefill?.referenceNumber?.let {
                            if (it.isNotBlank()) "Ref: $it" else ""
                        } ?: "",
                        onCancel = {
                            appScreen = AppScreen.MAIN
                            smsPreFill = null
                        },
                        onSaved = {
                            appScreen = AppScreen.MAIN
                            smsPreFill = null
                        }
                    )
                }

                AppScreen.EDIT_TRANSACTION -> {
                    val txnId = editTransactionId
                    if (txnId != null) {
                        var transaction by remember { mutableStateOf<com.moneyminder.app.data.entity.Transaction?>(null) }
                        LaunchedEffect(txnId) {
                            viewModel.getTransactionById(txnId) { transaction = it }
                        }
                        transaction?.let { txn ->
                            AddTransactionScreen(
                                viewModel = viewModel,
                                editTransaction = txn,
                                onCancel = { appScreen = AppScreen.MAIN },
                                onSaved = { appScreen = AppScreen.MAIN }
                            )
                        }
                    }
                }

                AppScreen.SETTINGS -> {
                    SettingsScreen(
                        viewModel = viewModel,
                        onBack = { appScreen = AppScreen.MAIN },
                        onResetMpin = { appScreen = AppScreen.RESET_MPIN }
                    )
                }

                AppScreen.RESET_MPIN -> {
                    ResetMpinScreen(
                        onBack = { appScreen = AppScreen.SETTINGS },
                        onReset = { appScreen = AppScreen.SETTINGS }
                    )
                }

                AppScreen.HELD_MONEY_DETAIL -> {
                    HeldMoneyDetailScreen(
                        viewModel = viewModel,
                        heldMoneyId = heldMoneyDetailId,
                        onBack = { appScreen = AppScreen.MAIN }
                    )
                }
            }
        }

        if (showTransactionDetail && transactionDetailId != null) {
            TransactionDetailSheet(
                viewModel = viewModel,
                transactionId = transactionDetailId!!,
                onDismiss = { showTransactionDetail = false },
                onEdit = { id ->
                    showTransactionDetail = false
                    editTransactionId = id
                    appScreen = AppScreen.EDIT_TRANSACTION
                },
                onDuplicate = { txn ->
                    viewModel.duplicateTransaction(txn)
                    showTransactionDetail = false
                },
                onDelete = { txn ->
                    viewModel.deleteTransaction(txn)
                    showTransactionDetail = false
                }
            )
        }
    }
}

@Composable
fun TransactionDetailSheet(
    viewModel: MoneyMinderViewModel,
    transactionId: Long,
    onDismiss: () -> Unit,
    onEdit: (Long) -> Unit,
    onDuplicate: (com.moneyminder.app.data.entity.Transaction) -> Unit,
    onDelete: (com.moneyminder.app.data.entity.Transaction) -> Unit
) {
    var transaction by remember { mutableStateOf<com.moneyminder.app.data.entity.Transaction?>(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    LaunchedEffect(transactionId) {
        viewModel.getTransactionById(transactionId) { transaction = it }
    }

    val txn = transaction ?: return

    androidx.compose.ui.window.Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = androidx.compose.foundation.shape.RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = com.moneyminder.app.ui.theme.Charcoal)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Transaction Detail",
                        color = com.moneyminder.app.ui.theme.TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                    )
                    androidx.compose.material3.IconButton(onClick = onDismiss) {
                        androidx.compose.material3.Icon(
                            androidx.compose.material.icons.Icons.Filled.Close,
                            "Close",
                            tint = com.moneyminder.app.ui.theme.TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val typeColor = when (txn.type) {
                    TransactionType.INCOME -> com.moneyminder.app.ui.theme.IncomeGreen
                    TransactionType.EXPENSE -> com.moneyminder.app.ui.theme.ExpenseRed
                    TransactionType.TRANSFER -> com.moneyminder.app.ui.theme.TransferBlue
                }

                Text(txn.type.name, color = typeColor, fontSize = 13.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    when (txn.type) {
                        TransactionType.INCOME -> "+ ${com.moneyminder.app.util.CurrencyUtils.formatAmount(txn.amount)}"
                        TransactionType.EXPENSE -> "− ${com.moneyminder.app.util.CurrencyUtils.formatAmount(txn.amount)}"
                        TransactionType.TRANSFER -> com.moneyminder.app.util.CurrencyUtils.formatAmount(txn.amount)
                    },
                    color = typeColor,
                    fontSize = 28.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (txn.category.isNotBlank()) {
                    DetailRow("Category", txn.category)
                }
                DetailRow("Date & Time", com.moneyminder.app.util.DateUtils.formatDateTime(txn.dateTime))

                when (txn.type) {
                    TransactionType.EXPENSE -> {
                        DetailRow("Paid From", txn.fromAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
                    }
                    TransactionType.INCOME -> {
                        DetailRow("Received In", txn.toAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
                    }
                    TransactionType.TRANSFER -> {
                        DetailRow("Transfer From", txn.fromAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
                        DetailRow("Transfer To", txn.toAccount?.name?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "")
                        Text(
                            "${txn.fromAccount?.name ?: ""} → ${txn.toAccount?.name ?: ""}",
                            color = com.moneyminder.app.ui.theme.TransferBlue,
                            fontSize = 14.sp,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                if (txn.note.isNotBlank()) {
                    DetailRow("Note", txn.note)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { onEdit(txn.id) },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, com.moneyminder.app.ui.theme.CardBorder)
                    ) {
                        Text("Edit", color = com.moneyminder.app.ui.theme.TextPrimary)
                    }
                    OutlinedButton(
                        onClick = { onDuplicate(txn) },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, com.moneyminder.app.ui.theme.CardBorder)
                    ) {
                        Text("Duplicate", color = com.moneyminder.app.ui.theme.TextPrimary)
                    }
                    OutlinedButton(
                        onClick = { showDeleteConfirm = true },
                        modifier = Modifier.weight(1f),
                        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, com.moneyminder.app.ui.theme.ExpenseRed.copy(alpha = 0.5f))
                    ) {
                        Text("Delete", color = com.moneyminder.app.ui.theme.ExpenseRed)
                    }
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = com.moneyminder.app.ui.theme.Charcoal,
            title = { Text("Delete Transaction?", color = com.moneyminder.app.ui.theme.TextPrimary) },
            text = { Text("This action cannot be undone. Account balances will be recalculated.", color = com.moneyminder.app.ui.theme.TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    onDelete(txn)
                }) {
                    Text("Delete", color = com.moneyminder.app.ui.theme.ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = com.moneyminder.app.ui.theme.TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = com.moneyminder.app.ui.theme.TextTertiary, fontSize = 13.sp)
        Text(
            value,
            color = com.moneyminder.app.ui.theme.TextPrimary,
            fontSize = 13.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
        )
    }
}
