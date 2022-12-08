package com.centuryprogrammer18thwasentsingleland.sales

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.util.Log
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief
import com.centuryprogrammer18thwasentsingleland.data.Payment
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrderBriefGetBetweenCallback
import com.centuryprogrammer18thwasentsingleland.repository.PaymentGetBetweenCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.getTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.makeEndDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.makeStartDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.notifyObserver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class SalesActVM: ViewModel(), DatePickerDialog.OnDateSetListener,
    InvoiceOrderBriefGetBetweenCallback , PaymentGetBetweenCallback {
    private val TAG = SalesActVM::class.java.simpleName

    lateinit var sharedPrefs : SharedPreferences

    // radio button two way data binding ref) https://stackoverflow.com/a/54262153/3151712
    var checkedRadioBtnId  = MutableLiveData<Int>(R.id.rbtnInvoiceDashboardSalesFrg)

    private val _startDate = MutableLiveData<MutableMap<String,Int>>()
    val startDate : LiveData<MutableMap<String, Int>>
        get() = _startDate

    private val _entDate = MutableLiveData<MutableMap<String,Int>>()
    val entDate : LiveData<MutableMap<String, Int>>
        get() = _entDate

    private val _invoiceOrderBriefs = MutableLiveData<MutableList<InvoiceOrderBrief>>()
    val invoiceOrderBriefs : LiveData<MutableList<InvoiceOrderBrief>>
        get() = _invoiceOrderBriefs

    private val _payments = MutableLiveData<MutableList<Payment>>()
    val payments : LiveData<MutableList<Payment>>
        get() = _payments

    fun updateInvoiceOrderBriefs(receivedInvoiceOrderBriefs : MutableList<InvoiceOrderBrief>){
        _invoiceOrderBriefs.value = receivedInvoiceOrderBriefs
    }

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun onClickSearchBtn(){
        Log.d(TAG, "----- onClickSearchBtn -----")

        when(checkedRadioBtnId.value){
            R.id.rbtnInvoiceSalesAct ->{

                val startTimestamp = makeStartDateTimestamp(startDate.value!!["year"]!!,startDate.value!!["month"]!!,startDate.value!!["day"]!!)
                val endTimestamp = makeEndDateTimestamp(entDate.value!!["year"]!!,entDate.value!!["month"]!!,entDate.value!!["day"]!!)

                Log.d(TAG, "----- startTimestamp : ${startTimestamp.toString()}-----")
                Log.d(TAG, "----- endTimestamp : ${endTimestamp.toString()}-----")

                Repository.getInvoiceOrderBriefsBetween(sharedPrefs, startTimestamp, endTimestamp, this)
            }

            R.id.rbtnPickupSalesAct ->{
                val startTimestamp = makeStartDateTimestamp(startDate.value!!["year"]!!,startDate.value!!["month"]!!,startDate.value!!["day"]!!)
                val endTimestamp = makeEndDateTimestamp(entDate.value!!["year"]!!,entDate.value!!["month"]!!,entDate.value!!["day"]!!)

                Log.d(TAG, "----- startTimestamp : ${startTimestamp.toString()}-----")
                Log.d(TAG, "----- endTimestamp : ${endTimestamp.toString()}-----")

                Repository.getPaymentsBetween(sharedPrefs, startTimestamp, endTimestamp, this)
            }
        }

    }

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

    override fun onInvoiceOrderBriefsCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onInvoiceOrderBriefsCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val invoiceOrderBriefs = mutableListOf<InvoiceOrderBrief>()

            for (item in snapshot.children){
                Log.e(TAG, "----- item : ${item.toString()}-----")
                invoiceOrderBriefs.add(item.getValue(InvoiceOrderBrief::class.java)!!)
            }

            _invoiceOrderBriefs.value = invoiceOrderBriefs

        }
    }

    override fun onPaymentsCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onPaymentsCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val tempPayments = mutableListOf<Payment>()

            for (item in snapshot.children){
                Log.e(TAG, "----- item : ${item.toString()}-----")
                tempPayments.add(item.getValue(Payment::class.java)!!)
            }

            _payments.value = tempPayments

        }
    }

    fun clearInvoiceOrderBriefs(){
        _invoiceOrderBriefs.value = mutableListOf<InvoiceOrderBrief>()
    }

    fun clearPayments(){
        _payments.value = mutableListOf<Payment>()
    }
}