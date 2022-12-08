package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.repository.AddCustomerCallbacks
import com.centuryprogrammer18thwasentsingleland.repository.Repository


class NewCustomerFragmentViewModel : ViewModel(), AddCustomerCallbacks {
    private val TAG = NewCustomerFragmentViewModel::class.java.simpleName

    private val _addedCustomer = MutableLiveData<Customer>()
    val addedCustomer : LiveData<Customer>
        get() = _addedCustomer


    // ref) https://stackoverflow.com/a/51595202/3151712
    // after save new customer into database, this callback called
    override fun onAddCustomerCall(isSuccess: Boolean, customer:Customer) {
        Log.d(TAG,"----- isSuccess value is ${isSuccess.toString()}' -----")
        Log.d(TAG,"----- customer value is ${customer.toString()}' -----")

        _addedCustomer.value = customer
    }

    fun addCustomer(sharedPrefs:SharedPreferences, customer: Customer){
        Repository.addCustomer(sharedPrefs ,customer, this)
    }

    // after save new customer into database and move to next stage , and clean up flag vars
    fun cleanUpVars(){

        _addedCustomer.value = null
    }

    override fun onCleared() {
        super.onCleared()
    }
}