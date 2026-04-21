package com.tracker.subscription

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.compose.material3.Icon
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.tracker.subscription.Utility.calculateNextBillingDate
import com.tracker.subscription.data.SubscriptionType
import com.tracker.subscription.data.db.SubscriptionDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

class SubscriptionReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val id = inputData.getInt("id", -1)
        val dao = SubscriptionDatabase
            .getDatabase(applicationContext)
            .subscriptionDao()
        val sub = dao.getSubscription(id) ?: return Result.success()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val daysLeft =
            TimeUnit.MILLISECONDS.toDays(sub.nextBillingDate - today)
        val remindBefore = sub.reminderDaysBefore.coerceIn(1, 30).toLong()

        Log.d("SDFS", "doWork: "+remindBefore)
        if (daysLeft in 0..remindBefore) {
            showNotification(
                id,
                sub.name,
                sub.price,
                sub.subscriptionType,
                daysLeft,
                sub.logoResId
            )        }
        if (daysLeft < -2) return Result.success()

        if (daysLeft > 0) {

            // keep checking tomorrow
            scheduleTomorrow(id)

        } else {

            // trial should not repeat
            if (sub.subscriptionType == SubscriptionType.FREE_TRIAL.value) {
                return Result.success()

            }

            // paid subscription → calculate next cycle
            val nextDate =
                calculateNextBillingDate(
                    sub.nextBillingDate,
                    sub.billingCycle,
                    sub.subscriptionType
                )

            dao.updateNextBillingDate(id, nextDate)

            ReminderScheduler.scheduleReminder(
                applicationContext,
                id,
                nextDate,
                sub.reminderDaysBefore
            )
        }

        return Result.success()
    }

    fun scheduleTomorrow(id: Int) {

        val request =
            OneTimeWorkRequestBuilder<SubscriptionReminderWorker>()
                .setInitialDelay(1, TimeUnit.DAYS)
                .setInputData(workDataOf("id" to id))
                .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "subscription_daily_$id",
                ExistingWorkPolicy.REPLACE,
                request
            )
    }


    private fun showNotification(
        id: Int,
        name: String,
        price: Double,
        subscriptionType: String,
        daysLeft: Long,
        icon: Int?
    ) {

        val manager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE)
                    as NotificationManager

        val channelId = "subscription_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Subscription Reminder",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val title: String
        val message: String

        if (subscriptionType == SubscriptionType.FREE_TRIAL.value) {

            title = "Free Trial Ending"

            message = when (daysLeft) {
                0L -> "$name trial ends today"
                1L -> "$name trial ends tomorrow"
                else -> "$name trial ends in $daysLeft days"
            }

        } else {

            title = "Subscription Renewal Alert"

            message = when (daysLeft) {
                0L -> "$name renews today • ₹$price will be charged"
                1L -> "$name renews tomorrow • ₹$price will be charged"
                else -> "$name renews in $daysLeft days • ₹$price will be charged"
            }
        }


        val intent = Intent(applicationContext, MainActivity::class.java)

        val pendingIntent = PendingIntent.getActivity(
            applicationContext,
            id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification =
            NotificationCompat.Builder(applicationContext, channelId)
                .setSmallIcon(icon?: R.drawable.ic_launcher)
                .setLargeIcon(
                    BitmapFactory.decodeResource(
                        applicationContext.resources,
                        icon ?: R.drawable.ic_launcher
                    )
                )
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .build()

        manager.notify(id, notification)
    }



}