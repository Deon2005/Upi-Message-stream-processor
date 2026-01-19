package com.example.miniproject

import android.os.Bundle
import android.provider.Settings.Secure.putString
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.viewmodel.CreationExtras

class SetupDialogFragment : DialogFragment() {
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
        val submitbtn=view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.submitbtn)
        val savebtn=view.findViewById<androidx.appcompat.widget.AppCompatButton>(R.id.savebtn)
        val senderid=view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.senderid)
        val samplesms=view.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.samplesms)

        closebtn.setOnClickListener {
            dismiss()
        }
        submitbtn.setOnClickListener {
            if(senderid.text.toString().isNotEmpty() && samplesms.text.toString().isNotEmpty())
            {
                var sender=senderid.text.toString()
                var sample=samplesms.text.toString()
                val result = Bundle().apply {
                    putString("bundle_sender", sender)
                    putString("bundle_sample", sample)
                }
                parentFragmentManager.setFragmentResult("requestKey", result)
            }
            else
            {
                Toast.makeText(requireContext(), "Please fill all the fields", Toast.LENGTH_SHORT).show()
            }
        }
        savebtn.setOnClickListener {
            dismiss()
        }
    }
    fun onRegexGenerated(regex: String) {
        val regexTextView = view?.findViewById<TextView>(R.id.resultTextview)
        regexTextView?.setText(regex)
    }
    override fun onStart() {
        super.onStart()
        // To close the dialog on clicking outside dialog box
        dialog?.window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )

    }
}