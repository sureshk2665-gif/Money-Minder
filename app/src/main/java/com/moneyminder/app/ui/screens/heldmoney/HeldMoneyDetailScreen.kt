package com.moneyminder.app.ui.screens.heldmoney

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.data.entity.HeldMoney
import com.moneyminder.app.data.entity.HeldMoneyEntry
import com.moneyminder.app.data.entity.HeldMoneyEntryType
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.util.DateUtils
import com.moneyminder.app.viewmodel.MoneyMinderViewModel

@Composable
fun HeldMoneyDetailScreen(
    viewModel: MoneyMinderViewModel,
    heldMoneyId: Long,
    onBack: () -> Unit
) {
    var heldMoney by remember { mutableStateOf<HeldMoney?>(null) }
    val entries by viewModel.getHeldMoneyEntries(heldMoneyId).collectAsState(emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<HeldMoneyEntry?>(null) }

    LaunchedEffect(heldMoneyId) {
        viewModel.getHeldMoneyById(heldMoneyId) { heldMoney = it }
    }

    val person = heldMoney ?: return

    val totalSpent = entries.filter { it.type == HeldMoneyEntryType.SPENT }.sumOf { it.amount }
    val totalReturned = entries.filter { it.type == HeldMoneyEntryType.RETURNED }.sumOf { it.amount }
    val remaining = person.totalAmount - totalSpent - totalReturned

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Text(person.personName, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, "Add Entry", tint = AccentGold)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Held", color = TextSecondary, fontSize = 13.sp)
                    Text(CurrencyUtils.formatAmount(person.totalAmount), color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Spent", color = ExpenseRed, fontSize = 13.sp)
                    Text(CurrencyUtils.formatAmount(totalSpent), color = ExpenseRed, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Returned", color = TransferBlue, fontSize = 13.sp)
                    Text(CurrencyUtils.formatAmount(totalReturned), color = TransferBlue, fontSize = 14.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = CardBorder)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Remaining", color = AccentGold, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                    Text(
                        CurrencyUtils.formatAmount(remaining),
                        color = if (remaining >= 0) AccentGold else ExpenseRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "Transaction Log",
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Filled.ReceiptLong, null, tint = TextTertiary, modifier = Modifier.size(40.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("No entries yet", color = TextTertiary, fontSize = 14.sp)
                    Text("Tap + to add a spent or return entry", color = TextTertiary, fontSize = 12.sp)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    EntryCard(entry) { showDeleteConfirm = entry }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddEntryDialog(
            remaining = remaining,
            onDismiss = { showAddDialog = false },
            onAdd = { type, amount, purpose ->
                viewModel.addHeldMoneyEntry(heldMoneyId, type, amount, purpose)
                showAddDialog = false
            }
        )
    }

    showDeleteConfirm?.let { entry ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor = Charcoal,
            title = { Text("Delete Entry?", color = TextPrimary) },
            text = { Text("Remove this ${entry.type.name.lowercase()} entry of ${CurrencyUtils.formatAmount(entry.amount)}?", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHeldMoneyEntry(entry)
                    showDeleteConfirm = null
                }) {
                    Text("Delete", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EntryCard(entry: HeldMoneyEntry, onLongPress: () -> Unit) {
    val isSpent = entry.type == HeldMoneyEntryType.SPENT
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onLongClick = onLongPress, onClick = {}),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Charcoal),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isSpent) ExpenseRedBg else TransferBlueDark.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (isSpent) Icons.Filled.ShoppingCart else Icons.Filled.Undo,
                    null,
                    tint = if (isSpent) ExpenseRed else TransferBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    if (isSpent) "Spent" else "Returned",
                    color = if (isSpent) ExpenseRed else TransferBlue,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (entry.purpose.isNotBlank()) {
                    Text(entry.purpose, color = TextSecondary, fontSize = 12.sp, maxLines = 1)
                }
                Text(DateUtils.formatDateTime(entry.dateTime), color = TextTertiary, fontSize = 11.sp)
            }
            Text(
                "${if (isSpent) "−" else "←"} ${CurrencyUtils.formatAmount(entry.amount)}",
                color = if (isSpent) ExpenseRed else TransferBlue,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEntryDialog(
    remaining: Double,
    onDismiss: () -> Unit,
    onAdd: (HeldMoneyEntryType, Double, String) -> Unit
) {
    var selectedType by remember { mutableStateOf(HeldMoneyEntryType.SPENT) }
    var amount by remember { mutableStateOf("") }
    var purpose by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        title = { Text("Add Entry", color = TextPrimary) },
        text = {
            Column {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == HeldMoneyEntryType.SPENT,
                        onClick = { selectedType = HeldMoneyEntryType.SPENT },
                        label = { Text("Spent") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = ExpenseRed.copy(alpha = 0.2f),
                            selectedLabelColor = ExpenseRed
                        )
                    )
                    FilterChip(
                        selected = selectedType == HeldMoneyEntryType.RETURNED,
                        onClick = { selectedType = HeldMoneyEntryType.RETURNED },
                        label = { Text("Returned") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = TransferBlue.copy(alpha = 0.2f),
                            selectedLabelColor = TransferBlue
                        )
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = AccentGold,
                        unfocusedLabelColor = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = purpose,
                    onValueChange = { purpose = it },
                    label = { Text(if (selectedType == HeldMoneyEntryType.SPENT) "Purpose" else "Note (optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = AccentGold,
                        unfocusedBorderColor = CardBorder,
                        focusedLabelColor = AccentGold,
                        unfocusedLabelColor = TextSecondary
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Remaining: ${CurrencyUtils.formatAmount(remaining)}", color = TextTertiary, fontSize = 12.sp)
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(error, color = ExpenseRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val amt = amount.toDoubleOrNull()
                if (amt == null || amt <= 0) {
                    error = "Enter a valid amount"
                } else if (amt > remaining && remaining > 0) {
                    error = "Amount exceeds remaining balance"
                } else {
                    onAdd(selectedType, amt, purpose.trim())
                }
            }) {
                Text("Add", color = AccentGold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
