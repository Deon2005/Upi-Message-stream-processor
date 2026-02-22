package com.example.miniproject

import android.app.DatePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionDialogFragment : DialogFragment() {

    private var isEditMode = false
    private var transactionId = 0L
    private var selectedDate = LocalDate.now()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return inflater.inflate(R.layout.dialog_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val title = view.findViewById<TextView>(R.id.dialog_title)
        val inputAmount = view.findViewById<EditText>(R.id.dialog_input_amount)
        val toggleGroup = view.findViewById<MaterialButtonToggleGroup>(R.id.dialog_toggle_type)
        val btnExpense = view.findViewById<MaterialButton>(R.id.btn_type_expense)
        val btnIncome = view.findViewById<MaterialButton>(R.id.btn_type_income)
        val inputDesc = view.findViewById<EditText>(R.id.dialog_input_desc)
        val inputSource = view.findViewById<EditText>(R.id.dialog_input_source)
        val textDate = view.findViewById<TextView>(R.id.dialog_text_date)
        val btnCancel = view.findViewById<View>(R.id.dialog_btn_cancel)
        val btnSave = view.findViewById<View>(R.id.dialog_btn_save)

        // --- FIX 1: VISIBLE TOGGLE SELECTION ---
        // Listen for clicks and change the background tint of the selected button
        toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btn_type_expense) {
                    btnExpense.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#30363D"))
                    btnIncome.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                } else {
                    btnIncome.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#30363D"))
                    btnExpense.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
                }
            }
        }

        arguments?.let {
            isEditMode = it.getBoolean("isEdit")
            if (isEditMode) {
                title.text = "Edit Transaction"
                transactionId = it.getLong("id")

                inputAmount.setText(it.getDouble("amount").toString())
                inputDesc.setText(it.getString("desc"))
                inputSource.setText(it.getString("source"))

                if (it.getInt("type") == 0) toggleGroup.check(R.id.btn_type_expense)
                else toggleGroup.check(R.id.btn_type_income)

                selectedDate = LocalDate.parse(it.getString("date"))
            } else {
                // Ensure default state is highlighted for new entries
                toggleGroup.check(R.id.btn_type_expense)
            }
        }

        val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
        textDate.text = selectedDate.format(formatter)

        // --- FIX 2: DATE PICKER ---
        textDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    // Android's calendar months are 0-indexed (Jan = 0), so we add 1 for LocalDate
                    selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                    textDate.text = selectedDate.format(formatter)
                },
                selectedDate.year,
                selectedDate.monthValue - 1, // Subtract 1 to match Android's 0-indexed calendar
                selectedDate.dayOfMonth
            )
            datePickerDialog.show()
        }

        btnCancel.setOnClickListener { dismiss() }

        btnSave.setOnClickListener {
            val amount = inputAmount.text.toString().toDoubleOrNull() ?: 0.0
            val type = if (toggleGroup.checkedButtonId == R.id.btn_type_expense) 0 else 1

            val updatedTransaction = Transaction(
                id = if (isEditMode) transactionId else 0L,
                amount = amount,
                type = type,
                upiID = inputDesc.text.toString(),
                accountNumber = inputSource.text.toString(),
                date = selectedDate
            )

            val dao = AppDatabase.getDatabase(requireContext()).transactionDao()
            lifecycleScope.launch {
                if (isEditMode) {
                    dao.update(updatedTransaction)
                } else {
                    dao.insertTransaction(updatedTransaction)
                }
                dismiss()
            }
        }
    }

    companion object {
        fun newInstance(transaction: Transaction? = null): TransactionDialogFragment {
            val fragment = TransactionDialogFragment()
            val bundle = Bundle()

            if (transaction != null) {
                bundle.putBoolean("isEdit", true)
                bundle.putLong("id", transaction.id)
                bundle.putDouble("amount", transaction.amount)
                bundle.putInt("type", transaction.type)
                bundle.putString("desc", transaction.upiID)
                bundle.putString("source", transaction.accountNumber)
                bundle.putString("date", transaction.date.toString())
            } else {
                bundle.putBoolean("isEdit", false)
            }

            fragment.arguments = bundle
            return fragment
        }
    }
}