package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.DetailBaseItem
import com.centuryprogrammer18thwasentsingleland.repository.AddDetailBaseItemCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository

class DetailBaseItemAddDialogFrgVM : ViewModel(), AddDetailBaseItemCallback {
    private val TAG = DetailBaseItemAddDialogFrgVM::class.java.simpleName

    private lateinit var args :DetailBaseItemAddDialogFrgArgs
    private lateinit var sharedPrefs : SharedPreferences

    // user entered value , both way data binding
    var etRate :String = ""
    var etAmount: String = ""

    private val _messageOnFragment = MutableLiveData<String>()
    val messageOnFragment : LiveData<String>
        get() = _messageOnFragment

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog : LiveData<Boolean>
        get() = _closeDialog

    fun setArgs(receivedArgs: DetailBaseItemAddDialogFrgArgs){
        args = receivedArgs
    }

    fun setSharedPreferences(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

//    fun emptyMessageOnFragment(){
//        _messageOnFragment.value = ""
//    }
//
//    fun changeCloseDialogTo(close:Boolean){
//        _closeDialog.value = close
//    }

    fun clearVars(){
        etRate = ""
        etAmount = ""
    }


    // called from data biding detail_base_item_add_dialog_frg.xml
    fun onClickCancel(){
        clearVars()
        _closeDialog.value = true
    }

    // called from data biding detail_base_item_add_dialog_frg.xml
    fun onClickOk(){
        if(etAmount != "" && etRate != ""){
            // user entered new value, save it
            Log.d(TAG,"----- user enter DetailBaseItem rate or amount -----")
            Log.d(TAG,"----- etRate value is '${etRate.toString()}' -----")

            val detailName = args.mergedDetailItem.name
            val baseItemProcess = args.mergedDetailItem.selectedFabricare!!.fabricare!!.process
            val rate = etRate.toFloat()
            val amount = etAmount.replace("[$,]".toRegex(),"").toDouble()

            val newDetailBaseItem = DetailBaseItem(detailName,baseItemProcess,rate,amount)

            // save newDetailBaseItem in database
            Repository.addDetailBaseItem(sharedPrefs,newDetailBaseItem,this)

        }else{
            // i couldn't use resource string ,so i changed Application class
            // ref) https://stackoverflow.com/a/51279662/3151712
            _messageOnFragment.value = App.resourses!!.getString(R.string.empty_not_allowed)
        }

    }


    // called after save newDetailBaseItem in database
    override fun onAddDetailBaseItemCallback(isSuccess: Boolean) {
        if(isSuccess){
            _messageOnFragment.value = App.resourses!!.getString(R.string.add_item_success)
            _closeDialog.value = true
        }else{
            _messageOnFragment.value = App.resourses!!.getString(R.string.failed_to_add_item)
        }
    }
}