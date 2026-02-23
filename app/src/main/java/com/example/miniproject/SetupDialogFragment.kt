package com.example.miniproject

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch

class SetupDialogFragment : DialogFragment() {

    private var submitbtn: MaterialButton? = null
    private var senderid: EditText? = null
    private var samplesms: EditText? = null
    private var typeValue = 0 // Default: Debit (radio0)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return inflater.inflate(R.layout.setupdialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        val closebtn = view.findViewById<ImageButton>(R.id.closeButton)
        val radiogp = view.findViewById<MaterialButtonToggleGroup>(R.id.radiogp)
        submitbtn = view.findViewById(R.id.submitbtn)
        senderid = view.findViewById(R.id.senderid)
        samplesms = view.findViewById(R.id.samplesms)

        closebtn.setOnClickListener { dismiss() }

        // Adaptive Color Logic for Toggle Group
        val activeColor = ColorStateList.valueOf(Color.parseColor("#1F6FEB")) // Blue accent
        val inactiveColor = ColorStateList.valueOf(Color.TRANSPARENT)

        // Set initial state (matches XML checkedButton="@+id/radio0")
        view.findViewById<MaterialButton>(R.id.radio0).backgroundTintList = activeColor

        radiogp.addOnButtonCheckedListener { group, checkedId, isChecked ->
            val button = view.findViewById<MaterialButton>(checkedId)
            if (isChecked) {
                button.backgroundTintList = activeColor
                typeValue = if (checkedId == R.id.radio0) 0 else 1
            } else {
                button.backgroundTintList = inactiveColor
            }
        }

        submitbtn?.setOnClickListener {
            val sender = senderid?.text.toString().trim()
            val sample = samplesms?.text.toString().trim()

            if (sender.isNotEmpty() && sample.isNotEmpty()) {
                val result = Bundle().apply {
                    putString("bundle_sender", sender)
                    putString("bundle_sample", sample)
                    putInt("bundle_type", typeValue)
                }
                parentFragmentManager.setFragmentResult("requestKey", result)
                // We keep the dialog open while MainActivity asks Gemini for the Regex
            } else {
                Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun onRegexGenerated(regex: String, type: String) {
        val name = senderid?.text.toString()
        lifecycleScope.launch {
            val database = AppDatabase.getDatabase(requireContext())
            val newRow = Regexdatabase(
                tid = 0,
                regex = regex,
                name = name,
                typecode = typeValue,
                type = type
            )
            database.regexDao().insertRegex(newRow)
            Toast.makeText(context, "Saved successfully!", Toast.LENGTH_LONG).show()
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    fun onLoading() {
        submitbtn?.apply {
            isEnabled = false
            text = "Generating..."
        }
    }

    fun onError() {
        submitbtn?.apply {
            isEnabled = true
            text = "Try Again"
        }
    }
}