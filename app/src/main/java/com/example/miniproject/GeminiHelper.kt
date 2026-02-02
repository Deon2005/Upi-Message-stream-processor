package com.example.miniproject

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.delay

// FIX IS HERE: "private val" makes 'context' usable throughout the class
class GeminiHelper(private val context: Context) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash-lite",
        apiKey = "AIzaSyD0Ivl727wV9m2nJ9c9PnElZvhN2T6xiow"
    )

    suspend fun generateRegexFromSms(sms: String): String? {
        return withContext(Dispatchers.IO) {
            val prompt = """
                I need a Java/Kotlin Regex pattern to parse this SMS:
                "${'$'}sms"
               
                REQUIREMENTS:
                1. Capture the Account Number in GROUP 1.
                2. Capture the Transaction Type (debited/credited) in GROUP 2.
                3. Capture the Amount (digits/decimals only) in GROUP 3.
                4. Capture the Date in GROUP 4.
                
                CRITICAL RULES:
                - Do NOT use named groups like (?<name>...). Use standard capturing groups (...).
                - Ignore currency symbols (Rs, INR).
                - Return ONLY the raw regex string. No code blocks. No explanations.
            """.trimIndent()

            var attempts = 0
            while (attempts < 3) {
                try {
                    val response = generativeModel.generateContent(prompt)
                    var result = response.text ?: ""

                    // Cleanup
                    result = result.replace("```regex", "")
                        .replace("```kotlin", "")
                        .replace("```", "")
                        .trim()

                    return@withContext result

                } catch (e: Exception) {
                    val errorMsg = e.message ?: ""

                    if (errorMsg.contains("overloaded") || e.javaClass.name.contains("MissingFieldException")) {
                        Log.w("GEMINI_TEST", "Server busy (Attempt ${attempts + 1}/3). Retrying...")
                        attempts++
                        delay(5000)
                    } else {
                        Log.e("GEMINI_TEST", "Fatal API Error: $errorMsg")

                        // SHOW ERROR TOAST ON MAIN THREAD
                        withContext(Dispatchers.Main) {
                            Toast.makeText(context, "API Error: Check Logcat", Toast.LENGTH_SHORT).show()
                        }
                        return@withContext null
                    }
                }
            }

            // SHOW TIMEOUT TOAST ON MAIN THREAD
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Server Busy - Try Again Later", Toast.LENGTH_LONG).show()
            }
            return@withContext null
        }
    }
}