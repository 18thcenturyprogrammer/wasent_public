package com.centuryprogrammer18thwasentsingleland.utils

import android.content.SharedPreferences
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.centuryprogrammer18thwasentsingleland.data.InvoiceWithPayment
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import java.lang.StringBuilder


// imeoption done button click ref) https://stackoverflow.com/a/59758037/3151712
fun EditText.onDone(callback: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            callback.invoke()
            true
        }
        false
    }
}


// this is for passing data back to previouse fragment when popupto is happened
// navigation pass data popupto popup ref) https://stackoverflow.com/a/60757744/3151712
// navigation pass data popupto popup android docs ref) https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result
fun Fragment.getNavigationResult(key: String = "result") =
    findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<MutableList<InvoiceWithPayment>>(key)

fun Fragment.setNavigationResult(result: MutableList<InvoiceWithPayment>, key: String = "result") {
    findNavController().previousBackStackEntry?.savedStateHandle?.set(key, result)
}

fun Fragment.makeTopTicket(sharedPrefs:SharedPreferences):MutableList<PrintingJob>{
    val cleanerName =
        StringBuilder().
        append(sharedPrefs.getString("cleaner_name","")).
        append("\r\n").
        toString()

    val cleanerAddress =
        StringBuilder().
        append(sharedPrefs.getString("cleaner_street_address","")).
        append("\r\n").
        append(sharedPrefs.getString("cleaner_city","")).
        append(" , ").
        append(sharedPrefs.getString("cleaner_state","")).
        append(" ").
        append(sharedPrefs.getString("cleaner_zipcode","")).
        append("\r\n").
        toString()

    val cleanerPhone =
        StringBuilder().
        append(sharedPrefs.getString("cleaner_phone_num","")).
        append("\r\n").
        toString()

    val printingJobs = mutableListOf<PrintingJob>()

    printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())
    printingJobs.add(GenstarPrint.makeLargeBoldPrintingJob(cleanerName))
    printingJobs.add(GenstarPrint.makeSmallBoldPrintingJob(cleanerAddress))
    printingJobs.add(GenstarPrint.makeSmallBoldPrintingJob(cleanerPhone))
    printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

    return printingJobs
}