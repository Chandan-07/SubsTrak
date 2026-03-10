package com.example.subscription.ui.data

data class OnboardingPage(
    val title: String,
    val description: String
)

val pages = listOf(
    OnboardingPage(
        "Track All Subscriptions",
        "Manage Netflix, Spotify and all services in one place."
    ),
    OnboardingPage(
        "Never Miss Renewal",
        "Get reminders before subscriptions renew."
    ),
    OnboardingPage(
        "Control Your Spending",
        "See how much you spend monthly on subscriptions."
    )
)