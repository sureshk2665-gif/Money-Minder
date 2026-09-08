package com.moneyminder.app.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.moneyminder.app.data.dao.CategoryDao
import com.moneyminder.app.data.dao.TransactionDao
import com.moneyminder.app.data.entity.Category
import com.moneyminder.app.data.entity.Transaction

@Database(
    entities = [Transaction::class, Category::class],
    version = 1,
    exportSchema = false
)
abstract class MoneyMinderDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun categoryDao(): CategoryDao

    companion object {
        @Volatile
        private var INSTANCE: MoneyMinderDatabase? = null

        fun getDatabase(context: Context): MoneyMinderDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MoneyMinderDatabase::class.java,
                    "money_minder_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
