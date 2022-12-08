package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.repository.DeleteDetailBaseItemCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository

class DetailBaseItemRemoveDialogFrgVM : ViewModel(), DeleteDetailBaseItemCallback {
    private val TAG = DetailBaseItemRemoveDialogFrgVM::class.java.simpleName

    private lateinit var sharedPrefs: SharedPreferences

    var detailBaseItemPushKey :String? = null

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
        Log.d(TAG, "===== cancel button clicked in BaseItemRemoveDial =====")
        _closeDialog.value = true
    }

    fun onClickDeleteButton(detailBaseItemPushKey : String?){
        Log.d(TAG, "===== delete button clicked in BaseItemRemoveDial =====")
        Log.d(TAG, "===== detailBaseItemPushKey | ${detailBaseItemPushKey} =====")


        Repository.deleteDetailBaseItem(sharedPrefs,detailBaseItemPushKey!!,this)
    }

    override fun onDeleteDetailBaseItemCallback(isSuccess: Boolean) {
        detailBaseItemPushKey = null
        _isDeletedItem.value = isSuccess
    }
}