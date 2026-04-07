package com.tracker.subscription

import android.util.Log
import com.tracker.subscription.data.SubscriptionType
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale
import java.util.concurrent.TimeUnit

object Utility {
    fun calculateNextBillingDate(
        startDate: Long,
        billingCycle: String,
        subscriptionType: String
    ): Long {

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate

        if (subscriptionType == SubscriptionType.PAID_SUBSCRIPTION.value){
            when (billingCycle) {

                "Monthly" -> calendar.add(Calendar.MONTH, 1)

                "Weekly" -> calendar.add(Calendar.WEEK_OF_MONTH, 1)

                "Yearly" -> calendar.add(Calendar.YEAR, 1)
            }
        }
        return calendar.timeInMillis
    }

    fun formatCurrency(amount: Double): String {
        val format = NumberFormat.getCurrencyInstance(
            Locale.forLanguageTag("en-IN")
        )

        // 🔥 Remove .00 if no decimals
        if (amount % 1.0 == 0.0) {
            format.maximumFractionDigits = 0
        } else {
            format.maximumFractionDigits = 2
        }

        return format.format(amount)
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        return when (hour) {
            in 5..11 -> "Good Morning ☀️"
            in 12..16 -> "Good Afternoon 🌤️"
            in 17..20 -> "Good Evening 🌇"
            else -> "Good Night 🌙"
        }
    }

    fun getDaysLeft(date: Long): Int {

        val diff =
            date - System.currentTimeMillis()

        return TimeUnit.MILLISECONDS
            .toDays(diff)
            .toInt()
    }



}