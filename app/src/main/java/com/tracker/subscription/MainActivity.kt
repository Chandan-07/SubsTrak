package com.tracker.subscription

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.google.firebase.auth.FirebaseAuth
import com.tracker.subscription.auth.GoogleAuthHelper
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.db.DatabaseProvider
import com.tracker.subscription.data.db.OnboardingPreference
import com.tracker.subscription.data.repo.BillingRepository
import com.tracker.subscription.data.repo.SubscriptionRepository
import com.tracker.subscription.presentation.AddSubscriptionViewModel
import com.tracker.subscription.presentation.AddSubscriptionViewModelFactory
import com.tracker.subscription.presentation.DashboardViewModel
import com.tracker.subscription.presentation.DashboardViewModelFactory
import com.tracker.subscription.presentation.PremiumViewModel
import com.tracker.subscription.presentation.PremiumViewModelFactory
import com.tracker.subscription.screens.addSub.AddSubscriptionScreen
import com.tracker.subscription.screens.onboard.AuthScreen
import com.tracker.subscription.screens.home.DashboardScreen
import com.tracker.subscription.screens.onboard.OnboardingScreen
import com.tracker.subscription.screens.PremiumPlanScreen
import com.tracker.subscription.screens.ProfileScreen
import com.tracker.subscription.screens.SubscriptionScreen
import com.tracker.subscription.screens.home.DashboardUiState
import com.tracker.subscription.screens.home.ViewAllScreen
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

            val isLoggedIn by OnboardingPreference
                .isLoggedIn(context)
                .collectAsState(initial = false)


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
                AnimatedVisibility(
                    visible = currentRoute in bottomBarRoutes,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    NavigationBar(
                        containerColor = Color.White,
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .shadow(
                                16.dp,
                                RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
                            )
                    ) {
                        bottomItems.forEach { item ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(item.icon),
                                        contentDescription = item.label
                                    )
                                },
                                selected = currentRoute == item.route,
                                onClick = {
                                    navController.navigate(item.route) {
                                        popUpTo("dashboard")
                                        launchSingleTop = true
                                        restoreState = true   // 👈 important
                                    }
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color(0xFF1565C0),
                                    unselectedIconColor = Color.Gray,
                                    indicatorColor = Color(0xFFE3F2FD)
                                )
                            )
                        }
                    }
                }
            }) { innerPadding ->

                val bottomPadding by animateDpAsState(
                    targetValue = if (currentRoute in bottomBarRoutes) 80.dp else 20.dp,
                    label = ""
                )
                    NavHost(
                    navController = navController,
                    startDestination = destination,
                    modifier = Modifier
                        .padding(
                            start = innerPadding.calculateStartPadding(LayoutDirection.Ltr),
                            end = innerPadding.calculateEndPadding(LayoutDirection.Ltr),
                            bottom = bottomPadding
                        )
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
                            viewModel.setLoading(true)
                            if (result.resultCode == Activity.RESULT_OK) {

                                coroutineScope.launch {

                                    try {
                                        val user = googleAuthClient.signInWithIntent(result.data!!)
                                        viewModel.setUser(user)
                                        scope.launch {
                                            OnboardingPreference.setLoggedIn(context, true)
                                            viewModel.setLoggedIn(true)
                                        }
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
                        val state by viewModel.uiState.collectAsState()

                        DashboardScreen(
                            isLoggedIn,
                            navController = navController,
                            onAddSubscription = {

                                val subsCount = (state as? DashboardUiState.Success)
                                    ?.data
                                    ?.subscriptions
                                    ?.size ?: 0

                                if (subsCount == 5) {
                                    navController.navigate("premium")
                                } else {
                                    navController.navigate("add_subscription")
                                }
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
                        ), enterTransition = {
                            fadeIn(animationSpec = tween(300))
                        },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }
                    ) { backStackEntry ->

                        val id = backStackEntry.arguments?.getInt("id")

                        val addSubViewModel: AddSubscriptionViewModel = viewModel(
                            factory = AddSubscriptionViewModelFactory(repository)
                        )
                        var subscription by remember { mutableStateOf<Subscription?>(null) }

                        LaunchedEffect(id) {
                            if (id != -1) {
                                subscription = addSubViewModel.getSubscription(id!!)
                            }
                        }

                        AddSubscriptionScreen(
                            existingSubscription = subscription,
                            onSave = { entity ->

                                if (id == -1) {
                                    addSubViewModel.saveSubscription(
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
                                    addSubViewModel.updateSubscription(entity)
                                }

                               navController.popBackStack()
                            }
                            ,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable(route="view_all_renewals",
                        enterTransition = {
                            slideInHorizontally { it }
                        },
                        exitTransition = {
                           slideOutHorizontally { it }
                        }) {
                        val state by viewModel.uiState.collectAsState()

                        val renewals = (state as? DashboardUiState.Success)
                            ?.data
                            ?.upcomingRenewals ?: emptyList()

                        ViewAllScreen(
                            title = "Upcoming Renewals",
                            renewals = renewals,
                            onBack = {
                                navController.popBackStack()
                            },
                            viewModel,
                            navController
                        )
                    }

                    composable("view_all_free_trials",enterTransition = {
                        slideInHorizontally { it }
                    },
                        exitTransition = {
                            slideOutHorizontally { it }
                        }) {

                        val state by viewModel.uiState.collectAsState()

                        val freeTrials = (state as? DashboardUiState.Success)
                            ?.data
                            ?.freeTrials ?: emptyList()

                        ViewAllScreen(
                            title = "Free Trials",
                            renewals = freeTrials,
                            onBack = {
                                navController.popBackStack()
                            },
                            viewModel,
                            navController
                        )
                    }

                    composable("renewals", enterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }) {
                        SubscriptionScreen(isLoggedIn, navController)
                    }

                    composable("premium",enterTransition = {
                        slideInHorizontally { it }
                    },
                        exitTransition = {
                            slideOutHorizontally { it }
                        }) {
                        val context = LocalContext.current

                        val db = DatabaseProvider.getDatabase(context)


                        val premiumRepository = remember {
                            BillingRepository( context, db.userDao())
                        }
                        val premiumViewModel: PremiumViewModel = viewModel(
                            factory = PremiumViewModelFactory(premiumRepository)
                        )
                        PremiumPlanScreen(viewModel = premiumViewModel, onClose = {
                            navController.popBackStack()
                        })
                    }

                    composable("profile", enterTransition = {
                        fadeIn(animationSpec = tween(300))
                    },
                        exitTransition = {
                            fadeOut(animationSpec = tween(300))
                        }) {

                        val isLoading = viewModel.isSigningIn
                        val coroutineScope = rememberCoroutineScope()

                        val googleAuthClient = remember {
                            GoogleAuthHelper(context)
                        }
                        val state by viewModel.uiState.collectAsState()
                        val user = if (isLoggedIn) {
                            (state as? DashboardUiState.Success)?.data?.user
                        } else null
                            ProfileScreen(
                                user = user,
                                onSignOut = {
                                    viewModel.setLoading(true)

                                    coroutineScope.launch {
                                        try {
                                            googleAuthClient.signOut()
                                            FirebaseAuth.getInstance().signOut()
                                            OnboardingPreference.setLoggedIn(context, false)
                                            viewModel.setLoggedIn(false)
                                            navController.navigate("auth") {
                                                popUpTo(0)
                                            }

                                        } catch (e: Exception) {

                                            // 🔥 Show error
                                            Toast.makeText(
                                                context,
                                                "Something went wrong. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()

                                        } finally {
                                            Toast.makeText(
                                                context,
                                                "Something went wrong. Please try again.",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            viewModel.setLoading(false) // ✅ always stop loading
                                        }
                                    }
                                },
                                onLogin = {
                                    navController.navigate("auth")
                                }
                            )

                    }


                }

            }

        }
    }
}

