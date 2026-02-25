package com.example.miniproject

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import java.time.LocalDate

class AutoDeleteWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val context = applicationContext
        val prefs = context.getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isAutoDeleteEnabled = prefs.getBoolean("auto_delete_90", false)

        if (!isAutoDeleteEnabled) {
            return Result.success()
        }

        return try {
            val dao = AppDatabase.getDatabase(context).transactionDao()

            // Calculate date 90 days ago
            val cutoffDate = LocalDate.now().minusDays(90)

            // Delete old transactions
            // Note: You need to add this query to your TransactionDao (Step 3)
            dao.deleteOlderThan(cutoffDate)

            Log.d("AutoDelete", "Old transactions deleted successfully")
            Result.success()
        } catch (e: Exception) {
            Log.e("AutoDelete", "Error deleting transactions", e)
            Result.retry()
        }
    }
}