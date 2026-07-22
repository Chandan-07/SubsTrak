package com.tracker.subscription.screens

import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import com.tracker.subscription.data.db.OnboardingPreference
import com.tracker.subscription.screens.home.cards.FreeTag
import com.tracker.subscription.screens.home.cards.PremiumTag
import com.tracker.subscription.ui.theme.ThemeColors
import kotlinx.coroutines.launch

@Composable
fun ProfileScreen(
    isPremium: Boolean,
    user: AuthUser?,
    onSignOut: () -> Unit,
    onLogin: () -> Unit,
    isDarkTheme: Boolean = false,
    onThemeToggle: (Boolean) -> Unit = {},
    onReplayOnboarding: () -> Unit = {}
) {
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    var showLogoutDialog by remember { mutableStateOf(false) }
    var themeState by remember { mutableStateOf(isDarkTheme) }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    val bgColor = if (isDarkTheme) Color(0xFF121212) else Color.White
    val textColor = if (isDarkTheme) Color.White else colorResource(R.color.dark_grey)
    val cardBgColor = if (isDarkTheme) Color(0xFF2A2A2A) else colorResource(R.color.blue_bg_light)
    val scrollState = rememberScrollState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
            .verticalScroll(scrollState)
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
                fontSize = 24.sp,
                fontFamily = manropeExtraBold,
                color = textColor,
                modifier = Modifier.padding(start = 26.dp)
            )
        }

        Spacer(Modifier.height(40.dp))

        // 👤 Profile Card
        if (user != null) {
            ProfileCard(user, isDarkTheme)
        } else {
            Card(
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(cardBgColor)
                        .padding(20.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(CircleShape)
                            .background(ThemeColors.getDarkBlueColor(isDarkTheme)),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFC6FF00)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "G",
                                fontSize = 28.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    Text(
                        text = "Guest User",
                        fontSize = 20.sp,
                        fontFamily = manropeBold,
                        color = textColor
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
                            .clickable { onLogin() }
                            .padding(top = 3.dp, bottom = 6.dp, start = 20.dp, end = 20.dp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        // ⚙️ Options (shown for everyone!)
        PremiumItem("Current Plan", isPremium, isDarkTheme)
        OptionItem("Help & Support", "FAQs & contact", isDarkTheme)
        OptionItem(
            title = "Rate App",
            subtitle = "Support us on the Google Play Store",
            isDarkTheme = isDarkTheme,
            onClick = {
                val packageName = context.packageName
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=$packageName")
                    setPackage("com.android.vending")
                }
                runCatching {
                    context.startActivity(intent)
                }.onFailure {
                    runCatching {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
                    }
                }
            }
        )

        OptionItem(
            title = "Quick Tour",
            subtitle = "Replay app introduction walkthrough",
            isDarkTheme = isDarkTheme,
            onClick = onReplayOnboarding
        )

        // Theme Toggle
        ThemeToggleItem(isDarkTheme) { newTheme ->
            scope.launch {
                OnboardingPreference.setDarkTheme(context, newTheme)
                themeState = newTheme
                onThemeToggle(newTheme)
            }
        }

        // Sign Out (shown only if logged in!)
        if (user != null) {
            Spacer(Modifier.height(8.dp))
            Card(
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ThemeColors.getBackgroundColor(isDarkTheme))
                    .clickable {
                        showLogoutDialog = true
                    }
                    .padding(vertical = 6.dp, horizontal = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(cardBgColor)
                        .padding(16.dp),
                ) {
                    Column {
                        Text("Sign Out", color = Color.Red, fontSize = 14.sp, fontFamily = manropeExtraBold)
                    }
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
fun ProfileCard(user: AuthUser, isDarkTheme: Boolean) {
    val cardBgColor = if (isDarkTheme) Color(0xFF2A2A2A) else Color.White
    val textColor = if (isDarkTheme) Color.White else Color.Black
    val subtextColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color.Gray

    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
    ) {
        Column(
            modifier = Modifier
                .background(cardBgColor)
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Box {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(90.dp)
                        .clip(CircleShape)
                        .background(ThemeColors.getDarkBlueColor(isDarkTheme)),
                    contentAlignment = Alignment.Center
                ) {
                    Log.d("ASF", "ProfileCard: "+user.photo)
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
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

            }
            Spacer(Modifier.height(12.dp))

            user.name?.replaceFirstChar { it.uppercase() }?.let {
                Text(
                    text = it,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = textColor
                )
            }

            user.email?.let {
                Text(
                    text = it,
                    color = subtextColor,
                    fontSize = 14.sp
                )
            }
        }
    }
}


    @Composable
    fun OptionItem(title: String, subtitle: String, isDarkTheme: Boolean, onClick: () -> Unit = {}) {
        val manropeBold = FontFamily( Font(R.font.manrope_bold) )
        val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
        val cardBgColor = if (isDarkTheme) Color(0xFF2A2A2A) else colorResource(R.color.blue_bg_light)
        val textColor = if (isDarkTheme) Color.White else Color.Black
        val subtextColor = if (isDarkTheme) Color(0xFFB0B0B0) else Color.Gray
        
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(vertical = 8.dp, horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBgColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(title, fontFamily = manropeBold, fontSize = 14.sp, color = textColor)
                    Spacer(Modifier.height(3.dp))
                    Text(subtitle, color = subtextColor, fontFamily = manropeMedium, fontSize = 12.sp)
                }

                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = subtextColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }

    @Composable
    fun PremiumItem(title: String, isPremium: Boolean, isDarkTheme: Boolean) {
        val manropeBold = FontFamily( Font(R.font.manrope_bold) )
        val cardBgColor = if (isDarkTheme) Color(0xFF2A2A2A) else colorResource(R.color.blue_bg_light)
        val textColor = if (isDarkTheme) Color.White else Color.Black
        
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBgColor)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, fontFamily = manropeBold, fontSize = 14.sp, modifier = Modifier.padding(top = 3.dp), color = textColor)

                Spacer(Modifier.height(20.dp))
                if (isPremium) {
                    PremiumTag()
                }else{
                    FreeTag(isDarkTheme)
                }
            }
        }
    }

    @Composable
    fun ThemeToggleItem(isDarkTheme: Boolean, onToggle: (Boolean) -> Unit) {
        val manropeBold = FontFamily( Font(R.font.manrope_bold) )
        val cardBgColor = if (isDarkTheme) Color(0xFF2A2A2A) else colorResource(R.color.blue_bg_light)
        val textColor = if (isDarkTheme) Color.White else Color.Black
        
        Card(
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 10.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(cardBgColor)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dark Theme", fontFamily = manropeBold, fontSize = 14.sp, color = textColor)
                
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedTrackColor = ThemeColors.getBackgroundColor(isDarkTheme),
                        checkedThumbColor = ThemeColors.getBlueLightColor(isDarkTheme),
                        uncheckedThumbColor = Color(0xFF303F9F)
                    )
                )
            }
        }
    }

