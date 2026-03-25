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
            Log.d("REMINDER", "Worker triggered")
        val id = inputData.getInt("id", -1)
        Log.d("REMINDER", "id "+ id)

        val dao = SubscriptionDatabase
            .getDatabase(applicationContext)
            .subscriptionDao()
        Log.d("REMINDER", "dao.getSubscription(id)"+ dao.getSubscription(id))

        val sub = dao.getSubscription(id) ?: return Result.success()

        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val daysLeft =
            TimeUnit.MILLISECONDS.toDays(sub.nextBillingDate - today)
        // Send notification 2 days before
        Log.d("ASJKBNDJ", "daysLeft: "+daysLeft+ sub.subscriptionType)

        if (daysLeft in 0..2) {
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
            Log.d("ASJKBNDJ", "doWork: "+daysLeft+ sub.subscriptionType)
            scheduleTomorrow(id)

        } else {

            // trial should not repeat
            if (sub.subscriptionType == SubscriptionType.FREE_TRIAL.value) {
                Log.d("ASJKBNDJ", "free trial: "+sub.subscriptionType)

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
                nextDate
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

            title = "Subscription Renewal "

            message = when (daysLeft) {
                0L -> "$name renews today • ₹$price"
                1L -> "$name renews tomorrow • ₹$price"
                else -> "$name renews in $daysLeft days • ₹$price"
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