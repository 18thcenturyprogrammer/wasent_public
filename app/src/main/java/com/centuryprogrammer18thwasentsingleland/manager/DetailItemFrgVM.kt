package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.DetailItem
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.repository.ValueEventOnDetailItemCallback
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DetailItemFrgVM : ViewModel(), ValueEventOnDetailItemCallback {
    private val TAG = DetailItemFrgVM::class.java.simpleName

    private lateinit  var sharedPrefs : SharedPreferences

    private val _detailItems = MutableLiveData<MutableMap<String, DetailItem>>()
    val detailItems : LiveData<MutableMap<String, DetailItem>>
        get() = _detailItems

    private var valueEventListener: ValueEventListener? = null

    fun setSharedPreferences(receivedSharedPrefs : SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }


    //   this func called whenever fragment created, if user rotate it, this will be called again,
    //   so check child event listener has been added before
    fun addValueEventOnDetailItem(){
        if (valueEventListener == null){
            Log.d(TAG,"----- valueEventListener is null, so add value event listener on Detail Item -----")
            valueEventListener= Repository.addValueEventOnDetailItem(sharedPrefs,this)
        }
    }

    fun removeValueEventListener(){
        valueEventListener?:let {
            Repository.removeValueEventOnDetailItem(sharedPrefs, valueEventListener!!)
        }
    }


    override fun onValueEventOnDetailItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onValueEventOnDetailItemCallback -----")
        Log.d(TAG,"-----  'detail item' value first load or changed in firebase  -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val receivedDetailItems = mutableMapOf<String, DetailItem>()

            for (item in snapshot.children){
                receivedDetailItems.put(item.key!!, item.getValue(DetailItem::class.java)!!)
            }

            _detailItems.value = receivedDetailItems

            Log.d(TAG, "----- detailItems is ${detailItems.toString()} -----")
        }
    }



    override fun onCleared() {
        super.onCleared()

        removeValueEventListener()

    }
}