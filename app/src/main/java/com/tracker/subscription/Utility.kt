package com.tracker.subscription

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.ui.graphics.Color
import com.tracker.subscription.data.SubscriptionType
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

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

    fun formatCurrency(amount: Double, symbol: String): String {
        val format = NumberFormat.getNumberInstance()

        format.minimumFractionDigits = 0
        format.maximumFractionDigits = if (amount % 1.0 == 0.0) 0 else 2

        return "$symbol ${format.format(amount)}"
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun renewalDateText(date: Long, subscriptionType: String): String {

        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        val formattedDate = sdf.format(Date(date))
        val today = LocalDate.now()
        val targetDate = Instant.ofEpochMilli(date)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val isPast = targetDate.isBefore(today)

        return if (subscriptionType == SubscriptionType.FREE_TRIAL.value) {
            if (isPast) {
                "Trial ended on $formattedDate"
            } else {
                "Trial ends on $formattedDate"
            }
        } else {
            if (isPast) {
                "Renewed on $formattedDate" // 🔥 FIX
            } else {
                "Renews on $formattedDate"
            }
        }
    }
    fun remainingTimeText(daysLeft: Int, subscriptionType: String): String {

        val absDays = abs(daysLeft)

        fun formatPast(days: Int): String {
            return when {
                days >= 365 -> {
                    val years = days / 365
                    if (years == 1) "1 year ago" else "$years years ago"
                }
                days >= 30 -> {
                    val months = days / 30
                    if (months == 1) "1 month ago" else "$months months ago"
                }
                else -> {
                    if (days == 1) "1 day ago" else "$days days ago"
                }
            }
        }

        fun formatFuture(days: Int): String {
            return when {
                days >= 365 -> {
                    val years = days / 365
                    if (years == 1) "1 year left" else "$years years left"
                }
                days >= 30 -> {
                    val months = days / 30
                    if (months == 1) "1 month left" else "$months months left"
                }
                else -> {
                    if (days == 1) "1 day left" else "$days days left"
                }
            }
        }

        // 🔹 FREE TRIAL
        if (subscriptionType == SubscriptionType.FREE_TRIAL.value) {
            return when {
                daysLeft < 0 -> "Trial ended ${formatPast(absDays)}"
                daysLeft == 0 -> "Ends today"
                daysLeft == 1 -> "Ends tomorrow"
                else -> formatFuture(daysLeft)
            }
        }

        // 🔹 PAID SUBSCRIPTION
        return when {
            daysLeft < 0 -> "Renewed ${formatPast(absDays)}"
            daysLeft == 0 -> "Renews today"
            daysLeft == 1 -> "Renews tomorrow"
            else -> formatFuture(daysLeft)
        }
    }



    fun renewalColor(daysLeft: Int): Color {
        return when {
            daysLeft <= 2 -> Color(0xFFE53935)   // Red
            daysLeft <= 7 -> Color(0xFFFB8C00)   // Orange
            else -> Color.Gray
        }
    }

    fun randomColor(): Color {
        val colors = listOf(
            Color(0xFF3D5AFE), // Blue
            Color(0xFF00C853), // Green
            Color(0xFFFF6D00), // Orange
            Color(0xFFD500F9), // Purple
            Color(0xFFFF4081), // Pink
            Color(0xFF00BFA5), // Teal
            Color(0xFFFFAB00)  // Amber
        )
        return colors.random()
    }


}