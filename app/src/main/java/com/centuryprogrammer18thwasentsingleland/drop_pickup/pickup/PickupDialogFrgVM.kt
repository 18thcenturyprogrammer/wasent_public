package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.Pickup
import com.centuryprogrammer18thwasentsingleland.invoice_works.InvoiceWorksDialogUpgradedFrgVM
import com.centuryprogrammer18thwasentsingleland.repository.PickupByIdCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class PickupDialogFrgVM : ViewModel(), PickupByIdCallback {
    private val TAG = PickupDialogFrgVM::class.java.simpleName

    private lateinit var sharedPrefs: SharedPreferences

    private var pickupId : Long? = null

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog : LiveData<Boolean>
        get() = _closeDialog

    private val _foundPickup = MutableLiveData<Pickup>()
    val foundPickup : LiveData<Pickup>
        get() = _foundPickup


    fun initSharedPrefs(receivedSharedPrefs:SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun initPickupId(receivedPickupId:Long){
        pickupId = receivedPickupId
    }

    fun getPickup(){
        Repository.getPickupById(sharedPrefs,pickupId.toString(),this)
    }

    override fun onPickupByIdCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onPickupByIdCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val foundPickup = snapshot.getValue(Pickup::class.java)

            // if i cant' find one , foundInvoiceOrder will be null
            Log.d(TAG,"----- foundPickup : ${foundPickup.toString()}-----")

            if(foundPickup == null){
                _msgToFrg.value = App.resourses!!.getString(R.string.not_found_matched_pickup)
            }else{
                _foundPickup.value = foundPickup
            }

            Log.d(TAG, "----- _foundPickup.value is ${foundPickup.toString()} -----")
        }
    }
}