package com.example.miniproject

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "transactions")
data class Transaction
    (
        @PrimaryKey(autoGenerate = true) val id: Long = 0,
        val amount: Double,
        val date: LocalDate,
        val type: Int,
        val upiID: String,
        val accountNumber: String
    )