package com.example.miniproject

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface RegexDao
{
    @Insert
    suspend fun insertRegex(regex: Regexdatabase)
    @Query("SELECT * FROM regexdatabase")
    suspend fun getAllRegex(): List<Regexdatabase>
    @Query("SELECT COUNT(*) FROM regexdatabase")
    suspend fun getCount(): Int
}
