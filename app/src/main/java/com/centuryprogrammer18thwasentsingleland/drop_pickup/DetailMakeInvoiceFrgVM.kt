package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.*
import com.centuryprogrammer18thwasentsingleland.singletons.SeletedFabricare

class DetailMakeInvoiceFrgVM : ViewModel() {
    private val TAG = DetailMakeInvoiceFrgVM::class.java.simpleName

    lateinit var actViewModel : MakeInvoiceActVM

    // all DetailItem which is in the database
    // all DetailItem which is in MakeInvoiceAcVM,and DetailMakeInvoiceFrg is observing
    private val _detailItems = MutableLiveData<MutableMap<String, DetailItem>>()
    val detailItems : LiveData<MutableMap<String, DetailItem>>
        get() = _detailItems

    // key ex) pant_small_dry_clean_press
    private val _detailBaseItems = MutableLiveData<MutableMap<String, DetailBaseItem>>()
    val detailBaseItems : LiveData<MutableMap<String, DetailBaseItem>>
        get() = _detailBaseItems

    // key is DetailItem name ex) small, large, fancy
    private val _mergedDetailItems = MutableLiveData<MutableMap<String, MergedDetailItem>>()
    val mergedDetailItems : LiveData<MutableMap<String, MergedDetailItem>>
        get() = _mergedDetailItems

    private val _selectedFabricare = MutableLiveData<SeletedFabricare>()
    val selectedFabricare: LiveData<SeletedFabricare>
        get() = _selectedFabricare

    private val _newDeatilBaseItem = MutableLiveData<MergedDetailItem>()
    val newDeatilBaseItem : LiveData<MergedDetailItem>
        get() = _newDeatilBaseItem





    fun updateDetailItems(updatedItems : MutableMap<String, DetailItem> ){
        _detailItems.value = updatedItems
    }

    fun updateDetailBaseItems(updatedItems : MutableMap<String, DetailBaseItem> ){
        _detailBaseItems.value = updatedItems
    }

    // MergedDetailItem will have DetailItem -- has name, category properties --,
    // DetailBaseItem -- has name,baseItemProcess,rate,amount properties--data combined.
    // each base item can have all DetailItem and DetailBaseItem for each categories
    // ex)  pant_dry_clean_press    has all DetailItem lining, long, hvy etc
    //    pant_wet_clean_press      has all DetailItem lining, long, hvy etc
    //    pant_dry_clean            has all DetailItem lining, long, hvy etc
    //    pant_wet_clean            has all DetailItem lining, long, hvy etc
    //    pant_dry_press            has all DetailItem lining, long, hvy etc
    //    pant_wet_press            has all DetailItem lining, long, hvy etc
    //    pant_alter                has all DetailItem lining, long, hvy etc
    //
    // i explained in different words. if i look in firebase db detail_base_item , i can find these
    // lined_2pc suite_dry_clean_press
    // lined_2pc suite_wet_clean_press
    // lined_2pc suite_dry_clean
    // lined_2pc suite_dry_press
    fun updateMergedDetailItems(category: String){

        // key is DetailItem name ex) small, large, special
        val mergedDetailItems = mutableMapOf<String,MergedDetailItem>()

        // bring only selected DetailBaseItem
        val selectedDetailBaseItems= getSelectedDetailBaseItems()

        // detailItems has
        // all DetailItem which is in the database
        // all DetailItem which is in MakeInvoiceAcVM,and DetailMakeInvoiceFrg is observing
        detailItems.value?.let {

            // we need all DetailItem , go through and make each MergedBaseItem
            for ( detailItem in it.values ){
                if (selectedDetailBaseItems.containsKey(detailItem.name)){
                    // there is matched DetailBaseItem

                    mergedDetailItems.put(detailItem.name,MergedDetailItem(
                        detailItem.name,
                        detailItem.category,
                        selectedDetailBaseItems[detailItem.name]!!.baseItemProcess,
                        selectedDetailBaseItems[detailItem.name]!!.rate,
                        selectedDetailBaseItems[detailItem.name]!!.amount,
                        selectedFabricare.value)
                    )
                }else{
                    // there is NO matched DetailBaseItem

                    mergedDetailItems.put(detailItem.name,MergedDetailItem(
                        detailItem.name,
                        detailItem.category,
                        null,
                        null,
                        null,
                        selectedFabricare.value)
                    )
                }
            }

            Log.d(TAG,"----- these will be shown in recyclerview detail items -----")
            for(item in mergedDetailItems){
                Log.d(TAG,"----- key|${item.key}, name|${item.value.name}, baseItemProcess|${item.value.baseItemProcess}, category|${item.value.category}, rate|${item.value.rate}}, amount|${item.value.amount}} -----")
            }

        }

        // only certain category MergedDetailItem
        // save in the variable
        _mergedDetailItems.value = getMergedDetailItemsinCategory(category,mergedDetailItems)
    }



    // check with selectedFabricare 's process and bring only matched DetailBaseItem for process
    // ex) in fabricares  user select BaseItem: pant, process : pant_dry_clean_press
    // this fuction will return DetailBaseItems for selected fabricare
    fun getSelectedDetailBaseItems():MutableMap<String,DetailBaseItem>{

        // make temp var
        val selectedDetailBaseItems = mutableMapOf<String,DetailBaseItem>()

        detailBaseItems.value?.let {

            Log.d(TAG,"----- COLLECTING selectedFabricare process == detailBaseItem.baseItemProcess ----")

            for (detailBaseItem in it.values){

                if(selectedFabricare.value !=null && selectedFabricare.value!!.isThereSelectedOne()) {

                    // DetailBaseItem.baseItemProcess ex) pant_dry_clean_press
                    // we need only matched ones
                    if (detailBaseItem.baseItemProcess == selectedFabricare.value!!.fabricare?.process){

                        Log.d(TAG,"-----found matched one ${detailBaseItem.baseItemProcess} , ${selectedFabricare.value!!.fabricare?.process} ----")

                        // key will be DetailItem name ex) small, large, fancy
                        selectedDetailBaseItems.put(detailBaseItem.name,detailBaseItem)
                    }
                }
            }
        }

        return selectedDetailBaseItems
    }

    // filter only certain category, and return only matched mergedDetailItems
    fun getMergedDetailItemsinCategory(category: String, mergedDetailItems: MutableMap<String,MergedDetailItem>):MutableMap<String,MergedDetailItem>{
        val temp = mutableMapOf<String,MergedDetailItem>()

        for (mergedDetailItem in mergedDetailItems.values){
            if (mergedDetailItem.category == category){
                temp.put(mergedDetailItem.name,mergedDetailItem)
            }
        }
        return temp
    }

    fun updateSelectedFabricare(updated : SeletedFabricare){
        _selectedFabricare.value = updated
    }

    fun onClickedMergedDetailItem(mergedDetailItem: MergedDetailItem){
        Log.d(TAG,"----- onClickedMergedDetailItem -----" )
        Log.d(TAG,"----- price is '${mergedDetailItem.toString()}' -----" )

        // when user not select any BaseItem , fabricare is null
        // so fabricare is null , nothing will happen

        if(mergedDetailItem.selectedFabricare != null && mergedDetailItem.selectedFabricare!!.isThereSelectedOne()){

            // check if there is DetatilBaseItem for selected Fabricare base item
            if(mergedDetailItem.rate == null || mergedDetailItem.amount == null){
                // there is no DetailBaseItem for selected Fabricare base item
                // we need to make new DetailBaseItem

                _newDeatilBaseItem.value = mergedDetailItem


            }else{
                // add DetailBaseItem data into Fabricare and send to MakeInvoiceActVM

                // save original fabricare
                // keep original data

                val orgfabricare = mergedDetailItem.selectedFabricare!!.fabricare

                var numFabricareFabricare : Int = orgfabricare!!.numFabricare
                var nameFabricare : String = orgfabricare!!.name
                var numPieceFabricare : Int = orgfabricare!!.numPiece
                var processFabricare : String = orgfabricare!!.process
                var basePriceFabricare : Double = orgfabricare!!.basePrice


                var subTotalPriceFabricare : Double = orgfabricare.subTotalPrice
                var dryPriceFabricare : Double = orgfabricare.dryPrice
                var wetPriceFabricare  : Double = orgfabricare.wetPrice
                var alterationPriceFabricare  : Double = orgfabricare.alterationPrice

                var commentFabricare  : String = orgfabricare!!.comment
                var fabricareDetails  : MutableList<FabricareDetail> = orgfabricare.fabricareDetails



                // update with clicked mergedDetailItem

                var subCharge =
                    if(mergedDetailItem.rate != 0f){
                        orgfabricare.basePrice*mergedDetailItem.rate!!
                    }else {
                        mergedDetailItem.amount!!
                    }

                // update with clicked mergedDetailItem

                when(mergedDetailItem.category){
                    "general" -> {
                        if(orgfabricare.dryPrice != 0.0){
                            dryPriceFabricare += subCharge
                        }
                        if(orgfabricare.wetPrice != 0.0){
                            wetPriceFabricare += subCharge
                        }
                    }
                    "alteration" -> {
                        alterationPriceFabricare += subCharge
                    }
                    else->{

                    }
                }

                subTotalPriceFabricare += subCharge

                val fabricareDetail = FabricareDetail(
                    mergedDetailItem.name,
                    mergedDetailItem.category,
                    subCharge,
                    mergedDetailItem.rate!!,
                    mergedDetailItem.amount!!
                )

                // update with clicked mergedDetailItem

                fabricareDetails.add(fabricareDetail)

                val updatedFabricare = Fabricare(
                    numFabricareFabricare,
                    nameFabricare,
                    numPieceFabricare,
                    processFabricare,
                    basePriceFabricare,
                    subTotalPriceFabricare,
                    dryPriceFabricare,
                    wetPriceFabricare,
                    alterationPriceFabricare,
                    commentFabricare,
                    fabricareDetails
                )

                actViewModel.updateFabricares(mergedDetailItem.selectedFabricare!!.index,updatedFabricare)

                // something changed in faricares , we need to refresh selectedFabricare in VM . eventhough it has same value
                actViewModel.updateSelectedFabricare(mergedDetailItem.selectedFabricare!!.index,updatedFabricare)
                
            }
        }
    }

    fun setNullNewDeatilBaseItem(){
        _newDeatilBaseItem.value = null
    }

}