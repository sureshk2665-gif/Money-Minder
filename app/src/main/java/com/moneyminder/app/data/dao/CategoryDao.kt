package com.moneyminder.app.data.dao

import androidx.room.*
import com.moneyminder.app.data.entity.Category
import com.moneyminder.app.data.entity.TransactionType
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category): Long

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    fun getCategoriesByType(type: TransactionType): Flow<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type ORDER BY name ASC")
    suspend fun getCategoriesByTypeList(type: TransactionType): List<Category>

    @Query("SELECT EXISTS(SELECT 1 FROM categories WHERE name = :name AND type = :type)")
    suspend fun exists(name: String, type: TransactionType): Boolean

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
