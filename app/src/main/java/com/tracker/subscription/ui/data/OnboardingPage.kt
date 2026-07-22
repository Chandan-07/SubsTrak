package com.tracker.subscription.ui.data

import com.tracker.subscription.R

data class OnboardingPage(
    val title: String,
    val description: String,
    val image: Int = R.drawable.launch_big
)

val pages = listOf(
    OnboardingPage(
        "Subscription Management, Simplified",
        "See every recurring charge in one beautiful dashboard. Stop guessing where your money goes.",
        R.drawable.onboard_title
    ),
    OnboardingPage(
        "Never Pay More Than You Should",
        "Get smart, proactive alerts before free trials end and expensive renewals hit your card.",
        R.drawable.notification_svg
    ),
    OnboardingPage(
        "Save More with Every Cancellation",
        "Pause or cancel unused services with a swipe. Instantly unlock hidden savings.",
        R.drawable.analytics
    )
)