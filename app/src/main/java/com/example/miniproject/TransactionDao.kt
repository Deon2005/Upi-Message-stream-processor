package com.example.miniproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao
{
    @Insert
    suspend fun insertTransaction(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT TOTAL(amount) FROM transactions WHERE date = :date AND (type LIKE :type1 OR type LIKE :type2)")
    fun getDailySum(date: LocalDate, type1: String,type2: String): Flow<Double>

    @Query("SELECT TOTAL(amount) FROM transactions WHERE date BETWEEN :startDate AND :endDate AND (type LIKE :type1 OR type LIKE :type2)")
    fun getMonthlySum(startDate: LocalDate, endDate: LocalDate, type1: String, type2: String): Flow<Double>

}
