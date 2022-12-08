package com.centuryprogrammer18thwasentsingleland.utils

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.Preference
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R



//android:singleLine android:inputType
//ref) https://stackoverflow.com/a/55461028/3151712

class NonEmptySingleLineNumEditTextPreference :EditTextPreference {
    private val TAG = NonEmptySingleLineNumEditTextPreference::class.java.simpleName

    fun setNotAllowedEmpty(){
        this.setOnPreferenceChangeListener(object : Preference.OnPreferenceChangeListener {
            override fun onPreferenceChange(preference: Preference?, newValue: Any?): Boolean {
                Log.d(TAG, "===== newValue is '${newValue.toString()}'=====" )
                if (newValue.toString() ==""){

                    // Toast extension function
                    Toast(context).showCustomToast(context, App.resourses!!.getString(R.string.empty_not_allowed))
                    return false
                }else{
                    return true
                }
            }
        })
    }

    fun setOnlyNumber(){
        this.setOnBindEditTextListener(object : EditTextPreference.OnBindEditTextListener{
            override fun onBindEditText(editText: EditText) {
                editText.inputType = InputType.TYPE_CLASS_NUMBER
            }
        })
    }

    fun setSingleLine(){
        this.setOnBindEditTextListener(object : EditTextPreference.OnBindEditTextListener{
            override fun onBindEditText(editText: EditText) {
                editText.setSingleLine(true)
            }
        })
    }

    init {
        setNotAllowedEmpty()
        setSingleLine()
        setOnlyNumber()
    }

    //    multiple constuctor in kotlin
//    ref) https://stackoverflow.com/a/56277144/3151712
    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {}

}