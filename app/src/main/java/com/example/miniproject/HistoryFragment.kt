package com.example.miniproject

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast // Added for the click listener example
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryFragment : Fragment() {

    private val viewModel: MainViewModel by viewModels()

    // --- FIX 1: Initialize Adapter exactly as your class requires ---
    private val historyAdapter = TransactionAdapter(
        emptyList(),
        onEditClick = { transaction ->
            // This is the part that was missing!
            val dialog = TransactionDialogFragment.newInstance(transaction)
            dialog.show(childFragmentManager, "TransactionDialog")
        },
        onDeleteClick = { transaction ->
            viewModel.deleteTransaction(transaction)
        }
    )

    // Filter State Variables
    private var filterType = "All"
    private var filterMethod = "All"
    private var filterDateMode = "All"

    // Date Variables
    private var selectedStartDate: Long = 0L
    private var selectedEndDate: Long = System.currentTimeMillis()

    // UI Components
    private lateinit var btnSpecificDate: MaterialButton
    private lateinit var btnStartDate: MaterialButton
    private lateinit var btnEndDate: MaterialButton
    private lateinit var layoutSpecificDate: LinearLayout
    private lateinit var layoutDateRange: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Views
        layoutSpecificDate = view.findViewById(R.id.layoutSpecificDate)
        layoutDateRange = view.findViewById(R.id.layoutDateRange)
        btnSpecificDate = view.findViewById(R.id.btnSpecificDate)
        btnStartDate = view.findViewById(R.id.btnStartDate)
        btnEndDate = view.findViewById(R.id.btnEndDate)

        // Setup RecyclerView
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerViewHistory)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = historyAdapter // Attach your adapter

        val toggleDateMode = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleDateMode)
        val toggleType = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleType)
        val toggleMethod = view.findViewById<MaterialButtonToggleGroup>(R.id.toggleMethod)

        // Set Default Active States
        setButtonActiveState(view.findViewById(R.id.dateModeAll), true)
        setButtonActiveState(view.findViewById(R.id.typeAll), true)
        setButtonActiveState(view.findViewById(R.id.methodAll), true)

        // Load Initial Data
        applyFilters()


        // --- LISTENERS ---

        // 1. DATE TOGGLE
        toggleDateMode.addOnButtonCheckedListener { _, checkedId, isChecked ->
            val button = view.findViewById<MaterialButton>(checkedId)
            setButtonActiveState(button, isChecked)

            if (isChecked) {
                when (checkedId) {
                    R.id.dateModeAll -> {
                        layoutSpecificDate.visibility = View.GONE
                        layoutDateRange.visibility = View.GONE
                        filterDateMode = "All"
                    }
                    R.id.dateModeSpecific -> {
                        layoutSpecificDate.visibility = View.VISIBLE
                        layoutDateRange.visibility = View.GONE
                        filterDateMode = "Specific"
                        if (selectedStartDate == 0L) showDatePicker(false)
                    }
                    R.id.dateModeRange -> {
                        layoutSpecificDate.visibility = View.GONE
                        layoutDateRange.visibility = View.VISIBLE
                        filterDateMode = "Range"
                        if (selectedStartDate == 0L) showDatePicker(true)
                    }
                }
                applyFilters()
            }
        }

        // 2. TYPE TOGGLE
        toggleType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            val button = view.findViewById<MaterialButton>(checkedId)
            setButtonActiveState(button, isChecked)
            if (isChecked) {
                filterType = when (checkedId) {
                    R.id.typeDebit -> "Debit"
                    R.id.typeCredit -> "Credit"
                    else -> "All"
                }
                applyFilters()
            }
        }

        // 3. METHOD TOGGLE
        toggleMethod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            val button = view.findViewById<MaterialButton>(checkedId)
            setButtonActiveState(button, isChecked)
            if (isChecked) {
                filterMethod = when (checkedId) {
                    R.id.methodOnline -> "Online"
                    R.id.methodCash -> "Cash"
                    else -> "All"
                }
                applyFilters()
            }
        }

        // 4. DATE BUTTON CLICKS
        btnSpecificDate.setOnClickListener { showDatePicker(false) }
        val rangeClickListener = View.OnClickListener { showDatePicker(true) }
        btnStartDate.setOnClickListener(rangeClickListener)
        btnEndDate.setOnClickListener(rangeClickListener)
    }

    private fun showDatePicker(isRange: Boolean) {
        if (isRange) {
            val builder = MaterialDatePicker.Builder.dateRangePicker()
            builder.setTitleText("Select Range")
            val picker = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                selectedStartDate = selection.first
                selectedEndDate = selection.second
                btnStartDate.text = formatDate(selectedStartDate)
                btnEndDate.text = formatDate(selectedEndDate)
                applyFilters()
            }
            picker.show(parentFragmentManager, "RangePicker")
        } else {
            val builder = MaterialDatePicker.Builder.datePicker()
            builder.setTitleText("Select Date")
            val picker = builder.build()
            picker.addOnPositiveButtonClickListener { selection ->
                selectedStartDate = selection
                selectedEndDate = selection
                btnSpecificDate.text = formatDate(selection)
                applyFilters()
            }
            picker.show(parentFragmentManager, "DatePicker")
        }
    }

    private fun applyFilters() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.getHistory(
                filterType,
                filterMethod,
                filterDateMode,
                selectedStartDate,
                selectedEndDate
            ).collect { transactionList ->

                // DEBUG: This will tell us if data is actually arriving!
                android.widget.Toast.makeText(context, "Found ${transactionList.size} items", android.widget.Toast.LENGTH_SHORT).show()

                // Update the adapter
                historyAdapter.updateData(transactionList)
                historyAdapter.closeMenu()
            }
        }
    }

    private fun setButtonActiveState(button: MaterialButton, isActive: Boolean) {
        if (isActive) {
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#1F6FEB"))
            button.setTextColor(android.graphics.Color.WHITE)
            button.strokeWidth = 0
        } else {
            button.backgroundTintList = android.content.res.ColorStateList.valueOf(android.graphics.Color.TRANSPARENT)
            button.setTextColor(android.graphics.Color.WHITE)
            button.strokeColor = android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#30363D"))
            button.strokeWidth = 3
        }
    }

    private fun showEditDialog(transaction: Transaction) {

        Toast.makeText(context, "Opening Edit for ${transaction.amount}", Toast.LENGTH_SHORT).show()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd MMM", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}