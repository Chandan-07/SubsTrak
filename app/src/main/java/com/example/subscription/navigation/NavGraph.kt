package com.example.subscription.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.*
import com.example.subscription.screens.AuthScreen
import com.example.subscription.screens.OnboardingScreen

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

        composable("auth") {

            AuthScreen(
                onGoogleSignIn = {
                    // TODO Google Sign In
                },
                onSkip = {
                    // Navigate to dashboard later
                }
            )
        }
    }
}