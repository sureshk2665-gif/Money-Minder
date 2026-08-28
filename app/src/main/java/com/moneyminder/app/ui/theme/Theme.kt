package com.moneyminder.app.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Black = Color(0xFF000000)
val Charcoal = Color(0xFF1A1A1A)
val DarkCard = Color(0xFF1E1E1E)
val CardBorder = Color(0xFF2A2A2A)
val GreyOutline = Color(0xFF333333)
val TextPrimary = Color(0xFFFFFFFF)
val TextSecondary = Color(0xFFB0B0B0)
val TextTertiary = Color(0xFF808080)
val IncomeGreen = Color(0xFF2ECC71)
val IncomeGreenDark = Color(0xFF1A5A32)
val IncomeGreenBg = Color(0xFF0D2E19)
val ExpenseRed = Color(0xFFE74C3C)
val ExpenseRedDark = Color(0xFF6B1E16)
val ExpenseRedBg = Color(0xFF2E0D0A)
val TransferBlue = Color(0xFF607D8B)
val TransferBlueDark = Color(0xFF37474F)
val AccentGold = Color(0xFFFFD700)
val SurfaceDark = Color(0xFF121212)
val NavBarBg = Color(0xFF0A0A0A)
val GlassHighlight = Color(0xFF2A2A2A)

private val DarkColorScheme = darkColorScheme(
    primary = TextPrimary,
    onPrimary = Black,
    secondary = TextSecondary,
    onSecondary = Black,
    tertiary = TransferBlue,
    background = Black,
    surface = DarkCard,
    surfaceVariant = Charcoal,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline = GreyOutline,
    outlineVariant = CardBorder
)

@Composable
fun MoneyMinderTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography(),
        content = content
    )
}
