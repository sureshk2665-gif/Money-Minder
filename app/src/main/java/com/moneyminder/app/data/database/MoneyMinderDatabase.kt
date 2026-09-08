package com.moneyminder.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moneyminder.app.data.dao.CategoryDao
import com.moneyminder.app.data.dao.HeldMoneyDao
import com.moneyminder.app.data.dao.TransactionDao
import com.moneyminder.app.data.entity.Category
import com.moneyminder.app.data.entity.HeldMoney
import com.moneyminder.app.data.entity.HeldMoneyEntry
import com.moneyminder.app.data.entity.Transaction

@Database(
    entities = [Transaction::class, Category::class, HeldMoney::class, HeldMoneyEntry::class],
    version = 2,
    exportSchema = false
)
abstract class MoneyMinderDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao
    abstract fun heldMoneyDao(): HeldMoneyDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyMinderDatabase? = null

        fun getDatabase(context: Context): MoneyMinderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyMinderDatabase::class.java,
                    "money_minder_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
