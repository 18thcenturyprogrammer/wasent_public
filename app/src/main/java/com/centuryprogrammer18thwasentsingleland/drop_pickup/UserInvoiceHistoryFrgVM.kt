package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.util.Log
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.UserInvoiceHistoryViewModelCallback
import com.centuryprogrammer18thwasentsingleland.utils.makeEndDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.makeStartDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.someMonthsAgo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class UserInvoiceHistoryFrgVM : ViewModel(), DatePickerDialog.OnDateSetListener,
    InvoiceOrdersCallback , UserInvoiceHistoryViewModelCallback {

    private val TAG = UserInvoiceHistoryFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    lateinit var customer: Customer

    private val _startDate = MutableLiveData<MutableMap<String,Int>>()
    val startDate : LiveData<MutableMap<String,Int>>
        get() = _startDate

    private val _entDate = MutableLiveData<MutableMap<String,Int>>()
    val entDate : LiveData<MutableMap<String,Int>>
        get() = _entDate

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _invoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val invoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _invoiceOrders

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun updateCustomer(receivedCustomer : Customer){
        customer = receivedCustomer
    }

    // this is shared by start date picker and end date picker
    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        Log.d(TAG,"----- onDateSet -----")
        Log.d(TAG,"----- datePicker tag : '${datePicker?.tag.toString()}' -----")
        Log.d(TAG,"----- year : '${year.toString()}' ----- month : '${month.toString()}' ----- day : '${day.toString()}' -----")

        when(datePicker?.tag.toString()){
            "start" -> {
                val temp = mutableMapOf<String,Int>(
                    "year" to year,
                    "month" to month,
                    "day" to day
                )

                _startDate.value = temp
            }
            "end" -> {
                val temp = mutableMapOf<String,Int>(
                    "year" to year,
                    "month" to month,
                    "day" to day
                )

                _entDate.value = temp
            }
        }

    }

    fun onClick3monsBtn(){
        Log.d(TAG,"----- onClick3monsBtn -----")

        _startDate.value = someMonthsAgo(3)
        _entDate.value = someMonthsAgo(0)

    }

    fun onClick6monsBtn(){
        Log.d(TAG,"----- onClick6monsBtn -----")

        _startDate.value = someMonthsAgo(6)
        _entDate.value = someMonthsAgo(0)
    }

    fun onClickSearchBtn(){
        Log.d(TAG,"----- onClickSearchBtn -----")

        val startTimestamp = makeStartDateTimestamp(startDate.value!!["year"]!!, startDate.value!!["month"]!!,startDate.value!!["day"]!!)

        val endTimestamp = makeEndDateTimestamp(entDate.value!!["year"]!!, entDate.value!!["month"]!!,entDate.value!!["day"]!!)

        if(startTimestamp > endTimestamp){
            // user put wrong time period

            _msgToFrg.value = App.resourses!!.getString(R.string.enter_wrong_date)
        }else{
            // user enter correct dates

            Repository.getUserInvoiceHistory(sharedPrefs,customer,startTimestamp,endTimestamp, this)
        }
    }

    override fun onInvoiceOrdersCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onInvoiceOrdersCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val invoiceOrders = mutableListOf<InvoiceOrder>()

            for (item in snapshot.children){
                Log.e(TAG, "----- item : ${item.toString()}-----")
                invoiceOrders.add(item.getValue(InvoiceOrder::class.java)!!)
            }

            _invoiceOrders.value = invoiceOrders.sortedByDescending { it.createdAtTimestamp }.toMutableList()

            Log.d(TAG, "----- _invoiceOrders.value is ${_invoiceOrders.value.toString()} -----")
        }
    }

}