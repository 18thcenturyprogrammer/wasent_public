package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.UserInvoiceHistoryViewModelCallback
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.someMonthsAgo
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.lang.StringBuilder
import java.text.SimpleDateFormat


// this is similar with UserInvoiceHistoryFrg, so i will use UserInvoiceHistoryApt and callbacks (UserInvoiceHistoryFrgCallback, UserInvoiceHistoryViewModelCallback)
class UserHistoryMakeInvoiceFrgVM : ViewModel() ,
    InvoiceOrdersCallback, UserInvoiceHistoryViewModelCallback {
    private val TAG = UserHistoryMakeInvoiceFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    lateinit var customer: Customer

    private val _invoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val invoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _invoiceOrders

    fun updateSharedPrefs(receivedSharedPrefs : SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun updateCusomer(receivedCustomer : Customer){
        customer = receivedCustomer
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
            _invoiceOrders.value = invoiceOrders.sortedByDescending { it.createdAtTimestamp }.toMutableList()

            Log.d(TAG, "----- _invoiceOrders.value is ${_invoiceOrders.value.toString()} -----")
        }
    }

    fun getInvoicesLastThreeMons(){

        val format = SimpleDateFormat("yyyy-MM-dd")

        var yearForThreeMonsAgo = someMonthsAgo(3)["year"]

        // month 0 - 11, i will add one
        var monthForThreeMonsAgo = someMonthsAgo(3)["month"]!!+1
        var dayForThreeMonsAgo = someMonthsAgo(3)["day"]

        var dateStr = StringBuilder()
            .append(yearForThreeMonsAgo)
            .append("-")
            .append(monthForThreeMonsAgo)
            .append("-")
            .append(dayForThreeMonsAgo)

        val timestampForThreeMonsAgo = format.parse(dateStr.toString()).time

        Repository.getUserInvoiceHistory(sharedPrefs,customer,timestampForThreeMonsAgo,
            getCurrentTimestamp(), this)
    }


}