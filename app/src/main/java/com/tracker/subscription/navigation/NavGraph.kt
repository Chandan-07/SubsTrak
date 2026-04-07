package com.tracker.subscription.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.tracker.subscription.screens.AuthScreen
import com.tracker.subscription.screens.OnboardingScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "onboarding"
    ) {

        composable("onboarding") {

            OnboardingScreen(
                onGetStarted = {
                    navController.navigate("auth")
                }
            )
        }


    }
}