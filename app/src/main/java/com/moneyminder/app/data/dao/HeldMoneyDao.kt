package com.moneyminder.app.data.dao

import androidx.room.*
import com.moneyminder.app.data.entity.HeldMoney
import com.moneyminder.app.data.entity.HeldMoneyEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface HeldMoneyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHeldMoney(heldMoney: HeldMoney): Long

    @Update
    suspend fun updateHeldMoney(heldMoney: HeldMoney)

    @Delete
    suspend fun deleteHeldMoney(heldMoney: HeldMoney)

    @Query("SELECT * FROM held_money ORDER BY createdAt DESC")
    fun getAllHeldMoney(): Flow<List<HeldMoney>>

    @Query("SELECT * FROM held_money WHERE id = :id")
    suspend fun getHeldMoneyById(id: Long): HeldMoney?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HeldMoneyEntry): Long

    @Delete
    suspend fun deleteEntry(entry: HeldMoneyEntry)

    @Query("SELECT * FROM held_money_entries WHERE heldMoneyId = :heldMoneyId ORDER BY dateTime DESC")
    fun getEntriesForHeldMoney(heldMoneyId: Long): Flow<List<HeldMoneyEntry>>

    @Query("SELECT COALESCE(SUM(amount), 0) FROM held_money_entries WHERE heldMoneyId = :heldMoneyId")
    suspend fun getTotalEntriesAmount(heldMoneyId: Long): Double

    @Query("DELETE FROM held_money_entries WHERE heldMoneyId = :heldMoneyId")
    suspend fun deleteEntriesForHeldMoney(heldMoneyId: Long)

    @Query("DELETE FROM held_money")
    suspend fun deleteAllHeldMoney()

    @Query("DELETE FROM held_money_entries")
    suspend fun deleteAllEntries()
}
