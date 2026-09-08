package com.moneyminder.app.ui.screens.mpin

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.R
import com.moneyminder.app.ui.theme.*
import com.moneyminder.app.util.MpinManager

@Composable
fun ResetMpinScreen(
    onBack: () -> Unit,
    onReset: () -> Unit
) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var oldPin by remember { mutableStateOf("") }
    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
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
            Text("Reset MPIN", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(24.dp))

        Icon(
            Icons.Filled.LockReset,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            when (step) {
                0 -> "Enter Current MPIN"
                1 -> "Enter New MPIN"
                else -> "Confirm New MPIN"
            },
            color = TextPrimary,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            when (step) {
                0 -> "Verify your current PIN first"
                1 -> "Choose a new 4-digit PIN"
                else -> "Re-enter to confirm"
            },
            color = TextSecondary,
            fontSize = 14.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        val currentPin = when (step) {
            0 -> oldPin
            1 -> newPin
            else -> confirmPin
        }

        PinDotsReset(currentPin.length)

        Spacer(modifier = Modifier.height(16.dp))

        if (error.isNotEmpty()) {
            Text(error, color = ExpenseRed, fontSize = 13.sp, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.weight(1f))

        PinKeypadReset(
            onDigit = { digit ->
                when (step) {
                    0 -> if (oldPin.length < 4) oldPin += digit
                    1 -> if (newPin.length < 4) newPin += digit
                    2 -> if (confirmPin.length < 4) confirmPin += digit
                }
            },
            onDelete = {
                when (step) {
                    0 -> if (oldPin.isNotEmpty()) oldPin = oldPin.dropLast(1)
                    1 -> if (newPin.isNotEmpty()) newPin = newPin.dropLast(1)
                    2 -> if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1)
                }
            },
            onDone = {
                when (step) {
                    0 -> {
                        if (MpinManager.verifyMpin(context, oldPin)) {
                            error = ""
                            step = 1
                        } else {
                            error = "Incorrect current PIN"
                            oldPin = ""
                        }
                    }
                    1 -> {
                        if (newPin.length == 4) {
                            error = ""
                            step = 2
                        }
                    }
                    2 -> {
                        if (confirmPin == newPin) {
                            MpinManager.resetMpin(context, newPin)
                            onReset()
                        } else {
                            error = "PINs don't match. Try again."
                            confirmPin = ""
                        }
                    }
                }
            },
            showDone = currentPin.length == 4
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PinDotsReset(filled: Int) {
    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(
                        if (index < filled) AccentGold else CardBorder
                    )
                    .then(
                        if (index >= filled) Modifier.border(1.dp, TextTertiary, CircleShape)
                        else Modifier
                    )
            )
        }
    }
}

@Composable
private fun PinKeypadReset(
    onDigit: (String) -> Unit,
    onDelete: () -> Unit,
    onDone: () -> Unit,
    showDone: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 48.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "DEL")
        ).forEach { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                row.forEach { key ->
                    when (key) {
                        "" -> {
                            if (showDone) {
                                Box(
                                    modifier = Modifier
                                        .size(72.dp)
                                        .clip(CircleShape)
                                        .background(AccentGold.copy(alpha = 0.2f))
                                        .clickable(onClick = onDone),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Check, "Done", tint = AccentGold, modifier = Modifier.size(28.dp))
                                }
                            } else {
                                Spacer(modifier = Modifier.size(72.dp))
                            }
                        }
                        "DEL" -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .clickable(onClick = onDelete),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Filled.Backspace, "Delete", tint = TextSecondary, modifier = Modifier.size(24.dp))
                            }
                        }
                        else -> {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(DarkCard)
                                    .border(1.dp, CardBorder, CircleShape)
                                    .clickable { onDigit(key) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(key, color = TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
