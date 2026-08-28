package com.moneyminder.app

import android.app.Application
import com.moneyminder.app.data.database.MoneyMinderDatabase
import com.moneyminder.app.data.repository.TransactionRepository

class MoneyMinderApp : Application() {
    val database by lazy { MoneyMinderDatabase.getDatabase(this) }
    val repository by lazy {
        TransactionRepository(database.transactionDao(), database.categoryDao())
    }
}
