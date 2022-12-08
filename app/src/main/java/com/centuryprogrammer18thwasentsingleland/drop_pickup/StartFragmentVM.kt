package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.repository.SearchInvoiceCallback
import com.centuryprogrammer18thwasentsingleland.repository.SearchLastNameCallback
import com.centuryprogrammer18thwasentsingleland.repository.SearchPhoneCallback
import com.centuryprogrammer18thwasentsingleland.utils.isNumberStr
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class StartFragmentVM : ViewModel() , SearchPhoneCallback, SearchInvoiceCallback, SearchLastNameCallback {

    private val TAG = StartFragmentVM::class.java.simpleName

    lateinit var sharedPrefs :SharedPreferences

    private val _messageToFrg = MutableLiveData<String>()
    val messageToFrg : LiveData<String>
        get() = _messageToFrg

    private val _goNewCustomerFragment = MutableLiveData<MutableMap<String,Boolean>>()
    val goNewCustomerFragment : LiveData<MutableMap<String,Boolean>>
        get() = _goNewCustomerFragment

    private val _foundCustomers = MutableLiveData<MutableList<Customer>>()
    val foundCustomers : LiveData<MutableList<Customer>>
        get() = _foundCustomers

    private val _foundInvoiceOrder = MutableLiveData<InvoiceOrder>()
    val foundInvoiceOrder : LiveData<InvoiceOrder>
        get() = _foundInvoiceOrder

    var searchWord: String= ""

    fun initCustomer(receivedCustomer:Customer){
        val temp = mutableListOf<Customer>()
        temp.add(receivedCustomer)

        _foundCustomers.value = temp
    }

    fun onClickSearchBtn(){
        Log.d(TAG,"----- onClickSearchBtn -----")
        Log.d(TAG,"----- searchWord : '${searchWord}' -----")


        if(!searchWord.isEmpty() && searchWord.length >= 2) {
              // there is some search word

            var searchCategory = ""


            // check string is number string or not
            if (isNumberStr(searchWord)) {
                Log.d(TAG,"----- number -----")
                // search word is numeric for phone number or invoice

                when(searchWord.length){
                    10 -> {
                        // phone number
                        Log.d(TAG,"----- searchCategory is phoneNum -----")

                        searchCategory = "phoneNum"
                    }
                    6,7,8 -> {
                        // invoice number
                        Log.d(TAG,"----- searchCategory is invoice number -----")


                        searchCategory = "invoiceNum"
                    }
                    else -> {
                        // not correct format of search word
                        _messageToFrg.value = App.resourses!!.getString(R.string.not_correct_length_of_number)

                        // nothing to do, get out of function
                        return
                    }
                }

            }else{
                // search word is characters for last name
                Log.d(TAG,"----- searchCategory is lastName -----")

                searchCategory = "lastName"

            }

            // search word by category
            Repository.searchLastNamePhoneInvoice(
                sharedPrefs,
                searchCategory,
                searchWord,
                this,
                this,
                this
            )
        }
    }

    fun clearSearchWord(){
        searchWord = ""
    }

    override fun onSearchPhoneCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to search phone number: error is '${error.toString()}' -----")
        }

        snapshot?.let{
            Log.d(TAG,"----- onSearchPhoneCallback : snapshot '${it.toString()}' -----")
            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")

            receivingCustomers(it)

        }
        // clear edittext etNamePhoneInvoiceStartFrg
        clearSearchWord()
    }


    override fun onSearchInvoiceCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to search invoice number: error is '${error.toString()}' -----")
        }

        snapshot?.let{
            Log.d(TAG,"----- onSearchInvoiceCallback : snapshot '${it.toString()}' -----")
            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")

            val invoiceOrder = it.getValue(InvoiceOrder::class.java)

            if(invoiceOrder != null) {
                // there is matched invoiceOrder

                _foundInvoiceOrder.value = invoiceOrder
                _foundCustomers.value = mutableListOf(invoiceOrder.customer!!)
            }else{
                // there is no matched invoiceOrder

                _messageToFrg.value = App.resourses!!.getString(R.string.no_search_invoice_result)
            }
        }
        // clear edittext etNamePhoneInvoiceStartFrg
        clearSearchWord()
    }

    override fun onSearchLastNameCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to search last name : error is '${error.toString()}' -----")
        }

        snapshot?.let{
            receivingCustomers(it)
        }
        // clear edittext etNamePhoneInvoiceStartFrg
        clearSearchWord()
    }


    fun receivingCustomers(customersSnapshot: DataSnapshot){
        val customers = mutableListOf<Customer>()

        for(customerNode in customersSnapshot.children){
            Log.d(TAG,"----- customerNode.key.toString() : '${customerNode.key.toString()}'-----")
            Log.d(TAG,"----- customerNode.value.toString() : '${customerNode.value.toString()}'-----")

            customers.add(customerNode.getValue(Customer::class.java)!!)
        }

        Log.d(TAG,"----- customers : '${customers.toString()}' -----")
        Log.d(TAG,"----- customers.count() : '${customers.count().toString()}' -----")
        when(customers.count()){
            0->{
                _goNewCustomerFragment.value = mutableMapOf(searchWord to true)
                _messageToFrg.value = App.resourses!!.getString(R.string.no_search_result)
            }

            else->{
                // there are match more than one

                _foundCustomers.value = customers
            }
        }
    }

    fun clearVars(){
        _messageToFrg.value = ""
        _goNewCustomerFragment.value = mutableMapOf<String,Boolean>()
        _foundCustomers.value = mutableListOf<Customer>()
        _foundInvoiceOrder.value = InvoiceOrder()
    }

}

data class SearchResult(
    var customerId: String?= null,
    var customerLastName: String?= null,
    var customerFirstName: String?= null,
    var customerPhonNum: String?= null,
    var InvoiceId: String? = null
){}