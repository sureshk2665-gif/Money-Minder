package com.moneyminder.app.data.repository

import com.moneyminder.app.data.dao.CategoryDao
import com.moneyminder.app.data.dao.CategorySum
import com.moneyminder.app.data.dao.TransactionDao
import com.moneyminder.app.data.entity.*
import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val transactionDao: TransactionDao,
    private val categoryDao: CategoryDao
) {
    fun getAllTransactions(): Flow<List<Transaction>> = transactionDao.getAllTransactions()

    suspend fun getAllTransactionsSorted(): List<Transaction> = transactionDao.getAllTransactionsSorted()

    fun getTransactionsByDateRange(start: Long, end: Long): Flow<List<Transaction>> =
        transactionDao.getTransactionsByDateRange(start, end)

    suspend fun getTransactionsByDateRangeSorted(start: Long, end: Long): List<Transaction> =
        transactionDao.getTransactionsByDateRangeSorted(start, end)

    fun getTransactionsByAccount(account: AccountType): Flow<List<Transaction>> =
        transactionDao.getTransactionsByAccount(account)

    fun getTransactionsByAccountAndTypes(account: AccountType, types: List<TransactionType>): Flow<List<Transaction>> =
        transactionDao.getTransactionsByAccountAndTypes(account, types)

    suspend fun getTransactionById(id: Long): Transaction? = transactionDao.getById(id)

    suspend fun insertTransaction(transaction: Transaction): Long {
        val id = transactionDao.insert(transaction)
        if (transaction.category.isNotBlank() && transaction.type != TransactionType.TRANSFER) {
            if (!categoryDao.exists(transaction.category, transaction.type)) {
                categoryDao.insert(Category(name = transaction.category, type = transaction.type))
            }
        }
        return id
    }

    suspend fun updateTransaction(transaction: Transaction) {
        transactionDao.update(transaction)
        if (transaction.category.isNotBlank() && transaction.type != TransactionType.TRANSFER) {
            if (!categoryDao.exists(transaction.category, transaction.type)) {
                categoryDao.insert(Category(name = transaction.category, type = transaction.type))
            }
        }
    }

    suspend fun deleteTransaction(transaction: Transaction) = transactionDao.delete(transaction)

    suspend fun deleteAllTransactions() {
        transactionDao.deleteAll()
        categoryDao.deleteAll()
    }

    fun getCategoriesByType(type: TransactionType): Flow<List<Category>> =
        categoryDao.getCategoriesByType(type)

    suspend fun getCategoriesByTypeList(type: TransactionType): List<Category> =
        categoryDao.getCategoriesByTypeList(type)

    suspend fun getIncomeSumForMonth(start: Long, end: Long): Double =
        transactionDao.getSumByTypeAndDateRange(TransactionType.INCOME, start, end) ?: 0.0

    suspend fun getExpenseSumForMonth(start: Long, end: Long): Double =
        transactionDao.getSumByTypeAndDateRange(TransactionType.EXPENSE, start, end) ?: 0.0

    suspend fun getExpenseCategorySums(start: Long, end: Long): List<CategorySum> =
        transactionDao.getExpenseCategorySums(start, end)

    suspend fun calculateAccountBalance(account: AccountType): Double {
        val transactions = transactionDao.getAllTransactionsSorted()
        var balance = 0.0
        for (txn in transactions) {
            when (txn.type) {
                TransactionType.INCOME -> {
                    if (txn.toAccount == account) balance += txn.amount
                }
                TransactionType.EXPENSE -> {
                    if (txn.fromAccount == account) balance -= txn.amount
                }
                TransactionType.TRANSFER -> {
                    if (txn.fromAccount == account) balance -= txn.amount
                    if (txn.toAccount == account) balance += txn.amount
                }
            }
        }
        return balance
    }

    suspend fun calculateRunningBalances(transactions: List<Transaction>, account: AccountType): Map<Long, Double> {
        val allTransactions = transactionDao.getAllTransactionsSorted()
        val balanceMap = mutableMapOf<Long, Double>()
        var balance = 0.0
        for (txn in allTransactions) {
            when (txn.type) {
                TransactionType.INCOME -> {
                    if (txn.toAccount == account) balance += txn.amount
                }
                TransactionType.EXPENSE -> {
                    if (txn.fromAccount == account) balance -= txn.amount
                }
                TransactionType.TRANSFER -> {
                    if (txn.fromAccount == account) balance -= txn.amount
                    if (txn.toAccount == account) balance += txn.amount
                }
            }
            if (transactions.any { it.id == txn.id }) {
                balanceMap[txn.id] = balance
            }
        }
        return balanceMap
    }

    suspend fun calculateAllRunningBalances(): Map<Long, Map<AccountType, Double>> {
        val allTransactions = transactionDao.getAllTransactionsSorted()
        val result = mutableMapOf<Long, Map<AccountType, Double>>()
        val balances = mutableMapOf(
            AccountType.BANK to 0.0,
            AccountType.WALLET to 0.0,
            AccountType.CASH to 0.0
        )
        for (txn in allTransactions) {
            when (txn.type) {
                TransactionType.INCOME -> {
                    txn.toAccount?.let { balances[it] = (balances[it] ?: 0.0) + txn.amount }
                }
                TransactionType.EXPENSE -> {
                    txn.fromAccount?.let { balances[it] = (balances[it] ?: 0.0) - txn.amount }
                }
                TransactionType.TRANSFER -> {
                    txn.fromAccount?.let { balances[it] = (balances[it] ?: 0.0) - txn.amount }
                    txn.toAccount?.let { balances[it] = (balances[it] ?: 0.0) + txn.amount }
                }
            }
            result[txn.id] = balances.toMap()
        }
        return result
    }

    suspend fun findPotentialDuplicates(amount: Double, dateTime: Long, type: TransactionType): List<Transaction> {
        val range = 60 * 60 * 1000L
        return transactionDao.findPotentialDuplicates(amount, dateTime - range, dateTime + range, type)
    }

    suspend fun findByReferenceNumber(refNum: String): List<Transaction> =
        transactionDao.findByReferenceNumber(refNum)

    suspend fun getTransactionCount(): Int = transactionDao.getCount()
}
