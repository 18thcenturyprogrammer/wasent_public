package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.PickupHistory
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersForCustomerCallback
import com.centuryprogrammer18thwasentsingleland.repository.LastPickupHistoryForCustomerCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError


class TaskBoardFragmentVM : ViewModel() , InvoiceOrdersForCustomerCallback,
    LastPickupHistoryForCustomerCallback {
    private val TAG = TaskBoardFragmentVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    private lateinit var customer : Customer


    private val _invoiceOrders = MutableLiveData<MutableList<InvoiceOrder>>()
    val invoiceOrders : LiveData<MutableList<InvoiceOrder>>
        get() = _invoiceOrders

    private val _lastPickHistory = MutableLiveData<PickupHistory>()
    val lastPickHistory : LiveData<PickupHistory>
        get() = _lastPickHistory

    fun setSharedPrefs(receivedShared : SharedPreferences){
        sharedPrefs = receivedShared
    }

    fun setCustomer(receivedCustomer : Customer){
        customer = receivedCustomer
    }

    fun getInvoiceOrderForCustomer(){
        Repository.getInvoiceOrdersForCustomer(sharedPrefs,customer,this)
        Repository.getLastPickupHistoryForCustomer(sharedPrefs,customer,this)
    }

    override fun onInvoiceOrdersForCustomerCallback(
        snapshot: DataSnapshot?,
        error: DatabaseError?
    ) {
        error?.let {
            Log.e(TAG, "----- there was problem to bring up InvoiceOrdes by customer : error is '${error.toString()}' -----")
        }

        snapshot?.let{
//            Log.d(TAG,"----- onInvoiceOrdersForCustomerCallback : snapshot '${it.toString()}' -----")
            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")

            val items = mutableListOf<InvoiceOrder>()

            for (item in it.children){

                items.add(item.getValue(InvoiceOrder::class.java)!!)
            }

            _invoiceOrders.value = items
//            Log.d(TAG,"----- invoiceOrderBriefs '${invoiceOrderBriefs.value.toString()}' -----")
        }
    }

    override fun onLastPickupHistoryForCustomerCallback(
        snapshot: DataSnapshot?,
        error: DatabaseError?
    ) {
        error?.let {
            Log.e(TAG, "----- there was problem to bring up last PickupHistory by customer : error is '${error.toString()}' -----")
        }

        snapshot?.let{
//            Log.d(TAG,"----- onLastPickupHistoryForCustomerCallback : snapshot '${it.toString()}' -----")
            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")

            for(item in it.children){
                _lastPickHistory.value = item.getValue(PickupHistory::class.java)
            }

            Log.d(TAG,"----- lastPickHistory '${lastPickHistory.value.toString()}' -----")
        }
    }
}