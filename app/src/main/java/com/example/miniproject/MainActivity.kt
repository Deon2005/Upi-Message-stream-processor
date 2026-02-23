package com.example.miniproject

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Only load the fragments the VERY FIRST time the activity is created
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_content_container, HomeFragment())
                .replace(R.id.bottom_nav_container, NavBarFragment())
                .commit()
        }

        requestPermission()
        checkifnoregex()
    }

    private fun requestPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECEIVE_SMS), 1)
        }
    }

    private fun checkifnoregex() {
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(this@MainActivity)
            val count = database.regexDao().getCount()
            if (count == 0) {
                callSetupDialog()
                receiveRegex()
            }
        }
    }

    private fun receiveRegex() {
        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->
            val sampleSMS = bundle.getString("bundle_sample")
            val activedialog = supportFragmentManager.findFragmentByTag("setupDialog") as? SetupDialogFragment

            if (sampleSMS != null) {
                lifecycleScope.launch {
                    activedialog?.onLoading()
                    Toast.makeText(this@MainActivity, "Asking Gemini for Regex...", Toast.LENGTH_SHORT).show()

                    val helper = GeminiHelper(this@MainActivity)
                    val result = helper.generateRegexFromSms(sampleSMS)

                    if (result != null) {
                        Toast.makeText(this@MainActivity, "Regex Fetched!", Toast.LENGTH_LONG).show()
                        if (activedialog != null && activedialog.isVisible) {
                            activedialog.onRegexGenerated(
                                regex = result.pattern,
                                type = result.extractedWord
                            )
                        }
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