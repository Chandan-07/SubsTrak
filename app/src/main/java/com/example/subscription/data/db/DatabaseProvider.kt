package com.example.subscription.data.db

import android.content.Context
import androidx.room.Room

object DatabaseProvider {

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