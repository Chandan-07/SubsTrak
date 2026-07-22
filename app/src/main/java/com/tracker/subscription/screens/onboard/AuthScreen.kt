package com.tracker.subscription.screens.onboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tracker.subscription.R

@Composable
fun AuthScreen(
    isLoading: Boolean,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    onGoogleSignIn: () -> Unit,
    onSkip: () -> Unit
) {

    val manropeBold = FontFamily(Font(R.font.manrope_bold))
    val stackSansBold = FontFamily(Font(R.font.stack_sans_bold))
    val manropeMedium = FontFamily(Font(R.font.manrope_medium))

    val bgColor = if (isDarkTheme) Color(0xFF0F172A) else Color(0xFFF8FAFC)
    val cardBgColor = if (isDarkTheme) Color(0xFF17171C) else Color(0xFFF1F5F9)
    val pillBgColor = if (isDarkTheme) Color.White else Color(0xFF0F172A)
    val pillTextColor = if (isDarkTheme) Color.Black else Color.White
    val arrowCircleBg = if (isDarkTheme) Color(0xFFE2E8F0) else Color(0xFF1E293B)
    val arrowIconTint = if (isDarkTheme) Color.Black else Color.White
    val skipTextColor = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF64748B)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(bgColor)
    ) {

        Image(
            painter = painterResource(id = R.drawable.splash),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = if (isDarkTheme) 0.05f else 0.08f,
            modifier = Modifier.matchParentSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 24.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Top Hero Content Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {

                Icon(
                    painter = painterResource(R.drawable.login),
                    contentDescription = "",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(135.dp)
                )

                Spacer(modifier = Modifier.height(28.dp))

                Text(
                    text = "Sign in to Subtly",
                    color = if (isDarkTheme) Color.White else Color(0xFF0F172A),
                    fontSize = 34.sp,
                    fontFamily = stackSansBold,
                    textAlign = TextAlign.Center,
                    lineHeight = 42.sp,
                    modifier = Modifier.padding(horizontal = 30.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Sync all your active plans seamlessly across devices and never lose track.",
                    color = if (isDarkTheme) Color(0xFF94A3B8) else Color(0xFF475569),
                    fontSize = 13.sp,
                    fontFamily = manropeMedium,
                    lineHeight = 22.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 36.dp)
                )
            }

            // Bottom Premium Card Container (100% matched to Onboarding layout)
            Surface(
                modifier = Modifier
                    .fillMaxWidth(),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = cardBgColor
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Google Login Action Pill Button (Matching Onboarding style)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(62.dp)
                            .clickable(enabled = !isLoading) { onGoogleSignIn() },
                        shape = CircleShape,
                        color = pillBgColor
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 20.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.google),
                                    contentDescription = "Google",
                                    tint = Color.Unspecified,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = "Continue with Google",
                                    color = pillTextColor,
                                    fontFamily = manropeBold,
                                    fontSize = 15.sp
                                )
                            }

                            // Circular Arrow / Loading Indicator inside Pill Button
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(arrowCircleBg),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(18.dp),
                                        strokeWidth = 2.dp,
                                        color = arrowIconTint
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                        contentDescription = "Continue",
                                        tint = arrowIconTint,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Skip for now link button
                    Text(
                        text = "Skip for now",
                        fontSize = 13.sp,
                        fontFamily = manropeBold,
                        color = skipTextColor,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(enabled = !isLoading) { onSkip() }
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }
}