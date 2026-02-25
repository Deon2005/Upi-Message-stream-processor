package com.example.miniproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

data class DailyTotal(
    val date: LocalDate,
    val total: Double
)
@Dao
interface TransactionDao
{
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT TOTAL(amount) FROM transactions WHERE date = :date AND type=:type")
    fun getDailySum(date: LocalDate, type:Int): Flow<Double>

    @Query("SELECT TOTAL(amount) FROM transactions WHERE date = :date AND type=:type AND mode=:mode")
    fun getSMSDailySum(date: LocalDate, type:Int,mode:Int): Flow<Double>

    @Query("SELECT TOTAL(amount) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type=:type")
    fun getMonthlySum(startDate: LocalDate, endDate: LocalDate, type: Int): Flow<Double>

    @Query("SELECT TOTAL(amount) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type=:type AND mode=:mode")
    fun getSMSMonthlySum(startDate: LocalDate, endDate: LocalDate, type: Int,mode: Int): Flow<Double>

    @Query("SELECT date, SUM(amount) as total FROM transactions WHERE type = 0 AND date BETWEEN :startDate AND :endDate GROUP BY date ORDER BY date ASC")
    fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>>

    @Query("SELECT * FROM transactions WHERE date = :date and type = :type")
    fun getTransactionsByDateandtype(date: LocalDate, type: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date = :date and type = :type AND mode=:mode")
    fun getSMSTransactionsByDateandtype(date: LocalDate, type: Int,mode: Int): Flow<List<Transaction>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type=:type")
    fun getMonthlydata(startDate: LocalDate, endDate: LocalDate, type: Int): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type=:type AND mode=:mode")
    fun getSMSMonthlydata(startDate: LocalDate, endDate: LocalDate, type: Int,mode: Int): Flow<List<Transaction>>

    // Inside TransactionDao.kt

    // 1. Query for everything
    @Query("SELECT * FROM transactions WHERE date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getAllHistory(startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    // 2. Query filtered ONLY by Type
    @Query("SELECT * FROM transactions WHERE type = :type AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getHistoryByType(type: Int, startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    // 3. Query filtered ONLY by Mode
    @Query("SELECT * FROM transactions WHERE mode = :mode AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getHistoryByMode(mode: Int, startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    // 4. Query filtered by BOTH (Your original logic)
    @Query("SELECT * FROM transactions WHERE type = :type AND mode = :mode AND date >= :startDate AND date <= :endDate ORDER BY date DESC")
    fun getHistoryFullFilter(type: Int, mode: Int, startDate: LocalDate, endDate: LocalDate): Flow<List<Transaction>>

    @Query("DELETE FROM transactions WHERE date < :cutoffDate")
    suspend fun deleteOlderThan(cutoffDate: LocalDate)
}

