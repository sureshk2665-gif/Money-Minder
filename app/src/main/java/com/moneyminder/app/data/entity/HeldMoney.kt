package com.moneyminder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class HeldMoneyEntryType {
    SPENT, RETURNED
}

@Entity(tableName = "held_money")
data class HeldMoney(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val personName: String,
    val totalAmount: Double,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "held_money_entries")
data class HeldMoneyEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val heldMoneyId: Long,
    val type: HeldMoneyEntryType,
    val amount: Double,
    val purpose: String = "",
    val dateTime: Long = System.currentTimeMillis()
)
