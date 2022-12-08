package com.centuryprogrammer18thwasentsingleland.sales

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.Pickup
import com.centuryprogrammer18thwasentsingleland.repository.PickupGetBetweenCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.makeEndDateTimestamp
import com.centuryprogrammer18thwasentsingleland.utils.makeStartDateTimestamp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class PickupListSalesFrgVM : ViewModel(), PickupGetBetweenCallback {
    private val TAG = PickupListSalesFrgVM::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    private lateinit var startDate: MutableList<Int>
    private lateinit var endDate: MutableList<Int>

    private val _displayPickups = MutableLiveData<MutableList<Pickup>>()
    val displayPickups : LiveData<MutableList<Pickup>>
        get() = _displayPickups


    fun initSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun initVars(receivedStartDate:MutableList<Int>,receivedEndDate:MutableList<Int>  ){
        startDate = receivedStartDate
        endDate = receivedEndDate
    }

    fun getPickups(){
        val startTimestamp = makeStartDateTimestamp(startDate[0], startDate[1], startDate[2])
        val endTimestamp = makeEndDateTimestamp(endDate[0], endDate[1], endDate[2])

        Log.d(TAG, "----- startTimestamp : ${startTimestamp.toString()} -----")
        Log.d(TAG, "----- endTimestamp : ${endTimestamp.toString()} -----")

        Repository.getPickupsBetween(sharedPrefs,startTimestamp, endTimestamp,this)

    }

    override fun onPickupsCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onPickupsCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val pickups = mutableListOf<Pickup>()

            for (item in snapshot.children){
                Log.e(TAG, "----- item : ${item.toString()}-----")
                pickups.add(item.getValue(Pickup::class.java)!!)
            }

            _displayPickups.value = pickups.sortedByDescending { it.pickUpAtTimestamp }.toMutableList()

        }
    }
}