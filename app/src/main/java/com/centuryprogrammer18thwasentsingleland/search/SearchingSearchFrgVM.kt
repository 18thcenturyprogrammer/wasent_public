package com.centuryprogrammer18thwasentsingleland.search

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.PickupHistory
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.repository.SearchFirstNameCallback
import com.centuryprogrammer18thwasentsingleland.repository.SearchInvoiceCallback
import com.centuryprogrammer18thwasentsingleland.repository.SearchLastNameCallback
import com.centuryprogrammer18thwasentsingleland.utils.isNumberStr
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import java.lang.StringBuilder

class SearchingSearchFrgVM : ViewModel(), SearchLastNameCallback, SearchFirstNameCallback,
    SearchInvoiceCallback {
    private val TAG = SearchingSearchFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    var searchWord = ""
    var category = ""

    private val _searchResults = MutableLiveData<MutableList<SearchResult>>()
    val searchResults : LiveData<MutableList<SearchResult>>
        get() = _searchResults

    fun initSharedPrefs(receivedSharedPrefs:SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun onClickSearchBtn(){
        if(searchWord.isNotEmpty()){
            when(category){
                "Last Name" -> {
                    Log.d(TAG,"----- searching Last Name -----")

                    if(searchWord.length >= 2){
                        Repository.searchByLastName(sharedPrefs,searchWord,this)
                    }
                }
                "First Name" -> {
                    Log.d(TAG,"----- searching First Name -----")

                    if(searchWord.length >= 2){
                        Repository.searchByFirstName(sharedPrefs,searchWord,this)
                    }
                }
                "Invoice Number" -> {
                    Log.d(TAG,"----- searching Invoice Number -----")

                    // search word should be number and 6-8 digits
                    if(isNumberStr(searchWord) && searchWord.length >= 6 && searchWord.length <= 8){
                        Repository.searchByInvoiceId(sharedPrefs,searchWord,this)
                    }
                }

            }

        }
    }

    override fun onSearchLastNameCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to search by lastname : error is '${error.toString()}' -----")
        }

        snapshot?.let{
            Log.d(TAG,"----- onSearchLastNameCallback : snapshot '${it.toString()}' -----")
            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")

            val tempSearchResults = mutableListOf<SearchResult>()

            for(customerNode in it.children){
                val customer = customerNode.getValue(Customer::class.java)!!

                val content =
                    StringBuilder().
                    append(customer.firstName).
                    append("     ").
                    append(customer.lastName).
                    append("          ").
                    append(customer.phoneNum).
                    append("          ").
                    append(customer.email).
                    append("\n").
                    append(customer.streetAddress).
                    append("     ").
                    append(customer.city).
                    append("     ").
                    append(customer.state).
                    append("     ").
                    append(customer.zipcode).
                    append("\n").
                    append(customer.shirt).
                    append("          ").
                    append(customer.note).
                    toString()

                val searchResult = SearchResult("customer",customer.id,customer,content)

                Log.d(TAG,"----- content :${content} -----")

                tempSearchResults.add(searchResult)
            }

            _searchResults.value = tempSearchResults

        }
    }

    override fun onSearchFirstNameCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to search by firstname : error is '${error.toString()}' -----")
        }

        snapshot?.let{
            Log.d(TAG,"----- onSearchFirstNameCallback : snapshot '${it.toString()}' -----")
            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")

            val tempSearchResults = mutableListOf<SearchResult>()

            for(customerNode in it.children){
                val customer = customerNode.getValue(Customer::class.java)!!

                val content =
                    StringBuilder().
                    append(customer.firstName).
                    append("     ").
                    append(customer.lastName).
                    append("          ").
                    append(customer.phoneNum).
                    append("          ").
                    append(customer.email).
                    append("\n").
                    append(customer.streetAddress).
                    append("     ").
                    append(customer.city).
                    append("     ").
                    append(customer.state).
                    append("     ").
                    append(customer.zipcode).
                    append("\n").
                    append(customer.shirt).
                    append("          ").
                    append(customer.note).
                    toString()

                val searchResult = SearchResult("customer",customer.id,customer,content)

                tempSearchResults.add(searchResult)
            }

            _searchResults.value = tempSearchResults

        }
    }

    override fun onSearchInvoiceCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to search by invoiceid : error is '${error.toString()}' -----")
        }

        snapshot?.let{
            Log.d(TAG,"----- onSearchInvoiceCallback : snapshot '${it.toString()}' -----")
            Log.d(TAG,"----- it.children.count() '${it.children.count().toString()}' -----")

            val tempSearchResults = mutableListOf<SearchResult>()


            val invoiceOrder = it.getValue(InvoiceOrder::class.java)!!

            val content =
                StringBuilder().
                append(invoiceOrder.id).
                append("          ").
                append(invoiceOrder.customer!!.firstName).
                append("     ").
                append(invoiceOrder.customer!!.lastName).
                append("          ").
                append(invoiceOrder.customer!!.phoneNum).
                append("          ").
                append(invoiceOrder.customer!!.email).
                append("\n").
                append(App.resourses!!.getString(R.string.created_at)).
                append("  :   ").
                append(invoiceOrder.createdAt).
                append("     ").
                append(App.resourses!!.getString(R.string.by)).
                append("  :   ").
                append(invoiceOrder.createdBy)

            invoiceOrder.rackedBy?.let {
                content.
                append("          ").
                append(App.resourses!!.getString(R.string.rack)).
                append("  :   ").
                append(invoiceOrder.rackLocation).
                append("     ").
                append(App.resourses!!.getString(R.string.at)).
                append("  :   ").
                append(invoiceOrder.rackedAt).
                append("     ").
                append(App.resourses!!.getString(R.string.by)).
                append("  :   ").
                append(it)
            }

            invoiceOrder.pickedUpBy?.let {
                content.
                append("          ").
                append(App.resourses!!.getString(R.string.picked_up_at)).
                append("  :   ").
                append(invoiceOrder.pickedUpAt).
                append("     ").
                append(App.resourses!!.getString(R.string.by)).
                append("  :   ").
                append(it)
            }

            invoiceOrder.adjustedBy?.let {
                content.
                append("          ").
                append(App.resourses!!.getString(R.string.adjusted_by)).
                append("  :   ").
                append(it)
            }

            invoiceOrder.orgInvoiceOrderId?.let {
                content.
                append("          ").
                append(App.resourses!!.getString(R.string.original_id)).
                append("  :   ").
                append(it)
            }

            invoiceOrder.voidAt?.let {
                content.
                append("          ").
                append(App.resourses!!.getString(R.string.voided_at)).
                append("  :   ").
                append(it).
                append("     ").
                append(App.resourses!!.getString(R.string.by)).
                append("  :   ").
                append(invoiceOrder.voidBy)
            }

            invoiceOrder.tag?.let {
                content.
                append("          ").
                append(App.resourses!!.getString(R.string.tag)).
                append("  :   ").
                append(it).
                append("     ").
                append(App.resourses!!.getString(R.string.tag_color)).
                append("  :   ").
                append(invoiceOrder.tagColor)
            }

                content.append("\n").
                append(makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["total"]!!)).
                append("          ").
                append(invoiceOrder.qtyTable!!["dry"]!!+invoiceOrder.qtyTable!!["wet"]!!+invoiceOrder.qtyTable!!["alter"]!!+invoiceOrder.qtyTable!!["clean"]!!+invoiceOrder.qtyTable!!["press"]!!).
                append("     ").
                append(App.resourses!!.getString(R.string.pieces))

            val searchResult = SearchResult("invoice",invoiceOrder.id.toString(),invoiceOrder,content.toString())

            tempSearchResults.add(searchResult)


            _searchResults.value = tempSearchResults

        }
    }
}

data class SearchResult(
  var type : String? = null,
  var id: String? = null,
  var obj: Any? = null,
  var content : String? = null
){}