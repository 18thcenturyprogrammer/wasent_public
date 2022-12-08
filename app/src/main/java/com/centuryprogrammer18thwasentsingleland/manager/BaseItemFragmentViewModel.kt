package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.BaseItem
import com.centuryprogrammer18thwasentsingleland.data.PartialBaseItem
import com.centuryprogrammer18thwasentsingleland.repository.*
import com.centuryprogrammer18thwasentsingleland.utils.BaseItemVMAdapterInterface
import com.centuryprogrammer18thwasentsingleland.utils.getCollectionItemFromSnapshot
import com.centuryprogrammer18thwasentsingleland.utils.notifyObserver
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class BaseItemFragmentViewModel : ViewModel(), ValueEventOnItemsCallback,
    UpdateBaseItemPartialBaseItemCallback, BaseItemVMAdapterInterface {
    private val TAG = BaseItemFragmentViewModel::class.java.simpleName

    private lateinit var sharedPrefs : SharedPreferences

    private var valueEvnetListener : ValueEventListener? = null

    private val _allBaseItems = MutableLiveData<MutableMap<String, BaseItem>>()
    val allBaseItems : LiveData<MutableMap<String, BaseItem>>
        get() = _allBaseItems

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

    fun getAllBaseItemsPartialBaseItems(){
        allBaseItems.value?.let{
            return
        }

        // there are NO saved data
        valueEvnetListener = Repository.addValueEventOnItems(sharedPrefs,this)
    }

    fun saveAllBaseItemsInFirebase(){

        Repository.updateBaseItemsPartialBaseItems(sharedPrefs,allBaseItems.value!!, allPartialBaseItems.value!!, this)
    }

    fun defaultIsUpdatedOk(){
        _isUpdatedOk.value = false
    }

    override fun onValueEventOnItemsCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.d(TAG, "----- error : ${error.message}-----")
        }

        snapshot?.let{
            Log.d(TAG,"----- snapshot.child(\"base_item\") is '${snapshot.child("base_item").toString()}'-----")

            _allBaseItems.value = getCollectionItemFromSnapshot<BaseItem>(snapshot.child("base_item"))
            _allPartialBaseItems.value = getCollectionItemFromSnapshot<PartialBaseItem>(snapshot.child("partial_base_item"))

        }

    }


    // after updating all base items & all partial base items in firebase, this is called from Repository
    override fun onUpdateBaseItemPartialBaseItemCallback(isSuccess: Boolean) {
        if (isSuccess){
            Log.d(TAG, "----- update all base items is succeeded -----")
            _isUpdatedOk.value = true
        }
    }

    // this is called from BaseItemAdapter when user change price of BaseItem
    override fun onOkBtnClicked(key: String, baseItem: BaseItem){
        Log.d(TAG,"----- save button clicked ----")
        Log.d(TAG,"----- key value is '${key}' ----")
        Log.d(TAG,"----- baseItem is '${baseItem}' ----")

        // update data with price
        _allBaseItems.value?.let {
            it[key]= baseItem
            _allBaseItems.notifyObserver()
        }

        // update data with price
        _allPartialBaseItems.value?.let {
            val cleanOnlyKey = baseItem.name +"_clean_only"
            val pressOnlyKey = baseItem.name +"_press_only"

            when(baseItem.process){
                "dryclean_press" -> {
                    it[cleanOnlyKey]?.drycleanPressPrice = baseItem.price
                    it[pressOnlyKey]?.drycleanPressPrice = baseItem.price
                }

                "wetclean_press" -> {
                    it[cleanOnlyKey]?.wetcleanPressPrice = baseItem.price
                    it[pressOnlyKey]?.wetcleanPressPrice = baseItem.price
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        valueEvnetListener?.let{
            Repository.removeValueEventOnItems(sharedPrefs,valueEvnetListener!!)
        }

    }
}