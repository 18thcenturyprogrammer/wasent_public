package com.centuryprogrammer18thwasentsingleland.utils

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class PhoneNumTextWatcher(val editText:EditText) : TextWatcher {

    //we need to know if the user is erasing or inputing some new character
    var backspacingFlag :Boolean = false
    //we need to block the :afterTextChanges method to be called again after we just replaced the EditText text
    var editedFlag:Boolean = false
    //we need to mark the cursor position and restore it after the edition
    var cursorComplement:Int? = null

    override fun afterTextChanged(s: Editable?) {}


    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        //we store the cursor local relative to the end of the string in the EditText before the edition
        cursorComplement = s?.length?.minus(editText.selectionStart)
        //we check if the user ir inputing or erasing a character
        backspacingFlag = count > after
    }

    override fun onTextChanged(s: CharSequence?, p1: Int, p2: Int, p3: Int) {
        val string: String = s.toString()
        //what matters are the phone digits beneath the mask, so we always work with a raw string with only digits
        //what matters are the phone digits beneath the mask, so we always work with a raw string with only digits
        val phone = string.replace("[^\\d]".toRegex(), "")

        //if the text was just edited, :afterTextChanged is called another time... so we need to verify the flag of edition
        //if the flag is false, this is a original user-typed entry. so we go on and do some magic

        //if the text was just edited, :afterTextChanged is called another time... so we need to verify the flag of edition
        //if the flag is false, this is a original user-typed entry. so we go on and do some magic
        if (!editedFlag) {

            //we start verifying the worst case, many characters mask need to be added
            //example: 999999999 <- 6+ digits already typed
            // masked: (999) 999-999
            if (phone.length >= 6 && !backspacingFlag) {
                //we will edit. next call on this textWatcher will be ignored
                editedFlag = true
                //here is the core. we substring the raw digits and add the mask as convenient
                val ans =
                    "(" + phone.substring(0, 3) + ") " + phone.substring(
                        3,
                        6
                    ) + "-" + phone.substring(6)
                editText.setText(ans)
                //we deliver the cursor to its original position relative to the end of the string
                editText.setSelection(editText.text.length - cursorComplement!!)

                //we end at the most simple case, when just one character mask is needed
                //example: 99999 <- 3+ digits already typed
                // masked: (999) 99
            } else if (phone.length >= 3 && !backspacingFlag) {
                editedFlag = true
                val ans =
                    "(" + phone.substring(0, 3) + ") " + phone.substring(3)
                editText.setText(ans)
                editText.setSelection(editText.text.length - cursorComplement!!)
            }
            // We just edited the field, ignoring this cicle of the watcher and getting ready for the next
        } else {
            editedFlag = false
        }
    }
}