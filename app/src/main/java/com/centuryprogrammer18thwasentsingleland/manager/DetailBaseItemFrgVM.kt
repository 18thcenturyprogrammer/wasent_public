package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.BaseItem
import com.centuryprogrammer18thwasentsingleland.data.DetailBaseItem
import com.centuryprogrammer18thwasentsingleland.data.DetailItem
import com.centuryprogrammer18thwasentsingleland.data.PartialBaseItem
import com.centuryprogrammer18thwasentsingleland.repository.*

import com.centuryprogrammer18thwasentsingleland.utils.getCollectionItemFromSnapshot
import com.centuryprogrammer18thwasentsingleland.utils.getCollectionPartialItemFromSnapshot
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError

class DetailBaseItemFrgVM : ViewModel(), ValueEventOnItemCallback ,ValueEventOnBaseItemCallback,
    ValueEventOnPartialBaseItemByPushKeyCallback {
    private val TAG = DetailBaseItemFrgVM::class.java.simpleName

    private lateinit var sharedPrefs: SharedPreferences

    private val _detailBaseItems = MutableLiveData<MutableMap<String, DetailBaseItem>>()
    val detailBaseItems : LiveData<MutableMap<String, DetailBaseItem>>
        get() = _detailBaseItems

    private val _foundItemPrice = MutableLiveData<Double?>()
    val foundItemPrice : LiveData<Double?>
        get() = _foundItemPrice

    fun initSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun getDetailBaseItems(){
        Repository.addValueEventOnDetailBaseItems(sharedPrefs,this)
    }

    fun getBaseItemByPushKey(pushKey:String){
        Repository.addValeEventOnBaseItemByPushKey(pushKey,sharedPrefs,this)
    }

    // typeClean can be "dryclean" or "wetclean"
    fun getPartialBaseItemByPushKey(pushKey:String, typeClean:String){
        Repository.addValeEventOnPartialBaseItemByPushKey(pushKey,typeClean, sharedPrefs,this)
    }

    // getting all DetailBaseItem
    override fun onValueEventOnItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to bring whole items: error is '${error.toString()}' -----")
        }

        snapshot?.let{
            _detailBaseItems.value = getCollectionItemFromSnapshot<DetailBaseItem>(snapshot)
        }
    }

    // get BaseItem by push key
    override fun onValueEventOnBaseItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to bring whole items: error is '${error.toString()}' -----")
        }

        snapshot?.let{
            Log.d(TAG,"----- BaseItem _foundItemPrice.value will be | ${it.getValue(BaseItem::class.java)?.price.toString()} -----")
            _foundItemPrice.value = it.getValue(BaseItem::class.java)?.price

        }
    }

    override fun onValueEventOnPartialBaseItemByPushKeyCallback(
        snapshot: DataSnapshot?,
        error: DatabaseError?,
        typeClean: String
    ) {
        error?.let {
            Log.e(TAG, "----- there was problem to bring whole items: error is '${error.toString()}' -----")
        }

        snapshot?.let{
            val partialBaseItem =  it.getValue(PartialBaseItem::class.java)

            partialBaseItem?.let {
                val baseItemPrice =
                    when(typeClean){
                        "dryclean" -> {
                            it.drycleanPressPrice + (it.drycleanPressPrice*it.rate)+it.amount
                        }

                        "wetclean"-> {
                            it.wetcleanPressPrice + (it.wetcleanPressPrice*it.rate)+it.amount
                        }

                        else -> {
                            0.0
                        }
                    }

                Log.d(TAG,"----- PartialBaseItem _foundItemPrice.value will be | ${baseItemPrice.toString()} -----")
                _foundItemPrice.value = baseItemPrice

            }
        }
    }

    fun setNullFoundItemPrice(){
        _foundItemPrice.value = null
    }
}