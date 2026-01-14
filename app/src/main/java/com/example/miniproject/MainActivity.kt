package com.example.miniproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        lifecycleScope.launch {
            // Fake SMS to test
            val fakeSms = "Acct XX123 debited by Rs 540.00 for ZOMATO LIMITED on 12-Dec."

            Toast.makeText(this@MainActivity, "Asking Gemini...", Toast.LENGTH_SHORT).show()

            val helper = GeminiHelper()
            val regex = helper.generateRegexFromSms(fakeSms)

            if (regex != null) {
                // Verify if it works immediately
                testRegexOnSms(fakeSms, regex)
            }
        }
    }
    private fun testRegexOnSms(sms: String, patternStr: String) {
        try {
            val pattern = java.util.regex.Pattern.compile(patternStr, java.util.regex.Pattern.CASE_INSENSITIVE)
            val matcher = pattern.matcher(sms)

            if (matcher.find()) {
                // WE USE NUMBERS NOW (Matches the prompt order)
                val accNo = matcher.group(1)  // Group 1: Account
                val type = matcher.group(2)   // Group 2: Type
                val amount = matcher.group(3) // Group 3: Amount
                val date = matcher.group(4)   // Group 4: Date

                Log.d("GEMINI_TEST", "✅ SUCCESS!")
                Log.d("GEMINI_TEST", "   - Account: $accNo")
                Log.d("GEMINI_TEST", "   - Type:    $type")
                Log.d("GEMINI_TEST", "   - Amount:  $amount")
                Log.d("GEMINI_TEST", "   - Date:    $date")
            } else {
                Log.d("GEMINI_TEST", "❌ FAILED. Regex didn't match the SMS.")
            }
        } catch (e: Exception) {
            // Print the specific error to understand why it crashes
            Log.e("GEMINI_TEST", "❌ CRASH: ${e.message}")
            e.printStackTrace()
        }
    }
}