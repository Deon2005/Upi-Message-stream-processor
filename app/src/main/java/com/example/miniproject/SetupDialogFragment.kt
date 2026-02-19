package com.example.miniproject

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.RadioGroup
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
    var radiogp: RadioGroup?=null
    var typeValue=-1

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
        radiogp=view.findViewById(R.id.radiogp)

        closebtn.setOnClickListener {
            dismiss()
        }
        submitbtn?.setOnClickListener {
            val selectedoptionId = radiogp?.checkedRadioButtonId?:-1
            if(senderid?.text.toString().isNotEmpty() && samplesms?.text.toString().isNotEmpty() && selectedoptionId != -1) {
                var sender= senderid?.text.toString()
                var sample= samplesms?.text.toString()
                typeValue=if(selectedoptionId==R.id.radio0) 0 else 1
                val result = Bundle().apply {
                    putString("bundle_sender", sender)
                    putString("bundle_sample", sample)
                    putInt("bundle_type", typeValue)
                }
                parentFragmentManager.setFragmentResult("requestKey", result)
            } else {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }

    }
    fun onRegexGenerated(regex: String,type : String) {
        val senderid=view?.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.senderid)
        lifecycleScope.launch()
        {
            val database=AppDatabase.getDatabase(requireContext())
            val newRow = Regexdatabase(
                tid=0,
                regex=regex,
                name=senderid?.text.toString(),
                typecode=typeValue,
                type=type
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