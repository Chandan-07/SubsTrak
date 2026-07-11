package com.tracker.subscription.screens.home

import android.Manifest
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tracker.subscription.data.Renewal
import com.tracker.subscription.data.Subscription
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.tracker.subscription.R
import com.tracker.subscription.Utility.getGreeting
import com.google.firebase.auth.FirebaseUser
import com.tracker.subscription.Utility.displayFirstName
import com.tracker.subscription.data.DashboardData
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.screens.home.cards.MonthlySpendCard
import com.tracker.subscription.screens.home.cards.PremiumTag
import com.tracker.subscription.screens.home.cards.RenewalItem
import com.tracker.subscription.ui.theme.ThemeColors

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    isLoggedIn: Boolean,
    firebaseUser: FirebaseUser?,
    guestPremiumOwned: Boolean,
    navController: NavController,
    onAddSubscription: () -> Unit = {},
    isDarkTheme: Boolean
) {
    val context = LocalContext.current
    val isAuthenticated = isLoggedIn || firebaseUser != null
    val state by viewModel.uiState.collectAsState()
    val smsState by viewModel.smsSyncState.collectAsState()
    val manropeBold = FontFamily( Font(R.font.manrope_bold) )
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    Scaffold(
        floatingActionButton = {
            when(state) {
                DashboardUiState.Loading -> {

                }
                is DashboardUiState.Success -> {
                    val  data = (state as DashboardUiState.Success).data
                    if (data.subscriptions?.isEmpty() == true) {
                        Box(
                            modifier = Modifier
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(30),
                                    ambientColor = ThemeColors.getDarkBlueColor(isDarkTheme),
                                    spotColor = colorResource(R.color.dark_blue)
                                )
                                .background(
                                    color = colorResource(R.color.blue),
                                    shape = if (data?.subscriptions?.isEmpty() == true) RoundedCornerShape(
                                        30
                                    ) else RoundedCornerShape(100.dp),
                                )
                        ) {

                            ExtendedFloatingActionButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                    onAddSubscription()
                                },
                                containerColor = Color.Transparent,
                                shape = if (data?.subscriptions?.isEmpty() == true)
                                    RoundedCornerShape(30)
                                else CircleShape,
                                elevation = FloatingActionButtonDefaults.elevation(
                                    0.dp, 0.dp, 0.dp, 0.dp
                                ),
                                interactionSource = interactionSource, // 👈 important
                            ) {

                                if (data?.subscriptions?.isEmpty() == true) {

                                    Row(Modifier.padding(5.dp)) {
                                        Icon(imageVector = Icons.Default.Add,"", tint = Color.White)
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Add Subscriptions",
                                            color = colorResource(R.color.white),
                                            fontFamily = manropeBold
                                        )
                                    }

                                }

                            }
                        }
                    } else {
                        FloatingActionButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.VirtualKey)
                                onAddSubscription()
                            },
                            containerColor = Color.Transparent,
                            shape = CircleShape,
                            modifier = Modifier.size(48.dp),
                            elevation = FloatingActionButtonDefaults.elevation(
                                defaultElevation = 4.dp,
                                pressedElevation = 0.dp
                            ),
                        ) {
                            Icon(
                                painterResource(R.drawable.fab_add),
                                contentDescription = null,
                                tint = Color.Unspecified,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }
            }


        },
        floatingActionButtonPosition = FabPosition.Center ,
    ) { padding ->

        val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
        val manropeRegular = FontFamily( Font(R.font.manrope_regular) )

        Log.d("ASFDS", "DashboardScreen: "+state)

        when(state) {
            is DashboardUiState.Loading -> {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally , verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }

            }

            is DashboardUiState.Success -> {
                val data = (state as DashboardUiState.Success).data
                val smsSuggestions by viewModel.smsSyncState.collectAsState()
                var smsScanTriggerSignal by remember { mutableStateOf(0) }
                var isBannerDismissed by remember { mutableStateOf(false) }

                if (data.subscriptions.isEmpty()) {

                    Box {
                            EmptySubscriptionScreen(
                                viewModel = viewModel,
                                navController = navController,
                                isAuthenticated = isAuthenticated,
                                data = data,
                                firebaseUser = firebaseUser,
                                guestPremiumOwned = guestPremiumOwned,
                                isAppUserSignedIn = isAuthenticated,
                                isDarkTheme = isDarkTheme
                            )

                    }

                } else {

                    Box(modifier = Modifier.fillMaxSize()) {
                        LazyColumn(
                            modifier = Modifier
                                .background(color = ThemeColors.getBlueBgColor(isDarkTheme))
                                .fillMaxSize()
                                .padding(padding)
                                .padding(start = 10.dp, end = 10.dp, top = 0.dp, bottom = 20.dp)
                        ) {
                            item {
                                val firstName = displayFirstName(
                                    profileName = data.user?.name,
                                    firebaseDisplayName = firebaseUser?.displayName,
                                    isAuthenticated = isAuthenticated
                                )
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Row  {
                                        Text(
                                            text = getGreeting(),
                                            color = ThemeColors.getTextColor(isDarkTheme),
                                            fontFamily = manropeExtraBold,
                                            fontSize = 20.sp
                                        )

                                        Text(
                                            text = firstName,
                                            color = ThemeColors.getTextColor(isDarkTheme),
                                            fontFamily = manropeExtraBold,
                                            fontSize = 22.sp
                                        )
                                    }

                                    Box(
                                        modifier = Modifier
                                            .shadow(4.dp, RoundedCornerShape(12.dp))
                                            .background(
                                                Color.White,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { navController.navigate("renewals") }
                                            .padding(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = Color.Black
                                        )
                                    }
                                }

                            }
                            item {
                                MonthlySpendCard(
                                    isLoggedIn = isLoggedIn,
                                    isAppUserSignedIn = isAuthenticated,
                                    guestPremiumOwned = guestPremiumOwned,
                                    data = data,
                                    currency = data.currency,
                                    amount = data.monthlySpend,
                                    navController = navController,
                                    isDarkTheme = isDarkTheme,
                                    showSyncChip = isBannerDismissed,
                                    pendingCount = smsSuggestions.size,
                                    onSyncSmsClick = { smsScanTriggerSignal++ }
                                )
                            }

                            item {
                                SmsSyncDashboardBanner(
                                    viewModel = viewModel,
                                    navController = navController,
                                    isDarkTheme = isDarkTheme,
                                    externalTriggerSignal = smsScanTriggerSignal,
                                    onDismissBanner = {
                                        isBannerDismissed = true
                                    }
                                )
                            }

                        if (data.freeTrials.isNotEmpty()){
                            item {
                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {


                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Free Trials",
                                            color = ThemeColors.getTextColor(isDarkTheme),
                                            fontSize = 20.sp,
                                            fontFamily = manropeExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${data.freeTrials.size}",
                                                fontSize = 12.sp,
                                                fontFamily = manropeExtraBold,
                                                color = ThemeColors.getPrimaryColor(isDarkTheme)
                                            )
                                        }
                                    }


                                    if (!data.freeTrials.isEmpty() && data.freeTrials.size >2) {
                                        Row {
                                            Text(
                                                text = "View All",
                                                color = Color(0xFF8DA0D0),
                                                fontSize = 12.sp,
                                                fontFamily = manropeExtraBold,
                                                modifier = Modifier
                                                    .clickable {
                                                        navController.navigate("view_all_free_trials")
                                                    }
                                                    .padding(
                                                        start = 10.dp,
                                                        end = 10.dp,
                                                        top = 4.dp,
                                                        bottom = 4.dp
                                                    )
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Icon(imageVector = Icons.Default.ArrowForward,"", tint = colorResource(R.color.blue))
                                        }

                                    }

                                }

                                Spacer(modifier = Modifier.height(8.dp))
                            }

                            items(data.freeTrials.take(3)) {
                                RenewalItem(it, context, viewModel.getServiceByKey(it.key), isDarkTheme, onEdit = { subscription ->
                                    navController.navigate("add_subscription?id=${subscription.id}")

                                }, onDelete = { subscription ->
                                    viewModel.deleteSubscription(subscription.id)
                                })
                            }
                        }


                        if (data.upcomingRenewals.isNotEmpty()) {
                            item {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "Upcoming Renewals",
                                            fontFamily = manropeExtraBold,
                                            color = ThemeColors.getTextColor(isDarkTheme),
                                            fontSize = 20.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = "${data.upcomingRenewals.size}",
                                                fontSize = 14.sp,
                                                fontFamily = manropeExtraBold,
                                                color = ThemeColors.getPrimaryColor(isDarkTheme)
                                            )
                                        }
                                    }

                                    if (data.upcomingRenewals.size >2){
                                        Text(
                                            text = "View All",
                                            color = Color(0xFF8DA0D0),
                                            fontSize = 12.sp,
                                            fontFamily = manropeExtraBold,
                                            modifier = Modifier
                                                .clickable {
                                                    navController.navigate("view_all_renewals")
                                                }
                                                .padding(
                                                    start = 10.dp,
                                                    end = 10.dp,
                                                    top = 4.dp,
                                                    bottom = 4.dp
                                                )
                                        )
                                    }

                                }
                                Spacer(modifier = Modifier.height(8.dp))

                            }
                            items(data.upcomingRenewals.take(3)) {
                                RenewalItem(it, context, viewModel.getServiceByKey(it.key), isDarkTheme, onEdit = { subscription ->
                                    navController.navigate("add_subscription?id=${subscription.id}")

                                }, onDelete = { subscription ->
                                    viewModel.deleteSubscription(subscription.id)
                                })
                            }

                        }
                    }


                }
            }
        }
    }
}
}





fun openSubscription(context: Context, sub: Renewal) {

    val packageName = sub.packageName
    if (!packageName.isNullOrEmpty()) {

        // fallback → Play Store
        try {
            context.startActivity(
                Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("https://play.google.com/store/account/subscriptions")
                    setPackage("com.android.vending")
                }
            )
        } catch (e: Exception) {
            context.startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/account/subscriptions")
                )
            )
        }

    } else {
        // fallback → Google search
        context.startActivity(
            Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.google.com/search?q=${sub.name} unsubscribe"))
        )
    }
}



@Composable
fun EmptySubscriptionScreen(
    viewModel: DashboardViewModel,
    navController: NavController,
    isAuthenticated: Boolean,
    data: DashboardData?,
    firebaseUser: FirebaseUser?,
    guestPremiumOwned: Boolean,
    isAppUserSignedIn: Boolean,
    isDarkTheme: Boolean
    ) {
    val manropeMedium = FontFamily( Font(R.font.manrope_medium) )
    val manropeExtraBold = FontFamily( Font(R.font.manrope_extra_bold) )
    val isPremium = data?.user?.isPremium == true &&
            (isAppUserSignedIn || guestPremiumOwned)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 40.dp, start = 16.dp, end = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
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
                                ThemeColors.getDarkBlueColor(isDarkTheme),
                                ThemeColors.getPrimaryColor(isDarkTheme)
                            )
                        )
                    )
                ,
                contentAlignment = Alignment.TopStart
            ) {

                Column(horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 26.dp, vertical = 30.dp)) {
                    val firstName = displayFirstName(
                        profileName = data?.user?.name,
                        firebaseDisplayName = firebaseUser?.displayName,
                        isAuthenticated = isAuthenticated
                    )

                    data?.let {


                        Row(modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,) {
                            Text(
                                text = getGreeting(),
                                color = Color.White,
                                fontFamily = manropeExtraBold,
                                fontSize = 20.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val isPremium = data.user?.isPremium == true &&
                                    (isAppUserSignedIn || guestPremiumOwned)

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
                                                        ThemeColors.getDarkBlueColor(isDarkTheme),
                                                        ThemeColors.getPrimaryColor(isDarkTheme),
                                                        ThemeColors.getDarkBlueColor(isDarkTheme)
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
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
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

                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "You have ${data.upcomingRenewals.size} Subscriptions & ${data.freeTrials.size} FreeTrials",
                            color = colorResource(R.color.white),
                            fontSize = 14.sp,
                            fontFamily = manropeMedium,
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                    }

                }
            }

        }
        Spacer(modifier = Modifier.height(30.dp))

        Icon(
           painter = painterResource(R.drawable.empty_task),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .width(200.dp)
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            "No Subscriptions Yet",
            style = MaterialTheme.typography.titleLarge,
            color = ThemeColors.getDarkBlueColor(isDarkTheme),
            fontSize = 26.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            "Add your first subscription to start tracking spending.",
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            modifier = Modifier.padding(start = 30.dp, end = 30.dp)
        )
    }

    EmptySubscriptionSyncPrompt(
        viewModel = viewModel,
        navController = navController,
        isDarkTheme = isDarkTheme
    )
}
