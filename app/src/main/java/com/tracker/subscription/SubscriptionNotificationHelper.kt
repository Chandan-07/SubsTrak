package com.tracker.subscription

import android.app.PendingIntent
import android.content.Context
import android.content.Intent

object SubscriptionNotificationHelper {

    const val EXTRA_SUBSCRIPTION_ID = "subscription_id"
    const val EXTRA_ACTION = "notification_action"
    const val ACTION_SHOW_SHEET = "show_options_sheet"

    fun contentPendingIntent(context: Context, subscriptionId: Int): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(EXTRA_SUBSCRIPTION_ID, subscriptionId)
            putExtra(EXTRA_ACTION, ACTION_SHOW_SHEET)
        }
        return PendingIntent.getActivity(
            context,
            subscriptionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    fun shouldShowOptionsSheet(intent: Intent?): Boolean {
        return intent?.getStringExtra(EXTRA_ACTION) == ACTION_SHOW_SHEET
    }

    fun subscriptionIdFrom(intent: Intent?): Int? {
        val id = intent?.getIntExtra(EXTRA_SUBSCRIPTION_ID, -1) ?: -1
        return id.takeIf { it != -1 }
    }

    fun clearNotificationExtras(intent: Intent?) {
        intent?.removeExtra(EXTRA_ACTION)
        intent?.removeExtra(EXTRA_SUBSCRIPTION_ID)
    }
}
