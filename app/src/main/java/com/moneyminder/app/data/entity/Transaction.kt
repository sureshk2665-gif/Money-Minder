package com.moneyminder.app.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER
}

enum class AccountType {
    BANK, WALLET, CASH
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    val category: String = "",
    val note: String = "",
    val dateTime: Long,
    val fromAccount: AccountType? = null,
    val toAccount: AccountType? = null,
    val referenceNumber: String = "",
    val smsSource: Boolean = false,
    val balanceAfterFrom: Double? = null,
    val balanceAfterTo: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
