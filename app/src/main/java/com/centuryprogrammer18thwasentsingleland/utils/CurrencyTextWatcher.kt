package com.centuryprogrammer18thwasentsingleland.utils

import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import java.lang.ref.WeakReference
import java.text.NumberFormat

// currency mask textwatcher
// ref) https://stackoverflow.com/a/24621325/3151712

class CurrencyTextWatcher(val editText : EditText) : TextWatcher {

    private val editTextWeakReference: WeakReference<EditText> = WeakReference<EditText>(editText)
    var current =""

    override fun afterTextChanged(editable: Editable?) {
        val editText: EditText = editTextWeakReference.get() ?: return
        val s = editable.toString()
        if (s.isEmpty()) return
        editText.removeTextChangedListener(this)
        val cleanString = s.replace("[$,.]".toRegex(), "")
        Log.d("CurrencyTextWatcher", "##### cleanString | ${cleanString} " )
        Log.d("CurrencyTextWatcher", "##### cleanString length | ${cleanString.length.toString()} " )

        val parsed: Double = cleanString.toDouble()/100

        val formatted: String = NumberFormat.getCurrencyInstance().format(parsed)
        Log.d("CurrencyTextWatcher", "##### formatted | ${formatted} " )
        Log.d("CurrencyTextWatcher", "##### formatted length | ${formatted.length.toString()} " )

        editText.setText(formatted)

        editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(9))

        editText.setSelection(formatted.length)


        editText.addTextChangedListener(this)
    }

    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

    }
}