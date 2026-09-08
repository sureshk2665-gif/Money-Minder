package com.moneyminder.app.ui.screens.mpin

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
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
import kotlinx.coroutines.delay

private val securityQuestions = listOf(
    "What is your pet's name?",
    "What is your mother's maiden name?",
    "What city were you born in?",
    "What is your favorite food?",
    "What was your first school's name?",
    "What is your best friend's name?"
)

@Composable
fun MpinSetupScreen(onComplete: () -> Unit) {
    val context = LocalContext.current
    var step by remember { mutableStateOf(0) }
    var pin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var selectedQuestion by remember { mutableStateOf(securityQuestions[0]) }
    var securityAnswer by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var showQuestionDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.money_minder_logo),
            contentDescription = "Money Minder",
            modifier = Modifier.height(40.dp),
            contentScale = ContentScale.FillHeight
        )

        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            Icons.Filled.Lock,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            when (step) {
                0 -> "Create MPIN"
                1 -> "Confirm MPIN"
                else -> "Security Question"
            },
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            when (step) {
                0 -> "Set a 4-digit PIN to secure your app"
                1 -> "Re-enter your PIN to confirm"
                else -> "This helps recover your PIN if forgotten"
            },
            color = TextSecondary,
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 32.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        when (step) {
            0 -> {
                PinDots(pin.length)
                Spacer(modifier = Modifier.height(16.dp))
                if (error.isNotEmpty()) {
                    Text(error, color = ExpenseRed, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                PinKeypad(
                    onDigit = { if (pin.length < 4) pin += it },
                    onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                    onDone = {
                        if (pin.length == 4) {
                            error = ""
                            step = 1
                        }
                    },
                    showDone = pin.length == 4
                )
            }
            1 -> {
                PinDots(confirmPin.length)
                Spacer(modifier = Modifier.height(16.dp))
                if (error.isNotEmpty()) {
                    Text(error, color = ExpenseRed, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                }
                Spacer(modifier = Modifier.weight(1f))
                PinKeypad(
                    onDigit = { if (confirmPin.length < 4) confirmPin += it },
                    onDelete = { if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1) },
                    onDone = {
                        if (confirmPin == pin) {
                            error = ""
                            step = 2
                        } else {
                            error = "PINs don't match. Try again."
                            confirmPin = ""
                        }
                    },
                    showDone = confirmPin.length == 4
                )
            }
            2 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Box {
                        OutlinedButton(
                            onClick = { showQuestionDropdown = true },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, CardBorder),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = DarkCard)
                        ) {
                            Text(
                                selectedQuestion,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Filled.ArrowDropDown, null, tint = TextSecondary)
                        }
                        DropdownMenu(
                            expanded = showQuestionDropdown,
                            onDismissRequest = { showQuestionDropdown = false },
                            modifier = Modifier.background(Charcoal)
                        ) {
                            securityQuestions.forEach { question ->
                                DropdownMenuItem(
                                    text = { Text(question, color = TextPrimary, fontSize = 14.sp) },
                                    onClick = {
                                        selectedQuestion = question
                                        showQuestionDropdown = false
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = securityAnswer,
                        onValueChange = { securityAnswer = it },
                        placeholder = { Text("Your answer", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = CardBorder,
                            cursorColor = TextPrimary,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = ExpenseRed, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (securityAnswer.trim().isEmpty()) {
                                error = "Please enter an answer"
                            } else {
                                MpinManager.setupMpin(context, pin, selectedQuestion, securityAnswer)
                                onComplete()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold)
                    ) {
                        Icon(Icons.Filled.Check, null, tint = Black)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Set MPIN", color = Black, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun MpinLockScreen(
    onUnlocked: () -> Unit,
    onForgotPin: () -> Unit
) {
    val context = LocalContext.current
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var isLockedOut by remember { mutableStateOf(MpinManager.isLockedOut(context)) }
    var remainingSeconds by remember { mutableStateOf(MpinManager.getRemainingLockoutSeconds(context)) }

    LaunchedEffect(isLockedOut) {
        while (isLockedOut) {
            remainingSeconds = MpinManager.getRemainingLockoutSeconds(context)
            if (remainingSeconds <= 0) {
                isLockedOut = false
                MpinManager.clearLockout(context)
                error = ""
            }
            delay(1000)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Black)
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Image(
            painter = painterResource(id = R.drawable.money_minder_logo),
            contentDescription = "Money Minder",
            modifier = Modifier.height(40.dp),
            contentScale = ContentScale.FillHeight
        )

        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            if (isLockedOut) Icons.Filled.LockClock else Icons.Filled.Lock,
            contentDescription = null,
            tint = if (isLockedOut) ExpenseRed else AccentGold,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            if (isLockedOut) "App Locked" else "Enter MPIN",
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLockedOut) {
            Text(
                "Too many failed attempts. Try again in ${remainingSeconds}s",
                color = ExpenseRed,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        } else {
            Text(
                "Enter your 4-digit PIN",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        PinDots(pin.length)

        Spacer(modifier = Modifier.height(16.dp))

        if (error.isNotEmpty() && !isLockedOut) {
            Text(error, color = ExpenseRed, fontSize = 13.sp)
            Spacer(modifier = Modifier.height(4.dp))
            val attempts = MpinManager.getFailedAttempts(context)
            if (attempts >= 3) {
                Text(
                    "${5 - attempts} attempts remaining",
                    color = TextTertiary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        if (!isLockedOut) {
            PinKeypad(
                onDigit = { if (pin.length < 4) pin += it },
                onDelete = { if (pin.isNotEmpty()) pin = pin.dropLast(1) },
                onDone = {
                    if (MpinManager.verifyMpin(context, pin)) {
                        onUnlocked()
                    } else {
                        if (MpinManager.isLockedOut(context)) {
                            isLockedOut = true
                            remainingSeconds = MpinManager.getRemainingLockoutSeconds(context)
                        }
                        error = "Incorrect PIN"
                        pin = ""
                    }
                },
                showDone = pin.length == 4
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        TextButton(onClick = onForgotPin) {
            Text("Forgot MPIN?", color = AccentGold, fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ForgotMpinScreen(
    onReset: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val question = MpinManager.getSecurityQuestion(context)
    var answer by remember { mutableStateOf("") }
    var step by remember { mutableStateOf(0) }
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
        Spacer(modifier = Modifier.height(40.dp))

        Image(
            painter = painterResource(id = R.drawable.money_minder_logo),
            contentDescription = "Money Minder",
            modifier = Modifier.height(40.dp),
            contentScale = ContentScale.FillHeight
        )

        Spacer(modifier = Modifier.height(32.dp))

        Icon(
            Icons.Filled.HelpOutline,
            contentDescription = null,
            tint = AccentGold,
            modifier = Modifier.size(48.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            when (step) {
                0 -> "Security Question"
                1 -> "New MPIN"
                else -> "Confirm New MPIN"
            },
            color = TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        when (step) {
            0 -> {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkCard),
                        border = BorderStroke(1.dp, CardBorder)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Question:", color = TextTertiary, fontSize = 12.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(question, color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = answer,
                        onValueChange = { answer = it },
                        placeholder = { Text("Your answer", color = TextTertiary) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = AccentGold,
                            unfocusedBorderColor = CardBorder,
                            cursorColor = TextPrimary,
                            focusedContainerColor = DarkCard,
                            unfocusedContainerColor = DarkCard
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    if (error.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(error, color = ExpenseRed, fontSize = 13.sp)
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (MpinManager.verifySecurityAnswer(context, answer)) {
                                error = ""
                                step = 1
                            } else {
                                error = "Incorrect answer. Please try again."
                            }
                        },
                        enabled = answer.trim().isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AccentGold,
                            disabledContainerColor = DarkCard
                        )
                    ) {
                        Text("Verify", color = Black, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(
                        onClick = onBack,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Back to MPIN", color = TextTertiary)
                    }
                }
            }
            1 -> {
                PinDots(newPin.length)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Enter new 4-digit PIN", color = TextSecondary, fontSize = 14.sp)
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = ExpenseRed, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                PinKeypad(
                    onDigit = { if (newPin.length < 4) newPin += it },
                    onDelete = { if (newPin.isNotEmpty()) newPin = newPin.dropLast(1) },
                    onDone = {
                        if (newPin.length == 4) {
                            error = ""
                            step = 2
                        }
                    },
                    showDone = newPin.length == 4
                )
            }
            2 -> {
                PinDots(confirmPin.length)
                Spacer(modifier = Modifier.height(16.dp))
                Text("Confirm new PIN", color = TextSecondary, fontSize = 14.sp)
                if (error.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(error, color = ExpenseRed, fontSize = 13.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                PinKeypad(
                    onDigit = { if (confirmPin.length < 4) confirmPin += it },
                    onDelete = { if (confirmPin.isNotEmpty()) confirmPin = confirmPin.dropLast(1) },
                    onDone = {
                        if (confirmPin == newPin) {
                            MpinManager.resetMpin(context, newPin)
                            MpinManager.clearLockout(context)
                            onReset()
                        } else {
                            error = "PINs don't match. Try again."
                            confirmPin = ""
                        }
                    },
                    showDone = confirmPin.length == 4
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun PinDots(filled: Int) {
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
private fun PinKeypad(
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
