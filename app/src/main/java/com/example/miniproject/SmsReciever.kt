package com.example.miniproject

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class SmsReciever : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent)
    {
        if(intent.action=="android.provider.Telephony.SMS_RECEIVED")
        {
            val pendingResult=goAsync()
            CoroutineScope(Dispatchers.Default).launch()
            {
                try
                {
                    handleSms(context,intent)
                }
                finally
                {
                    pendingResult.finish()
                }
            }
        }
    }
    private suspend fun handleSms(context: Context, intent: Intent) {
        val bundle = intent.extras
        val pdus = bundle?.get("pdus") as Array<*> ?: return

        for (pdu in pdus) {
            val format = bundle.getString("format")
            val smsMessage = android.telephony.SmsMessage.createFromPdu(pdu as ByteArray, format)

            val sender = smsMessage.displayOriginatingAddress
            val message = smsMessage.displayMessageBody
            Log.d("SMS_TEST", "$sender: $message")

            val parser = DataParser()
            val database = AppDatabase.getDatabase(context)

            // 1. Get ALL matching rules (List instead of single object)
            val rulesList = database.regexDao().getRegexBySender(sender)

            if (rulesList.isNotEmpty()) {
                var isParsed = false

                // 2. Loop through rules until one works
                for (rule in rulesList) {
                    Log.d("PARSER_ATTEMPT", "Trying rule: ${rule.name}")

                    // Try to parse using this rule
                    val success = parser.smsParser(context, message, rule)

                    if (success) {
                        Log.d("PARSER_SUCCESS", "Success with rule: ${rule.regex}")
                        isParsed = true
                        break // Stop checking other rules for this SMS
                    }
                }

                if (!isParsed) {
                    Log.d("PARSER_FAIL", "Matched sender but no rules worked for this message format.")
                }
            } else {
                Log.d("PARSER_FAIL", "No Regex Found for sender: $sender")
            }
        }
    }
}