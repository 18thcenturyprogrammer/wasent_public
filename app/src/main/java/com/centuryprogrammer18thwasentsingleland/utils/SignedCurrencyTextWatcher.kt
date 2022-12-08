package com.centuryprogrammer18thwasentsingleland.utils

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import java.lang.ref.WeakReference
import java.text.DecimalFormat
import java.text.NumberFormat

class SignedCurrencyTextWatcher (val editText : EditText) : TextWatcher {
    private val TAG = SignedCurrencyTextWatcher::class.java.simpleName

    private val editTextWeakReference: WeakReference<EditText> = WeakReference<EditText>(editText)
    var current =""

    override fun afterTextChanged(editable: Editable?) {
//        Log.d(TAG,"##### afterTextChanged #####")

        val editText: EditText = editTextWeakReference.get() ?: return
        val s = editable.toString()
//        Log.d(TAG,"##### s is '${s}' #####")

        if (s.isEmpty()) return
        editText.removeTextChangedListener(this)
        var cleanString = s.replace("[$,.]".toRegex(), "")
//        Log.d(TAG,"##### cleanString is '${cleanString}' #####")

        var formatted = ""

        if (cleanString == "-"){
             formatted = "-"
        }else{
            val parsed: Double = cleanString.toDouble()/100
//            Log.d(TAG,"##### parsed is '${parsed.toString()}' #####")

            val formatter = NumberFormat.getCurrencyInstance()  as DecimalFormat
            val symbol = formatter.currency.symbol
            formatter.negativePrefix = "-"+symbol // or "-"+symbol if that's what you need
            formatter.negativeSuffix = ""

            formatted = formatter.format(parsed)
//            Log.d(TAG,"##### formatted is '${formatted}' #####")
        }


        editText.setText(formatted)

        editText.setSelection(formatted.length)
        editText.addTextChangedListener(this)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
//        Log.d(TAG,"##### beforeTextChanged #####")
    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
//        Log.d(TAG,"##### onTextChanged #####")
//        Log.d(TAG,"##### CharSequence : ${s.toString()}, start: ${start.toString()}  , before: ${before.toString()} , count: ${count.toString()}#####")

    }
}