package com.tracker.subscription.presentation

import com.tracker.subscription.data.Option

object CommonOptions {

    val billing = listOf(
        Option("Weekly", "📅"),
        Option("Monthly", "🗓️"),
        Option("Yearly", "📆")
    )

    val subscriptionType = listOf(
        Option("Paid Subscription", "💳"),
        Option("Free Trial", "🆓")
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