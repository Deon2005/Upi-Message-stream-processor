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

    @Query("SELECT TOTAL(amount) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type=:type")
    fun getMonthlySum(startDate: LocalDate, endDate: LocalDate, type: Int): Flow<Double>

    @Query("SELECT date, SUM(amount) as total FROM transactions WHERE type = 0 AND date BETWEEN :startDate AND :endDate GROUP BY date ORDER BY date ASC")
    fun getDailyTotals(startDate: LocalDate, endDate: LocalDate): Flow<List<DailyTotal>>

    @Query("SELECT * FROM transactions WHERE date = :date and type = :type")
    fun getTransactionsByDateandtype(date: LocalDate, type: Int): Flow<List<Transaction>>

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTransaction(id: Long)

    @Update
    suspend fun update(transaction: Transaction)

    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate AND type=:type")
    fun getMonthlydata(startDate: LocalDate, endDate: LocalDate, type: Int): Flow<List<Transaction>>
}

