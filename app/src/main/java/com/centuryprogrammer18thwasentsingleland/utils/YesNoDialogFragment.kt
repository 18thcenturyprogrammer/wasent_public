package com.centuryprogrammer18thwasentsingleland.utils

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.centuryprogrammer18thwasentsingleland.R

//for studying DialogFragment
// ref) two ways DialogFragment https://blog.mindorks.com/implementing-dialog-fragment-in-android
// ref) DialogFragment with Navigation https://stackoverflow.com/a/55256858/3151712
// ref) DialogFragmetn https://stackoverflow.com/a/47514643/3151712
// ref) youtube Android Studio DIalog Fragment Tutorial https://www.youtube.com/watch?v=alV6wxrbULs&t=2s

class YesNoDialogFragment : DialogFragment() {
    private val TAG = YesNoDialogFragment::class.java.simpleName

//    private lateinit var args : YesNoDialogFragmentArgs

    private var setFullScreen:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Hey", "onCreate")
        var  setFullScreen = false

//        get the arguments which are passed by safe args
//        ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-start-external-activity/index.html?index=..%2F..android-kotlin-fundamentals#3
//        args = YesNoDialogFragmentArgs.fromBundle(requireArguments())

    }

    // if i want to make dialog from layout xml , i need to make it as below
//    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
//        return inflater.inflate(R.layout.fragment_yes_no_dialog, container, false)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?):Dialog {

        val builder = AlertDialog.Builder(requireContext())
//        builder.setTitle(args.title)
//        builder.setMessage(args.message)

        builder.setPositiveButton(R.string.ok,object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                dismiss()
            }

        })
        builder.setNegativeButton(R.string.cancel, object: DialogInterface.OnClickListener {
            override fun onClick(dialog:DialogInterface, which:Int) {
                dismiss()
            }
        })
        return builder.create()
    }
}