package com.tracker.subscription.screens.calendar

import com.tracker.subscription.Utility
import com.tracker.subscription.data.Subscription
import com.tracker.subscription.data.SubscriptionType
import java.util.Calendar

/**
 * Maps subscriptions onto calendar days using the same fields as
 * [com.tracker.subscription.SubscriptionReminderWorker]: [Subscription.nextBillingDate],
 * [Subscription.name], [Subscription.logoResId], plus billing cycle for recurring paid plans.
 */
object SubscriptionRenewalCalendar {

    data class DayEvent(
        val subscriptionId: String,
        val name: String,
        val logoResId: Int?
    )

    fun renewalEventsForMonth(
        subscriptions: List<Subscription>,
        year: Int,
        monthIndex: Int // Calendar.JANUARY .. Calendar.DECEMBER
    ): Map<Long, List<DayEvent>> {
        val monthStart = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(Calendar.DAY_OF_MONTH, 1)
            clearTime()
        }
        val monthEnd = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, monthIndex)
            set(
                Calendar.DAY_OF_MONTH,
                getActualMaximum(Calendar.DAY_OF_MONTH)
            )
            clearTime()
        }
        val startMs = monthStart.timeInMillis
        val endMs = monthEnd.timeInMillis

        val result = mutableMapOf<Long, MutableList<DayEvent>>()

        for (sub in subscriptions) {
            val nextMs = normalizeDay(sub.nextBillingDate)
            when (sub.subscriptionType) {
                SubscriptionType.FREE_TRIAL.value -> {
                    if (nextMs in startMs..endMs) {
                        result.getOrPut(nextMs) { mutableListOf() }.add(
                            DayEvent(sub.id, sub.name, sub.logoResId)
                        )
                    }
                }

                SubscriptionType.PAID_SUBSCRIPTION.value -> {
                    var cursor = nextMs
                    var guard = 0
                    while (cursor > endMs && guard < 600) {
                        cursor = stepBillingMillis(cursor, sub.billingCycle, forward = false)
                        guard++
                    }
                    guard = 0
                    while (cursor < startMs && guard < 600) {
                        cursor = Utility.calculateNextBillingDate(
                            cursor,
                            sub.billingCycle,
                            sub.subscriptionType
                        )
                        guard++
                    }
                    guard = 0
                    while (cursor in startMs..endMs && guard < 600) {
                        result.getOrPut(cursor) { mutableListOf() }.add(
                            DayEvent(sub.id, sub.name, sub.logoResId)
                        )
                        cursor = Utility.calculateNextBillingDate(
                            cursor,
                            sub.billingCycle,
                            sub.subscriptionType
                        )
                        guard++
                    }
                }
            }
        }

        return result
    }

    private fun normalizeDay(timeInMillis: Long): Long {
        val c = Calendar.getInstance().apply {
            this.timeInMillis = timeInMillis
            clearTime()
        }
        return c.timeInMillis
    }

    private fun Calendar.clearTime() {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    /**
     * Inverse of [Utility.calculateNextBillingDate] for paid subscriptions (same cycle rules).
     */
    private fun stepBillingMillis(fromMillis: Long, billingCycle: String, forward: Boolean): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = fromMillis
        }
        val delta = if (forward) 1 else -1
        when (billingCycle) {
            "Monthly" -> calendar.add(Calendar.MONTH, delta)
            "Weekly" -> calendar.add(Calendar.WEEK_OF_MONTH, delta)
            "Yearly" -> calendar.add(Calendar.YEAR, delta)
            else -> calendar.add(Calendar.MONTH, delta)
        }
        calendar.clearTime()
        return calendar.timeInMillis
    }
}
