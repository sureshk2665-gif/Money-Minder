package com.moneyminder.app.ui.screens.add

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.R
import com.moneyminder.app.data.entity.*
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.util.DateUtils
import com.moneyminder.app.viewmodel.MoneyMinderViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTransactionScreen(
    viewModel: MoneyMinderViewModel,
    initialType: TransactionType = TransactionType.EXPENSE,
    prefillAmount: String = "",
    prefillCategory: String = "",
    prefillAccount: AccountType? = null,
    prefillDateTime: Long? = null,
    prefillNote: String = "",
    editTransaction: Transaction? = null,
    onCancel: () -> Unit,
    onSaved: () -> Unit
) {
    var type by remember { mutableStateOf(editTransaction?.type ?: initialType) }
    var amount by remember { mutableStateOf(editTransaction?.amount?.let {
        if (it == it.toLong().toDouble()) it.toLong().toString() else it.toString()
    } ?: prefillAmount) }
    var category by remember { mutableStateOf(editTransaction?.category ?: prefillCategory) }
    var note by remember { mutableStateOf(editTransaction?.note ?: prefillNote) }
    var fromAccount by remember { mutableStateOf(editTransaction?.fromAccount ?: if (initialType == TransactionType.EXPENSE) (prefillAccount ?: AccountType.BANK) else AccountType.BANK) }
    var toAccount by remember { mutableStateOf(editTransaction?.toAccount ?: if (initialType == TransactionType.INCOME) (prefillAccount ?: AccountType.BANK) else AccountType.WALLET) }
    var selectedDateTime by remember { mutableLongStateOf(editTransaction?.dateTime ?: prefillDateTime ?: System.currentTimeMillis()) }
    var showCategoryDropdown by remember { mutableStateOf(false) }
    var showSaveAnimation by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf("") }

    val expenseCategories by viewModel.expenseCategories.collectAsState()
    val incomeCategories by viewModel.incomeCategories.collectAsState()
    val bankBalance by viewModel.bankBalance.collectAsState()
    val walletBalance by viewModel.walletBalance.collectAsState()
    val cashBalance by viewModel.cashBalance.collectAsState()

    val context = LocalContext.current

    val categories = when (type) {
        TransactionType.EXPENSE -> expenseCategories
        TransactionType.INCOME -> incomeCategories
        TransactionType.TRANSFER -> emptyList()
    }

    fun getAccountBalance(account: AccountType): Double = when (account) {
        AccountType.BANK -> bankBalance
        AccountType.WALLET -> walletBalance
        AccountType.CASH -> cashBalance
    }

    fun save(addAnother: Boolean) {
        val amt = amount.toDoubleOrNull()
        if (amt == null || amt <= 0) {
            showError = "Please enter a valid amount"
            return
        }
        if (type != TransactionType.TRANSFER && category.isBlank()) {
            showError = "Please enter a category"
            return
        }
        if (type == TransactionType.TRANSFER && fromAccount == toAccount) {
            showError = "Source and destination cannot be the same"
            return
        }

        val fa = when (type) {
            TransactionType.EXPENSE -> fromAccount
            TransactionType.TRANSFER -> fromAccount
            TransactionType.INCOME -> null
        }
        val ta = when (type) {
            TransactionType.INCOME -> toAccount
            TransactionType.TRANSFER -> toAccount
            TransactionType.EXPENSE -> null
        }

        if (editTransaction != null) {
            viewModel.updateTransaction(
                editTransaction.copy(
                    type = type,
                    amount = amt,
                    category = if (type == TransactionType.TRANSFER) "" else category,
                    note = note,
                    dateTime = selectedDateTime,
                    fromAccount = fa,
                    toAccount = ta
                )
            )
        } else {
            viewModel.addTransaction(
                type = type,
                amount = amt,
                category = if (type == TransactionType.TRANSFER) "" else category,
                note = note,
                dateTime = selectedDateTime,
                fromAccount = fa,
                toAccount = ta
            )
        }

        showSaveAnimation = true
        if (addAnother) {
            amount = ""
            category = ""
            note = ""
            showSaveAnimation = false
        } else {
            onSaved()
        }
    }

    if (showSaveAnimation) {
        LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(500)
            showSaveAnimation = false
        }
    }

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
            Text(
                if (editTransaction != null) "Edit Transaction" else "Add Transaction",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onCancel) {
                Icon(Icons.Filled.Close, "Cancel", tint = TextSecondary)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(DarkCard)
                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
        ) {
            listOf(
                Triple(TransactionType.EXPENSE, "Expense", ExpenseRed),
                Triple(TransactionType.INCOME, "Income", IncomeGreen),
                Triple(TransactionType.TRANSFER, "Transfer", TransferBlue)
            ).forEach { (t, label, color) ->
                val selected = type == t
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (selected) color.copy(alpha = 0.7f) else Color.Transparent)
                        .clickable { type = t }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        label,
                        color = TextPrimary,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        fontSize = 14.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("₹", color = TextPrimary, fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                placeholder = { Text("Amount", color = TextTertiary) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = CardBorder,
                    unfocusedBorderColor = CardBorder,
                    cursorColor = TextPrimary,
                    focusedContainerColor = DarkCard,
                    unfocusedContainerColor = DarkCard
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                textStyle = LocalTextStyle.current.copy(fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (type != TransactionType.TRANSFER) {
            Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Category", color = TextTertiary) },
                    trailingIcon = {
                        if (categories.isNotEmpty()) {
                            IconButton(onClick = { showCategoryDropdown = !showCategoryDropdown }) {
                                Icon(Icons.Filled.ArrowDropDown, "Categories", tint = TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CardBorder,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = TextPrimary,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                DropdownMenu(
                    expanded = showCategoryDropdown,
                    onDismissRequest = { showCategoryDropdown = false },
                    modifier = Modifier.background(Charcoal)
                ) {
                    categories.forEach { cat ->
                        DropdownMenuItem(
                            text = { Text(cat.name, color = TextPrimary) },
                            onClick = {
                                category = cat.name
                                showCategoryDropdown = false
                            }
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        when (type) {
            TransactionType.EXPENSE -> {
                AccountSelector(
                    label = "Paid From",
                    selected = fromAccount,
                    onSelect = { fromAccount = it },
                    showBalance = true,
                    getBalance = { getAccountBalance(it) }
                )
            }
            TransactionType.INCOME -> {
                AccountSelector(
                    label = "Received In",
                    selected = toAccount,
                    onSelect = { toAccount = it },
                    showBalance = false,
                    getBalance = { getAccountBalance(it) }
                )
            }
            TransactionType.TRANSFER -> {
                AccountSelector(
                    label = "Transfer From",
                    selected = fromAccount,
                    onSelect = { fromAccount = it },
                    showBalance = true,
                    getBalance = { getAccountBalance(it) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.ArrowDownward, "Transfer direction", tint = TransferBlue, modifier = Modifier.size(28.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                AccountSelector(
                    label = "Transfer To",
                    selected = toAccount,
                    onSelect = { toAccount = it },
                    showBalance = false,
                    getBalance = { getAccountBalance(it) }
                )
                if (fromAccount == toAccount) {
                    Text(
                        "Source and destination cannot be the same",
                        color = ExpenseRed,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = DateUtils.formatDateTime(selectedDateTime),
            onValueChange = {},
            label = { Text("Date & Time", color = TextTertiary) },
            readOnly = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .clickable {
                    val cal = Calendar.getInstance().apply { timeInMillis = selectedDateTime }
                    DatePickerDialog(
                        context,
                        android.R.style.Theme_DeviceDefault_Dialog,
                        { _, y, m, d ->
                            TimePickerDialog(
                                context,
                                android.R.style.Theme_DeviceDefault_Dialog,
                                { _, h, min ->
                                    val newCal = Calendar.getInstance()
                                    newCal.set(y, m, d, h, min, 0)
                                    selectedDateTime = newCal.timeInMillis
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        },
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    ).show()
                },
            trailingIcon = {
                Icon(Icons.Filled.CalendarMonth, "Select date", tint = TextSecondary)
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CardBorder,
                unfocusedBorderColor = CardBorder,
                disabledTextColor = TextPrimary,
                disabledBorderColor = CardBorder,
                disabledContainerColor = DarkCard,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            ),
            shape = RoundedCornerShape(12.dp),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("Note (optional)", color = TextTertiary) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedBorderColor = CardBorder,
                unfocusedBorderColor = CardBorder,
                cursorColor = TextPrimary,
                focusedContainerColor = DarkCard,
                unfocusedContainerColor = DarkCard
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 3
        )

        if (showError.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                showError,
                color = ExpenseRed,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { showError = ""; save(false) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = TextPrimary)
        ) {
            Text(
                if (editTransaction != null) "Update Transaction" else "Save Transaction",
                color = Black,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
        }

        if (editTransaction == null) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = { showError = ""; save(true) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .height(48.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Text("Save & Add Another", color = TextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(
            onClick = onCancel,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            Text("Cancel", color = TextTertiary)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AccountSelector(
    label: String,
    selected: AccountType,
    onSelect: (AccountType) -> Unit,
    showBalance: Boolean,
    getBalance: (AccountType) -> Double
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text(label, color = TextSecondary, fontSize = 13.sp, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(DarkCard)
                .border(1.dp, CardBorder, RoundedCornerShape(12.dp)),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AccountType.entries.forEach { account ->
                val isSelected = account == selected
                val icon = when (account) {
                    AccountType.BANK -> Icons.Filled.AccountBalance
                    AccountType.WALLET -> Icons.Filled.AccountBalanceWallet
                    AccountType.CASH -> Icons.Filled.Payments
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) GlassHighlight else Color.Transparent)
                        .clickable { onSelect(account) }
                        .padding(vertical = 12.dp, horizontal = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(icon, account.name, tint = if (isSelected) TextPrimary else TextTertiary, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        account.name.lowercase().replaceFirstChar { it.uppercase() },
                        color = if (isSelected) TextPrimary else TextTertiary,
                        fontSize = 12.sp,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                    if (showBalance && isSelected) {
                        Text(
                            CurrencyUtils.formatAmount(getBalance(account)),
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
