package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.DetailItem
import com.centuryprogrammer18thwasentsingleland.repository.*
import com.centuryprogrammer18thwasentsingleland.utils.isValidKeyCharaters
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class DetailItemAddDialogFrgVM : ViewModel(),
    ValueEventOnDetailItemCallback, UpdateDetailItemCallback {
    private val TAG = DetailItemAddDialogFrgVM::class.java.simpleName

    private lateinit var sharedPrefs: SharedPreferences

    // contain three ValueEventListener for removing later
    private var valueEventListener : ValueEventListener? = null

    var etName: String = ""
    var selectedCatetory = "general"

    private val _messageOnFragment = MutableLiveData<String>()
    val messageOnFragment: LiveData<String>
        get() = _messageOnFragment

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog: LiveData<Boolean>
        get() = _closeDialog

    private val _isAddedItem = MutableLiveData<Boolean>()
    val isAddedItem: LiveData<Boolean>
        get() = _isAddedItem

    private val _allDetailItems = MutableLiveData<MutableMap<String, DetailItem>>()
    val allDetailItems : LiveData<MutableMap<String, DetailItem>>
        get() = _allDetailItems

    private val _isLoaded = MutableLiveData<Boolean>()
    val isLoaded : LiveData<Boolean>
        get() = _isLoaded

    fun setSharedPreferences(receivedSharedPrefs: SharedPreferences) {
        sharedPrefs = receivedSharedPrefs
    }

    fun attachValueEventListener() {
        // check event listener has been attached and it has not been then attach it
        if(valueEventListener == null){
            valueEventListener = Repository.addValueEventOnDetailItem(sharedPrefs, this)
        }
    }

    fun emptyMessageOnFragment() {
        _messageOnFragment.value = ""
    }

    fun changeCloseDialogTo(close: Boolean) {
        _closeDialog.value = close
    }

    fun changeIsLoadedTo(isLoaded: Boolean) {
        _isLoaded.value = isLoaded
    }


    fun onClickBtnItemAddOk() {
        Log.d(TAG, "===== item add button clicked =====")
        Log.d(TAG, "===== item name is '${etName}' =====")

        lateinit var newDetailItem: DetailItem

        // check there is empty field
        if (etName.isEmpty()) {
            Log.d(TAG, "===== etName is empty  =====")

            // i couldn't use resource string ,so i changed Application class
            // ref) https://stackoverflow.com/a/51279662/3151712
            _messageOnFragment.value = App.resourses!!.getString(R.string.could_you_check)
        } else {

            // firebase not allow some special characters for key
            // . $ [ ] # / not allowed
            // check if there is not allowed characters
            if(!isValidKeyCharaters(etName)){
                _messageOnFragment.value = App.resourses!!.getString(R.string.not_allowed_special_chars)
            }else{
                Log.d(TAG, "=====etName is '${etName}' =====")

                newDetailItem = DetailItem(etName,selectedCatetory)

                if (existedAlready(newDetailItem)) {
                    _messageOnFragment.value = App.resourses!!.getString(R.string.already_exist_item)
                } else {

                    _allDetailItems.value!!.put(newDetailItem.name, newDetailItem)

                    // update with new Item
                    Repository.updateDetailItem(
                        sharedPrefs,
                        allDetailItems.value!!,
                        this
                    )
                }
            }
        }
    }

    fun onClickBtnItemAddCancel() {
        Log.d(TAG, "===== cancel button clicked =====")
        changeCloseDialogTo(true)
        clearVars()
    }

//
    fun onCheckedChangedRbtng(radioGroup: RadioGroup, id:Int){
        Log.d(TAG,"----- onCheckedChangedRbtng -----")
        Log.d(TAG,"----- selected radion button id is '${id.toString()}' -----")

        selectedCatetory = when(id){
            R.id.rbtnGeneralDetailItemAdd ->  "general"
            R.id.rbtnAlterationDetailItemAdd -> "alteration"
            else -> "unknown"
        }

        Log.d(TAG,"----- selectedCatetory value is '${selectedCatetory}' -----")

    }

    override fun onValueEventOnDetailItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onValueEventOnDetailItemCallback -----")
        Log.d(TAG, "-----  'Detail item' value first load or changed in firebase  -----")

        error?.let {
            Log.d(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {

            val receivedItems = mutableMapOf<String, DetailItem>()

            for (item in snapshot.children) {
                receivedItems.put(item.key!!, item.getValue(DetailItem::class.java)!!)
            }
            _allDetailItems.value = receivedItems
            _isLoaded.value = true
        }
    }

    override fun onUpdateDetailItemCall(isSuccess: Boolean) {
        _isAddedItem.value = isSuccess
    }

    fun existedAlready(detailItem: DetailItem): Boolean {
        return allDetailItems.value!!.containsKey(detailItem.name)
    }

    fun clearVars() {
        etName = ""
        selectedCatetory = "general"
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "----- onCleared() called -----")

        valueEventListener?.let{
            Repository.removeValueEventOnDetailItem(sharedPrefs,valueEventListener!!)
        }
    }

}


