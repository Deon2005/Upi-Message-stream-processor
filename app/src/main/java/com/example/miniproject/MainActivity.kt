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

        // Open the dialog immediately
        callSetupDialog()

        // Listen for the result from the SetupDialogFragment
        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            // We only need the sample SMS to generate the pattern
            val sampleSMS = bundle.getString("bundle_sample")

            if (sampleSMS != null) {
                lifecycleScope.launch {
                    Toast.makeText(this@MainActivity, "Asking Gemini for Regex...", Toast.LENGTH_SHORT).show()

                    val helper = GeminiHelper(this@MainActivity)

                    // 1. Fetch ONLY the Regex string
                    val regexPattern = helper.generateRegexFromSms(sampleSMS)

                    if (regexPattern != null) {
                        // 2. Just Log and Toast the raw pattern
                        Log.d("GEMINI_TEST", "✅ FINAL REGEX: $regexPattern")
                        Toast.makeText(this@MainActivity, "Regex Fetched!", Toast.LENGTH_LONG).show()
                        if(regexPattern!=null)
                        {
                            val activedialog=supportFragmentManager.findFragmentByTag("setupDialog") as? SetupDialogFragment
                            if (activedialog!=null&&activedialog.isVisible)
                            {
                                activedialog.onRegexGenerated(regexPattern)
                            }
                        }
                        // TODO: Save 'regexPattern' to SharedPreferences or a database here if needed
                    } else {
                        Log.e("GEMINI_TEST", "❌ Failed to generate Regex")
                        Toast.makeText(this@MainActivity, "Failed to get Regex", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun callSetupDialog() {
        val dialog = SetupDialogFragment()
        dialog.show(supportFragmentManager, "setupDialog")
    }
}