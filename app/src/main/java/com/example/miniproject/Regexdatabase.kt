package com.example.miniproject

import android.R
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "regexdatabase"
)
data class Regexdatabase(
    @PrimaryKey(autoGenerate = true) val tid: Long = 0,
    val regex: String,
    val name: String,
    val type: String,
    val typecode: Int
)
