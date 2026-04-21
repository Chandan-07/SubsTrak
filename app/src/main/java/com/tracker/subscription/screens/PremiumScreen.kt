package com.tracker.subscription.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.subscription.R
import com.tracker.subscription.presentation.PremiumViewModel


@Composable
fun PremiumPlanScreen(
    viewModel: PremiumViewModel,
    onClose: () -> Unit
){
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeRegular = FontFamily( Font(R.font.manrope_regular) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val plans = viewModel.plans
    val selectedPlan = viewModel.selectedPlan
    val context = LocalContext.current
    val activity = context as Activity

    LaunchedEffect(Unit) {
        viewModel.loadPlans()
    }
    Box (

        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF7F8FB))
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
        ) {

            Spacer(modifier = Modifier.height(50.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 🏆 Title
                Text(
                    text = "Unlock Premium",
                    fontFamily = manropeBold,
                    fontSize = 28.sp
                )
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = null)
                }
            }




            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Get unlimited access to all features and take control of your subscriptions",
                color = Color.Gray,
                fontFamily = manropeRegular
            )

            Spacer(modifier = Modifier.height(20.dp))
            if (plans.isNotEmpty()){
                plans.forEach { plan ->

                    PlanCard(
                        title = plan.title,
                        price = plan.price, // 🔥 dynamic price
                        subText = if (!plan.isYearly) "per month • Billed monthly" else null,
                        isSelected = selectedPlan == plan,
                        tag = if (plan.isYearly) "SAVE 23%" else null
                    ) {
                        viewModel.selectedPlan = plan
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ⭐ Features
            FeatureItem("∞", "Unlimited Subscriptions", "Add as many subscriptions as you want")
            FeatureItem("🔔", "Unlimited Notifications", "Never miss a payment with smart reminders")
            FeatureItem("📊", "Advanced Analytics", "Detailed insights and spending patterns")
            FeatureItem( "⚡", "Take Actions Faster", "One step away to Unsubscribe")

            Spacer(modifier = Modifier.weight(1f))

            // 🚀 Continue Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                Color(0xFF2979FF), // blue
                                Color(0xFF2979FF)  // light blue
                            )
                        ),
                        RoundedCornerShape(25.dp)
                    ),
                contentAlignment = Alignment.BottomCenter
            ) {
                Button(
                    onClick = {
                        viewModel.purchase(activity)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    )
                ) {
                    Text("Continue",  color = Color.White, fontSize = 20.sp, fontFamily = manropeBold)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Auto-renewable. Cancel anytime.\nBy subscribing, you agree to our Terms of Service and Privacy Policy.",
                textAlign = TextAlign.Center,
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun PlanCard(
    title: String,
    price: String,
    isSelected: Boolean,
    tag: String? = null,
    subText: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .border(
                width = 2.dp,
                color = if (isSelected) Color(0xFF5A5DF0) else Color(0xFFE0E0E0),
                shape = RoundedCornerShape(20.dp)
            )
            .clickable { onClick() }
            .padding(16.dp)
    ) {

        Column {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(title, fontWeight = FontWeight.Bold)

                if (tag != null) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFF2ECC71))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text(tag, color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = price,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A5DF0)
            )

            if (subText != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(subText, color = Color.Gray, fontSize = 12.sp)
            }
        }
    }
}

@Composable
fun FeatureItem(
    icon: String,
    title: String,
    desc: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp)
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(Color(0xFFEDEBFF)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(desc, color = Color.Gray, fontSize = 12.sp)
        }
    }
}