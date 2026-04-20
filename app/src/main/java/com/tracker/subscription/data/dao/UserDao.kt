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

    // ✅ Update only subscription status
    @Query("""
        UPDATE users 
        SET isPremium = :isPremium 
        WHERE id = :userId
    """)
    suspend fun updatePremiumStatus(userId: Int, isPremium: Boolean)

    // ✅ Store purchase token (important for validation)
    @Query("""
        UPDATE users 
        SET purchaseToken = :token 
        WHERE id = :userId
    """)
    suspend fun updatePurchaseToken(userId: Int, token: String)

    // ✅ Store subscription expiry
    @Query("""
        UPDATE users 
        SET expiryTime = :expiryTime 
        WHERE id = :userId
    """)
    suspend fun updateExpiry(userId: Int, expiryTime: Long)

    // ✅ Check if user is premium (quick access)
    @Query("SELECT isPremium FROM users LIMIT 1")
    suspend fun isUserPremium(): Boolean?

    // ✅ Get token (for backend verification / restore)
    @Query("SELECT purchaseToken FROM users LIMIT 1")
    suspend fun getPurchaseToken(): String?
}