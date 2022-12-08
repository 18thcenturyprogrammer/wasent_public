package com.centuryprogrammer18thwasentsingleland.sales

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.util.Log
import android.widget.DatePicker
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.BuildConfig
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief
import com.centuryprogrammer18thwasentsingleland.data.Payment
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrderBriefGetBetweenCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.getCurrencyString
import com.centuryprogrammer18thwasentsingleland.utils.getTimestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class DashboardSalesFrgVM : ViewModel(), DatePickerDialog.OnDateSetListener,
    InvoiceOrderBriefGetBetweenCallback {
    private val TAG = DashboardSalesFrgVM::class.java.simpleName

    lateinit var sharedPrefs : SharedPreferences

    // radio button two way data binding ref) https://stackoverflow.com/a/54262153/3151712
    var checkedRadioBtnId  = MutableLiveData<Int>(R.id.rbtnInvoiceDashboardSalesFrg)

    private val _startDate = MutableLiveData<MutableMap<String,Int>>()
    val startDate : LiveData<MutableMap<String, Int>>
        get() = _startDate

    private val _entDate = MutableLiveData<MutableMap<String,Int>>()
    val entDate : LiveData<MutableMap<String, Int>>
        get() = _entDate

    private val _receivedInvoiceOrderBriefs = MutableLiveData<MutableList<InvoiceOrderBrief>>()
    val receivedInvoiceOrderBriefs : LiveData<MutableList<InvoiceOrderBrief>>
        get() = _receivedInvoiceOrderBriefs

    private val _receivedPayments = MutableLiveData<MutableList<Payment>>()
    val receivedPayments : LiveData<MutableList<Payment>>
        get() = _receivedPayments

    private val _isUpdatedInvoiceVars = MutableLiveData<Boolean>()
    val isUpdatedInvoiceVars : LiveData<Boolean>
        get() = _isUpdatedInvoiceVars

    private val _isUpdatedPickupVars = MutableLiveData<Boolean>()
    val isUpdatedPickupVars : LiveData<Boolean>
        get() = _isUpdatedPickupVars

////////////////////////////////////////////////////////////////////////////////////////////////////
//    for invoice

    var totalNumInvoice = 0

    var dryQty = 0
    var wetQty = 0
    var alterQty = 0
    var cleanOnlyQty = 0
    var pressOnlyQty = 0

    var totalNumQty = 0

    var dryAmount = 0.0
    var wetAmount = 0.0
    var alterAmount = 0.0
    var discountAmount = 0.0
    var taxAmount = 0.0
    var envAmount = 0.0

    var totalAmountInvoice = 0.0



////////////////////////////////////////////////////////////////////////////////////////////////////
//    for pickup
    var totalNumPickupInvoice = 0
    var totalAmountPickupInvoice = 0.0

    // number of order customer paid
    var NumPrepaidInvoice = 0

    // number of order customer pickup which was paid already
    var NumPrepaidPickupInvoice = 0

    // number of order customer didn't and pickup
    var NumCreditpaidInvoice = 0

    // number of order customer payback
    var NumCreditPaybackInvoice = 0

    var PrepaidInvoiceAmount = 0.0
    var PrepaidPickupInvoiceAmount = 0.0
    var CreditpaidInvoiceAmount = 0.0
    var CreditPaybackInvoiceAmount = 0.0

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun onClickSearchBtn(){
        Log.d(TAG, "----- onClickSearchBtn -----")

        when(checkedRadioBtnId.value){
            R.id.rbtnInvoiceDashboardSalesFrg ->{

                val startTimestamp = getTimestamp(startDate.value!!)
                val endTimestamp = getTimestamp(entDate.value!!)

                Repository.getInvoiceOrderBriefsBetween(sharedPrefs, startTimestamp, endTimestamp, this)
            }

            R.id.rbtnPickupDashboardSalesFrg ->{

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

            _receivedInvoiceOrderBriefs.value = invoiceOrderBriefs
            updateVarsWithInvoiceOrderBriefs(invoiceOrderBriefs)
        }
    }

    fun updateVarsWithInvoiceOrderBriefs(invoiceOrderBriefs:MutableList<InvoiceOrderBrief>){
        totalNumInvoice = invoiceOrderBriefs.count()

        dryQty = 0
        wetQty = 0
        alterQty = 0
        cleanOnlyQty = 0
        pressOnlyQty = 0

        totalNumQty = 0

        dryAmount = 0.0
        wetAmount = 0.0
        alterAmount = 0.0
        discountAmount = 0.0
        taxAmount = 0.0
        envAmount = 0.0

        totalAmountInvoice = 0.0

        for(item in invoiceOrderBriefs){
            dryQty += item.qtyTable["dry"]!!
            wetQty += item.qtyTable["wet"]!!
            alterQty += item.qtyTable["alter"]!!
            cleanOnlyQty += item.qtyTable["clean"]!!
            pressOnlyQty += item.qtyTable["press"]!!

            totalNumQty += item.qtyTable!!["dry"]!!+item.qtyTable!!["wet"]!!+item.qtyTable!!["alter"]!!+item.qtyTable!!["clean"]!!+item.qtyTable!!["press"]!!

            dryAmount += item.priceStatement["dryPrice"]!!
            wetAmount += item.priceStatement["wetPrice"]!!
            alterAmount += item.priceStatement["alterPrice"]!!
            discountAmount += item.priceStatement["discount"]!!
            taxAmount += item.priceStatement["tax"]!!
            envAmount += item.priceStatement["env"]!!

            totalAmountInvoice += item.priceStatement["total"]!!
        }

        _isUpdatedInvoiceVars.value = true
    }
}