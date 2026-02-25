package com.example.miniproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RegexDao
{
    @Insert
    suspend fun insertRegex(regex: Regexdatabase)
    @Query("SELECT * FROM regexdatabase")
     fun getAllRegex(): Flow<List<Regexdatabase>>
    @Query("SELECT COUNT(*) FROM regexdatabase")
    suspend fun getCount(): Int

    @Query("SELECT * FROM regexdatabase WHERE :sender LIKE '%' || name || '%' LIMIT 1")
    suspend fun getRegexBySender(sender: String): List<Regexdatabase>

    @Delete
    suspend fun deleteRegex(regex: Regexdatabase)

}
