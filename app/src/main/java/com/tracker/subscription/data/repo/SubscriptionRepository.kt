package com.tracker.subscription.data.repo

import android.content.Context
import android.util.Log
import androidx.work.WorkManager
import com.tracker.subscription.R
import com.tracker.subscription.ReminderScheduler
import com.tracker.subscription.data.AuthUser
import com.tracker.subscription.data.ParsedSubscription
import com.tracker.subscription.data.Service
import com.tracker.subscription.data.dao.SmsDataSource
import com.tracker.subscription.data.dao.SubscriptionDao
import com.tracker.subscription.data.dao.SubscriptionEntity
import com.tracker.subscription.data.dao.UserDao
import com.tracker.subscription.data.dao.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import java.util.Calendar

class SubscriptionRepository(
    private val dao: SubscriptionDao,
    private val userDao: UserDao,
    private val context: Context,
    private val smsDataSource: SmsDataSource,
) {

     val services = listOf(

         Service("netflix", "Netflix", R.drawable.netflix, "com.netflix.mediaclient", "OTT"),
         Service("youtube", "YouTube", R.drawable.youtube, "com.google.android.youtube", "OTT"),
         Service("prime", "Amazon Prime Video", R.drawable.prime, "com.amazon.avod.thirdpartyclient", "OTT"),
         Service("hotstar", "Disney + Hotstar", R.drawable.jiohotstar, "in.startv.hotstar", "OTT"),
         Service("zee5", "ZEE5", R.drawable.zee, "com.graymatrix.did", "OTT"),
         Service("appletv", "Apple TV+", R.drawable.apple_tv, "", "OTT"),
         Service("discovery", "Discovery+", R.drawable.discovery, "com.discovery.discoveryplus.mobile", "OTT"),

         Service("spotify", "Spotify", R.drawable.spotify, "com.spotify.music", "Music"),
         Service("ytmusic", "YouTube Music", R.drawable.ytbmusic, "com.google.android.apps.youtube.music", "Music"),

         Service("googleone", "Google One", R.drawable.google, "com.google.android.apps.subscriptions.red", "Productivity"),
         Service("dropbox", "Dropbox", R.drawable.dropbox, "com.dropbox.android", "Productivity"),
         Service("notion", "Notion", R.drawable.notion, "notion.id", "Productivity"),
         Service("evernote", "Evernote", R.drawable.evernote, "com.evernote", "Productivity"),
         Service("photoshop", "Photoshop", R.drawable.photoshop, "com.adobe.psmobile", "Productivity"),

         Service("linkedin", "LinkedIn Premium", R.drawable.linkedin, "com.linkedin.android", "Social"),
         Service("twitter", "X / Twitter", R.drawable.x, "com.twitter.android", "Social"),
         Service("instagram", "Instagram+", R.drawable.instagram, "com.instagram.android", "Social"),

         Service("tinder", "Tinder", R.drawable.tinder, "com.tinder", "Dating"),
         Service("bumble", "Bumble", R.drawable.bumble, "com.bumble.app", "Dating"),
         Service("hinge", "Hinge", R.drawable.hinge, "", "Dating"),
         Service("okcupid", "OkCupid", R.drawable.okcupid, "com.okcupid.okcupid", "Dating"),

         Service("duolingo", "Duolingo", R.drawable.duolingo, "com.duolingo", "Learning"),
         Service("coursera", "Coursera", R.drawable.coursera, "org.coursera.android", "Learning"),
         Service("udemy", "Udemy", R.drawable.udemy, "com.udemy.android", "Learning"),
         Service("skillshare", "Skillshare", R.drawable.skillshare, "", "Learning"),
         Service("unacademy", "Unacademy", R.drawable.unacademy, "com.unacademyapp", "Learning"),

         Service("chatgpt", "ChatGPT", R.drawable.chatgpt, "com.openai.chatgpt", "AI"),
         Service("gemini", "Google Gemini", R.drawable.googlegemini, "com.google.android.apps.bard", "AI"),
         Service("claude", "Claude", R.drawable.claude, "", "AI"),
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
                subscription.nextBillingDate
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
                subscription.nextBillingDate
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
                        photo = it.logoResId ?: ""
                    )
                }
            }
    }

    suspend fun saveUserDetails(userEntity: UserEntity){
        userDao.insert(userEntity)
    }

    suspend fun deleteSubData(){
        dao.deleteAll()
    }


}