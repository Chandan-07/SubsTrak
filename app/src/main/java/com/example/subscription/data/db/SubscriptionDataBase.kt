package com.example.subscription.data.db


import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.subscription.data.dao.SubscriptionDao
import com.example.subscription.data.dao.SubscriptionEntity

@Database(
    entities = [SubscriptionEntity::class],
    version = 1,
    exportSchema = false
)
abstract class SubscriptionDatabase : RoomDatabase() {

    abstract fun subscriptionDao(): SubscriptionDao
}