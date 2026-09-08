package com.moneyminder.app.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.moneyminder.app.MoneyMinderApplication
import com.moneyminder.app.data.dao.CategorySum
import com.moneyminder.app.data.entity.*
import com.moneyminder.app.data.repository.TransactionRepository
import com.moneyminder.app.util.DateUtils
import com.moneyminder.app.util.SmsParser
import com.moneyminder.app.util.ParsedSmsTransaction
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MoneyMinderViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: TransactionRepository = (application as MoneyMinderApplication).repository

    private val _selectedYear = MutableStateFlow(DateUtils.getCurrentYear())
    val selectedYear: StateFlow<Int> = _selectedYear

    private val _selectedMonth = MutableStateFlow(DateUtils.getCurrentMonth())
    val selectedMonth: StateFlow<Int> = _selectedMonth

    private val _bankBalance = MutableStateFlow(0.0)
    val bankBalance: StateFlow<Double> = _bankBalance

    private val _walletBalance = MutableStateFlow(0.0)
    val walletBalance: StateFlow<Double> = _walletBalance

    private val _cashBalance = MutableStateFlow(0.0)
    val cashBalance: StateFlow<Double> = _cashBalance

    val overallBalance: StateFlow<Double> = combine(bankBalance, walletBalance, cashBalance) { b, w, c -> b + w + c }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _monthlyIncome = MutableStateFlow(0.0)
    val monthlyIncome: StateFlow<Double> = _monthlyIncome

    private val _monthlyExpense = MutableStateFlow(0.0)
    val monthlyExpense: StateFlow<Double> = _monthlyExpense

    private val _monthlyTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val monthlyTransactions: StateFlow<List<Transaction>> = _monthlyTransactions

    private val _allTransactions = MutableStateFlow<List<Transaction>>(emptyList())

    private val _runningBalances = MutableStateFlow<Map<Long, Map<AccountType, Double>>>(emptyMap())
    val runningBalances: StateFlow<Map<Long, Map<AccountType, Double>>> = _runningBalances

    private val _categorySums = MutableStateFlow<List<CategorySum>>(emptyList())
    val categorySums: StateFlow<List<CategorySum>> = _categorySums

    private val _monthlyTransferTotal = MutableStateFlow(0.0)
    val monthlyTransferTotal: StateFlow<Double> = _monthlyTransferTotal

    private val _expenseCategories = MutableStateFlow<List<Category>>(emptyList())
    val expenseCategories: StateFlow<List<Category>> = _expenseCategories

    private val _incomeCategories = MutableStateFlow<List<Category>>(emptyList())
    val incomeCategories: StateFlow<List<Category>> = _incomeCategories

    private val _saveSuccess = MutableSharedFlow<Boolean>()
    val saveSuccess: SharedFlow<Boolean> = _saveSuccess

    private val _isFirstLaunch = MutableStateFlow(true)
    val isFirstLaunch: StateFlow<Boolean> = _isFirstLaunch

    init {
        checkFirstLaunch()
        loadData()
        loadCategories()
    }

    private fun checkFirstLaunch() {
        val prefs = getApplication<MoneyMinderApplication>().getSharedPreferences("money_minder_prefs", 0)
        _isFirstLaunch.value = prefs.getBoolean("first_launch", true)
    }

    fun completeOnboarding() {
        val prefs = getApplication<MoneyMinderApplication>().getSharedPreferences("money_minder_prefs", 0)
        prefs.edit().putBoolean("first_launch", false).apply()
        _isFirstLaunch.value = false
    }

    fun setSelectedMonth(year: Int, month: Int) {
        _selectedYear.value = year
        _selectedMonth.value = month
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val start = DateUtils.getMonthStart(_selectedYear.value, _selectedMonth.value)
            val end = DateUtils.getMonthEnd(_selectedYear.value, _selectedMonth.value)

            repository.getTransactionsByDateRange(start, end).collect { transactions ->
                _monthlyTransactions.value = transactions
                _monthlyIncome.value = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
                _monthlyExpense.value = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount }
                _monthlyTransferTotal.value = transactions.filter { it.type == TransactionType.TRANSFER }.sumOf { it.amount }
            }
        }
        refreshBalances()
        refreshCategorySums()
    }

    private fun refreshBalances() {
        viewModelScope.launch {
            _bankBalance.value = repository.calculateAccountBalance(AccountType.BANK)
            _walletBalance.value = repository.calculateAccountBalance(AccountType.WALLET)
            _cashBalance.value = repository.calculateAccountBalance(AccountType.CASH)
            _runningBalances.value = repository.calculateAllRunningBalances()
        }
    }

    private fun refreshCategorySums() {
        viewModelScope.launch {
            val start = DateUtils.getMonthStart(_selectedYear.value, _selectedMonth.value)
            val end = DateUtils.getMonthEnd(_selectedYear.value, _selectedMonth.value)
            _categorySums.value = repository.getExpenseCategorySums(start, end)
        }
    }

    private fun loadCategories() {
        viewModelScope.launch {
            repository.getCategoriesByType(TransactionType.EXPENSE).collect {
                _expenseCategories.value = it
            }
        }
        viewModelScope.launch {
            repository.getCategoriesByType(TransactionType.INCOME).collect {
                _incomeCategories.value = it
            }
        }
    }

    fun addTransaction(
        type: TransactionType,
        amount: Double,
        category: String,
        note: String,
        dateTime: Long,
        fromAccount: AccountType?,
        toAccount: AccountType?,
        referenceNumber: String = ""
    ) {
        viewModelScope.launch {
            val transaction = Transaction(
                type = type,
                amount = amount,
                category = category,
                note = note,
                dateTime = dateTime,
                fromAccount = fromAccount,
                toAccount = toAccount,
                referenceNumber = referenceNumber,
                smsSource = false
            )
            repository.insertTransaction(transaction)
            refreshBalances()
            refreshCategorySums()
            _saveSuccess.emit(true)
        }
    }

    fun updateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.updateTransaction(transaction)
            refreshBalances()
            refreshCategorySums()
            _saveSuccess.emit(true)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            repository.deleteTransaction(transaction)
            refreshBalances()
            refreshCategorySums()
        }
    }

    fun duplicateTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val duplicate = transaction.copy(
                id = 0,
                dateTime = System.currentTimeMillis(),
                createdAt = System.currentTimeMillis()
            )
            repository.insertTransaction(duplicate)
            refreshBalances()
            refreshCategorySums()
        }
    }

    fun getTransactionById(id: Long, callback: (Transaction?) -> Unit) {
        viewModelScope.launch {
            callback(repository.getTransactionById(id))
        }
    }

    fun parseSmsText(text: String): List<ParsedSmsTransaction> {
        return SmsParser.parseMultipleSms(text)
    }

    suspend fun checkDuplicate(amount: Double, dateTime: Long, type: TransactionType, refNum: String): Boolean {
        if (refNum.isNotBlank()) {
            val byRef = repository.findByReferenceNumber(refNum)
            if (byRef.isNotEmpty()) return true
        }
        val duplicates = repository.findPotentialDuplicates(amount, dateTime, type)
        return duplicates.isNotEmpty()
    }

    fun getTransactionsForExport(): List<Transaction> {
        var result = emptyList<Transaction>()
        viewModelScope.launch {
            val start = DateUtils.getMonthStart(_selectedYear.value, _selectedMonth.value)
            val end = DateUtils.getMonthEnd(_selectedYear.value, _selectedMonth.value)
            result = repository.getTransactionsByDateRangeSorted(start, end)
        }
        return result
    }

    suspend fun getTransactionsForExportSuspend(): List<Transaction> {
        val start = DateUtils.getMonthStart(_selectedYear.value, _selectedMonth.value)
        val end = DateUtils.getMonthEnd(_selectedYear.value, _selectedMonth.value)
        return repository.getTransactionsByDateRangeSorted(start, end)
    }

    fun getTransactionsByAccount(account: AccountType): Flow<List<Transaction>> =
        repository.getTransactionsByAccount(account)

    fun getTransactionsByAccountAndTypes(account: AccountType, types: List<TransactionType>): Flow<List<Transaction>> =
        repository.getTransactionsByAccountAndTypes(account, types)

    fun getDayTransactions(year: Int, month: Int, day: Int): Flow<List<Transaction>> {
        val start = DateUtils.getDayStart(year, month, day)
        val end = DateUtils.getDayEnd(year, month, day)
        return repository.getTransactionsByDateRange(start, end)
    }

    fun deleteAllData() {
        viewModelScope.launch {
            repository.deleteAllTransactions()
            refreshBalances()
            refreshCategorySums()
            _monthlyTransactions.value = emptyList()
        }
    }

    fun getAllTransactionsSorted(callback: (List<Transaction>) -> Unit) {
        viewModelScope.launch {
            callback(repository.getAllTransactionsSorted())
        }
    }
}
