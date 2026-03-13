package com.tracker.subscription

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tracker.subscription.data.Subscription
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
import com.tracker.subscription.screens.ViewAllScreen
import com.tracker.subscription.ui.theme.SubscriptionTheme
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.Box as Box1

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

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
            val destination = when{
                isAuthSkipped == true -> "dashboard"
                onboardingCompleted == true -> "auth"
                else -> "onboarding"
            }

            if (onboardingCompleted == null || isAuthSkipped == null) {
                Box1(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@setContent
            }


            SubscriptionTheme {

                NavHost(
                    navController = navController,
                    startDestination = destination,
                ) {

                    composable("onboarding") {

                        OnboardingScreen(
                            onGetStarted = {
                                navController.navigate("auth")
                            }
                        )
                    }

                    composable("auth") {

                        AuthScreen(
                            onGoogleSignIn = {
                                // TODO Google login
                            },
                            onSkip = {
                                scope.launch {
                                    OnboardingPreference.setAuthSkipped(context)
                                }
                                navController.navigate("dashboard")
                            }
                        )
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


                        val repository = remember {
                            SubscriptionRepository(db.subscriptionDao(), context)
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
                                        subscriptionType = entity.subscriptionType
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

                        val repository = remember {
                            SubscriptionRepository(db.subscriptionDao(), context)
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
                            }
                        )
                    }

                    composable("view_all_free_trials") {
                        val context = LocalContext.current

                        val db = DatabaseProvider.getDatabase(context)

                        val repository = remember {
                            SubscriptionRepository(db.subscriptionDao(), context)
                        }
                        val viewModel: DashboardViewModel = viewModel(
                            factory = DashboardViewModelFactory(repository)
                        )
                        val state by viewModel.uiState.collectAsState()

                        ViewAllScreen(
                            title = "Upcoming Renewals",
                            renewals = state?.freeTrials,
                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}