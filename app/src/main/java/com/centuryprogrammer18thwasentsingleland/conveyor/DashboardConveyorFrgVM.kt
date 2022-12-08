package com.centuryprogrammer18thwasentsingleland.conveyor

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.util.Log
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class DashboardConveyorFrgVM : ViewModel() , DatePickerDialog.OnDateSetListener,
    InvoiceConveyorViewModelCallback, InvoiceOrdersCallback {
    private val TAG = DashboardConveyorFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    var rackLocation : String = ""

    private val _startDate = MutableLiveData<MutableMap<String,Int>>()
    val startDate : LiveData<MutableMap<String,Int>>
        get() = _startDate

    private val _entDate = MutableLiveData<MutableMap<String,Int>>()
    val entDate : LiveData<MutableMap<String,Int>>
        get() = _entDate

    private val _conveyorInvoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val conveyorInvoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _conveyorInvoiceOrders

    private val _displayConveyorInvoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val displayConveyorInvoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _displayConveyorInvoiceOrders

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun getInvoiceOrdersByConveyor(){
        Repository.getInvoiceOrdersByConveyor(sharedPrefs, rackLocation,this)
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
        if(rackLocation.isNotEmpty()){
            getInvoiceOrdersByConveyor()
        }
    }

    fun onClickApplyBtn(){
        Log.d(TAG,"----- onClickApplyBtn -----")
        val startDateTimestamp = makeStartDateTimestamp(startDate.value!!["year"]!!,startDate.value!!["month"]!!,startDate.value!!["day"]!!)
        val endDateTimestamp = makeEndDateTimestamp(entDate.value!!["year"]!!,entDate.value!!["month"]!!,entDate.value!!["day"]!!)
        val temp = mutableListOf<InvoiceOrder>()
        conveyorInvoiceOrders.value?.let {
            if(it.count()>0){
                for(invoiceOrder in it){
                    if(invoiceOrder.createdAtTimestamp!! >= startDateTimestamp && invoiceOrder.createdAtTimestamp!! <= endDateTimestamp){
                        temp.add(invoiceOrder)
                    }
                }

                _displayConveyorInvoiceOrders.value = temp
            }
        }
    }

    fun sortByCreatedAtInvoiceId(){
        Log.d(TAG,"-----  sortByCreatedAtInvoiceId -----")
        _displayConveyorInvoiceOrders.value?.sortBy { it.createdAtTimestamp }
        _displayConveyorInvoiceOrders.notifyObserver()
    }

    fun sortByCustomerName(){
        Log.d(TAG,"-----  sortByCustomerName -----")
        _displayConveyorInvoiceOrders.value?.sortByDescending { it.customer!!.firstName }
        _displayConveyorInvoiceOrders.notifyObserver()
    }

    fun sortByDueDate(){
        Log.d(TAG,"-----  sortByDueDate -----")
        _displayConveyorInvoiceOrders.value?.sortBy { it.dueTimestamp }
        _displayConveyorInvoiceOrders.notifyObserver()
    }

    fun sortByPickedUpAt(){
        Log.d(TAG,"-----  sortByPickedUpAt -----")
        _displayConveyorInvoiceOrders.value?.sortBy { it.pickedAtTimestamp }
        _displayConveyorInvoiceOrders.notifyObserver()
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

            // neweast one will be shown on top
            _conveyorInvoiceOrders.value = invoiceOrders.sortedByDescending { it.createdAtTimestamp }.toMutableList()
            _displayConveyorInvoiceOrders.value = invoiceOrders.sortedByDescending { it.createdAtTimestamp }.toMutableList()

            Log.d(TAG, "----- _conveyorInvoiceOrders.value is ${_conveyorInvoiceOrders.value.toString()} -----")
        }
    }

    fun clearInvoiceOrders(){
        Log.d(TAG, "----- clearInvoiceOrders -----")
        _conveyorInvoiceOrders.value = mutableListOf<InvoiceOrder>()
        _displayConveyorInvoiceOrders.value = mutableListOf<InvoiceOrder>()
    }
}