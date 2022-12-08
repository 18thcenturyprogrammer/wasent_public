package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.repository.DeleteDetailItemCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository

class DetailItemRemoveFrgVM : ViewModel(), DeleteDetailItemCallback {
    private val TAG = DetailItemRemoveFrgVM::class.java.simpleName
    private lateinit var sharedPrefs: SharedPreferences

    var detailItemName :String?= null

    private val _isDeletedDetailItem = MutableLiveData<Boolean?>()
    val isDeletedDetailItem : LiveData<Boolean?>
        get() = _isDeletedDetailItem

    private val _closeDialog = MutableLiveData<Boolean?>()
    val closeDialog : LiveData<Boolean?>
        get() = _closeDialog


    fun changeCloseDialogTo(close:Boolean?){
        _closeDialog.value = close
    }

    fun changeIsDeletedTo(isDeleted:Boolean?){

        _isDeletedDetailItem.value = isDeleted
    }

    fun setSharedPreferences(receivedSharedPrefs : SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun onClickCancelButton(){
        Log.d(TAG, "===== cancel button clicked in ItemRemoveDial =====")
        changeCloseDialogTo(true)

        // back to default status
        changeCloseDialogTo(null)
    }

    fun onClickDeleteButton(itemName:String){
        Log.d(TAG, "===== delete button clicked in ItemRemoveDial =====")

        Repository.deleteDetailItem(sharedPrefs,itemName,this)
    }

    override fun onDeleteDetailItemCallback(isSucceed: Boolean) {
        detailItemName = null
        changeIsDeletedTo(isSucceed)

        // back to default status
        changeIsDeletedTo(null)
    }
}