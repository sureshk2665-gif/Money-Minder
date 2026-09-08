package com.moneyminder.app.ui.screens.sms

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.moneyminder.app.R
import com.moneyminder.app.data.entity.TransactionType
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.CurrencyUtils
import com.moneyminder.app.util.DateUtils
import com.moneyminder.app.util.ParsedSmsTransaction
import com.moneyminder.app.viewmodel.MoneyMinderViewModel

@Composable
fun SmsScreen(
    viewModel: MoneyMinderViewModel,
    onAddFromSms: (ParsedSmsTransaction) -> Unit
) {
    var smsText by remember { mutableStateOf("") }
    var parsedTransactions by remember { mutableStateOf<List<ParsedSmsTransaction>>(emptyList()) }
    var showReview by remember { mutableStateOf(false) }

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
            Text("Review SMS", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        if (!showReview) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.Sms, null, tint = TextTertiary, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Paste SMS Messages", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Paste one or more transaction SMS messages below to extract transaction details.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = smsText,
                    onValueChange = { smsText = it },
                    placeholder = { Text("Paste one or more transaction SMS messages here", color = TextTertiary) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedBorderColor = CardBorder,
                        unfocusedBorderColor = CardBorder,
                        cursorColor = TextPrimary,
                        focusedContainerColor = DarkCard,
                        unfocusedContainerColor = DarkCard
                    ),
                    shape = RoundedCornerShape(16.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        parsedTransactions = viewModel.parseSmsText(smsText)
                        showReview = true
                    },
                    enabled = smsText.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = TextPrimary,
                        disabledContainerColor = DarkCard
                    )
                ) {
                    Icon(Icons.Filled.Search, "Review", tint = Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Review", color = Black, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(onClick = { smsText = "" }) {
                    Text("Cancel", color = TextTertiary)
                }
            }
        } else {
            if (parsedTransactions.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Filled.Warning, null, tint = TextTertiary, modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No transactions detected", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                        Text("The pasted text doesn't contain recognizable transaction details.", color = TextSecondary, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { showReview = false },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = DarkCard)
                        ) {
                            Text("Try Again", color = TextPrimary)
                        }
                    }
                }
            } else {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Text(
                        "${parsedTransactions.size} transaction(s) detected",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 100.dp)
                ) {
                    itemsIndexed(parsedTransactions) { index, parsed ->
                        SmsTransactionCard(
                            parsed = parsed,
                            onAddClick = { onAddFromSms(parsed) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            showReview = false
                            smsText = ""
                            parsedTransactions = emptyList()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Text("Back", color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmsTransactionCard(
    parsed: ParsedSmsTransaction,
    onAddClick: () -> Unit
) {
    val accentColor = when (parsed.type) {
        TransactionType.INCOME -> IncomeGreen
        TransactionType.EXPENSE -> ExpenseRed
        TransactionType.TRANSFER -> TransferBlue
    }
    val bgColor = when (parsed.type) {
        TransactionType.INCOME -> IncomeGreenBg
        TransactionType.EXPENSE -> ExpenseRedBg
        TransactionType.TRANSFER -> DarkCard
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        border = BorderStroke(1.dp, accentColor.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        parsed.transactionName,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        parsed.type.name,
                        color = accentColor,
                        fontSize = 12.sp
                    )
                }
                Text(
                    when (parsed.type) {
                        TransactionType.INCOME -> "+ ${CurrencyUtils.formatAmount(parsed.amount)}"
                        TransactionType.EXPENSE -> "− ${CurrencyUtils.formatAmount(parsed.amount)}"
                        else -> CurrencyUtils.formatAmount(parsed.amount)
                    },
                    color = accentColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    "Account: ${parsed.account.name.lowercase().replaceFirstChar { it.uppercase() }}",
                    color = TextSecondary,
                    fontSize = 12.sp
                )
                if (parsed.dateTime != null) {
                    Text(
                        DateUtils.formatDateTime(parsed.dateTime),
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            if (parsed.balanceAfter != null) {
                Text(
                    "Balance after: ${CurrencyUtils.formatAmount(parsed.balanceAfter)}",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
            if (parsed.referenceNumber.isNotBlank()) {
                Text(
                    "Ref: ${parsed.referenceNumber}",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }
            if (parsed.isPending) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Pending verification",
                    color = Color(0xFFFFA726),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onAddClick,
                modifier = Modifier.fillMaxWidth().height(40.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accentColor.copy(alpha = 0.3f))
            ) {
                Icon(Icons.Filled.Add, "Add", tint = TextPrimary, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Transaction", color = TextPrimary, fontSize = 13.sp)
            }
        }
    }
}
