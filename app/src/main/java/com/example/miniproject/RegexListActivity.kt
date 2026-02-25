package com.example.miniproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import kotlinx.coroutines.launch

class RegexListActivity : AppCompatActivity() {

    private lateinit var dao: RegexDao
    private lateinit var adapter: RegexAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_regex_list)

        // 1. Setup Toolbar & RecyclerView
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        dao = AppDatabase.getDatabase(this).regexDao()
        adapter = RegexAdapter { regexItem -> deleteRule(regexItem) }

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewRegex)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 2. Load Data
        lifecycleScope.launch {
            dao.getAllRegex().collect { list ->
                adapter.submitList(list)
            }
        }

        // 3. FAB Click -> Show the Gemini Dialog
        findViewById<ExtendedFloatingActionButton>(R.id.fabAddRegex).setOnClickListener {
            val dialog = SetupDialogFragment()
            dialog.show(supportFragmentManager, "setupDialog")
        }

        // 4. REGISTER LISTENER (This was missing!)
        setupRegexListener()
    }

    // --- The Missing Logic ---
    private fun setupRegexListener() {
        supportFragmentManager.setFragmentResultListener("requestKey", this) { _, bundle ->

            // 1. Get Data from Dialog
            val senderName = bundle.getString("bundle_sender") ?: ""
            val sampleSMS = bundle.getString("bundle_sample") ?: ""
            val typeValue = bundle.getInt("bundle_type", 0)

            val activeDialog = supportFragmentManager.findFragmentByTag("setupDialog") as? SetupDialogFragment

            if (senderName.isNotEmpty() && sampleSMS.isNotEmpty()) {
                lifecycleScope.launch {

                    // 2. Show Loading on Dialog
                    activeDialog?.onLoading() // Make sure your Dialog has this public method
                    Toast.makeText(this@RegexListActivity, "Asking Gemini...", Toast.LENGTH_SHORT).show()

                    // 3. Call Gemini
                    val helper = GeminiHelper(this@RegexListActivity)
                    val result = helper.generateRegexFromSms(sampleSMS)

                    if (result != null) {
                        // 4. Save to Database
                        val newRow = Regexdatabase(
                            tid = 0,
                            regex = result.pattern,
                            name = senderName,
                            typecode = typeValue,
                            type = result.extractedWord // e.g., "debited"
                        )
                        dao.insertRegex(newRow)

                        Toast.makeText(this@RegexListActivity, "Rule Saved!", Toast.LENGTH_SHORT).show()
                        activeDialog?.dismiss()
                    } else {
                        Toast.makeText(this@RegexListActivity, "Failed to generate Regex", Toast.LENGTH_SHORT).show()
                        activeDialog?.onError() // Re-enable buttons
                    }
                }
            }
        }
    }

    private fun deleteRule(item: Regexdatabase) {
        MaterialAlertDialogBuilder(this)
            .setTitle("Delete Rule?")
            .setMessage("Delete rule for ${item.name}?")
            .setPositiveButton("Delete") { _, _ ->
                lifecycleScope.launch {
                    dao.deleteRegex(item)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}