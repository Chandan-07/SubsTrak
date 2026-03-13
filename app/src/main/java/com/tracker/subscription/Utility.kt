package com.tracker.subscription

import android.util.Log
import com.tracker.subscription.data.SubscriptionType
import java.util.Calendar

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

        Log.d("ASJKBNDJ", "calculateNextBillingDate: "+subscriptionType)

        return calendar.timeInMillis
    }
}