package com.tracker.subscription.data.db


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tracker.subscription.data.dao.SubscriptionDao
import com.tracker.subscription.data.dao.SubscriptionEntity

@Database(
    entities = [SubscriptionEntity::class],
    version = 2,
    exportSchema = false
)
abstract class SubscriptionDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao

    companion object {

        @Volatile
        private var INSTANCE: SubscriptionDatabase? = null

        fun getDatabase(context: Context): SubscriptionDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubscriptionDatabase::class.java,
                    "subscription_db"
                ).build()

                INSTANCE = instance
                instance
            }
        }
    }
}
