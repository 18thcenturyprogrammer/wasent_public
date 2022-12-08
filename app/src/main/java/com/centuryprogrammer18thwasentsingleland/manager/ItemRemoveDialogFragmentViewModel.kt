package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.repository.DeleteItemCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository

class ItemRemoveDialogFragmentViewModel : ViewModel(), DeleteItemCallback {
    private val TAG = ItemRemoveDialogFragmentViewModel::class.java.simpleName
    private lateinit var sharedPrefs: SharedPreferences

    var itemName :String?= null

    private val _isDeletedItem = MutableLiveData<Boolean>()
    val isDeletedItem : LiveData<Boolean>
        get() = _isDeletedItem

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog : LiveData<Boolean>
        get() = _closeDialog


    fun changeCloseDialogTo(close:Boolean){
        _closeDialog.value = close
    }

    fun setSharedPreferences(receivedSharedPrefs : SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun onClickCancelButton(){
        Log.d(TAG, "===== cancel button clicked in ItemRemoveDial =====")
        _closeDialog.value = true
    }

    fun onClickDeleteButton(itemName:String){
        Log.d(TAG, "===== delete button clicked in ItemRemoveDial =====")


        Repository.deleteItem(sharedPrefs,itemName,this)
    }

    override fun onDeleteItemCallback(isSucceed: Boolean) {
        itemName = null
        _isDeletedItem.value = isSucceed
    }
}