package com.tracker.subscription

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.auth.GoogleAuthHelper
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.db.OnboardingPreference
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.AddSubscriptionViewModel
import com.tracker.subscription.presentation.AddSubscriptionViewModelFactory
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.presentation.DashboardViewModelFactory
import com.tracker.subscription.screens.AddSubscriptionScreen
import com.tracker.subscription.screens.AuthScreen
import com.tracker.subscription.screens.DashboardScreen
import com.tracker.subscription.screens.OnboardingScreen
import com.tracker.subscription.screens.PremiumPlanScreen
import com.tracker.subscription.screens.ProfileScreen
import com.tracker.subscription.screens.SubscriptionScreen
import com.tracker.subscription.screens.ViewAllScreen
import com.tracker.subscription.ui.data.BottomNavItem
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box as Box1

class MainActivity : ComponentActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(
                scrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb()
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.Transparent.toArgb(),
                darkScrim = Color.Transparent.toArgb()
            )
        )
        val bottomItems = listOf(
            BottomNavItem("dashboard", "Home", R.drawable.home),
            BottomNavItem("renewals", "Subscriptions", R.drawable.container),
            BottomNavItem("profile", "Profile", R.drawable.profile)
        )
        val bottomBarRoutes = listOf(
            "dashboard",
            "renewals",
            "profile"
        )
        setContent {

            val context = LocalContext.current

            val onboardingCompleted by OnboardingPreference
                .isCompleted(context)
                .collectAsState(initial = null)

            val isAuthSkipped by OnboardingPreference
                .isAuthSkipped(context)
                .collectAsState(initial = null)


            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            val firebaseUser = FirebaseAuth.getInstance().currentUser
            val destination = when {
                firebaseUser != null -> "dashboard"
                onboardingCompleted == true -> "auth"
                else -> "onboarding"
            }

            val db = DatabaseProvider.getDatabase(context)

            val smsDataSource = SmsDataSource(context)
            val repository = remember {
                SubscriptionRepository(db.subscriptionDao(), db.userDao(), context, smsDataSource)
            }
            val viewModel: DashboardViewModel = viewModel(
                factory = DashboardViewModelFactory(repository)
            )

            if (onboardingCompleted == null || isAuthSkipped == null) {
                Box1(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@setContent
            }
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentRoute = navBackStackEntry?.destination?.route

            Scaffold(bottomBar = {

                if (currentRoute in bottomBarRoutes) {
                    NavigationBar(
                        containerColor = Color.White,

                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars) // 👈 correct way
                            .shadow(16.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))

                    ) {
                        bottomItems.forEach { item ->

                            NavigationBarItem(
                                icon = { Icon(painter = painterResource( item.icon), contentDescription = item.label) },
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo("dashboard")
                                        launchSingleTop = true
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1565C0),
                                    unselectedIconColor = Color.Gray,
                                    indicatorColor = Color(0xFFE3F2FD) // light blue background
                                )
                            )
                        }
                    }
                }

            } ) { innerPadding ->
                NavHost(
                    navController = navController,
                    startDestination = destination,
                    modifier = Modifier
                        .padding(innerPadding)
                        .padding(bottom = 0.dp, top = 0.dp)
                ) {


                    composable("onboarding") {

                        OnboardingScreen(
                            onGetStarted = {
                                navController.navigate("auth")
                            }
                        )
                    }

                    composable("auth") {

                        val coroutineScope = rememberCoroutineScope()
                        val isLoading = viewModel.isSigningIn

                        val googleAuthClient = remember {
                            GoogleAuthHelper(context)
                        }

                        val launcher = rememberLauncherForActivityResult(
                            contract = ActivityResultContracts.StartActivityForResult()
                        ) { result ->

                            if (result.resultCode == Activity.RESULT_OK) {

                                coroutineScope.launch {
                                    viewModel.setLoading(true)

                                    try {
                                        val user = googleAuthClient.signInWithIntent(result.data!!)
                                        viewModel.setUser(user)
                                        navController.navigate("dashboard")
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    } finally {
                                        viewModel.setLoading(false)
                                    }
                                }
                            }
                        }

                        Box1 {
                            AuthScreen(
                                isLoading = isLoading,
                                onGoogleSignIn = {
                                    if (!isLoading) {
                                        launcher.launch(googleAuthClient.getSignInIntent())
                                    }
                                },
                                onSkip = {
                                    if (!isLoading) {
                                        scope.launch {
                                            OnboardingPreference.setAuthSkipped(context)
                                        }
                                        navController.navigate("dashboard")
                                    }
                                }
                            )

                            // 🔥 Full screen loader (optional but premium)
                            if (isLoading) {
                                Box1(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Color.Black.copy(alpha = 0.3f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                    }
                    composable("dashboard") {

                        DashboardScreen(
                            navController = navController,
                            onAddSubscription = {
                                navController.navigate("add_subscription")
                            }
                        )
                    }



                    composable(
                        route = "add_subscription?id={id}",
                        arguments = listOf(
                            navArgument("id") {
                                type = NavType.IntType
                                defaultValue = -1
                            }
                        )
                    ) { backStackEntry ->

                        val id = backStackEntry.arguments?.getInt("id")
                        val context = LocalContext.current

                        val db = DatabaseProvider.getDatabase(context)


                        val smsDataSource = SmsDataSource(context)
                        val repository = remember {
                            SubscriptionRepository(db.subscriptionDao(), db.userDao(), context, smsDataSource)
                        }
                        val viewModel: AddSubscriptionViewModel = viewModel(
                            factory = AddSubscriptionViewModelFactory(repository)
                        )
                        var subscription by remember { mutableStateOf<Subscription?>(null) }

                        LaunchedEffect(id) {
                            if (id != -1) {
                                subscription = viewModel.getSubscription(id!!)
                            }
                        }

                        AddSubscriptionScreen(
                            existingSubscription = subscription,
                            onSave = { entity ->

                                if (id == -1) {
                                    viewModel.saveSubscription(
                                        name = entity.name,
                                        price = entity.price,
                                        currency = entity.currency,
                                        billingCycle = entity.billingCycle,
                                        category = entity.category,
                                        startDate = entity.startDate,
                                        reminderEnabled = entity.reminderEnabled,
                                        subscriptionType = entity.subscriptionType,
                                        logoId = entity.logoResId,
                                        key = entity.key
                                    )
                                } else {
                                    viewModel.updateSubscription(entity)
                                }

                               navController.popBackStack()
                            }
                            ,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("view_all_renewals") {
                        val context = LocalContext.current

                        val db = DatabaseProvider.getDatabase(context)

                        val smsDataSource = SmsDataSource(context)
                        val repository = remember {
                            SubscriptionRepository(db.subscriptionDao(), db.userDao(), context, smsDataSource)
                        }
                        val viewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModelFactory(repository)
                        )
                        val state by viewModel.uiState.collectAsState()

                        ViewAllScreen(
                            title = "Upcoming Renewals",
                            renewals = state?.upcomingRenewals,
                            onBack = {
                                navController.popBackStack()
                            },
                            viewModel
                        )
                    }

                    composable("view_all_free_trials") {

                        val state by viewModel.uiState.collectAsState()

                        ViewAllScreen(
                            title = "Free Trials",
                            renewals = state?.freeTrials,
                            onBack = {
                                navController.popBackStack()
                            },
                            viewModel
                        )
                    }

                    composable("renewals") {
                        SubscriptionScreen(navController)
                    }

                    composable("premium") {
                        PremiumPlanScreen(onClose = {
                            navController.popBackStack()
                        }, onContinue = {
                            navController.navigate("dashboard")
                        })
                    }

                    composable("profile") {
                        val state by viewModel.uiState.collectAsState()
                        ProfileScreen(state?.user){

                        }
                    }


                }

            }

        }
    }
}

