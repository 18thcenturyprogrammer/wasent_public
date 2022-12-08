package com.centuryprogrammer18thwasentsingleland.drop_pickup

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.BaseItem
import com.centuryprogrammer18thwasentsingleland.data.FabricareProcess

class BaseItemMakeInvoiceFrgVM : ViewModel() {
    private val TAG = BaseItemMakeInvoiceFrgVM::class.java.simpleName

    lateinit var actViewModel : MakeInvoiceActVM

    private val _itemsWillShown = MutableLiveData<MutableMap<String,BaseItem>>()
    val itemsWillShown : LiveData<MutableMap<String,BaseItem>>
        get() = _itemsWillShown

//    fun baseItemChanged(updatedData : MutableMap<String,BaseItem>){
//        _itemsWillShown.value = updatedData
//    }

    // for MutableMap user choose dryclean clean only
    // PartialItem has dryclean pricen and wetclean price itself
    // this function make temp BaseItem which has name, piece, calculated price depends on clean only or press only,
    // fabricareprocess string
    fun chooseDryCleanOnly(){
        actViewModel.cleanOnlyPartialBaseItems.value?.let{
            val temp = mutableMapOf<String,BaseItem>()
            for (item in actViewModel.cleanOnlyPartialBaseItems.value!!.values){
                lateinit var tempBaseItem: BaseItem


                val itemName = item.name

                // dry_clean_press means 'dryclean and press'
                // wet_clean_press means 'wetclean and press'
                // dry_clean means 'dryclean clean only'
                // dry_press means 'dryclean press only'
                val fabricareProces = FabricareProcess.dry_clean.toString()

                if(item.rate == 0f){
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.drycleanPressPrice+item.amount)
                }else{
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.drycleanPressPrice+(item.drycleanPressPrice*item.rate))
                }

                temp.put(item.name+"_dry_clean", tempBaseItem)
            }
            _itemsWillShown.value = temp
        }
    }

    // for MutableMap user choose dryclean press only
    // PartialItem has dryclean pricen and wetclean price itself
    // this function make temp BaseItem which has name, piece, calculated price depends on clean only or press only,
    // fabricareprocess string
    fun chooseDryPressOnly(){
        actViewModel.pressOnlyPartialBaseItems.value?.let{
            val temp = mutableMapOf<String,BaseItem>()
            for (item in actViewModel.pressOnlyPartialBaseItems.value!!.values){
                lateinit var tempBaseItem: BaseItem

                val itemName = item.name

                // dry_clean_press means 'dryclean and press'
                // wet_clean_press means 'wetclean and press'
                // dry_clean means 'dryclean clean only'
                // dry_press means 'dryclean press only'
                val fabricareProces = FabricareProcess.dry_press.toString()

                if(item.rate == 0f){
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.drycleanPressPrice+item.amount)
                }else{
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.drycleanPressPrice+(item.drycleanPressPrice*item.rate))
                }

                temp.put(item.name+"_dry_press", tempBaseItem)
            }
            _itemsWillShown.value = temp
        }
    }

    // for MutableMap user choose dryclean and press
    // this function make temp BaseItem which has name, piece, calculated price depends on clean only or press only,
    // fabricareprocess string
    fun chooseDryPress(){
        actViewModel.drycleanBaseItems.value?.let {
            val temp = mutableMapOf<String,BaseItem>()

            for (item in it.values){
                lateinit var tempBaseItem: BaseItem

                val itemName = item.name

                // dry_clean_press means 'dryclean and press'
                // wet_clean_press means 'wetclean and press'
                // dry_clean means 'dryclean clean only'
                // dry_press means 'dryclean press only'
                val fabricareProces = FabricareProcess.dry_clean_press.toString()

                tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.price)

                temp.put(item.name+"_dry_clean_press", tempBaseItem)
            }
            _itemsWillShown.value = temp
        }
    }

    // for MutableMap user choose wetclean clean only
    // PartialItem has dryclean pricen and wetclean price itself
    // this function make temp BaseItem which has name, piece, calculated price depends on clean only or press only,
    // fabricareprocess string
    fun chooseWetCleanOnly(){
        actViewModel.cleanOnlyPartialBaseItems.value?.let{
            val temp = mutableMapOf<String,BaseItem>()
            for (item in actViewModel.cleanOnlyPartialBaseItems.value!!.values){
                lateinit var tempBaseItem: BaseItem

                val itemName = item.name

                // dry_clean_press means 'dryclean and press'
                // wet_clean_press means 'wetclean and press'
                // dry_clean means 'dryclean clean only'
                // dry_press means 'dryclean press only'
                val fabricareProces = FabricareProcess.wet_clean.toString()

                if(item.rate == 0f){
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.wetcleanPressPrice+item.amount)
                }else{
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.wetcleanPressPrice+(item.wetcleanPressPrice*item.rate))
                }

                temp.put(item.name+"_wet_clean", tempBaseItem)
            }
            _itemsWillShown.value = temp
        }
    }

    // for MutableMap user choose wetclean press only
    // PartialItem has dryclean pricen and wetclean price itself
    // this function make temp BaseItem which has name, piece, calculated price depends on clean only or press only,
    // fabricareprocess string
    fun chooseWetPressOnly(){
        actViewModel.pressOnlyPartialBaseItems.value?.let{
            val temp = mutableMapOf<String,BaseItem>()
            for (item in actViewModel.pressOnlyPartialBaseItems.value!!.values){
                lateinit var tempBaseItem: BaseItem

                val itemName = item.name

                // dry_clean_press means 'dryclean and press'
                // wet_clean_press means 'wetclean and press'
                // dry_clean means 'dryclean clean only'
                // dry_press means 'dryclean press only'
                val fabricareProces = FabricareProcess.wet_press.toString()

                if(item.rate == 0f){
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.wetcleanPressPrice+item.amount)
                }else{
                    tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.wetcleanPressPrice+(item.wetcleanPressPrice*item.rate))
                }

                temp.put(item.name+"_wet_press", tempBaseItem)
            }
            _itemsWillShown.value = temp
        }
    }

    // for MutableMap user choose wetclean and press
    // this function make temp BaseItem which has name, piece, calculated price depends on clean only or press only,
    // fabricareprocess string
    fun chooseWetPress(){
        actViewModel.wetcleanBaseItems.value?.let {
            val temp = mutableMapOf<String,BaseItem>()

            for (item in it.values){
                lateinit var tempBaseItem: BaseItem

                val itemName = item.name

                // dry_clean_press means 'dryclean and press'
                // wet_clean_press means 'wetclean and press'
                // dry_clean means 'dryclean clean only'
                // dry_press means 'dryclean press only'
                val fabricareProces = FabricareProcess.wet_clean_press.toString()

                tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.price)

                temp.put(item.name+"_wet_clean_press", tempBaseItem)
            }
            _itemsWillShown.value = temp
        }

    }

    // for MutableMap user choose dryclean clean only
    // this function make temp BaseItem which has name, piece, calculated price depends on clean only or press only,
    // fabricareprocess string
    fun chooseAlterationOnly(){

        actViewModel.alterationOnlyBaseItems.value?.let {
            val temp = mutableMapOf<String,BaseItem>()

            for (item in it.values){
                lateinit var tempBaseItem: BaseItem

                val itemName = item.name

                // dry_clean_press means 'dryclean and press'
                // wet_clean_press means 'wetclean and press'
                // dry_clean means 'dryclean clean only'
                // dry_press means 'dryclean press only'
                val fabricareProces = FabricareProcess.alter.toString()

                tempBaseItem = BaseItem(itemName,item.numPiece,itemName+fabricareProces,item.price)

                temp.put(item.name+"_alter", tempBaseItem)
            }
            _itemsWillShown.value = temp
        }
    }


}