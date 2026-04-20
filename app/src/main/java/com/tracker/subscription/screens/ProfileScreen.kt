package com.tracker.subscription.screens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.tracker.subscription.R
import com.tracker.subscription.data.AuthUser

@Composable
fun ProfileScreen(
    user:AuthUser?,
    onSignOut: () -> Unit,
    onLogin: () -> Unit
) {
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    var showLogoutDialog by remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        // 🔝 Header
        Spacer(Modifier.height(50.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "My Profile",
                fontSize = 22.sp,
                fontFamily = manropeExtraBold,
                color = colorResource(R.color.dark_blue)
            )

//            IconButton(onClick = { /* settings */ }) {
//                Icon(Icons.Default.Settings, contentDescription = null)
//            }
        }

        Spacer(Modifier.height(20.dp))

        // 👤 Profile Card
        if (user != null){
            user?.let {
                ProfileCard(user)
                Spacer(Modifier.height(16.dp))



                Spacer(Modifier.height(16.dp))



                Spacer(Modifier.height(16.dp))

                // ⚙️ Options
                OptionItem("Personal Details", "Member since 2026")
                OptionItem("Help & Support", "FAQs & contact")
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .clickable {
                            showLogoutDialog = true
                        }
                        .padding(vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().background(colorResource(R.color.blue_bg_light)).padding(16.dp),
                    ) {
                        Column {
                            Text("Sign Out", color = Color.Red, fontSize = 14.sp, fontFamily = manropeExtraBold)
                        }

                    }
                }
            }
        } else{
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color.White).padding(20.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Box {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFFFFFF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(CircleShape)
                                    .background(colorResource(R.color.lime)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "G",
                                    fontSize = 28.sp,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }


                        }


                    }
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Guest User",
                        fontSize = 20.sp,
                        fontFamily = manropeBold
                    )
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Login",
                        fontSize = 16.sp,
                        fontFamily = manropeExtraBold,
                        color = Color.White,
                        modifier = Modifier
                            .clip(RoundedCornerShape(25.dp))
                            .background(Color(0xFF3D5AFE))
                            .clickable { onLogin()  }
                            .padding(top = 3.dp, bottom = 6.dp, start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center
                    )


                }
            }
        }




        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                shape = RoundedCornerShape(20.dp),

                title = {
                    Text(
                        text = "Sign out?",
                        fontFamily = manropeExtraBold
                    )
                },

                text = {
                    Text(
                        text = "Are you sure you want to sign out of your account?",
                        fontFamily = manropeMedium
                    )
                },

                confirmButton = {
                    TextButton(
                        onClick = {
                            showLogoutDialog = false
                            onSignOut() // 🔥 actual logout
                        }
                    ) {
                        Text("Sign Out", color = Color.Red, fontFamily = manropeBold)
                    }
                },

                dismissButton = {
                    TextButton(
                        onClick = { showLogoutDialog = false }
                    ) {
                        Text("Cancel", fontFamily = manropeExtraBold)
                    }
                }
            )
        }

    }
}

@Composable
fun ProfileCard(user: AuthUser) {

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .background(Color.White).padding(20.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFFFFF)),
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
                                .background(Color(0xFFC6FF00)),
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
        val manropeBold = FontFamily( Font(R.font.manrope_bold) )
        val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
        val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
        var showLogoutDialog by remember { mutableStateOf(false) }
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(colorResource(R.color.blue_bg_light)).padding(16.dp),
            ) {
                Column {
                    Text(title, fontFamily = manropeBold, fontSize = 14.sp)
                    Spacer(Modifier.height(3.dp))
                    Text(subtitle, color = Color.Gray, fontFamily = manropeMedium, fontSize = 12.sp)
                }

            }
        }
    }
