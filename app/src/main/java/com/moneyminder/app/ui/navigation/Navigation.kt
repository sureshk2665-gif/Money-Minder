package com.moneyminder.app.ui.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.moneyminder.app.ui.theme.*

enum class Screen {
    HOME, INSIGHTS, ADD, CALENDAR, SMS
}

data class NavItem(
    val screen: Screen,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
)

val navItems = listOf(
    NavItem(Screen.HOME, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavItem(Screen.INSIGHTS, "Insights", Icons.Filled.PieChart, Icons.Outlined.PieChart),
    NavItem(Screen.ADD, "Add", Icons.Filled.Add, Icons.Outlined.Add),
    NavItem(Screen.CALENDAR, "Calendar", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    NavItem(Screen.SMS, "SMS", Icons.Filled.Sms, Icons.Outlined.Sms),
)

@Composable
fun BottomNavBar(
    currentScreen: Screen,
    onScreenSelected: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentIndex = navItems.indexOfFirst { it.screen == currentScreen }
    val animatedIndex by animateFloatAsState(
        targetValue = currentIndex.toFloat(),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 300f),
        label = "navIndicator"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color.Transparent, NavBarBg.copy(alpha = 0.95f), NavBarBg)
                )
            )
            .padding(bottom = 8.dp, top = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navItems.forEachIndexed { index, item ->
                val isSelected = item.screen == currentScreen
                val isAdd = item.screen == Screen.ADD

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) { onScreenSelected(item.screen) },
                    contentAlignment = Alignment.Center
                ) {
                    if (isAdd) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isSelected) GlassHighlight
                                    else Charcoal
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add",
                                tint = TextPrimary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    } else {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(
                                        if (isSelected) GlassHighlight.copy(alpha = 0.8f)
                                        else Color.Transparent
                                    )
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                                    contentDescription = item.label,
                                    tint = if (isSelected) TextPrimary else TextSecondary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.label,
                                color = if (isSelected) TextPrimary else TextSecondary,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
