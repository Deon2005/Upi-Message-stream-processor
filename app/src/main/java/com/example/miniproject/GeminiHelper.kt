package com.example.miniproject

import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GeminiHelper {

    // CORRECT MODEL NAME: "gemini-1.5-flash" (2.5 does not exist yet)
    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = "AIzaSyCaB3yEpPfo1b8gqnt1iO_OvuOghBMKW2o"
    )

    suspend fun generateRegexFromSms(sms: String): String? {
        return withContext(Dispatchers.IO) {
            val prompt = """
                I need a Java/Kotlin Regex pattern to parse this SMS:
                "$sms"
               
                REQUIREMENTS:
                1. Capture the Account Number in GROUP 1.
                2. Capture the Transaction Type (debited/credited) in GROUP 2.
                3. Capture the Amount (digits/decimals only) in GROUP 3.
                4. Capture the Date in GROUP 4.
                
                CRITICAL RULES:
                - Do NOT use named groups like (?<name>...). Use standard capturing groups (...).
                - Ignore currency symbols (Rs, INR).
                - Return ONLY the raw regex string. No code blocks. No explanations.
                
                Target Regex Structure Example:
                Acct\s+([A-Za-z0-9]+).*?(debited|credited).*?([\d,]+\.?\d*).*?(\d{2}-[A-Za-z]{3})
            """.trimIndent()

            try {
                val response = generativeModel.generateContent(prompt)
                var result = response.text ?: ""

                // Cleanup junk
                result = result.replace("```regex", "")
                    .replace("```kotlin", "")
                    .replace("```", "")
                    .trim()

                Log.d("GEMINI_TEST", "INPUT: $sms")
                Log.d("GEMINI_TEST", "REGEX: $result")

                return@withContext result
            } catch (e: Exception) {
                Log.e("GEMINI_TEST", "API Error: ${e.message}")
                return@withContext null
            }
        }
    }
}