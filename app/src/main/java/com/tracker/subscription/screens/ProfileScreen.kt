package com.tracker.subscription.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tracker.subscription.R
import com.tracker.subscription.data.AuthUser

@Composable
fun ProfileScreen(
    user:AuthUser?,
    onAddClick: () -> Unit
) {
    val firstName = user?.name?.trim()?.split(" ")
        ?.firstOrNull()
        ?: "Guest"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F6FA))
            .padding(16.dp)
    ) {

        // 🔝 Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Profile",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            IconButton(onClick = { /* settings */ }) {
                Icon(Icons.Default.Settings, contentDescription = null)
            }
        }

        Spacer(Modifier.height(16.dp))

        // 👤 Profile Card
        user?.let {
            ProfileCard(user)
        }


        Spacer(Modifier.height(16.dp))



        Spacer(Modifier.height(16.dp))

        // 💡 Insights Card
        InsightCard()

        Spacer(Modifier.height(16.dp))

        // ⚙️ Options
        OptionItem("Personal Details", "Member since 2026")
        OptionItem("Money Profile", "UPI • Active")
        OptionItem("Notifications", "Manage alerts")
        OptionItem("Help & Support", "FAQs & contact")
    }
}

@Composable
fun ProfileCard(user: AuthUser) {

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE0E0E0)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!user.photo.isNullOrEmpty()) {

                        AsyncImage(
                            model = user.photo,
                            contentDescription = null,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape),
                            contentScale = ContentScale.Crop
                        )

                    } else {
                        // 🔥 Fallback (first letter avatar)
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF90CAF9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user.name?.firstOrNull()?.uppercase() ?: "G",
                                fontSize = 28.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }


                }

                // Edit icon
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(colorResource(R.color.blue)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.White,modifier = Modifier.size(12.dp))
                }

            }
            Spacer(Modifier.height(12.dp))

            user?.name?.replaceFirstChar { it.uppercase() }?.let {
                Text(
                    text = it,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            user?.email?.let {
                Text(
                    text = it,
                    color = Color.Gray,
                    fontSize = 14.sp
                )
            }
        }
    }

    @Composable
    fun StatsRow(onAddClick: () -> Unit) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Card(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {

                    StatItem("Active", "6")
                    StatItem("Spent", "₹2.3k")
                    StatItem("Next", "₹499")
                }
            }

            Spacer(Modifier.width(12.dp))

            // ➕ Add Button
            Card(
                modifier = Modifier
                    .size(90.dp)
                    .clickable { onAddClick() },
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black)
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                    Text("Add", color = Color.White)
                }
            }
        }
    }
}

    @Composable
    fun StatItem(title: String, value: String) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, color = Color.Gray, fontSize = 12.sp)
            Text(value, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }

    @Composable
    fun InsightCard() {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = colorResource(R.color.blue)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Column {
                    Text(
                        "💡 Smart Insights",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "You can save ₹800/month",
                        color = Color(0xFFFFFFFF)
                    )
                }

                Spacer(Modifier.width(12.dp))
                Text(
                    "559",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    @Composable
    fun OptionItem(title: String, subtitle: String) {
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = Color.Gray, fontSize = 12.sp)
                }

                Icon(Icons.Default.KeyboardArrowRight, contentDescription = null)
            }
        }
    }
