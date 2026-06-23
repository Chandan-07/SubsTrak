package com.tracker.subscription.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: UserEntity)

    @Update
    suspend fun update(user: UserEntity)

    @Query("SELECT * FROM users LIMIT 1")
    fun observeUser(): Flow<UserEntity>

    // ✅ Delete user (useful for logout/reset)
    @Query("DELETE FROM users")
    suspend fun deleteUser()

    @Query("UPDATE users SET isPremium = :isPremium")
    suspend fun updatePremiumStatus(isPremium: Boolean)

    @Query("UPDATE users SET purchaseToken = :token")
    suspend fun updatePurchaseToken(token: String)

    @Query("UPDATE users SET expiryTime = :expiryTime")
    suspend fun updateExpiry(expiryTime: Long)

    @Query("SELECT expiryTime FROM users LIMIT 1")
    suspend fun getExpiryTime(): Long?

    // ✅ Check if user is premium (quick access)
    @Query("SELECT isPremium FROM users LIMIT 1")
    suspend fun isUserPremium(): Boolean?

    // ✅ Get token (for backend verification / restore)
    @Query("SELECT purchaseToken FROM users LIMIT 1")
    suspend fun getPurchaseToken(): String?

    @Query("SELECT id FROM users LIMIT 1")
    suspend fun getCurrentUserId(): String?

    @Query("UPDATE users SET isPremium = 0, purchaseToken = NULL, expiryTime = NULL")
    suspend fun clearPremiumStatus()
}