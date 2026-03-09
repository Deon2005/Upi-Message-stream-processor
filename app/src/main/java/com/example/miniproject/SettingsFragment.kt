package com.example.miniproject

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlin.jvm.java

class SettingsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup Parsing Rules Click
        val cardRegexManager = view.findViewById<MaterialCardView>(R.id.cardRegexManager)
        cardRegexManager.setOnClickListener {
            // Navigate to the Regex Manager Screen
            val intent = Intent(requireContext(), RegexListActivity::class.java)
            startActivity(intent)
        }

        // 2. Setup Auto-Delete Switch
        val switchAutoDelete = view.findViewById<SwitchMaterial>(R.id.switchAutoDelete)

        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),  // State when ON
            intArrayOf(-android.R.attr.state_checked)  // State when OFF (the minus sign means "not")
        )
        val colors = intArrayOf(
            Color.parseColor("#1F6FEB"), // Color when ON (e.g., your app's blue accent)
            Color.parseColor("#424242")  // Color when OFF (e.g., dark gray)
        )
        val colorStateList = ColorStateList(states, colors)
        switchAutoDelete.trackTintList = colorStateList
        val sharedPref = requireActivity().getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)
        val isAutoDeleteEnabled = sharedPref.getBoolean("auto_delete_90", false)
        switchAutoDelete.isChecked = isAutoDeleteEnabled

        // Save new state when toggled
        switchAutoDelete.setOnCheckedChangeListener { _, isChecked ->
            with(sharedPref.edit()) {
                putBoolean("auto_delete_90", isChecked)
                apply()
            }

            if (isChecked) {
                Toast.makeText(context, "Auto-delete enabled: Data older than 90 days will be removed daily.", Toast.LENGTH_LONG).show()
                // You can schedule the WorkManager task here if you want immediate action
            } else {
                Toast.makeText(context, "Auto-delete disabled.", Toast.LENGTH_SHORT).show()
            }
        }
        // Inside onViewCreated...

        switchAutoDelete.setOnCheckedChangeListener { _, isChecked ->
            // 1. Save Preference
            with(sharedPref.edit()) {
                putBoolean("auto_delete_90", isChecked)
                apply()
            }

            if (isChecked) {
                // 2. Schedule the Worker
                val deleteRequest = androidx.work.PeriodicWorkRequestBuilder<AutoDeleteWorker>(
                    24, java.util.concurrent.TimeUnit.HOURS // Run every 24 hours
                ).build()

                androidx.work.WorkManager.getInstance(requireContext())
                    .enqueueUniquePeriodicWork(
                        "AutoDeleteWork",
                        androidx.work.ExistingPeriodicWorkPolicy.KEEP, // Don't replace if already running
                        deleteRequest
                    )

                Toast.makeText(context, "Auto-delete scheduled daily.", Toast.LENGTH_SHORT).show()
            } else {
                // 3. Cancel if turned off
                androidx.work.WorkManager.getInstance(requireContext())
                    .cancelUniqueWork("AutoDeleteWork")

                Toast.makeText(context, "Auto-delete cancelled.", Toast.LENGTH_SHORT).show()
            }
        }
    }
}