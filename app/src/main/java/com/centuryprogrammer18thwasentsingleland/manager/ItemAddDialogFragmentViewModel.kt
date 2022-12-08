package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.*
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.BaseItem
import com.centuryprogrammer18thwasentsingleland.data.Item
import com.centuryprogrammer18thwasentsingleland.data.PartialBaseItem
import com.centuryprogrammer18thwasentsingleland.repository.*
import com.centuryprogrammer18thwasentsingleland.utils.isValidKeyCharaters

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class ItemAddDialogFragmentViewModel() : ViewModel(),
    ValueEventOnItemCallback,ValueEventOnBaseItemCallback, ValueEventOnPartialBaseItemCallback, childEventOnBaseItemCallback, UpdateAllItemsCallback {
    private val TAG = ItemAddDialogFragmentViewModel::class.java.simpleName

    private lateinit  var sharedPrefs : SharedPreferences

    // contain three ValueEventListener for removing later
//    "item", "baseItem","partialBaseItem"
    private var valueEventListeners = mutableMapOf<String,ValueEventListener>()

    var etName :String = ""
    var etNumPiece: String = ""

// Int number can reach 3 which mean Item, BaseItem, PartialBaseItem all loaded
    private val _isAllLoaded = MutableLiveData<Int>(0)
    val isAllLoaded : LiveData<Int>
        get() = _isAllLoaded

    private val _messageOnFragment = MutableLiveData<String>()
    val messageOnFragment : LiveData<String>
        get() = _messageOnFragment

    private val _closeDialog = MutableLiveData<Boolean>()
    val closeDialog : LiveData<Boolean>
        get() = _closeDialog

    private val _isAddedItem = MutableLiveData<Boolean>()
    val isAddedItem : LiveData<Boolean>
        get() = _isAddedItem



    private var allItems = mutableMapOf<String,Item>()
    private var allBaseItems = mutableMapOf<String,BaseItem>()
    private var allPartialBaseItems = mutableMapOf<String,PartialBaseItem>()

    fun setSharedPreferences(receivedSharedPrefs : SharedPreferences){
        sharedPrefs = receivedSharedPrefs
    }

    fun attachValueEventListeners(){
        // before started set flag _isAllLoaded as false
        _isAllLoaded.value = 0

        valueEventListeners.put("item",Repository.addValueEventOnItem(sharedPrefs,this))
        valueEventListeners.put("baseItem",Repository.addValeEventOnBaseItem(sharedPrefs,this))
        valueEventListeners.put("partialBaseItem",Repository.addValeEventOnPartialBaseItem(sharedPrefs,this))

        Log.d(TAG,"----- childEventListeners is '${valueEventListeners.toString()}' -----")

    }


    fun emptyMessageOnFragment(){
        _messageOnFragment.value = ""
    }

    fun changeCloseDialogTo(close:Boolean){
        _closeDialog.value = close
    }


    fun onClickBtnItemAddOk() {
        Log.d(TAG,"===== item add button clicked =====")
        Log.d(TAG,"===== item name is '${etName}' =====")
        Log.d(TAG,"===== item numPiece is '${etNumPiece}' =====")

        lateinit var newItem : Item

        // check there is empty field
        if(etName.isEmpty() || etNumPiece.isEmpty() || etNumPiece.toInt() == 0 ){
            Log.d(TAG,"===== some field are empty or 0 =====")

            // i couldn't use resource string ,so i changed Application class
            // ref) https://stackoverflow.com/a/51279662/3151712
            _messageOnFragment.value = App.resourses!!.getString(R.string.could_you_check)
        }else{

            // firebase not allow some special characters for key
            // . $ [ ] # / not allowed
            // check if there is not allowed characters
            if(!isValidKeyCharaters(etName)){
                _messageOnFragment.value = App.resourses!!.getString(R.string.not_allowed_special_chars)
            }else{
                Log.d(TAG,"=====etName is '${etName}',etNumPiece is '${etNumPiece}' =====")

                newItem = Item(etName, etNumPiece.toInt())

                if (existedAlready(newItem)){
                    _messageOnFragment.value = App.resourses!!.getString(R.string.already_exist_item)
                }else{

                    allItems.put(newItem.name,newItem)

                    // make three BaseItem and put in MutableMap
                    allBaseItems.putAll(makeBaseItemsFromItem(newItem))

                    // make two PartialBaseItem and put in MutableMap
                    allPartialBaseItems.putAll(makePartialBaseItemsFromItem(newItem))

                    // update with new Item
                    Repository.updateAllItems(sharedPrefs, allItems, allBaseItems,allPartialBaseItems, this)
                }
            }
        }
    }

    fun makeBaseItemsFromItem(newItem : Item): MutableMap<String, BaseItem> {
        val drycleanPressBaseItem = BaseItem(name= newItem.name, numPiece = newItem.numPiece, process = "dryclean_press")
        val wetcleanPressBaseItem = BaseItem(name= newItem.name, numPiece = newItem.numPiece, process = "wetclean_press")
        val alterationOnlyBaseItem = BaseItem(name= newItem.name, numPiece = newItem.numPiece, process = "alteration_only")

        val threeBaseItems = mutableMapOf<String, BaseItem>()
        threeBaseItems.put(newItem.name+"_dryclean_press", drycleanPressBaseItem)
        threeBaseItems.put(newItem.name+"_wetclean_press", wetcleanPressBaseItem)
        threeBaseItems.put(newItem.name+"_alteration_only", alterationOnlyBaseItem)

        return threeBaseItems
    }

    fun makePartialBaseItemsFromItem(newItem : Item): MutableMap<String, PartialBaseItem> {
        val cleanOnlyPartialBaseItem = PartialBaseItem(name= newItem.name, numPiece = newItem.numPiece, process = "clean_only")
        val pressOnlyPartialBaseItem = PartialBaseItem(name= newItem.name, numPiece = newItem.numPiece, process = "press_only")


        val BaseItems = mutableMapOf<String, PartialBaseItem>()
        BaseItems.put(newItem.name+"_clean_only", cleanOnlyPartialBaseItem)
        BaseItems.put(newItem.name+"_press_only", pressOnlyPartialBaseItem)

        return BaseItems
    }

    fun onClickBtnItemAddCancel(){
        Log.d(TAG,"===== cancel button clicked =====")
        changeCloseDialogTo(true)
        clearVars()
    }

    override fun onValueEventOnItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onValueEventOnItemCallback -----")
        Log.d(TAG,"-----  'item' value first load or changed in firebase  -----")

        error?.let {
            Log.d(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            loadedOk()

            val receivedItems = mutableMapOf<String,Item>()

            for (item in snapshot.children){
                receivedItems.put(item.key!!, item.getValue(Item::class.java)!!)
            }

            allItems = receivedItems

        }

    }


    override fun onValueEventOnBaseItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onValueEventOnBaseItemCallback -----")
        Log.d(TAG,"-----  'Baseitem' value first load or changed in firebase  -----")

        error?.let {
            Log.d(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            loadedOk()

            val receivedBaseItems = mutableMapOf<String,BaseItem>()

            for (item in snapshot.children){
                receivedBaseItems.put(item.key!!, item.getValue(BaseItem::class.java)!!)
            }

            allBaseItems = receivedBaseItems

            Log.d(TAG, "----- allBaseItems is ${allItems.toString()} -----")
        }
    }

    override fun onValueEventOnPartialBaseItemCallback(
        snapshot: DataSnapshot?,
        error: DatabaseError?
    ) {
        Log.d(TAG, "----- onValueEventOnPartialBaseItemCallback -----")
        Log.d(TAG,"-----  'PartialBaseitem' value first load or changed in firebase  -----")

        error?.let {
            Log.d(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            loadedOk()

            val receivedPartialBaseItems = mutableMapOf<String,PartialBaseItem>()

            for (item in snapshot.children){
                receivedPartialBaseItems.put(item.key!!, item.getValue(PartialBaseItem::class.java)!!)
            }

            allPartialBaseItems = receivedPartialBaseItems

            Log.d(TAG, "----- allPartialBaseItems is ${allPartialBaseItems.toString()} -----")
        }
    }

    override fun onChildEventOnBaseItemCall(
        snapshot: DataSnapshot?,
        error: DatabaseError?,
        whatHappend: String
    ) {
        Log.d(TAG, "----- onChildEventOnBaseItemCall -----")
        Log.d(TAG,"-----  child BaseItem changed in 'base_item_collection' in firebase  -----")

        error?.let {
            Log.d(TAG, "----- error : ${error.message}-----")
        }

        // if there is no BaseItem , snapshot will be null
        snapshot?.let {
            when(whatHappend){
                "changed"->{
                    Log.d(TAG, "----- snapshot is  ${snapshot.toString()}-----")
                }
                "added"->{
                    Log.d(TAG, "----- added : ${error?.message}-----")
                }
                "removed"->{
                    Log.d(TAG, "----- removed : ${error?.message}-----")
                }
                else->{
                    Log.d(TAG, "----- else : ${error?.message}-----")
                }
            }

            val receivedBaseItems = mutableMapOf<String,BaseItem>()

            for (baseItem in snapshot.children){
                receivedBaseItems.put(baseItem.key!!, baseItem.getValue(BaseItem::class.java)!!)
            }

            allBaseItems = receivedBaseItems

            Log.d(TAG, "----- allItems is ${allBaseItems.toString()} -----")
        }

        if (error == null){
            Log.d(TAG, "-----2-1 isAllLoaded.value is ${isAllLoaded.value.toString()} -----")
            val a = isAllLoaded.value

            _isAllLoaded.value = a!! + 2
            Log.d(TAG, "-----2-2 isAllLoaded.value is ${isAllLoaded.value.toString()} -----")
        }
    }




    // set flag as new item is added into firebase
    override fun onUpdateAllItemsCall(isSuccess: Boolean) {
        _isAddedItem.value = isSuccess
    }

    fun existedAlready(item:Item):Boolean{
        return allItems.containsKey(item.name)
    }

    fun clearVars(){
        etName = ""
        etNumPiece = ""
    }

    fun loadedOk(){
        Log.d(TAG, "----- before isAllLoaded.value is ${isAllLoaded.value.toString()} -----")
        val a = isAllLoaded.value
        _isAllLoaded.value = a!! + 1
        Log.d(TAG, "----- after isAllLoaded.value is ${isAllLoaded.value.toString()} -----")
    }

    fun removeListeners(){
        for (key in valueEventListeners.keys){
            when(key){
                "item" -> {
                    Repository.removeValueEventOnItem(sharedPrefs, valueEventListeners["item"]!!)
                }
                "baseItem" -> {
                    Repository.removeValueEventOnBaseItem(sharedPrefs, valueEventListeners["baseItem"]!!)
                }
                "partialBaseItem" -> {
                    Repository.removeValueEventOnPartialBaseItem(sharedPrefs, valueEventListeners["partialBaseItem"]!!)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "----- onCleared() called -----")

        removeListeners()
    }

}