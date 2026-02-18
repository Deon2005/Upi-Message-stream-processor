package com.example.miniproject

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

class SetupDialogFragment : DialogFragment() {

    var submitbtn: AppCompatButton? = null
    var senderid: TextInputEditText? = null
    var samplesms: TextInputEditText? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)//setting baground to transparent

        return inflater.inflate(R.layout.setupdialog, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val closebtn=view.findViewById<ImageButton>(R.id.closeButton)
        submitbtn=view.findViewById(R.id.submitbtn)
        senderid=view.findViewById(R.id.senderid)
        samplesms=view.findViewById(R.id.samplesms)

        closebtn.setOnClickListener {
            dismiss()
        }
        submitbtn?.setOnClickListener {
            if(senderid?.text.toString().isNotEmpty() && samplesms?.text.toString().isNotEmpty()) {
                var sender= senderid?.text.toString()
                var sample= samplesms?.text.toString()
                val result = Bundle().apply {
                    putString("bundle_sender", sender)
                    putString("bundle_sample", sample)
                }
                parentFragmentManager.setFragmentResult("requestKey", result)
            } else {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun onRegexGenerated(regex: String) {
        val senderid=view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.senderid)
        lifecycleScope.launch()
        {
            val database=AppDatabase.getDatabase(requireContext())
            val newRow = Regexdatabase(
                tid=0,
                regex=regex,
                name=senderid?.text.toString()
            )
            database.regexDao().insertRegex(newRow)
            Toast.makeText(context,"Saveed successfully!",Toast.LENGTH_LONG).show()
            dismiss()
        }
    }
    override fun onStart() {
        super.onStart()
        // To close the dialog on clicking outside dialog box
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }
    fun onLoading()
    {
        if(!isAdded) return
        submitbtn?.isEnabled=false
        submitbtn?.text="Generating..."
        submitbtn?.isClickable=false
    }

    fun onError()
    {
        if (!isAdded) return
        submitbtn?.isEnabled = true
        submitbtn?.text = "Try Again..."
        submitbtn?.isClickable=true
    }
}