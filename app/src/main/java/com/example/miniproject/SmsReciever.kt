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
    private suspend fun handleSms(context: Context,intent: Intent)
    {
        val bundle=intent.extras
        val pdus=bundle?.get("pdus") as Array<*> ?: return

        for(pdu in pdus)
        {
            val format=bundle.getString("format")
            val smsMessage=android.telephony.SmsMessage.createFromPdu(pdu as ByteArray,format)

            val sender=smsMessage.displayOriginatingAddress
            val message=smsMessage.displayMessageBody
            Log.d("SMS_TEST","$sender: $message")
            val parser=DataParser()
            val database=AppDatabase.getDatabase(context)
            val rule=database.regexDao().getRegexBySender(sender)
            if(rule!=null)
            {
                val regex= rule.regex
                Log.d("PARSER_SUCCESS", "Regex: $regex")
                val success = parser.smsParser( context,message, regex)
                if (success) {
                    Log.d("PARSER_SUCCESS", "Success")
                } else {
                    Log.d("PARSER_SUCCESS", "Failed")
                }
            }
            else
            {
                Log.d("PARSER_SUCCESS", "No Regex Found")
            }
        }
    }
}