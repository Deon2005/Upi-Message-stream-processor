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
Act as an expert Regex Generator. I need a Kotlin/Java Regex pattern to parse this specific SMS:

$sms

STRICT OUTPUT RULES:
1. Return ONLY the raw regex string. Do not use Markdown, code blocks (```), or explanations.
2. The regex must have EXACTLY 5 Capturing Groups in the strict order listed below.

CAPTURING GROUPS (Strict Order):
1. **Account Number**: Digits representing the account (Context clues: 'A/c', 'Account', 'Ending', 'X', or similar).
2. **Transaction Type**: The specific word indicating direction found in the text (e.g., 'credited', 'debited', 'sent', 'received', 'trf to').
3. **Amount**: The numeric value. IMPORTANT: Handle optional decimals `(?:\.\d+)?`. Do NOT capture currency symbols (like 'INR', 'Rs') unless they are part of the number. Look for the number near the transaction type.
4. **Date**: The date string found in the message. Match the EXACT format shown in the SMS (e.g., DD-MM-YYYY, DDMonYY, etc.).
5. **Entity/UPI ID**: The other party involved. (Logic: If debited, capture who it was paid 'to'. If credited, capture who it is 'from'. Look for keywords like 'to', 'from', 'VPA', or 'at').

REGEX LOGIC:
- **Be Adaptive**: Do not assume specific keywords (like "INR") exist unless they are actually in the provided SMS. Use the specific words found in the text to anchor your regex.
- **Flexibility**: Use `.*?` to skip unrelated text between groups. Use `\s+` to handle variable spaces.
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