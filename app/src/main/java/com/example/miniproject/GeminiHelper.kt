package com.example.miniproject

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import com.example.miniproject.BuildConfig

data class GeminiResult(val pattern: String, val extractedWord: String)

class GeminiHelper(private val context: Context) {

    private val generativeModel = GenerativeModel(
        modelName = "gemini-2.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY
    )

    suspend fun generateRegexFromSms(sms: String): GeminiResult? {
        return withContext(Dispatchers.IO) {

            // Normalize SMS slightly (do NOT change content)
            val cleanSms = sms.trim()

            val prompt = """
You are an expert Regex Engineer.

SMS TO MATCH EXACTLY (DO NOT MODIFY, DO NOT GENERALIZE):
<<<
$cleanSms
>>>

Generate ONE Java/Kotlin–compatible regex to parse the above SMS.

STRICT OUTPUT RULES
- Output ONLY the raw regex string
- No explanations
- No comments
- No markdown
- Regex must compile in Java/Kotlin
- Each named capturing group MUST appear exactly once

REQUIRED NAMED CAPTURING GROUPS
- (?<account>)
- (?<type>)
- (?<amount>)
- (?<date>) (optional)
- (?<upi>) 

FIELD RULES
- account, type, amount are mandatory
- date is optional and MUST NOT break matching
- The (?<upi>) group MUST capture the transaction counterparty (name, UPI ID, or Ref no). Because prepositions change based on the transaction type, you MUST use an alternation for the preceding word (e.g., use "(?:to|from|by|at)\s+(?<upi>[^.]+)" instead of hardcoding a single word like "from")

GLOBAL REGEX RULES
- Regex MUST start with (?i).*?
- Use lazy matching (.*?) between fields
- Enumerate transaction type strictly: credit|credited|debit|debited
- Anchor amount to Rs or INR
- NEVER duplicate named groups
- NEVER use alternation to reorder fields

OVERFITTING RULE
Matching THIS SMS is more important than generalization.
Discard any rule that prevents a match.
""".trimIndent()

            var attempt = 0
            val maxAttempts = 3

            while (attempt < maxAttempts) {
                try {
                    val response = generativeModel.generateContent(prompt)
                    var regexText = response.text ?: ""

                    // Cleanup Gemini formatting
                    regexText = regexText
                        .replace("```regex", "")
                        .replace("```", "")
                        .trim()

                    Log.d("GEMINI_REGEX_RAW", regexText)

                    // ---------- HARD VALIDATION ----------
                    try {
                        val regex = Regex(regexText)
                        val match=regex.find(cleanSms)
                        if (match == null) {
                            Log.e(
                                "GEMINI_REGEX_FAIL",
                                "Regex does NOT match SMS. Retrying...\nRegex: $regexText\nSMS: $cleanSms"
                            )
                            attempt++
                            continue
                        }

                        // SUCCESS
                        val extractedWord=match.groups["type"]?.value ?.lowercase()?.trim() ?: ""
                        Log.d("GEMINI_REGEX_OK", "Valid regex generated")
                        return@withContext GeminiResult(regexText,extractedWord)

                    } catch (re: Exception) {
                        Log.e(
                            "GEMINI_REGEX_SYNTAX",
                            "Invalid regex syntax. Retrying...\n$regexText"
                        )
                        attempt++
                        continue
                    }

                } catch (e: Exception) {
                    val msg = e.message ?: ""

                    if (msg.contains("overloaded", ignoreCase = true)) {
                        Log.w("GEMINI_API", "Server overloaded. Retrying...")
                        attempt++
                        delay(4000)
                    } else {
                        Log.e("GEMINI_API_FATAL", msg)
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "Gemini API Error. Check Logcat.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return@withContext null
                    }
                }
            }

            // Final failure
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    context,
                    "Failed to generate valid regex",
                    Toast.LENGTH_LONG
                ).show()
            }

            null
        }
    }
}
