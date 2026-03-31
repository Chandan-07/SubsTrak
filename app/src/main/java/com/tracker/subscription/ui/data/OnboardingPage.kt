package com.tracker.subscription.ui.data

import com.tracker.subscription.R

data class OnboardingPage(
    val title: String,
    val description: String,
    val image: Int = R.drawable.launch_big
)

val pages = listOf(
    OnboardingPage(
        "Track All Subscriptions",
        "Manage all your services in one place.",
        R.drawable.launch_big
    ),
    OnboardingPage(
        "Never Miss Any Renewals",
        "Get reminders before subscriptions renew.",
        R.drawable.notification_bell
    ),
    OnboardingPage(
        "Control Your Spending",
        "See how much you spend monthly on subscriptions.",
        R.drawable.analytics
    )
)