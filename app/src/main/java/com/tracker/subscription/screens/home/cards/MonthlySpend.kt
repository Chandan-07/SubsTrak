package com.tracker.subscription.screens.home.cards

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.Utility.formatCurrency
import com.tracker.subscription.Utility.getGreeting
import com.tracker.subscription.data.DashboardData
import com.tracker.subscription.ui.theme.ThemeColors

@Composable
fun MonthlySpendCard(
    isLoggedIn: Boolean,
    isAppUserSignedIn: Boolean,
    guestPremiumOwned: Boolean,
    data: DashboardData,
    currency: String,
    amount: Double,
    navController: NavController,
    isDarkTheme: Boolean,
    showSyncChip: Boolean = false,
    pendingCount: Int = 0,
    onSyncSmsClick: (() -> Unit)? = null
) {
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    var isYearly  by remember {  mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 2.dp)

            ,
            elevation = CardDefaults.cardElevation(20.dp),
            shape = RoundedCornerShape(30.dp)

        ) {

            // 🔹 MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isDarkTheme) {
                                listOf(
                                    Color(0xFF181226),
                                    Color(0xFF1F158A)
                                )} else {
                                listOf(
                                    Color(0xFF1A237E),
                                    Color(0xFF4866F1))
                            }
                        )
                    )

                ,
                contentAlignment = Alignment.TopStart
            ) {

                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 15.dp, vertical = 10.dp)) {

                    val isPremium = data.user?.isPremium == true &&
                        (isAppUserSignedIn || guestPremiumOwned)

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            modifier = Modifier.weight(1f, fill = false),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (!showSyncChip || isPremium) {
                                Text(
                                    text = if (isPremium) {
                                        "${data.subscriptions.size} subscriptions"
                                    } else {
                                        "${data.subscriptions.size}/5 subscriptions"
                                    },
                                    color = colorResource(R.color.white),
                                    fontFamily = manropeMedium,
                                    fontSize = 15.sp
                                )
                            }


                        }

                        if (isPremium) {
                            PremiumTag()
                        } else {

                            if (data.subscriptions.size >= 4) {
                                val infiniteTransition = rememberInfiniteTransition(label = "")
                                val shimmer by infiniteTransition.animateFloat(
                                    initialValue = 0f,
                                    targetValue = 1f,
                                    animationSpec = infiniteRepeatable(
                                        animation = tween(1200, easing = LinearEasing),
                                        repeatMode = RepeatMode.Reverse
                                    ),
                                    label = ""
                                )

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            brush = Brush.linearGradient(
                                                colors = listOf(
                                                    Color(0xFF1A237E),
                                                    Color(0xFF3D5AFE),
                                                    Color(0xFF1A237E)
                                                ),
                                                start = Offset(0f, shimmer * 200f),
                                                end = Offset(200f, shimmer * 400f)
                                            )
                                        )
                                        .border(
                                            1.dp,
                                            Color.White.copy(alpha = 0.5f),
                                            RoundedCornerShape(20.dp)
                                        )
                                        .clickable { navController.navigate("premium") }
                                        .padding(horizontal = 12.dp, vertical = 2.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Upgrade",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontFamily = manropeExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("✨", fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }


                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(20.dp))
                            .border(1.dp, Color(0xFFB5AEFF).copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        ,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(
                            emoji = R.drawable.timer,
                            title = "Free Trial",
                            count = data.freeTrials.size,
                            isDarkTheme = isDarkTheme
                        ) { navController.navigate("view_all_free_trials") }
                        StatItem(
                            emoji = R.drawable.text,
                            title = "Active",
                            count = data.subscriptions.size,
                            isDarkTheme = isDarkTheme
                        ) { navController.navigate("view_all_subscriptions") }
                        StatItem(
                            emoji = R.drawable.notification_bell,
                            title = "Renewals",
                            count = data.upcomingRenewals.size,
                            isDarkTheme = isDarkTheme
                        ) { navController.navigate("view_all_renewals") }
                    }


                    val displayAmount by animateFloatAsState(
                        targetValue = if (isYearly) (amount * 12).toFloat() else amount.toFloat(),
                        animationSpec = tween(200),
                        label = ""
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(28.dp))
                            .padding(horizontal = 12.dp, vertical = 15.dp)
                    ) {
                        Column() {

                            // 🔝 Header Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {

                                Row  {

                                    Text(
                                        text = if (isYearly) "Yearly Spend" else "Monthly Spend",
                                        color = Color.White,
                                        fontFamily = manropeMedium,
                                        modifier = Modifier.width(100.dp),
                                        fontSize = 13.sp
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    // 🔘 Premium Toggle
                                    Row(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(40))
                                            .background(if (isDarkTheme)Color.Black else  Color(
                                                0xFF182191
                                            )
                                            )
                                            .padding(2.dp)

                                    ) {

                                        ToggleItem(
                                            text = "m",
                                            selected = !isYearly,
                                            isDarkTheme
                                        ) { isYearly = false }

                                        ToggleItem(
                                            text = "y",
                                            selected = isYearly,
                                            isDarkTheme
                                        ) { isYearly = true }
                                    }
                                }

                                if (showSyncChip && onSyncSmsClick != null) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .clickable {
                                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                                onSyncSmsClick()
                                            }
                                            .padding(horizontal = 8.dp, vertical = 3.dp)
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            if (pendingCount > 0) {
                                                Text(
                                                    text = if (pendingCount == 1) "$pendingCount subs\nfound" else "$pendingCount subs\nfound",
                                                    color = Color(0xFFF6F6A3),
                                                    fontSize = 11.sp,
                                                    fontFamily = manropeMedium,
                                                    lineHeight = 13.sp,
                                                    minLines = 2
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Box(
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(10.dp))
                                                        .background(Color(0xFFCDDC39))
                                                        .padding(horizontal = 6.dp, vertical = 0.5.dp)
                                                ) {
                                                    Text(
                                                        text = "View",
                                                        color = Color.Black,
                                                        fontSize = 10.sp,
                                                        fontFamily = manropeExtraBold
                                                    )
                                                }
                                            } else {
                                                Row(modifier = Modifier.clip(RoundedCornerShape(25.dp)).background(Color(
                                                    0xFF4646AD
                                                )
                                                ).padding(start = 10.dp, end = 1.dp)) {
                                                    Text(
                                                        text = "Sync SMS",
                                                        color = Color.White,
                                                        fontSize = 12.sp,
                                                        fontFamily = manropeExtraBold
                                                    )
                                                    Icon(painterResource(R.drawable.sync), contentDescription = null, tint = Color.Unspecified, modifier = Modifier.size(25.dp))
                                                }

                                            }
                                        }
                                    }
                                }
                            }
                            // 💰 Animated Amount
                            Row ( modifier = Modifier.padding(top = 10.dp)) {
                                val displayRounded = displayAmount.toInt()
                                Text(
                                    text = formatCurrency(displayRounded.toDouble(), data.currency),
                                    fontSize = 25.sp,
                                    fontFamily = manropeExtraBold,
                                    color = Color(0xFFFAF6F5)
                                )

                                if (!isYearly) {
                                    data.monthlySpendChangePercent?.let { changePercent ->
                                        val arrow = when {
                                            changePercent > 0 -> "↑"
                                            changePercent < 0 -> "↓"
                                            else -> "→"
                                        }
                                        val percentColor = when {
                                            changePercent > 0 -> Color(0xFFADFF2F)
                                            changePercent < 0 -> Color(0xFFEF5350)
                                            else -> Color(0xFFADFF2F)
                                        }

                                        Spacer(modifier = Modifier.width(6.dp))

                                        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.padding(top = 10.dp)) {
                                            Text(
                                                text = "${kotlin.math.abs(changePercent).toInt()}% $arrow",
                                                fontSize = 10.sp,
                                                fontFamily = manropeMedium,
                                                color = percentColor
                                            )
                                            Text(
                                                text = " vs last month",
                                                fontSize = 10.sp,
                                                fontFamily = manropeMedium,
                                                color = ThemeColors.getTextGreyColor(isDarkTheme)
                                            )
                                        }
                                    }
                                }
                            }

                        }





                    }

                }

            }

        }


    }


}

@Composable
fun PremiumTag() {
    val manropeExtraBold = FontFamily(Font(R.font.manrope_extra_bold))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFFE082),
                        Color(0xFFFFC400)
                    )
                )
            )
            .border(1.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("👑", fontSize = 12.sp)
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Premium",
                color = Color(0xFF1A237E),
                fontSize = 12.sp,
                fontFamily = manropeExtraBold
            )
        }
    }
}

@Composable
fun FreeTag(isDarkTheme: Boolean) {
    val manropeExtraBold = FontFamily(Font(R.font.manrope_extra_bold))

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF70D067),
                        Color(0xFF70D067)
                    )
                )
            )
            .border(0.2.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Free",
                color = ThemeColors.getTextColor(!isDarkTheme),
                fontSize = 12.sp,
                fontFamily = manropeExtraBold
            )
        }
    }
}
@Composable
fun ToggleItem(
    text: String,
    selected: Boolean,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(40))
            .background(
                if (selected) Brush.linearGradient(
                    colors =  if (isDarkTheme) {listOf(
                        Color(0xFF7E66EC),
                        Color(0xFF182191)
                    )} else {
                        listOf(
                            Color(0xFFAFACE7),
                            Color(0xFF6869D5)
                        )
                    }
                ) else Brush.linearGradient(
                    colors = if (isDarkTheme) {listOf(
                        Color.Black,
                        Color.Black
                    )} else {
                        listOf(
                            Color(0xFF182191),
                            Color(0xFF182191)
                        )
                    }
                )
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                onClick()
            }
            .padding(horizontal = 10.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color(0xFFCED3D9),
            fontSize = 10.sp,
           fontFamily = manropeBold
        )
    }
}

@Composable
fun StatItem(
    emoji: Int,
    title: String,
    count: Int,
    isDarkTheme: Boolean,
    onClick: () -> Unit
) {
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )


        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(vertical = 8.dp)) {

            Row {
                Icon(
                    painter = painterResource(emoji),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(18.dp).padding(top = 7.dp) // icon size inside
                )
                Spacer(modifier = Modifier.width(1.dp))
                Text(
                    text = title,
                    fontSize = 12.sp,
                    color = ThemeColors.getLightWhiteBothColor(isDarkTheme),
                    fontFamily = manropeMedium
                )
            }
            Row(modifier = Modifier.clickable {
                onClick()
            }, horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = count.toString(),
                    fontSize = 14.sp,
                    color = Color.White,
                    fontFamily = manropeExtraBold
                )
                Icon(
                    painter = painterResource(R.drawable.outline_arrow_right_24),
                    contentDescription = null,
                    tint = Color(0xFF2979FF),
                    modifier = Modifier.size(20.dp).padding(top = 3.dp) // icon size inside
                )
            }

        }

}