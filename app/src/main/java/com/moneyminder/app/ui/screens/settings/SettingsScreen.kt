package com.moneyminder.app.ui.screens.settings

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.R
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.viewmodel.MoneyMinderViewModel

@Composable
fun SettingsScreen(
    viewModel: MoneyMinderViewModel,
    onBack: () -> Unit,
    onResetMpin: () -> Unit = {}
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showDeleteFinal by remember { mutableStateOf(false) }

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
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.ArrowBack, "Back", tint = TextPrimary)
            }
            Image(
                painter = painterResource(id = R.drawable.money_minder_logo),
                contentDescription = "Money Minder",
                modifier = Modifier.height(32.dp),
                contentScale = ContentScale.FillHeight
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text("Settings", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(8.dp))

        SettingsSection("General") {
            SettingsItem(Icons.Filled.CurrencyRupee, "Currency", "₹ INR (Indian Rupee)")
            SettingsItem(Icons.Filled.CalendarMonth, "First Day of Month", "1")
        }

        SettingsSection("Accounts") {
            SettingsItem(Icons.Filled.AccountBalance, "Bank", "Active")
            SettingsItem(Icons.Filled.AccountBalanceWallet, "Wallet", "Active")
            SettingsItem(Icons.Filled.Payments, "Cash", "Active")
        }

        SettingsSection("Backup") {
            SettingsItem(Icons.Filled.Email, "Gmail Backup & Sync", "Not connected") {}
            SettingsItem(Icons.Filled.CloudUpload, "Back Up Now", "Manual backup") {}
            SettingsItem(Icons.Filled.Schedule, "Automatic Backup", "Disabled") {}
        }

        SettingsSection("Data") {
            SettingsItem(Icons.Filled.History, "Export History", "View past exports") {}
        }

        SettingsSection("Privacy & Security") {
            SettingsItem(Icons.Filled.Lock, "Reset MPIN", "Change your app PIN") {
                onResetMpin()
            }
            SettingsItem(Icons.Filled.PrivacyTip, "Privacy Policy", "Your data stays local")
            SettingsItem(Icons.Filled.DeleteForever, "Delete All Data", "Remove everything") {
                showDeleteConfirm = true
            }
        }

        SettingsSection("Coming Soon") {
            SettingsItem(Icons.Filled.Savings, "Budgets", "Set monthly budgets")
            SettingsItem(Icons.Filled.Repeat, "Recurring Transactions", "Automate entries")
            SettingsItem(Icons.Filled.Fingerprint, "Biometric Lock", "Secure your data")
            SettingsItem(Icons.Filled.BackupTable, "Backup & Restore", "Cloud backup")
            SettingsItem(Icons.Filled.CameraAlt, "Receipt Scanning", "Scan receipts")
            SettingsItem(Icons.Filled.AccountTree, "Additional Accounts", "Add more accounts")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "Money Minder v1.0.0",
            color = TextTertiary,
            fontSize = 12.sp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            fontWeight = FontWeight.Normal
        )

        Spacer(modifier = Modifier.height(100.dp))
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = Charcoal,
            title = { Text("Delete All Data?", color = TextPrimary) },
            text = {
                Text(
                    "This will permanently remove all local transactions, transfers, account balances, categories, and stored app data. This action cannot be undone.",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    showDeleteFinal = true
                }) {
                    Text("Continue", color = ExpenseRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showDeleteFinal) {
        AlertDialog(
            onDismissRequest = { showDeleteFinal = false },
            containerColor = Charcoal,
            title = { Text("Are you absolutely sure?", color = ExpenseRed) },
            text = {
                Text(
                    "All your financial data will be permanently deleted. There is no way to recover it.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteAllData()
                        showDeleteFinal = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("Delete Everything", color = TextPrimary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteFinal = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text(
            title.uppercase(),
            color = TextTertiary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 8.dp, start = 4.dp)
        )
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkCard),
            border = BorderStroke(1.dp, CardBorder)
        ) {
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                content()
            }
        }
    }
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            )
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = TextSecondary, modifier = Modifier.size(22.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = TextPrimary, fontSize = 15.sp)
            Text(subtitle, color = TextTertiary, fontSize = 12.sp)
        }
        if (onClick != null) {
            Icon(Icons.Filled.ChevronRight, null, tint = TextTertiary, modifier = Modifier.size(20.dp))
        }
    }
}
