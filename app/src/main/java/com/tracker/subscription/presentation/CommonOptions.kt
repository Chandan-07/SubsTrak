package com.tracker.subscription.presentation

import com.tracker.subscription.data.Option

object CommonOptions {

    val billing = listOf(
        Option("Weekly", "📅"),
        Option("Monthly", "🗓️"),
        Option("Yearly", "📆")
    )

    val subscriptionType = listOf(
        Option("Free Trial", "🆓"),
        Option("Paid Subscription", "💳")
    )

    val freeTrial = listOf(
        Option("7 days", "📅"),
        Option("14 days", "🗓️"),
        Option("30 days", "📆"),
        Option("custom", "📆")
    )

    val categoryList = listOf(
        Option("Entertainment", "🎬"),
        Option("Work", "💼"),
        Option("Health", "💪"),
        Option("Education", "📚"),
        Option("Finance", "💳"),
        Option("Food", "🍔"),
        Option("Travel", "✈️"),
        Option("Shopping", "🛍️"),
        Option("Apps", "📱"),
        Option("Other", "❓")
    )
}