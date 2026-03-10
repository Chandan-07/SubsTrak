package com.example.subscription

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.room.Room
import com.example.subscription.data.Subscription
import com.example.subscription.data.dao.SubscriptionEntity
import com.example.subscription.data.db.DatabaseProvider
import com.example.subscription.data.db.SubscriptionDatabase
import com.example.subscription.data.repo.SubscriptionRepository
import com.example.subscription.presentation.AddSubscriptionViewModel
import com.example.subscription.presentation.AddSubscriptionViewModelFactory
import com.example.subscription.screens.AddSubscriptionScreen
import com.example.subscription.screens.AuthScreen
import com.example.subscription.screens.DashboardScreen
import com.example.subscription.screens.OnboardingScreen
import com.example.subscription.ui.theme.SubscriptionTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SubscriptionTheme {

                val navController = rememberNavController()

                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->

                    NavHost(
                        navController = navController,
                        startDestination = "onboarding",
                        modifier = Modifier.padding(innerPadding)
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
                                SubscriptionRepository(db.subscriptionDao())
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
                                        viewModel.addSubscription(entity)
                                    } else {
                                        viewModel.updateSubscription(entity)
                                    }

                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}