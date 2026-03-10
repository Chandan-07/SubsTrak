package com.example.subscription

import java.util.Calendar

object Utility {
    fun calculateNextBillingDate(
        startDate: Long,
        billingCycle: String
    ): Long {

        val calendar = Calendar.getInstance()
        calendar.timeInMillis = startDate

        when (billingCycle) {

            "Monthly" -> calendar.add(Calendar.MONTH, 1)

            "Yearly" -> calendar.add(Calendar.YEAR, 1)
        }

        return calendar.timeInMillis
    }
}