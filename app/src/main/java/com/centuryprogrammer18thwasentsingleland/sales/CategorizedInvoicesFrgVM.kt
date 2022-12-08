package com.centuryprogrammer18thwasentsingleland.sales

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.fragment.findNavController
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.Payment
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersByIdCallback
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.InvoiceInventoryFrgCallback
import com.centuryprogrammer18thwasentsingleland.utils.makeEndDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.makeStartDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class CategorizedInvoicesFrgVM : ViewModel(), InvoiceOrdersCallback, InvoiceOrdersByIdCallback {
    private val TAG = CategorizedInvoicesFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    private lateinit var categoryName: String
    private lateinit var startDate: MutableList<Int>
    private lateinit var endDate: MutableList<Int>

    private val _invoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val invoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _invoiceOrders

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _foundOrginvoiceOrder = MutableLiveData<InvoiceOrder>()
    val foundOrginvoiceOrder : LiveData<InvoiceOrder>
        get() = _foundOrginvoiceOrder



    fun initSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun initVars(receivedCategoryName: String,receivedStartDate:MutableList<Int>,receivedEndDate:MutableList<Int>  ){
        categoryName = receivedCategoryName
        startDate = receivedStartDate
        endDate = receivedEndDate
    }

    fun getInvoiceOrders(categoryName:String){
        val startTimestamp = makeStartDateTimestamp(startDate[0], startDate[1], startDate[2])
        val endTimestamp = makeEndDateTimestamp(endDate[0], endDate[1], endDate[2])

        Log.d(TAG, "----- startTimestamp : ${startTimestamp.toString()} -----")
        Log.d(TAG, "----- endTimestamp : ${endTimestamp.toString()} -----")

        when(categoryName){
            "all_invoices" -> {
                Repository.getInvoicesBetween(sharedPrefs,startTimestamp, endTimestamp,this)
            }

            "voided" -> {
                Repository.getVoidedInvoicesBetween(sharedPrefs,startTimestamp, endTimestamp,this)
            }

            "adjusted" -> {
                Repository.getInvoicesBetween(sharedPrefs,startTimestamp, endTimestamp,this)
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

                val invoiceOrder = item.getValue(InvoiceOrder::class.java)!!

                if(categoryName == "adjusted"){
                    invoiceOrder.orgInvoiceOrderId?.let {
                        invoiceOrders.add(invoiceOrder)
                    }
                }else{
                    invoiceOrders.add(invoiceOrder)
                }
            }

            _invoiceOrders.value = invoiceOrders.sortedByDescending { it.createdAtTimestamp }.toMutableList()

            Log.d(TAG, "----- _invoiceOrders.value is ${_invoiceOrders.value.toString()} -----")
        }
    }


    // after found original InvoiceOrder ( called from VoidedAdjustedInvoiceOrderApt )
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
                _foundOrginvoiceOrder.value = foundInvoiceOrder
            }

            Log.d(TAG, "----- _foundOrginvoiceOrder.value is ${foundOrginvoiceOrder.toString()} -----")
        }
    }

    fun showVoidedAdjustedInvoice(voidedAdjustedInvoiceOrder : InvoiceOrder){
        _foundOrginvoiceOrder.value = voidedAdjustedInvoiceOrder
    }
}