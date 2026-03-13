package com.tracker.subscription.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(subscription: SubscriptionEntity): Long

    @Query("SELECT * FROM subscriptions")
    fun getSubscriptions(): Flow<List<SubscriptionEntity>>

    @Update
    suspend fun update(subscription: SubscriptionEntity)

    @Delete
    suspend fun delete(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE id = :id")
    suspend fun deleteById(id: Int)


    @Query("SELECT * FROM subscriptions WHERE id = :id")
    suspend fun getSubscription(id: Int): SubscriptionEntity?

    @Query("UPDATE subscriptions SET nextBillingDate = :nextDate WHERE id = :id")
    suspend fun updateNextBillingDate(id: Int, nextDate: Long)
}