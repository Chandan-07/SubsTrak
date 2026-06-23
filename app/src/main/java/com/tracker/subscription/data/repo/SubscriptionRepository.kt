package com.tracker.subscription.data.repo

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.tracker.subscription.R
import com.tracker.subscription.ReminderScheduler
import com.tracker.subscription.data.AuthUser
import com.tracker.subscription.data.ParsedSubscription
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.SubscriptionPrice
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.dao.SubscriptionDao
import com.tracker.subscription.data.dao.SubscriptionEntity
import com.tracker.subscription.data.dao.UserDao
import com.tracker.subscription.data.dao.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class SubscriptionRepository(
    private val dao: SubscriptionDao,
    private val userDao: UserDao,
    private val context: Context,
    private val smsDataSource: SmsDataSource,
) {

    val services = listOf(

        // OTT
        Service(
            "netflix",
            "Netflix",
            R.drawable.netflix,
            "com.netflix.mediaclient",
            "OTT",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 649.0),
                SubscriptionPrice("US", "$", 22.99),
                SubscriptionPrice("UK", "£", 17.99)
            )
        ),

        Service(
            "youtube",
            "YouTube Premium",
            R.drawable.youtube,
            "com.google.android.youtube",
            "OTT",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 149.0),
                SubscriptionPrice("US", "$", 13.99),
                SubscriptionPrice("UK", "£", 11.99)
            )
        ),


        Service(
            "prime",
            "Amazon Prime Video",
            R.drawable.prime,
            "com.amazon.avod.thirdpartyclient",
            "OTT",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 299.0),
                SubscriptionPrice("US", "$", 14.99),
                SubscriptionPrice("UK", "£", 8.99)
            )
        ),

        Service(
            "hotstar",
            "Disney+ Hotstar",
            R.drawable.jiohotstar,
            "in.startv.hotstar",
            "OTT",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 299.0)
            )
        ),

        Service(
            "zee5",
            "ZEE5",
            R.drawable.zee,
            "com.graymatrix.did",
            "OTT",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 199.0)
            )
        ),

        Service(
            "appletv",
            "Apple TV+",
            R.drawable.apple_tv,
            "com.apple.atve.androidtv.appletv",
            "OTT",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 99.0),
                SubscriptionPrice("US", "$", 9.99),
                SubscriptionPrice("UK", "£", 8.99)
            )
        ),

        Service(
            "discovery",
            "Discovery+",
            R.drawable.discovery,
            "com.discovery.discoveryplus.mobile",
            "OTT",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 299.0),
                SubscriptionPrice("US", "$", 8.99)
            )
        ),

        // MUSIC
        Service(
            "spotify",
            "Spotify",
            R.drawable.spotify,
            "com.spotify.music",
            "Music",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 119.0),
                SubscriptionPrice("US", "$", 11.99),
                SubscriptionPrice("UK", "£", 10.99)
            )
        ),

        Service(
            "ytmusic",
            "YouTube Music",
            R.drawable.ytbmusic,
            "com.google.android.apps.youtube.music",
            "Music",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 119.0),
                SubscriptionPrice("US", "$", 10.99),
                SubscriptionPrice("UK", "£", 10.99)
            )
        ),

        // PRODUCTIVITY
        Service(
            "googleone",
            "Google One",
            R.drawable.google,
            "com.google.android.apps.subscriptions.red",
            "Productivity",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 130.0),
                SubscriptionPrice("US", "$", 1.99),
                SubscriptionPrice("UK", "£", 1.59)
            )
        ),

        Service(
            "dropbox",
            "Dropbox",
            R.drawable.dropbox,
            "com.dropbox.android",
            "Productivity",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 999.0),
                SubscriptionPrice("US", "$", 11.99),
                SubscriptionPrice("UK", "£", 9.99)
            )
        ),

        Service(
            "notion",
            "Notion",
            R.drawable.notion,
            "notion.id",
            "Productivity",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 399.0),
                SubscriptionPrice("US", "$", 10.0),
                SubscriptionPrice("UK", "£", 8.0)
            )
        ),

        Service(
            "evernote",
            "Evernote",
            R.drawable.evernote,
            "com.evernote",
            "Productivity",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 499.0),
                SubscriptionPrice("US", "$", 14.99)
            )
        ),

        Service(
            "photoshop",
            "Photoshop",
            R.drawable.photoshop,
            "com.adobe.psmobile",
            "Productivity",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 799.0),
                SubscriptionPrice("US", "$", 22.99)
            )
        ),

        // SOCIAL
        Service(
            "linkedin",
            "LinkedIn Premium",
            R.drawable.linkedin,
            "com.linkedin.android",
            "Social",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 899.0),
                SubscriptionPrice("US", "$", 39.99)
            )
        ),

        Service(
            "twitter",
            "X Premium",
            R.drawable.x,
            "com.twitter.android",
            "Social",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 650.0),
                SubscriptionPrice("US", "$", 8.0)
            )
        ),

        Service(
            "instagram",
            "Instagram+",
            R.drawable.instagram,
            "com.instagram.android",
            "Social",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 299.0)
            )
        ),

        // DATING
        Service(
            "tinder",
            "Tinder",
            R.drawable.tinder,
            "com.tinder",
            "Dating",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 499.0),
                SubscriptionPrice("US", "$", 19.99)
            )
        ),

        Service(
            "bumble",
            "Bumble",
            R.drawable.bumble,
            "com.bumble.app",
            "Dating",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 599.0),
                SubscriptionPrice("US", "$", 24.99)
            )
        ),

        Service(
            "hinge",
            "Hinge",
            R.drawable.hinge,
            "co.hinge.app",
            "Dating",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 699.0),
                SubscriptionPrice("US", "$", 29.99)
            )
        ),

        Service(
            "okcupid",
            "OkCupid",
            R.drawable.okcupid,
            "com.okcupid.okcupid",
            "Dating",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 499.0),
                SubscriptionPrice("US", "$", 19.99)
            )
        ),

        // LEARNING
        Service(
            "duolingo",
            "Duolingo",
            R.drawable.duolingo,
            "com.duolingo",
            "Learning",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 999.0),
                SubscriptionPrice("US", "$", 12.99)
            )
        ),

        Service(
            "coursera",
            "Coursera",
            R.drawable.coursera,
            "org.coursera.android",
            "Learning",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 3999.0),
                SubscriptionPrice("US", "$", 59.0)
            )
        ),

        Service(
            "udemy",
            "Udemy",
            R.drawable.udemy,
            "com.udemy.android",
            "Learning",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 849.0),
                SubscriptionPrice("US", "$", 29.99)
            )
        ),

        Service(
            "skillshare",
            "Skillshare",
            R.drawable.skillshare,
            "com.skillshare.Skillshare",
            "Learning",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 599.0),
                SubscriptionPrice("US", "$", 13.99)
            )
        ),

        Service(
            "unacademy",
            "Unacademy",
            R.drawable.unacademy,
            "com.unacademyapp",
            "Learning",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 999.0)
            )
        ),

        // AI
        Service(
            "chatgpt",
            "ChatGPT Plus",
            R.drawable.chatgpt,
            "com.openai.chatgpt",
            "AI",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 1950.0),
                SubscriptionPrice("US", "$", 20.0),
                SubscriptionPrice("UK", "£", 20.0)
            )
        ),

        Service(
            "gemini",
            "Google Gemini",
            R.drawable.googlegemini,
            "com.google.android.apps.bard",
            "AI",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 1950.0),
                SubscriptionPrice("US", "$", 19.99)
            )
        ),

        // AI & CREATOR TOOLS

        Service(
            "midjourney",
            "Midjourney",
            R.drawable.midjourney,
            "com.midjourney.app",
            "AI",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 830.0),
                SubscriptionPrice("US", "$", 10.0),
                SubscriptionPrice("CA", "CA$", 14.0),
                SubscriptionPrice("TR", "₺", 350.0)
            )
        ),

        Service(
            "canva",
            "Canva Pro",
            R.drawable.canva,
            "com.canva.editor",
            "AI",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 500.0),
                SubscriptionPrice("US", "$", 14.99),
                SubscriptionPrice("CA", "CA$", 17.99),
                SubscriptionPrice("TR", "₺", 220.0)
            )
        ),

        Service(
            "capcut",
            "CapCut Pro",
            R.drawable.capcut,
            "com.lemon.lvoverseas",
            "AI",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 599.0),
                SubscriptionPrice("US", "$", 9.99),
                SubscriptionPrice("CA", "CA$", 12.99),
                SubscriptionPrice("TR", "₺", 180.0)
            )
        ),


        Service(
            "cursor",
            "Cursor",
            R.drawable.cursor,
            "com.cursor.editor",
            "Developer Tools",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 1650.0),
                SubscriptionPrice("US", "$", 20.0),
                SubscriptionPrice("CA", "CA$", 27.0),
                SubscriptionPrice("TR", "₺", 700.0)
            )
        ),


        Service(
            "grammarly",
            "Grammarly Premium",
            R.drawable.grammarly,
            "com.grammarly.android.keyboard",
            "Productivity",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 999.0),
                SubscriptionPrice("US", "$", 12.0),
                SubscriptionPrice("CA", "CA$", 16.0),
                SubscriptionPrice("TR", "₺", 400.0)
            )
        ),

        Service(
            "perplexity",
            "Perplexity Pro",
            R.drawable.perplexity,
            "ai.perplexity.app.android",
            "AI",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 1950.0),
                SubscriptionPrice("US", "$", 20.0),
                SubscriptionPrice("CA", "CA$", 27.0),
                SubscriptionPrice("TR", "₺", 800.0)
            )
        ),

        Service(
            "claude",
            "Claude Pro",
            R.drawable.claude,
            "com.anthropic.claude",
            "AI",
            prices = listOf(
                SubscriptionPrice("IN", "₹", 1950.0),
                SubscriptionPrice("US", "$", 20.0),
                SubscriptionPrice("CA", "CA$", 27.0),
                SubscriptionPrice("TR", "₺", 800.0)
            )
        )
    )


    fun fetchSubscriptionsFromSms(): List<ParsedSubscription> {

        val messages = smsDataSource.readSms()

        val twoMonthsAgo = Calendar.getInstance().apply {
            add(Calendar.MONTH, -2)
        }.timeInMillis

        val keywords = listOf(
            "subscription", "renewal", "renewed",
            "upi mandate", "mandate",
            "auto-debit", "bill"
        )

        val ignoreKeywords = listOf(
            "cancelled", "canceled",
            "failed", "failure",
            "reversed", "refund", "refunded",
            "declined", "unsuccessful",
            "expired", "blocked"
        )

        val amountRegex = Regex(
            "(₹|\\$|€|£|rs\\.?|inr)\\s?\\d+(\\.\\d{1,2})?",
            RegexOption.IGNORE_CASE
        )

        return messages
            .asSequence() // 🔥 performance (lazy)
            .filter { it.date >= twoMonthsAgo }
            .mapNotNull { sms ->

                val message = sms.body
                val lowerMsg = message.lowercase()

                // 🔹 Step 0: ignore unwanted messages
                if (ignoreKeywords.any { lowerMsg.contains(it) }) return@mapNotNull null

                // 🔹 Step 1: keyword check
                if (!keywords.any { lowerMsg.contains(it) }) return@mapNotNull null
                // 🔹 Step 2: detect known service
                val service = services.firstOrNull { s ->
                    val name = s.name.lowercase()
                    lowerMsg.contains(name) ||
                            lowerMsg.contains("$name india") ||
                            lowerMsg.contains("$name.com") ||
                            lowerMsg.contains(name.replace(" ", ""))
                }

                // 🔹 Step 3: extract amount
                val rawAmount = amountRegex.find(message)?.value ?: return@mapNotNull null

                val cleanAmount = rawAmount
                    .replace(Regex("[^\\d.]"), "")
                    .trimStart('.')
                    .toDoubleOrNull() ?: return@mapNotNull null

                // 🔹 Step 4: fallback merchant detection
                val finalService = service?.name
                    ?: extractMerchantName(lowerMsg)
                    ?: return@mapNotNull null

                ParsedSubscription(
                    service = finalService,
                    amount = cleanAmount,
                    date = sms.date
                )
            }
            .toList()
    }    fun getAllServices(): List<Service> {
        return services
    }

    fun extractMerchantName(message: String): String? {

        val patterns = listOf(
            "to ([a-zA-Z .]+)",
            "paid to ([a-zA-Z0-9 .]+)",
            "sent to ([a-zA-Z0-9 .]+)",
            "debited to ([a-zA-Z0-9 .]+)",
            "via ([a-zA-Z0-9 .]+)"
        )

        for (pattern in patterns) {
            val match = Regex(pattern).find(message)
            if (match != null) {
                return match.groupValues[1]
                    .replace(Regex("[^a-zA-Z0-9 ]"), "")
                    .replace("india", "", ignoreCase = true)
                    .replace("media", "", ignoreCase = true)
                    .replace("pvt ltd", "", ignoreCase = true)
                    .replace("ltd", "", ignoreCase = true)
                    .replace("limited", "", ignoreCase = true)
                    .replace("has", "", ignoreCase = true)
                    .trim()
                    .split(" ")
                    .take(2)
                    .joinToString(" ")
                    .replaceFirstChar { it.uppercase() }
            }
        }

        return null
    }
    fun getExactService(name: String): Service? {
        return services.find {
            it.name.equals(name, ignoreCase = true)
        }
    }
    fun getSubscriptions() =
        dao.getSubscriptions()

    suspend fun addSubscription(subscription: SubscriptionEntity) {
        val id = dao.insert(subscription)   // capture generated ID
        if (subscription.reminderEnabled) {
            ReminderScheduler.scheduleReminder(
                context,
                id.toInt(),
                subscription.nextBillingDate,
                subscription.reminderDaysBefore
            )
        }
    }

    suspend fun updateSubscription(subscription: SubscriptionEntity) {
        dao.update(subscription)

        // cancel old reminder
        ReminderScheduler.cancelReminder(context, subscription.id)


        // schedule new reminder if enabled
        if (subscription.reminderEnabled) {
            ReminderScheduler.scheduleReminder(
                context,
                subscription.id,
                subscription.nextBillingDate,
                subscription.reminderDaysBefore
            )
        }
    }

    suspend fun getSubscription(id: Int): SubscriptionEntity? {
        return dao.getSubscription(id)
    }

    suspend fun deleteSubscription(id: Int) {
        dao.deleteById(id)
        WorkManager.getInstance(context)
            .cancelUniqueWork("subscription_${id}")
    }

    fun observeUserDetails(): Flow<AuthUser?> {
        return userDao.observeUser()
            .map { user ->
                user?.let {
                    AuthUser(
                        uid = it.id,
                        name = it.name ?: "",
                        email = it.email ?: "",
                        photo = it.logoResId ?: "",
                        isPremium = it.isPremium
                    )
                }
            }
    }

    suspend fun getCurrentUserId(): String? = userDao.getCurrentUserId()

    suspend fun saveUserDetails(userEntity: UserEntity) {
        val existing = userDao.observeUser().firstOrNull()
        val sameAccount = existing?.id == userEntity.id
        userDao.insert(
            userEntity.copy(
                isPremium = (if (sameAccount) existing?.isPremium else false) == true,
                purchaseToken = if (sameAccount) existing?.purchaseToken else null,
                expiryTime = if (sameAccount) existing?.expiryTime else null
            )
        )
    }

    suspend fun deleteSubData() {
        dao.deleteAll()
    }

    suspend fun clearUserProfile() {
        userDao.deleteUser()
        userDao.clearPremiumStatus()
    }


}