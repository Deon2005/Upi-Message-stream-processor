package com.example.miniproject

import android.content.Context
import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Regexdatabase::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase(){
    abstract fun regexDao(): RegexDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "regex_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}