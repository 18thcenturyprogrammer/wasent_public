package com.centuryprogrammer18thwasentsingleland.rack

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersByIdCallback
import com.centuryprogrammer18thwasentsingleland.repository.RackInvoiceOrderCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.invoiceOrderToBrief
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

class DoingRackFrgVM : ViewModel() , InvoiceOrdersByIdCallback, RackInvoiceOrderCallback {
    private val TAG = DoingRackFrgVM::class.java.simpleName

    private lateinit var sharedPrefs: SharedPreferences

    private val _foundInvoiceOrder = MutableLiveData<InvoiceOrder?>()
    val foundInvoiceOrder : LiveData<InvoiceOrder?>
        get() = _foundInvoiceOrder

    private var rackedInvoiceOrder : InvoiceOrder? = null

    private val _rackedInvoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val rackedInvoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _rackedInvoiceOrders

    private val _isfinishedDoneProcess = MutableLiveData<Boolean>()
    val isfinishedDoneProcess : LiveData<Boolean>
        get() = _isfinishedDoneProcess


    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    fun setSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun getInvoiceById(sharedPrefs:SharedPreferences, invoiceId: String){
        Repository.getInvoiceOrdersById(sharedPrefs, invoiceId, this)
    }

    fun clearVars(){
        _foundInvoiceOrder.value = null
        rackedInvoiceOrder = null
    }

    fun updateRackLocation(rackLocation: String){
        _foundInvoiceOrder.value?.let {
            val invoiceOrder = it

//            date time format ref) https://stackoverflow.com/a/38220579/3151712
//            date time format ref) https://stackoverflow.com/a/41507429/3151712
            val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Format date

            // get current datatime string
            val nowString: String = dateFormat.format(Calendar.getInstance().getTime())

            // kotlin time string timestamp ref) https://stackoverflow.com/a/6993420/3151712
            val rackTimestamp = dateFormat.parse(nowString).time

            // save data related to racking
            invoiceOrder.rackLocation = rackLocation
            invoiceOrder.rackedAt = nowString
            invoiceOrder.rackedAtTimestamp = rackTimestamp
            invoiceOrder.rackedBy = sharedPrefs.getString("logged_team_id",null)

            val invoiceOrderBrief = invoiceOrderToBrief(invoiceOrder)

            rackedInvoiceOrder = invoiceOrder
            Repository.rackInvoiceOrder(sharedPrefs,invoiceOrder,invoiceOrderBrief,this)
        }
    }

    // this is called after Repository.getInvoiceOrdersById()
    override fun onInvoiceOrdersByIdCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onInvoiceOrdersByIdCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val foundInvoiceOrder = snapshot.getValue(InvoiceOrder::class.java)

            // if i cant' find one , foundInvoiceOrder will be null
            Log.d(TAG,"----- foundInvoiceOrder : ${foundInvoiceOrder.toString()}-----")

            if(foundInvoiceOrder == null){
                _msgToFrg.value = App.resourses!!.getString(R.string.not_found_matched_invoice_id)
            }else{
                _foundInvoiceOrder.value = foundInvoiceOrder
            }

            Log.d(TAG, "----- _foundInvoiceOrder.value is ${foundInvoiceOrder.toString()} -----")
        }
    }

    // this is called after Repository.addInvoiceOrder() for updating invoice order, invoice order brief with racking data
    override fun onRackInvoiceOrderCallback(isSuccess: Boolean) {
        if(isSuccess){
            Log.d(TAG,"----- onRackInvoiceOrderCallback INVOICE ORDER added successfully -----")

            if(rackedInvoiceOrders.value == null){
                _rackedInvoiceOrders.value = mutableListOf(rackedInvoiceOrder!!)
            }else{
                _rackedInvoiceOrders.value!!.add(rackedInvoiceOrder!!)
            }
            _isfinishedDoneProcess.value = true

        }else{
            Log.e(TAG,"----- onRackInvoiceOrderCallback INVOICE ORDER failded -----")
        }
    }
}