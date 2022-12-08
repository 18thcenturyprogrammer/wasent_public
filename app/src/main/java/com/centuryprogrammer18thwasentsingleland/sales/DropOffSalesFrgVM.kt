package com.centuryprogrammer18thwasentsingleland.sales

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Dropoff
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.repository.DropoffGetBetweenCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.makeEndDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.makeStartDateTimestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class DropOffSalesFrgVM : ViewModel(), DropoffGetBetweenCallback {
    private val TAG = DropOffSalesFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    private val _displayDropoffs = MutableLiveData<MutableList<Dropoff>>()
    val displayDropoffs : LiveData<MutableList<Dropoff>>
        get() = _displayDropoffs

    private lateinit var startDate: MutableList<Int>
    private lateinit var endDate: MutableList<Int>

    fun initSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun initVars(receivedStartDate:MutableList<Int>,receivedEndDate:MutableList<Int>  ){
        startDate = receivedStartDate
        endDate = receivedEndDate
    }

    fun getDropoffsBetween(){
        val startTimestamp = makeStartDateTimestamp(startDate[0], startDate[1], startDate[2])
        val endTimestamp = makeEndDateTimestamp(endDate[0], endDate[1], endDate[2])

        Repository.getDropoffsBetween(sharedPrefs,startTimestamp,endTimestamp, this)
    }

    override fun onDropoffsCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onDropoffsCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val Dropoffs = mutableListOf<Dropoff>()

            for (item in snapshot.children){
                Log.e(TAG, "----- item : ${item.toString()}-----")
                Dropoffs.add(item.getValue(Dropoff::class.java)!!)
            }

            _displayDropoffs.value = Dropoffs.sortedByDescending { it.createdAtTimestamp }.toMutableList()

            Log.d(TAG, "----- _displayDropoffs.value is ${_displayDropoffs.value.toString()} -----")
        }
    }
}