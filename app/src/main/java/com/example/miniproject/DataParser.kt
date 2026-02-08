package com.example.miniproject

import android.util.Log

class DataParser
{
    fun smsParser( message: String, regex: String): Boolean
    {
            val match = Regex(regex).find(message)
            if (match != null) {
                val accNo = match.groupValues[1]
                val type = match.groupValues[2]
                val amount = match.groupValues[3]
                val date = match.groupValues[4]
                val upi = match.groupValues[5]

                Log.d("PARSER_SUCCESS", "Paid $amount to $upi on $date (Acc: $accNo, Type: $type)")
                return true
            }
            return false
    }
}