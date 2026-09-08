package com.moneyminder.app.ui.screens.heldmoney

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.data.entity.HeldMoney
import com.moneyminder.app.data.entity.HeldMoneyEntry
import com.moneyminder.app.data.entity.HeldMoneyEntryType
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.viewmodel.MoneyMinderViewModel

@Composable
fun HeldMoneySection(
    viewModel: MoneyMinderViewModel,
    onPersonClick: (Long) -> Unit
) {
    val heldMoneyList by viewModel.allHeldMoney.collectAsState()
    var showAddPerson by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf<HeldMoney?>(null) }

    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Held Money",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
            TextButton(
                onClick = { showAddPerson = true },
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
                Icon(Icons.Filled.PersonAdd, null, tint = AccentGold, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Person", color = AccentGold, fontSize = 12.sp)
            }
        }

        if (heldMoneyList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = DarkCard),
                border = BorderStroke(1.dp, CardBorder)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.People, null, tint = TextTertiary, modifier = Modifier.size(32.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No held money", color = TextTertiary, fontSize = 13.sp)
                        Text("Track money friends give you to hold", color = TextTertiary, fontSize = 11.sp)
                    }
                }
            }
        } else {
            heldMoneyList.forEach { hm ->
                HeldMoneyPersonCard(
                    viewModel = viewModel,
                    heldMoney = hm,
                    onClick = { onPersonClick(hm.id) },
                    onLongClick = { showDeleteConfirm = hm }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }

    if (showAddPerson) {
        AddPersonDialog(
            onDismiss = { showAddPerson = false },
            onAdd = { name, amount ->
                viewModel.addHeldMoney(name, amount)
                showAddPerson = false
            }
        )
    }

    showDeleteConfirm?.let { hm ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            containerColor = Charcoal,
            title = { Text("Delete ${hm.personName}?", color = TextPrimary) },
            text = { Text("This will remove all entries for ${hm.personName}. This cannot be undone.", color = TextSecondary) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteHeldMoney(hm)
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

@Composable
private fun HeldMoneyPersonCard(
    viewModel: MoneyMinderViewModel,
    heldMoney: HeldMoney,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val entries by viewModel.getHeldMoneyEntries(heldMoney.id).collectAsState(emptyList())
    val totalSpent = entries.filter { it.type == HeldMoneyEntryType.SPENT }.sumOf { it.amount }
    val totalReturned = entries.filter { it.type == HeldMoneyEntryType.RETURNED }.sumOf { it.amount }
    val remaining = heldMoney.totalAmount - totalSpent - totalReturned
    val usedFraction = if (heldMoney.totalAmount > 0) ((totalSpent + totalReturned) / heldMoney.totalAmount).coerceIn(0.0, 1.0) else 0.0

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(onClick = onClick, onLongClick = onLongClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkCard),
        border = BorderStroke(1.dp, CardBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(AccentGold.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        heldMoney.personName.take(1).uppercase(),
                        color = AccentGold,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(heldMoney.personName, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                    Text("Held: ${CurrencyUtils.formatAmount(heldMoney.totalAmount)}", color = TextTertiary, fontSize = 12.sp)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        CurrencyUtils.formatAmount(remaining),
                        color = if (remaining > 0) AccentGold else if (remaining == 0.0) IncomeGreen else ExpenseRed,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (remaining > 0) "remaining" else if (remaining == 0.0) "settled" else "overspent",
                        color = TextTertiary,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { usedFraction.toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = if (remaining >= 0) AccentGold else ExpenseRed,
                trackColor = CardBorder
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Spent: ${CurrencyUtils.formatAmount(totalSpent)}", color = ExpenseRed, fontSize = 11.sp)
                Text("Returned: ${CurrencyUtils.formatAmount(totalReturned)}", color = TransferBlue, fontSize = 11.sp)
            }
        }
    }
}

@Composable
private fun AddPersonDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Charcoal,
        title = { Text("Add Held Money", color = TextPrimary) },
        text = {
            Column {
                Text("Track money someone gave you to hold", color = TextSecondary, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Person's Name") },
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
                    value = amount,
                    onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
                    label = { Text("Amount Given") },
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
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(error, color = ExpenseRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val trimName = name.trim()
                val amt = amount.toDoubleOrNull()
                when {
                    trimName.isBlank() -> error = "Enter a name"
                    amt == null || amt <= 0 -> error = "Enter a valid amount"
                    else -> onAdd(trimName, amt)
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
