package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.PartialBaseItem
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.repository.UpdatePartialBaseItemsCallback
import com.centuryprogrammer18thwasentsingleland.repository.ValueEventOnPartialBaseItemCallback
import com.centuryprogrammer18thwasentsingleland.utils.PartialBaseItemAdapterInterface
import com.centuryprogrammer18thwasentsingleland.utils.getCollectionItemFromSnapshot
import com.centuryprogrammer18thwasentsingleland.utils.notifyObserver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class PartialBaseItemFragmentVIewModel : ViewModel(), ValueEventOnPartialBaseItemCallback,PartialBaseItemAdapterInterface,
    UpdatePartialBaseItemsCallback {
    private val TAG = PartialBaseItemFragmentVIewModel::class.java.simpleName

    private lateinit var sharedPrefs: SharedPreferences

    private var valueEvnetListener : ValueEventListener? = null

    private val _allPartialBaseItems = MutableLiveData<MutableMap<String, PartialBaseItem>>()
    val allPartialBaseItems : LiveData<MutableMap<String, PartialBaseItem>>
        get() = _allPartialBaseItems

    // this is flag set as true when we update all base items
    private val _isUpdatedOk = MutableLiveData<Boolean>()
    val isUpdatedOk : LiveData<Boolean>
        get() = _isUpdatedOk

    fun setSharedPreferences(receivedSharedPrefs : SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun getAllPartialBaseItems(){
        Log.d(TAG,"----- getAllPartialBaseItems -----")

        allPartialBaseItems.value?.let{
            return
        }

        // there are NO saved data
        valueEvnetListener = Repository.addValeEventOnPartialBaseItem(sharedPrefs,this)
    }

    fun saveAllPartialBaseItemsInFirebase(){
        Log.d(TAG,"----- saveAllPartialBaseItemsInFirebase -----")

        Repository.updatePartialBaseItems(sharedPrefs, allPartialBaseItems.value!!, this)
    }

    fun defaultIsUpdatedOk(){
        _isUpdatedOk.value = false
    }

    override fun onValueEventOnPartialBaseItemCallback(
        snapshot: DataSnapshot?,
        error: DatabaseError?
    ) {
        error?.let {
            Log.d(TAG, "----- error : ${error.message}-----")
        }

        snapshot?.let{
            _allPartialBaseItems.value = getCollectionItemFromSnapshot<PartialBaseItem>(snapshot)
        }
    }


    // after updating all partial base items in firebase, this is called from Repository
    override fun onUpdatePartialBaseItemsCallback(isSuccess: Boolean) {
        if (isSuccess){
            Log.d(TAG, "----- update all base items is succeeded -----")
            _isUpdatedOk.value = true
        }
    }

    override fun onOkBtnClicked(key: String, partialBaseItem: PartialBaseItem) {
        Log.d(TAG, "----- onOkBtnClicked -----")
        Log.d(TAG,"----- key value is '${key}' ----")
        Log.d(TAG,"----- partialBaseItem is '${partialBaseItem}' ----")

        // update data with price
        _allPartialBaseItems.value?.let {
            it[key]= partialBaseItem
            _allPartialBaseItems.notifyObserver()
        }

    }

    override fun onCleared() {
        super.onCleared()

        valueEvnetListener?.let{
            Repository.removeValueEventOnPartialBaseItem(sharedPrefs,valueEvnetListener!!)
        }

    }
}