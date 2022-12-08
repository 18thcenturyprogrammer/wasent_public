package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Item
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.repository.ValueEventOnItemCallback
import com.google.firebase.database.*

class ItemFragmentViewModel :ViewModel(), ValueEventOnItemCallback {
    private val TAG = ItemFragmentViewModel::class.java.simpleName

    private lateinit  var sharedPrefs : SharedPreferences

    private val _liveItems = MutableLiveData<MutableMap<String, Item>>()
    val liveItems : LiveData<MutableMap<String, Item>>
        get() = _liveItems

    private var valueEventListener: ValueEventListener? = null

    fun setSharedPreferences(receivedSharedPrefs : SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }


//   this func called whenever fragment created, if user rotate it, this will be called again,
//   so check child event listener has been added before
    fun addValueEventOnItem(){
        if (valueEventListener == null){
            Log.d(TAG,"----- valueEventListener is null, so add value event listener on Item -----")
            valueEventListener= Repository.addValueEventOnItem(sharedPrefs,this)
        }
    }

    fun removeChildEventListener(){
        valueEventListener?:let {
            Repository.removeValueEventOnItem(sharedPrefs, valueEventListener!!)
        }
    }


    override fun onValueEventOnItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onValueEventOnItemCallback -----")
        Log.d(TAG,"-----  'item' value first load or changed in firebase  -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val receivedItems = mutableMapOf<String,Item>()

            for (item in snapshot.children){
                receivedItems.put(item.key!!, item.getValue(Item::class.java)!!)
            }

            _liveItems.value = receivedItems

            Log.d(TAG, "----- liveItems is ${liveItems.toString()} -----")
        }
    }



    override fun onCleared() {
        super.onCleared()

        removeChildEventListener()

    }
}


