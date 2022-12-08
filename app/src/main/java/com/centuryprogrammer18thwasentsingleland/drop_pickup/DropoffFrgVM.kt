package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.util.Log
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.Dropoff
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.repository.AddDropoffCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.DatePickerCallback
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentTimeString
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentTimestamp
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat

class DropoffFrgVM :  ViewModel(), DatePickerDialog.OnDateSetListener, AddDropoffCallback,
    DatePickerCallback {
    private val TAG = DropoffFrgVM::class.java.simpleName

    // both way data binding

    var dry = MutableLiveData<String>("")
    var wet = MutableLiveData<String>("")
    var alter = MutableLiveData<String>("")
    var household = MutableLiveData<String>("")
    var cleanOnly = MutableLiveData<String>("")
    var pressOnly = MutableLiveData<String>("")
    var ext = MutableLiveData<String>("")
    var redo = MutableLiveData<String>("")

    private lateinit var customer: Customer

    private lateinit var sharedPrefs : SharedPreferences

    // used as switch flag for enabling edittext
    private val _cancelClicked = MutableLiveData<Boolean>()
    val cancelClicked : LiveData<Boolean>
        get() = _cancelClicked

    // used as flag for disabled edittext
    private val _okClicked = MutableLiveData<Boolean>()
    val okClicked : LiveData<Boolean>
        get() = _okClicked

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private var _printingContent : MutableList<PrintingJob>? = null

    private val _printing = MutableLiveData<MutableList<PrintingJob>>()
    val printing : LiveData<MutableList<PrintingJob>>
        get() = _printing


    // init var
    fun setCustomer(receivedCustomer:Customer){
        customer = receivedCustomer
    }

    // init var
    fun setSharedPrefs(receivedSharedPrefs:SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    // called from dropoff_frg.xml
    fun onClickCancel(){
        Log.d(TAG,"----- onClickCancel -----")

        clearVars()
        _cancelClicked.value = true
    }

    // called from dropoff_frg.xml
    fun onClickOk(){
        Log.d(TAG,"----- onClickOk -----")

        if(anyFilledField()){
            // user filled field
            Log.d(TAG,"----- dry : ''${dry} -----")
            Log.d(TAG,"----- wet : ''${wet} -----")
            Log.d(TAG,"----- alter : ''${alter} -----")
            Log.d(TAG,"----- household : ''${household} -----")
            Log.d(TAG,"----- cleanOnly : ''${cleanOnly} -----")
            Log.d(TAG,"----- pressOnly : ''${pressOnly} -----")
            Log.d(TAG,"----- ext : ''${ext} -----")
            Log.d(TAG,"----- redo : ''${redo} -----")

            _okClicked.value = true
        }
    }

    // called by showDatePicker in etc.kt after user click cancel button on data picker
    override fun onCancelBtnClicked(boolean: Boolean) {
        onClickCancel()
    }

    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        Log.d(TAG,"----- onDateSet -----")
        Log.d(TAG,"----- year : ${year.toString()} ----- month :${month.toString()} ----- day : ${day.toString()} -----")

        val qtyTable = mutableMapOf<String,Int>(
            "dry" to if(dry.value!!.isEmpty()) 0 else dry.value!!.toInt(),
            "wet" to if(wet.value!!.isEmpty()) 0 else wet.value!!.toInt(),
            "alter" to if(alter.value!!.isEmpty()) 0 else alter.value!!.toInt(),
            "household" to if(household.value!!.isEmpty()) 0 else household.value!!.toInt(),
            "clean_only" to if(cleanOnly.value!!.isEmpty()) 0 else cleanOnly.value!!.toInt(),
            "press_only" to if(pressOnly.value!!.isEmpty()) 0 else pressOnly.value!!.toInt(),
            "ext" to if(ext.value!!.isEmpty()) 0 else ext.value!!.toInt(),
            "redo" to if(redo.value!!.isEmpty()) 0 else redo.value!!.toInt()
        )

        val dueDateTime = StringBuilder().append(year).append("-").append(month+1).append("-").append(day).toString()

        val dueDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dueTimestamp = dueDateFormat.parse(dueDateTime).time

        val dropoff = Dropoff(
            customer,
            qtyTable,
            getCurrentTimeString(),
            sharedPrefs.getString("logged_team_id",null),
            getCurrentTimestamp(),
            dueDateTime,
            dueTimestamp
        )

        _printingContent = dropoff.makePrintTicket()

        Log.d(TAG,"----- _printingContent : ${_printingContent.toString()} -----")

        Repository.addDropoff(sharedPrefs,dropoff, this)

    }

    override fun onAddDropoffCallback(isSuccess: Boolean) {
        if(isSuccess){
            // drop off was added successfully

            Log.d(TAG,"----- drop off was added into firebase successfully -----")

            _msgToFrg.value = App.resourses!!.getString(R.string.drop_off_was_added_successfully)
            _printing.value = _printingContent

            Log.d(TAG,"----- _printing.value : ${_printing.value.toString()} -----")

        }else{

            Log.d(TAG,"----- drop off was FAILED added into firebase successfully -----")

            _printingContent = mutableListOf()
            _msgToFrg.value = App.resourses!!.getString(R.string.there_was_problem_to_add_drop_off)
        }
    }

    fun clearVars(){
        Log.d(TAG,"----- clearVars -----")

        dry.value =""
        wet.value  = ""
        alter.value  = ""
        household.value  = ""
        cleanOnly.value  = ""
        pressOnly.value  = ""
        ext.value  = ""
        redo.value  = ""

        _msgToFrg.value = ""
        _printingContent = mutableListOf()
        _printing.value = mutableListOf()

    }

    fun anyFilledField():Boolean{
        return !dry.value!!.isEmpty()
                || !wet.value!!.isEmpty()
                || !alter.value!!.isEmpty()
                || !household.value!!.isEmpty()
                || !cleanOnly.value!!.isEmpty()
                || !pressOnly.value!!.isEmpty()
                || !ext.value!!.isEmpty()
                || !redo.value!!.isEmpty()
    }

}