package com.centuryprogrammer18thwasentsingleland.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class NotifyDialogFragment : DialogFragment() {

    private lateinit var args : NotifyDialogFragmentArgs

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = NotifyDialogFragmentArgs.fromBundle(requireArguments())
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {


        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(args.title)
        builder.setMessage(args.message)

        val buttonText = args.buttonText ?: "Ok"

        builder.setPositiveButton(buttonText,object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dismiss()
            }

        })
        return builder.create()
    }

}