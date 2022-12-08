package com.centuryprogrammer18thwasentsingleland.inventory

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.InvoiceInventoryViewModelCallback
import com.centuryprogrammer18thwasentsingleland.utils.notifyObserver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class DashboardInventoryFrgVM : ViewModel(), InvoiceOrdersCallback, InvoiceInventoryViewModelCallback {
    private val TAG = DashboardInventoryFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    private val _inventoryInvoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val inventoryInvoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _inventoryInvoiceOrders

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun getInventoryInvoiceOrders(){
        Repository.getAllInventoryInvoices(sharedPrefs,this)
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
            _inventoryInvoiceOrders.value = invoiceOrders.sortedByDescending { it.createdAtTimestamp }.toMutableList()

            Log.d(TAG, "----- _invoiceOrders.value is ${_inventoryInvoiceOrders.value.toString()} -----")
        }
    }

    fun sortByCreatedAtInvoiceId(){
        _inventoryInvoiceOrders.value?.sortBy { it.createdAtTimestamp }
        _inventoryInvoiceOrders.notifyObserver()
    }
    fun sortCustomerName(){
        _inventoryInvoiceOrders.value?.sortByDescending { it.customer?.firstName }
        _inventoryInvoiceOrders.notifyObserver()
    }
    fun sortRackLocation(){
        _inventoryInvoiceOrders.value?.sortBy { it.rackLocation }
        _inventoryInvoiceOrders.notifyObserver()
    }
}