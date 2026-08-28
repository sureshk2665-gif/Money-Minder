package com.moneyminder.app.data.dao

import androidx.room.*
import com.moneyminder.app.data.entity.AccountType
import com.moneyminder.app.data.entity.Transaction
import com.moneyminder.app.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction): Long

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY dateTime DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: Long): Transaction?

    @Query("SELECT * FROM transactions ORDER BY dateTime ASC")
    suspend fun getAllTransactionsSorted(): List<Transaction>

    @Query("SELECT * FROM transactions WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime ASC")
    suspend fun getTransactionsByDateRangeSorted(startDate: Long, endDate: Long): List<Transaction>

    @Query("SELECT * FROM transactions WHERE type = :type AND dateTime BETWEEN :startDate AND :endDate ORDER BY dateTime DESC")
    fun getTransactionsByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE (fromAccount = :account OR toAccount = :account) ORDER BY dateTime DESC")
    fun getTransactionsByAccount(account: AccountType): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE (fromAccount = :account OR toAccount = :account) AND type IN (:types) ORDER BY dateTime DESC")
    fun getTransactionsByAccountAndTypes(account: AccountType, types: List<TransactionType>): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND dateTime BETWEEN :start AND :end")
    suspend fun getSumByTypeAndDateRange(type: TransactionType, start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND toAccount = :account AND dateTime BETWEEN :start AND :end")
    suspend fun getIncomeSumByAccount(type: TransactionType, account: AccountType, start: Long, end: Long): Double?

    @Query("SELECT SUM(amount) FROM transactions WHERE type = :type AND fromAccount = :account AND dateTime BETWEEN :start AND :end")
    suspend fun getExpenseSumByAccount(type: TransactionType, account: AccountType, start: Long, end: Long): Double?

    @Query("SELECT * FROM transactions WHERE dateTime BETWEEN :start AND :end AND type = :type ORDER BY amount DESC LIMIT :limit")
    suspend fun getTopTransactions(type: TransactionType, start: Long, end: Long, limit: Int): List<Transaction>

    @Query("SELECT category, SUM(amount) as total FROM transactions WHERE type = 'EXPENSE' AND dateTime BETWEEN :start AND :end AND category != '' GROUP BY category ORDER BY total DESC")
    suspend fun getExpenseCategorySums(start: Long, end: Long): List<CategorySum>

    @Query("SELECT * FROM transactions WHERE amount = :amount AND dateTime BETWEEN :startRange AND :endRange AND type = :type")
    suspend fun findPotentialDuplicates(amount: Double, startRange: Long, endRange: Long, type: TransactionType): List<Transaction>

    @Query("SELECT * FROM transactions WHERE referenceNumber = :refNum AND referenceNumber != ''")
    suspend fun findByReferenceNumber(refNum: String): List<Transaction>

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun getCount(): Int
}

data class CategorySum(
    val category: String,
    val total: Double
)
