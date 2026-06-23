package com.tracker.subscription.data.db


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tracker.subscription.data.dao.SubscriptionDao
import com.tracker.subscription.data.dao.SubscriptionEntity
import com.tracker.subscription.data.dao.UserDao
import com.tracker.subscription.data.dao.UserEntity

@Database(
    entities = [SubscriptionEntity::class, UserEntity::class],
    version = 7,
    exportSchema = false
)
abstract class SubscriptionDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao

    abstract fun userDao(): UserDao

    companion object {

        @Volatile
        private var INSTANCE: SubscriptionDatabase? = null

        fun getDatabase(context: Context): SubscriptionDatabase {

            return INSTANCE ?: synchronized(this) {

                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SubscriptionDatabase::class.java,
                    "subscription_db"
                ).fallbackToDestructiveMigration() // 👈 IMPORTANT
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}
