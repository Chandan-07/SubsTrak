package com.tracker.subscription.data.dao

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,   // Firebase UID

    val name: String,
    val phone: String,
    val email: String,
    val logoResId: String? = null,
    val isUserPremium: Boolean = false
)