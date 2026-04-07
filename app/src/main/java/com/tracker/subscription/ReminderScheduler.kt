package com.tracker.subscription

import android.content.Context
import android.util.Log
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.util.Calendar
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {

    fun scheduleReminder(
        context: Context,
        subscriptionId: Int,
        nextBillingDate: Long
    ) {

        val reminderTime = calculateReminderTime(nextBillingDate)

        val delay = max(0, reminderTime - System.currentTimeMillis())

        val request =
            OneTimeWorkRequestBuilder<SubscriptionReminderWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .setInputData(
                    workDataOf("id" to subscriptionId)
                )
                .build()

        WorkManager.getInstance(context)
            .enqueueUniqueWork(
                "subscription_$subscriptionId",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }

    private fun calculateReminderTime(nextBillingDate: Long): Long {

        val calendar = Calendar.getInstance()
        val now = System.currentTimeMillis()

        calendar.timeInMillis = nextBillingDate

        calendar.add(Calendar.DAY_OF_MONTH, -1)

        calendar.set(Calendar.HOUR_OF_DAY, 10)
        calendar.set(Calendar.MINUTE, 18)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        if (calendar.timeInMillis <= now) {
            calendar.timeInMillis = now + TimeUnit.SECONDS.toMillis(30)
        }

        return calendar.timeInMillis
    }

    fun cancelReminder(context: Context, id: Int) {
        WorkManager.getInstance(context)
            .cancelUniqueWork("subscription_$id")
    }
}