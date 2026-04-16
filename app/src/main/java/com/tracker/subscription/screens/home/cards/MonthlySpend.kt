package com.tracker.subscription.screens.home.cards

import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.Utility.formatCurrency
import com.tracker.subscription.Utility.getGreeting
import com.tracker.subscription.data.DashboardData

@Composable
fun MonthlySpendCard(data: DashboardData, currency: String, amount: Double, navController: NavController) {
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    var isYearly  by remember {  mutableStateOf(false) }

    Column {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)

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
                            colors = listOf(
                                Color(0xFF1A237E),
                                Color(0xFF4866F1)
                            )
                        )
                    )
                ,
                contentAlignment = Alignment.TopStart
            ) {

                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {

                    var firstName = "Guest"
                    if(data.isLoggedIn){
                        firstName = data.user?.name
                            ?.trim()
                            ?.split(" ")
                            ?.firstOrNull()
                            ?: ""
                    }

                    Text(
                        text = getGreeting(),
                        color = colorResource(R.color.white),
                        fontFamily = manropeExtraBold,
                        fontSize = 30.sp
                    )
                    Text(
                        text = firstName,
                        color = colorResource(R.color.white),
                        fontFamily = manropeExtraBold,
                        fontSize = 30.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row( modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,) {
                        Text(
                            text = "${data.subscriptions.size}/5 subscriptions",
                            color = colorResource(R.color.white),
                            fontFamily = manropeMedium,
                            fontSize = 16.sp
                        )
                        Spacer(modifier = Modifier.width(14.dp))

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
                                .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                                .clickable { navController.navigate("premium") }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                Text(
                                    text = "Upgrade",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontFamily = manropeExtraBold
                                )

                                Spacer(modifier = Modifier.width(4.dp))

                                // ✨ glitter emoji
                                Text("✨", fontSize = 12.sp)
                            }
                        }

                    }


                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .padding(top = 15.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFFF2F7FD))
                        ,
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(R.drawable.timer, "Free Trial", data.freeTrials.size)
                        StatItem(R.drawable.text, "Active", data.subscriptions.size)
                        StatItem(R.drawable.notification_bell, "Renewals", data.upcomingRenewals.size)
                    }

                }
            }

        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)

            ,
            elevation = CardDefaults.cardElevation(30.dp),
            shape = RoundedCornerShape(30.dp)

        ) {

            // 🔹 MAIN CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFFFF3E0),
                                Color(0xFFF1DEF3),
                                Color(0xFFE3F2FD)
                            )
                        )
                    )
                ,
                contentAlignment = Alignment.TopStart
            ) {

                val displayAmount by animateFloatAsState(
                    targetValue = if (isYearly) (amount * 12).toFloat() else amount.toFloat(),
                    animationSpec = tween(200),
                    label = ""
                )

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(28.dp))
                        .padding(20.dp)
                ) {

                    Column {

                        // 🔝 Header Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Top
                        ) {

                            Column {

                                Text(
                                    text = if (isYearly) "Yearly Spend" else "Monthly Spend",
                                    color = Color.Black.copy(alpha = 0.6f),
                                    fontFamily = manropeExtraBold,
                                    fontSize = 13.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // 🔘 Premium Toggle
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(Color.White)
                                        .padding(4.dp)
                                ) {

                                    ToggleItem(
                                        text = "Monthly",
                                        selected = !isYearly
                                    ) { isYearly = false }

                                    ToggleItem(
                                        text = "Yearly",
                                        selected = isYearly
                                    ) { isYearly = true }
                                }
                            }

                            // 💰 Animated Amount
                            Column(horizontalAlignment = Alignment.End) {
                                val displayRounded = displayAmount.toInt()
                                Text(
                                    text = formatCurrency(displayRounded.toDouble(), data.currency),
                                    fontSize = 34.sp,
                                    fontFamily = manropeExtraBold,
                                    color = Color(0xFF0D1B2A)
                                )

                            }
                        }
                    }
                }
            }

        }

    }


}
@Composable
fun ToggleItem(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(
                if (selected) Color(0xFFFF8A80) else Color.Transparent
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Gray,
            fontSize = 12.sp,
           fontFamily = manropeBold
        )
    }
}

@Composable
fun StatItem(
    emoji: Int,
    title: String,
    count: Int
) {
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Box(
            modifier = Modifier
                .size(40.dp) // circle size
                .clip(CircleShape)
                .background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(emoji),
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier.size(30.dp) // icon size inside
            )
        }
        Spacer(modifier = Modifier.height(6.dp))



        Text(
            text = title,
            fontSize = 12.sp,
            color = Color.Black,
            fontFamily = manropeMedium
        )
        Text(
            text = count.toString(),
            fontSize = 18.sp,
            color = Color.Black,
            fontFamily = manropeExtraBold
        )
    }
}