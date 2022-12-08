package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.repository.AddCustomerCallbacks
import com.centuryprogrammer18thwasentsingleland.repository.Repository


class EditCusomerFrgVM : ViewModel(), AddCustomerCallbacks {
    private val TAG = EditCusomerFrgVM::class.java.simpleName

    private val _editedCustomer = MutableLiveData<Customer>()
    val editedCustomer : LiveData<Customer>
        get() = _editedCustomer


    // ref) https://stackoverflow.com/a/51595202/3151712
    // after save new customer into database, this callback called
    override fun onAddCustomerCall(isSuccess: Boolean, customer: Customer) {
        Log.d(TAG,"----- isSuccess value is ${isSuccess.toString()}' -----")
        Log.d(TAG,"----- customer value is ${customer.toString()}' -----")

        _editedCustomer.value = customer
    }

    fun editCustomer(sharedPrefs: SharedPreferences, customer: Customer){

        // i will use add func for updating , it works
        Repository.editCustomer(sharedPrefs ,customer, this)
    }

    // after save updated customer into database and move to next stage , and clean up flag vars
    fun cleanUpVars(){
        _editedCustomer.value = null
    }

}