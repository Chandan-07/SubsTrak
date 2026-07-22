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
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import kotlinx.coroutines.delay
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material.icons.filled.KeyboardArrowRight
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
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
    val showGreenToast by viewModel.showGreenToast.collectAsState()
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
                                                if (isDarkTheme) ThemeColors.getCardBackgroundColor(isDarkTheme) else Color.White,
                                                RoundedCornerShape(12.dp)
                                            )
                                            .clickable { navController.navigate("renewals") }
                                            .padding(10.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = "Search",
                                            tint = if (isDarkTheme) ThemeColors.getTextColor(isDarkTheme) else Color.Black
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
                                            fontSize = 14.sp,
                                            fontFamily = manropeExtraBold
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.15f))
                                                .padding(horizontal = 5.dp, vertical = 0.dp)
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
                                            Icon(imageVector = Icons.Default.KeyboardArrowRight,"", tint = colorResource(R.color.blue))
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
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(CircleShape)
                                                .background(ThemeColors.getPrimaryColor(isDarkTheme).copy(alpha = 0.15f))
                                                .padding(horizontal = 5.dp, vertical = 0.dp)
                                        ) {
                                            Text(
                                                text = "${data.upcomingRenewals.size}",
                                                fontSize = 12.sp,
                                                fontFamily = manropeExtraBold,
                                                color = ThemeColors.getPrimaryColor(isDarkTheme)
                                            )
                                        }
                                    }

                                    if (data.upcomingRenewals.size >2){
                                        Row {
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
                                                    )
                                            )
                                            Icon(imageVector = Icons.Default.KeyboardArrowRight,"", tint = Color(0xFF8DA0D0),
                                                modifier = Modifier.size(20.dp).padding(top = 5.dp))

                                        }
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

    showGreenToast?.let { msg ->
        LaunchedEffect(msg) {
            delay(3000)
            viewModel.clearGreenToast()
        }
        Popup(
            alignment = Alignment.BottomCenter,
            properties = PopupProperties(
                focusable = false,
                dismissOnBackPress = false,
                dismissOnClickOutside = false
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp, vertical = 56.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2E7D32))
                        .border(1.dp, Color(0xFF81C784).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = msg,
                        color = Color.White,
                        fontFamily = manropeBold,
                        fontSize = 12.sp
                    )
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
    val manropeMedium = FontFamily(Font(R.font.manrope_medium))
    val manropeBold = FontFamily(Font(R.font.manrope_bold))
    val manropeExtraBold = FontFamily(Font(R.font.manrope_extra_bold))
    val stackSansBold = FontFamily(Font(R.font.stack_sans_bold))

    val isPremium = data?.user?.isPremium == true &&
            (isAppUserSignedIn || guestPremiumOwned)

    // State for cycling facts when user taps "More Facts" or "View All Tips"
    var factIndex by remember { mutableStateOf(0) }
    var tipIndex by remember { mutableStateOf(0) }

    val factsList = listOf(
        "💡 The average household spends over ₹2,500 per month on subscriptions, often overestimating and forgetting several.",
        "💸 Small ₹199 monthly subscriptions accumulate to over ₹2,388 every single year.",
        "📊 Most people underestimate their subscription spending by over 40% until tracked in one place.",
        "💰 Canceling just one unused subscription could save you thousands of rupees every year."
    )

    val tipsList = listOf(
        "📱 Review and consolidate streaming services. Canceling one or two unused plans can save up to ₹3,000 a year.",
        "🔔 Set reminders 2 days before your free trial turns into a paid auto-recurring subscription.",
        "👀 Check your bank statement monthly for hidden recurring app or cloud storage fees."
    )

    val didYouKnowList = listOf(
        "⏰ Track your 'Free Trial' end dates to avoid automatic credit card charges.",
        "⚡ Cloud storage, fitness apps, AI tools, and shopping memberships add up fast.",
        "🛡️ Your SMS data stays strictly on your device — privacy is guaranteed."
    )

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(top = 70.dp, start = 16.dp, end = 16.dp, bottom = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. 🌟 TOP GREETING BANNER CARD
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(26.dp)),
            elevation = CardDefaults.cardElevation(8.dp),
            shape = RoundedCornerShape(26.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = if (isDarkTheme) {
                                listOf(Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFF4338CA))
                            } else {
                                listOf(Color(0xFF2563EB), Color(0xFF4F46E5), Color(0xFF7C3AED))
                            }
                        )
                    ),
                contentAlignment = Alignment.TopStart
            ) {
                // Glow ambient canvas effect
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    drawCircle(
                        color = Color.White.copy(alpha = 0.12f),
                        center = Offset(w * 0.85f, h * 0.25f),
                        radius = 80.dp.toPx()
                    )
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        center = Offset(w * 0.70f, h * 0.85f),
                        radius = 110.dp.toPx()
                    )
                }

                Column(
                    horizontalAlignment = Alignment.Start,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    val firstName = displayFirstName(
                        profileName = data?.user?.name,
                        firebaseDisplayName = firebaseUser?.displayName,
                        isAuthenticated = isAuthenticated
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (firstName.isNotBlank() && firstName != "Guest") "Welcome, $firstName! 🌤️" else "Welcome to Subtly ! 🌤️",
                            color = Color.White,
                            fontFamily = stackSansBold,
                            fontSize = 22.sp
                        )

                        if (isPremium) {
                            PremiumTag()
                        }
                    }

                    Spacer(modifier = Modifier.height(15.dp))

                    Text(
                        text = "Your one-stop beautiful dashboard for managing subscriptions and savings.",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.5.sp,
                        fontFamily = manropeMedium,
                        lineHeight = 19.sp
                    )
                }
            }
        }

        // 2. 📌 SECTION HEADER: "Weekly Fact" & "More Facts ->"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 4.dp, end = 4.dp, top = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Did you know ?",
                fontSize = 18.sp,
                fontFamily = manropeExtraBold,
                color = ThemeColors.getDarkBlueColor(isDarkTheme)
            )
        }

        Spacer(modifier = Modifier.height(25.dp))
        // 3. ↔️ HORIZONTAL SCROLLING FACTS CARDS
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(25.dp),
            contentPadding = PaddingValues(horizontal = 2.dp)
        ) {
            // CARD 1: Fact of the Week (Warm Gold/Amber Soft Gradient)
            item {
                Card(
                    modifier = Modifier
                        .width(310.dp)
                        .height(100.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.dp,
                        if (isDarkTheme) Color(0xFF78350F).copy(alpha = 0.6f) else Color(0xFFFDE68A)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDarkTheme) {
                                        listOf(Color(0xFF451A03), Color(0xFF78350F))
                                    } else {
                                        listOf(Color(0xFFFFFBEB), Color(0xFFFEF3C7))
                                    }
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontFamily = manropeMedium,
                                            color = if (isDarkTheme) Color(0xFFFEF3C7) else Color(0xFF451A03)
                                        )
                                    ) {
                                        append(factsList[0])
                                    }
                                },
                                fontFamily = manropeMedium,
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // CARD 2: Savings Tip (Mint / Teal Gradient with Geometric Pattern)
            item {
                Card(
                    modifier = Modifier
                        .width(310.dp)
                        .height(100.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp))
                        ,
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.dp,
                        if (isDarkTheme) Color(0xFF047857).copy(alpha = 0.6f) else Color(0xFFA7F3D0)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDarkTheme) {
                                        listOf(Color(0xFF064E3B), Color(0xFF047857))
                                    } else {
                                        listOf(Color(0xFFECFDF5), Color(0xD1D1FAE5))
                                    }
                                )
                            )
                    ) {
                        // Subtle geometric canvas lines
                        Canvas(modifier = Modifier.matchParentSize()) {
                            val linePaint = if (isDarkTheme) Color.White.copy(alpha = 0.05f) else Color(0xFF059669).copy(alpha = 0.08f)
                            val strokeW = 1.dp.toPx()
                            for (i in 0..10) {
                                drawLine(
                                    color = linePaint,
                                    start = Offset(i * 40.dp.toPx(), 0f),
                                    end = Offset(0f, i * 40.dp.toPx()),
                                    strokeWidth = strokeW
                                )
                            }
                        }

                        Column(modifier = Modifier.padding(18.dp)) {

                            Text(
                                text = buildAnnotatedString {
                                    withStyle(
                                        SpanStyle(
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = manropeBold,
                                            color = if (isDarkTheme) Color(0xFFA7F3D0) else Color(0xFF065F46)
                                        )
                                    ) {
                                        append("Tip: ")
                                    }
                                    withStyle(
                                        SpanStyle(
                                            fontFamily = manropeMedium,
                                            color = if (isDarkTheme) Color(0xFFECFDF5) else Color(0xFF064E3B)
                                        )
                                    ) {
                                        append(factsList[2])
                                    }
                                },
                                fontSize = 13.sp,
                                lineHeight = 19.sp,
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // CARD 3: Did you know? (Soft Purple/Violet Wave Gradient Card)
            item {
                Card(
                    modifier = Modifier
                        .width(310.dp)
                        .height(100.dp)
                        .shadow(4.dp, RoundedCornerShape(20.dp)),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = BorderStroke(
                        1.dp,
                        if (isDarkTheme) Color(0xFF6D28D9).copy(alpha = 0.6f) else Color(0xFFDDD6FE)
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = if (isDarkTheme) {
                                        listOf(Color(0xFF3B0764), Color(0xFF5B21B6))
                                    } else {
                                        listOf(Color(0xFFF5F3FF), Color(0xFFEDE9FE))
                                    }
                                )
                            )
                            .padding(18.dp)
                    ) {
                        Column {
                            Text(
                                text = factsList[1 ],
                                fontSize = 13.sp,
                                fontFamily = manropeMedium,
                                lineHeight = 19.sp,
                                color = if (isDarkTheme) Color(0xFFF5F3FF) else Color(0xFF4C1D95),
                                maxLines = 4,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(100.dp))

        // 4. 📦 NO SUBSCRIPTION UI BELOW THE CARDS
        Icon(
            painter = painterResource(R.drawable.no_subs),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .width(80.dp)
                .height(80.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            "No Subscriptions Yet",
            color = ThemeColors.getTextColor(isDarkTheme),
            fontSize = 18.sp,
            fontFamily = manropeBold
        )
        Spacer(modifier = Modifier.height(10.dp))
        Text(
            "Add your first subscription to start tracking spending.",
            textAlign = TextAlign.Center,
            fontFamily = manropeMedium,
            color = ThemeColors.getDarkGreyColor(isDarkTheme),
            fontSize = 12.sp,
            modifier = Modifier.padding(start = 40.dp, end = 40.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // 5. 📲 SMS Sync Prompt Container
        EmptySubscriptionSyncPrompt(
            viewModel = viewModel,
            navController = navController,
            isDarkTheme = isDarkTheme
        )
    }
}
