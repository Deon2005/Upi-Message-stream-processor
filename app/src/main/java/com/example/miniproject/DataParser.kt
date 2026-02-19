package com.example.miniproject

import android.content.Context
import android.util.Log
import java.time.LocalDate

class DataParser {

    suspend fun smsParser(context: Context, message: String, rule: Regexdatabase): Boolean {
        try {
            val database = AppDatabase.getDatabase(context)

            val match = Regex(rule.regex, RegexOption.IGNORE_CASE).find(message)

            if (match != null) {
                val accNoString = match.groups["account"]?.value ?: ""
                val typeString = match.groups["type"]?.value ?: ""
                val rawAmount = match.groups["amount"]?.value ?: "0"
                val upiString = match.groups["upi"]?.value ?: ""
                val regexcode=rule.typecode
                var typecode=-1
                if(typeString.lowercase().trim()== rule.type.lowercase().trim())
                {
                    typecode=regexcode
                }
                else if(regexcode==0)
                {
                    typecode=1
                }
                else
                {
                    typecode=0
                }

                val finalAmount = rawAmount.replace(",", "").toDoubleOrNull() ?: 0.0

                val date = LocalDate.now()

                Log.d(
                    "PARSER_SUCCESS",
                    "Paid $finalAmount to $upiString on $date (Acc: $accNoString) $typeString"
                )

                val newTransaction = Transaction(
                    id = 0,
                    amount = finalAmount,
                    date = date,
                    type = typecode,
                    upiID = upiString,
                    accountNumber = accNoString
                )

                database.transactionDao().insertTransaction(newTransaction)
                Log.d("PARSER_SUCCESS", "✅ Saved to database")
                return true
            }
            Log.e("PARSER_FAIL", "❌ Regex did not match: $message")
        } catch (e: Exception) {
            Log.e("PARSER_FAIL", "❌ Error: $e")
        }
        return false
    }
}