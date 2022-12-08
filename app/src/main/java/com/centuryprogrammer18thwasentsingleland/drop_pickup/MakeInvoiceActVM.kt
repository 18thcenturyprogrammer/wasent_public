package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.*
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.*
import com.centuryprogrammer18thwasentsingleland.repository.AddInvoiceOrderCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.repository.ValueEventOnItemCallback
import com.centuryprogrammer18thwasentsingleland.singletons.HoldedInvoiceOrders
import com.centuryprogrammer18thwasentsingleland.singletons.SeletedFabricare
import com.centuryprogrammer18thwasentsingleland.utils.*
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import java.lang.StringBuilder
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class MakeInvoiceActVM : ViewModel(), ValueEventOnItemCallback, AddInvoiceOrderCallback, DatePickerDialog.OnDateSetListener {
    private val TAG = MakeInvoiceActVM::class.java.simpleName

    // if MakeInvoiceActivity stared with argument invoiceOrder, this is for editing
    // if it's started without invoiceOrder, this is for making new InvoiceOrder
    // default is null which means adding new InvoiceOrder
    private var originalInvoiceOrder : InvoiceOrder? = null

    private var envRate = 0.0f
    private var envAmount = 0.0
    private var tax : Float = 0.0f
    private var teamId: String = ""
    private var discount : Double = 0.0
    private var prepaid : Double = 0.0

    private lateinit var customer : Customer

    private lateinit var sharedPrefs: SharedPreferences

    private var valueEvnetListener : ValueEventListener? = null


    private val _drycleanBaseItems = MutableLiveData<MutableMap<String, BaseItem>>()
    val drycleanBaseItems : LiveData<MutableMap<String, BaseItem>>
        get() = _drycleanBaseItems

    private val _wetcleanBaseItems = MutableLiveData<MutableMap<String, BaseItem>>()
    val wetcleanBaseItems : LiveData<MutableMap<String, BaseItem>>
        get() = _wetcleanBaseItems

    private val _alterationOnlyBaseItems = MutableLiveData<MutableMap<String, BaseItem>>()
    val alterationOnlyBaseItems : LiveData<MutableMap<String, BaseItem>>
        get() = _alterationOnlyBaseItems

    private val _cleanOnlyPartialBaseItems = MutableLiveData<MutableMap<String, PartialBaseItem>>()
    val cleanOnlyPartialBaseItems : LiveData<MutableMap<String, PartialBaseItem>>
        get() = _cleanOnlyPartialBaseItems

    private val _pressOnlyPartialBaseItems = MutableLiveData<MutableMap<String, PartialBaseItem>>()
    val pressOnlyPartialBaseItems : LiveData<MutableMap<String, PartialBaseItem>>
        get() = _pressOnlyPartialBaseItems


    private val _detailItems = MutableLiveData<MutableMap<String, DetailItem>>()
    val detailItems : LiveData<MutableMap<String,DetailItem>>
        get() = _detailItems

    private val _detailBaseItems = MutableLiveData<MutableMap<String, DetailBaseItem>>()
    val detailBaseItems : LiveData<MutableMap<String,DetailBaseItem>>
        get() = _detailBaseItems

    private val _colorItems = MutableLiveData<MutableList<ColorItem>>()
    val colorItems : LiveData<MutableList<ColorItem>>
        get() = _colorItems

    // connected to activity_make_invoice.xml toggle button
    private val _splitSelected = MutableLiveData<Boolean>(false)
    val splitSelected : LiveData<Boolean>
        get() = _splitSelected

    private val _messageToActivity = MutableLiveData<String>()
    val messageToActivity : LiveData<String>
        get() = _messageToActivity



    private val _closeUpchargeDialog = MutableLiveData<Boolean>()
    val closeUpchargeDialog : LiveData<Boolean>
        get() = _closeUpchargeDialog

    private val _closeManualDialog = MutableLiveData<Boolean>()
    val closeManualDialog : LiveData<Boolean>
        get() = _closeManualDialog

    private val _closeDiscountDialog = MutableLiveData<Boolean>()
    val closeDiscountDialog : LiveData<Boolean>
        get() = _closeDiscountDialog

    private val _closeNumPieceDialog = MutableLiveData<Boolean>()
    val closeNumPieceDialog : LiveData<Boolean>
        get() = _closeNumPieceDialog

    private val _closeCommentDialog = MutableLiveData<Boolean>()
    val closeCommentDialog : LiveData<Boolean>
        get() = _closeCommentDialog

    private val _closeTagDialog = MutableLiveData<Boolean>()
    val closeTagDialog : LiveData<Boolean>
        get() = _closeTagDialog





    // fabricares hold each garment item which include BaseItem, DetailItem information
    private val _fabricares = MutableLiveData<MutableList<Fabricare>>()
    val fabricares : LiveData<MutableList<Fabricare>>
        get() = _fabricares

    private val _qty = MutableLiveData<MutableMap<String,Int>>()
    val qty : LiveData<MutableMap<String,Int>>
        get() = _qty

    private val _priceStatement = MutableLiveData<MutableMap<String,Double>>()
    val priceStatement : LiveData<MutableMap<String,Double>>
        get() = _priceStatement

    private val _discountStatement = MutableLiveData<DiscountStatement>()
    val discountStatement : LiveData<DiscountStatement>
        get() = discountStatement


    // default value is -1 which means nothing selected
    private val _selectedFabricare = MutableLiveData<SeletedFabricare>()
    val selectedFabricare: LiveData<SeletedFabricare>
        get() = _selectedFabricare


    private val _dueDate = MutableLiveData<String>()
    val dueDate : LiveData<String>
        get() = _dueDate

    private val _tag = MutableLiveData<String>()
    val tag : LiveData<String>
        get() = _tag

    private val _tagColor = MutableLiveData<String>()
    val tagColor : LiveData<String>
        get() = _tagColor

    private val _moveToDropPickupACT = MutableLiveData<Boolean>()
    val moveToDropPickupACT : LiveData<Boolean>
        get() = _moveToDropPickupACT

    private var _printingContent : InvoiceOrder? = null

    private val _printing = MutableLiveData<MutableList<PrintingJob>>()
    val printing : LiveData<MutableList<PrintingJob>>
        get() = _printing


    fun updateCustomer(receivedCustomer: Customer){
        customer = receivedCustomer
    }

    fun updateSharedPrefs(receivedSharedPrefs: SharedPreferences){
        sharedPrefs = receivedSharedPrefs
        envRate = sharedPrefs.getString("env_rate", "0").toFloat()
        envAmount = sharedPrefs.getString("env_amount", "0").replace("[$,]".toRegex(),"").toDouble()
        tax = sharedPrefs.getString("tax", "0").toFloat()
        teamId = sharedPrefs.getString("logged_team_id", "")
    }

    fun initWithInvoiceOrder(comingInvoiceOrder: InvoiceOrder){
        // MakeInvoiceActivity stared with argument invoiceOrder, this is for editing

        originalInvoiceOrder = comingInvoiceOrder

        _fabricares.value = comingInvoiceOrder.fabricares
        _qty.value = comingInvoiceOrder.qtyTable
        _priceStatement.value = comingInvoiceOrder.priceStatement

        prepaid = comingInvoiceOrder.priceStatement!!["prepaid"]!!

        SeletedFabricare.selected(comingInvoiceOrder.fabricares!!.count()-1,comingInvoiceOrder.fabricares!!.last() )
        _selectedFabricare.value = SeletedFabricare
    }

    fun onBaseItemClicked(nameTv:TextView,numPieceTv:TextView,processTv:TextView,priceTv:TextView){
        Log.e(TAG, "----- onBaseItemClicked -----")
        Log.e(TAG, "----- name is '${nameTv.text}' -----")
        Log.e(TAG, "----- numPiece is '${numPieceTv.text}' -----")
        Log.e(TAG, "----- process is '${processTv.text}' -----")
        Log.e(TAG, "----- price is '${priceTv.text}' -----")

        val nameStr = nameTv.text.toString()
        val numPieceStr = numPieceTv.text.toString()
        val processStr = processTv.text.toString()
        val priceStr = priceTv.text.toString().replace("[$,]".toRegex(),"")

        var dryPrice = 0.0
        var wetPrice = 0.0
        var alterationPrice = 0.0

        when(processStr){
            nameStr + "_dry_clean_press", nameStr + "_dry_clean" ->{
                dryPrice = priceStr.toDouble()
            }
            nameStr + "_wet_clean_press", nameStr + "_wet_clean" ->{
                wetPrice = priceStr.toDouble()
            }

            nameStr + "_alter" ->{
                alterationPrice = priceStr.toDouble()
            }

        }

        val newfabricare = Fabricare(
            name = nameStr,
            numPiece = numPieceStr.toInt(),
            process =  processStr,
            basePrice = priceStr.toDouble(),
            subTotalPrice = priceStr.toDouble(),
            dryPrice = dryPrice,
            wetPrice = wetPrice,
            alterationPrice = alterationPrice
        )

        addFabricare(newfabricare)
    }

    // select new fabricare when user add new fabricare, so user can work on the new fabricare
    fun setAsSelected(){
        val positionInFabricares=
            if(_fabricares.value == null) {0}
            else{_fabricares.value!!.count()-1}

        val fabricare = fabricares.value!!.get(positionInFabricares)

        _selectedFabricare.value = SeletedFabricare.selected(positionInFabricares,fabricare)
    }


    // user select one of fabricare
    fun onClickFabricare(view: View){
        Log.d(TAG,"----- fabricared clicked : view tag value is '${view.tag.toString().toInt() }' -----")


        val index = view.tag.toString().toInt()
        val fabricare = fabricares.value!![index]

        _selectedFabricare.value = SeletedFabricare.selected(index,fabricare)

    }

    // called from color_item_make_invoice_viewholder.xml
    fun onClickColorItem(colorItem:ColorItem){
        Log.d(TAG,"----- onClickColorItem -----")
        Log.d(TAG,"----- colorItem value is '${colorItem.toString()}' -----")

        selectedFabricare.value?.let {it ->

            // make fabricareDetail with colorItem name
            val fabricareDetail = FabricareDetail(colorItem.name)

            // add fabricareDetail and refresh
            _selectedFabricare.value!!.fabricare?.fabricareDetails?.add(fabricareDetail)
            _selectedFabricare.notifyObserver()
        }
    }


    // update Qty table
    fun updateQty(fabricares: MutableList<Fabricare>){

        var tempQty = mutableMapOf<String,Int>(
            "dry" to 0,
            "wet" to 0,
            "alter" to 0,
            "press" to 0,
            "clean" to 0
        )

        for(fabricare in fabricares){
            when(fabricare.process) {
                fabricare.name + "_dry_clean_press" -> {
                    tempQty["dry"] = tempQty["dry"]!!.plus(fabricare.numPiece)
                }
                fabricare.name + "_wet_clean_press" -> {
                    tempQty["wet"] = tempQty["wet"]!!.plus(fabricare.numPiece)
                }
                fabricare.name + "_dry_clean", fabricare.name + "_wet_clean" -> {
                    tempQty["clean"] = tempQty["clean"]!!.plus(fabricare.numPiece)
                }
                fabricare.name + "_dry_press", fabricare.name + "_wet_press" -> {
                    tempQty["press"] = tempQty["press"]!!.plus(fabricare.numPiece)
                }
                fabricare.name + "_alter" -> {
                    tempQty["alter"] = tempQty["alter"]!!.plus(fabricare.numPiece)
                }
            }
        }

        _qty.value= tempQty
        _qty.notifyObserver()

    }


    // update Price statement table
    fun updatePriceStatement(
        fabricares: MutableList<Fabricare>,
        envRate:Float=0f,
        envAmount: Double=0.0,
        tax:Float=0f,
        discountStatment:DiscountStatement?=null,
        prepaid:Double=0.0)
    {

        var tempPriceStatement = mutableMapOf<String,Double>(
            "subTotal" to 0.0,
            "discount" to 0.0,
            "tax" to 0.0,
            "env" to 0.0,
            "total" to 0.0,
            "prepaid" to 0.0,
            "balance" to 0.0,
            "dryPrice" to 0.0,
            "wetPrice" to 0.0,
            "alterPrice" to 0.0
        )

        for(fabricare in fabricares){

            tempPriceStatement["subTotal"] = tempPriceStatement["subTotal"]!!.plus(fabricare.subTotalPrice)

            if(envRate !=0f){
                // there is decided env rate

                when(fabricare.process){
                    fabricare.name+"_dry_clean_press", fabricare.name+"_dry_clean" ->{
                        tempPriceStatement["env"] = tempPriceStatement["env"]!!.plus(fabricare.subTotalPrice*envRate)
                    }
                }
            }

            if(envAmount !=0.0){
                // there is decided env rate

                when(fabricare.process){
                    fabricare.name+"_dry_clean_press", fabricare.name+"_dry_clean" ->{
                        tempPriceStatement["env"] = tempPriceStatement["env"]!!.plus(envAmount)
                    }
                }
            }

            tempPriceStatement["dryPrice"] = tempPriceStatement["dryPrice"]!!.plus(fabricare.dryPrice)
            tempPriceStatement["wetPrice"] = tempPriceStatement["wetPrice"]!!.plus(fabricare.wetPrice)
            tempPriceStatement["alterPrice"] = tempPriceStatement["alterPrice"]!!.plus(fabricare.alterationPrice)

        }

        var realDiscountAmount = 0.0
        discountStatment?.let {
            when(discountStatment){
                DiscountStatement.ENTIRE ->{
                    if(discountStatment.rate != 0f){
                        realDiscountAmount = tempPriceStatement["subTotal"]!!.times(discountStatment.rate)
                    }
                    if(discountStatment.amount != 0.0){
                        realDiscountAmount = discountStatment.amount
                    }
                }
                DiscountStatement.DRY ->{
                    realDiscountAmount = tempPriceStatement["dryPrice"]!!.times(discountStatment.rate)
                }
                DiscountStatement.WET ->{
                    realDiscountAmount = tempPriceStatement["wetPrice"]!!.times(discountStatment.rate)
                }
                DiscountStatement.ALTER ->{
                    realDiscountAmount = tempPriceStatement["alterPrice"]!!.times(discountStatment.rate)
                }
            }
        }

        tempPriceStatement["discount"] = realDiscountAmount
        tempPriceStatement["tax"] = (tempPriceStatement["subTotal"]!!-tempPriceStatement["discount"]!!) * tax
        tempPriceStatement["total"] = tempPriceStatement["subTotal"]!! + tempPriceStatement["tax"]!! + tempPriceStatement["env"]!! - tempPriceStatement["discount"]!!
        tempPriceStatement["prepaid"] = prepaid
        tempPriceStatement["balance"] = tempPriceStatement["total"]!! - tempPriceStatement["prepaid"]!!



        _priceStatement.value= tempPriceStatement
        _priceStatement.notifyObserver()

    }


























    fun getBaseItemsDetailItems(){
        valueEvnetListener = Repository.addValueEventOnWholeItems(sharedPrefs, this)
    }

    override fun onValueEventOnItemCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        error?.let {
            Log.e(TAG, "----- there was problem to bring whole items: error is '${error.toString()}' -----")
        }

        snapshot?.let{
            Log.d(TAG,"----- snapshot.child(\"base_item\") is '${snapshot.child("base_item").toString()}'-----")

            _drycleanBaseItems.value = getCollectionItemFromSnapshot<BaseItem>(snapshot.child("base_item"),"dryclean_press")
            _wetcleanBaseItems.value = getCollectionItemFromSnapshot<BaseItem>(snapshot.child("base_item"),"wetclean_press")
            _alterationOnlyBaseItems.value = getCollectionItemFromSnapshot<BaseItem>(snapshot.child("base_item"),"alteration_only")

            _cleanOnlyPartialBaseItems.value = getCollectionPartialItemFromSnapshot(snapshot.child("partial_base_item"),"clean_only")
            _pressOnlyPartialBaseItems.value = getCollectionPartialItemFromSnapshot(snapshot.child("partial_base_item"),"press_only")

            _detailItems.value = getCollectionItemFromSnapshot<DetailItem>(snapshot.child("detail_item"))

            Log.d(TAG,"----- snapshot.child(\"detail_base_item\") is '${snapshot.child("detail_base_item").toString()}'-----")
            _detailBaseItems.value = getCollectionItemFromSnapshot<DetailBaseItem>(snapshot.child("detail_base_item"))


            Log.d(TAG,"----- drycleanBaseItems.value is '${drycleanBaseItems.value.toString()}'-----")
            Log.d(TAG,"----- wetcleanBaseItems.value is '${wetcleanBaseItems.value.toString()}'-----")
            Log.d(TAG,"----- alterationOnlyBaseItems.value is '${alterationOnlyBaseItems.value.toString()}'-----")
            Log.d(TAG,"----- cleanOnlyPartialBaseItems.value is '${cleanOnlyPartialBaseItems.value.toString()}'-----")
            Log.d(TAG,"----- pressOnlyPartialBaseItems.value is '${pressOnlyPartialBaseItems.value.toString()}'-----")

            Log.d(TAG,"----- detailItems.value is '${detailItems.value.toString()}'-----")
            Log.d(TAG,"----- detailBaseItems.value is '${detailBaseItems.value.toString()}'-----")
        }
    }

    fun updateFabricares(fabricareIndex: Int, updatedFabricare : Fabricare){
        val tempFabricares = fabricares.value
        tempFabricares!![fabricareIndex] = updatedFabricare
        _fabricares.value = tempFabricares
    }

    fun updateSelectedFabricare(index:Int,updatedFabricare: Fabricare){
        _selectedFabricare.value = SeletedFabricare.selected(index,updatedFabricare)
    }

    // get new fabricare and put in fabricares
    // we have two ways to put new fabricare
    // one split
    // two together - default
    fun addFabricare(newFabricare:Fabricare) {
        Log.d(TAG, "----- splitSelected value is '${splitSelected.value.toString()}' -----")

        // if split mode on , fabricare will be in different row even thought there is already same BaseItem, process
        if (splitSelected.value!!) {
            // user want split fabricare

            var savedFabricares = fabricares.value

            if (savedFabricares == null) {
                savedFabricares = mutableListOf<Fabricare>()
            }
            savedFabricares?.add(newFabricare)
            _fabricares.value = savedFabricares

            // select new fabricare
            setAsSelected()

        } else {
            // user want to make together
            Log.d(TAG, "----- addFabricare. together mode -----")

            var savedFabricares = fabricares.value

            if (savedFabricares == null) {
                // first adding fabricare

                savedFabricares = mutableListOf<Fabricare>()
                savedFabricares?.add(newFabricare)
                _fabricares.value = savedFabricares

            } else {
                val temp = mutableListOf<Fabricare>()

                // if there is same BaseItem, process, this will be true
                // if there is no same one, we will add in end of fabricares
                var CanTogetherFlag = false

                // if there are same fabricare more than one, fabricare added multiple times,
                // so if we add fabricare once, set to true
                var addTogetherFlag = false

                // loop through fabricares which have been added already by user
                for (fabricare in savedFabricares) {
                    if (fabricare.name == newFabricare.name && fabricare.process == newFabricare.process) {
                        // we have same fabricare already which means we can put these in one row

                        if(addTogetherFlag){
                            // added into one row already

                            temp.add(fabricare)
                            continue
                        }

                        // flags
                        CanTogetherFlag = true
                        addTogetherFlag = true

                        // add new fabricare data into original one for making new Fabricare obj
                        val numFabricare = fabricare.numFabricare + newFabricare.numFabricare
                        val name = fabricare.name
                        val numPiece = fabricare.numPiece + newFabricare.numPiece
                        val process = fabricare.process
                        val basePrice = fabricare.basePrice
                        val subTotalPrice = fabricare.subTotalPrice + newFabricare.subTotalPrice
                        val dryPrice = fabricare.dryPrice + newFabricare.dryPrice
                        val wetPrice = fabricare.wetPrice + newFabricare.wetPrice
                        val alterationPrice =
                            fabricare.alterationPrice + newFabricare.alterationPrice
                        val fabricareDetails =
                            (fabricare.fabricareDetails + newFabricare.fabricareDetails) as MutableList<FabricareDetail>

                        val comment =
                            if(fabricare.comment.isNotEmpty()){
                                fabricare.comment + ", " + newFabricare.comment
                            }else{
                                newFabricare.comment
                            }


                        // make new Fabricare which new fabricare data added and put in temp storage
                        temp.add(
                            Fabricare(
                                numFabricare,
                                name,
                                numPiece,
                                process,
                                basePrice,
                                subTotalPrice,
                                dryPrice,
                                wetPrice,
                                alterationPrice,
                                comment,
                                fabricareDetails
                            )
                        )

                    } else {
                        // don't have same fabricare , so just add it

                        temp.add(fabricare)
                    }
                }

                if (!CanTogetherFlag){
                    // can't find same fabricare

                    temp.add(newFabricare)
                }

                _fabricares.value = temp
            }

            // select new fabricare
            setAsSelected()

        }
    }

    fun updateQtyPriceStament(){
        // update Qty table
        updateQty(fabricares.value!!)

        // updata price statement
        updatePriceStatement(fabricares.value!!, envRate, envAmount, tax,null, prepaid)
    }

    // notice i used CompoundButton not ToggleButton
    // data binding toggle button compound button ref) https://stackoverflow.com/a/27911988/3151712
    // called from data binding activity_make_invoice.xml
    fun onChangedSplitToggle(tbtn: CompoundButton, isSelected:Boolean){
        if(isSelected){
            _splitSelected.value = true
            _messageToActivity.value = App.resourses!!.getString(R.string.split_on)
        }else{
            _splitSelected.value = false
            _messageToActivity.value = App.resourses!!.getString(R.string.split_off)
        }

    }


    fun onClickCancelUpchargeDialog(editText: EditText ){
        editText.setText("")
        _closeUpchargeDialog.value = true
    }

    // check empty string
    fun onClickOkUpchargeDialog(upchargeView: EditText, selectedFabricare: SeletedFabricare){
        Log.d(TAG,"----- onClickOkUpchargeDialog : upcharge value is ${upchargeView.text}-----")

        val fabricare = selectedFabricare.fabricare

        if(upchargeView.text.toString().isNotEmpty()){
            var upcharge = upchargeView.text.toString().replace("[$,]".toRegex(),"").toDouble()

            var numfabricare = fabricare!!.numFabricare
            var name = fabricare!!.name
            var numPiece = fabricare!!.numPiece
            var process = fabricare!!.process
            var basePrice = fabricare!!.basePrice
            var subTotalPrice = fabricare!!.subTotalPrice+upcharge
            var dryPrice = fabricare!!.dryPrice
            var wetPrice = fabricare!!.wetPrice
            var alterationPrice = fabricare!!.alterationPrice
            var fabricareDetails = fabricare!!.fabricareDetails
            var comment = fabricare!!.comment

            when(process){
                name + "_dry_clean_press", name + "_dry_clean" ->{
                    dryPrice += upcharge
                }
                name+ "_wet_clean_press", name + "_wet_clean" ->{
                    wetPrice += upcharge
                }

                name + "_alter" ->{
                    alterationPrice += upcharge
                }

            }

            val updatedfabricare = Fabricare(
                numfabricare,
                name,
                numPiece,
                process,
                basePrice,
                subTotalPrice,
                dryPrice,
                wetPrice,
                alterationPrice,
                comment,
                fabricareDetails
            )

            updateFabricares(selectedFabricare.index,updatedfabricare)
            updateSelectedFabricare(selectedFabricare.index,updatedfabricare)

            // close dialog and clear edittext
            onClickCancelUpchargeDialog(upchargeView)
        }else{
            _messageToActivity.value = App.resourses!!.getString(R.string.empty_not_allowed)
        }
    }












    fun onClickCancelManualDialog(editText: EditText ){
        editText.setText("")
        _closeManualDialog.value = true
    }

    fun onClickOkManualDialog(manualView: EditText, selectedFabricare: SeletedFabricare){
        Log.d(TAG,"----- onClickOkManualDialog : manual value is ${manualView.text}-----")

        val fabricare = selectedFabricare.fabricare

        // check empty string
        if(manualView.text.toString().isNotEmpty()){
            var manualPrice = manualView.text.toString().replace("[$,]".toRegex(),"").toDouble()

            var numfabricare = fabricare!!.numFabricare
            var name = fabricare!!.name
            var numPiece = fabricare!!.numPiece
            var process = fabricare!!.process
            var basePrice = 0.0
            var subTotalPrice = manualPrice
            var dryPrice = 0.0
            var wetPrice = 0.0
            var alterationPrice = 0.0
            var fabricareDetails = fabricare!!.fabricareDetails
            var comment = fabricare!!.comment


            var alterationPriceDetail = 0.0

            for(item in fabricareDetails){
                if(item.category == "alteration"){
                    alterationPriceDetail += item.price
                }
            }

            when(process){
                name + "_dry_clean_press", name + "_dry_clean" ->{
                    dryPrice = manualPrice - alterationPriceDetail
                }
                name+ "_wet_clean_press", name + "_wet_clean" ->{
                    wetPrice = manualPrice - alterationPriceDetail
                }

                name + "_alter" ->{
                    alterationPrice = manualPrice
                }
            }

            val updatedfabricare = Fabricare(
                numfabricare,
                name,
                numPiece,
                process,
                basePrice,
                subTotalPrice,
                dryPrice,
                wetPrice,
                alterationPrice,
                comment,
                fabricareDetails
            )

            updateFabricares(selectedFabricare.index,updatedfabricare)
            updateSelectedFabricare(selectedFabricare.index,updatedfabricare)

            // close dialog and clear edittext
            onClickCancelManualDialog(manualView)
        }else{
            _messageToActivity.value = App.resourses!!.getString(R.string.empty_not_allowed)
        }
    }










    fun onClickCancelDiscountDialog(
        etRate: EditText,
        etAmount: EditText,
        tvDry: TextView,
        tvWet: TextView,
        tvAlter: TextView,
        tvEntire: TextView,
        tvTotal: TextView ){

        etRate.setText("")
        etAmount.setText("")
        tvDry.setText("")
        tvWet.setText("")
        tvAlter.setText("")
        tvEntire.setText("")
        tvTotal.setText("")

        _closeDiscountDialog.value = true

        // date picker (due date select) -> entering tag alert -> discount alert
        // if user cancel the process, i need to roll back all steps
        clearVars()
    }

    // this is for discount alert dialog
    // discount alert dialog can be called in two ways
    // 1. called middle of process adding fabricare
    // 2. right after user enter duedate , as last process of making invoice
    fun closeDiscountDialog(
        etRate: EditText,
        etAmount: EditText,
        tvDry: TextView,
        tvWet: TextView,
        tvAlter: TextView,
        tvEntire: TextView,
        tvTotal: TextView ){

        etRate.setText("")
        etAmount.setText("")
        tvDry.setText("")
        tvWet.setText("")
        tvAlter.setText("")
        tvEntire.setText("")
        tvTotal.setText("")

        _closeDiscountDialog.value = true
    }

    // this is for discount alert dialog
    // discount alert dialog can be called in two ways
    // 1. called middle of process adding fabricare
    // 2. right after user enter duedate , as last process of making invoice
    fun onClickOkDiscountDialog(
        priceStatement : MutableMap<String,Double>,
        rbtngType: RadioGroup,
        etRate: EditText,
        etAmount: EditText,
        tvDry: TextView,
        tvWet: TextView,
        tvAlter: TextView,
        tvEntire: TextView,
        tvTotal: TextView
    ){
        Log.d(TAG,"----- onClickOkDiscountDialog -----")


        val discountRate =
            if(etRate.text.toString().isEmpty()){
                0f
            }else{
                etRate.text.toString().toFloat()
            }

        val discountAmount =
            if(etAmount.text.toString().isEmpty()){
                0.0
            }else{
                etAmount.text.toString().replace("[$,]".toRegex(),"").toDouble()
            }

        val discountStatment =
            when(rbtngType.checkedRadioButtonId){
                R.id.rbEntireDCDialogMakeInvoice->{
                    DiscountStatement.ENTIRE
                }
                R.id.rbDryDCDialogMakeInvoice->{
                    DiscountStatement.DRY
                }
                R.id.rbWetDCDialogMakeInvoice->{
                    DiscountStatement.WET
                }
                R.id.rbAlterDCDialogMakeInvoice->{
                    DiscountStatement.ALTER
                }
                else->{
                    null
                }
            }

        discountStatment?.let {
            it.rate = discountRate
            it.amount = discountAmount
        }

        updatePriceStatement(
            fabricares.value!!,
            envRate,
            envAmount,
            tax,
            discountStatment,
            prepaid)

        makeInvoiceOrderAndSave()



        // close dialog and clear edittext
        closeDiscountDialog(
            etRate,
            etAmount,
            tvDry,
            tvWet,
            tvAlter,
            tvEntire,
            tvTotal
        )
    }

    fun makeInvoiceOrderAndSave(){
        if(dueDate.value != null && !dueDate.value!!.isEmpty()){
            // user put due date and clicked ok button in discount alert dialog
            // this is the last step to make invoice

            val newInvoice = getNewInvoiceOrder()
            val newInvoiceBrief = getNewInvoiceOrderBrief()

            if(originalInvoiceOrder == null){
                // making new InvoiceOrder

                // save invoice for printing ,which doesn't have invoice id yet
                _printingContent = newInvoice

                Repository.addInvoiceOrder(sharedPrefs,newInvoice,newInvoiceBrief,this)
            }else{
                // editing invoice
                // change original InvoiceOrder and save, also i need to make new InvoiceOrder

                val oldInvoice = originalInvoiceOrder!!

                oldInvoice.adjustedBy = sharedPrefs.getString("logged_team_id",null)

                val oldInvoiceBrief = invoiceOrderToBrief(oldInvoice)

                newInvoice.orgInvoiceOrderId = oldInvoice.id

                val newInvoiceWithOrigin = newInvoice
                val newInvoiceBriefWithOrigin = invoiceOrderToBrief(newInvoiceWithOrigin)

                // save invoice for printing ,which doesn't have invoice id yet
                _printingContent = newInvoice

                Repository.editAndAddInvoiceOrder(sharedPrefs,oldInvoice,oldInvoiceBrief,newInvoiceWithOrigin,newInvoiceBriefWithOrigin,this)
            }
        }
    }









    fun onClickRedoBtn(selectedFabricare: SeletedFabricare?){
        Log.d(TAG,"----- onClickRedoBtn -----")

        val fabricare = selectedFabricare?.fabricare

        fabricare?.let {
            it.basePrice = 0.0
            it.subTotalPrice = 0.0
            it.dryPrice = 0.0
            it.wetPrice = 0.0
            it.alterationPrice = 0.0
            it.fabricareDetails.add(FabricareDetail("Redo"))

            updateFabricares(selectedFabricare.index,it)
            updateSelectedFabricare(selectedFabricare.index,it)
        }
    }

    fun onClickCancelNumPieceDialog(editText: EditText){
        editText.setText("")
        _closeNumPieceDialog.value = true
    }

    fun onClickOkNumPieceDialog(numPieceView: EditText, selectedFabricare: SeletedFabricare){
        Log.d(TAG,"----- onClickOkNumPieceDialog -----")
        Log.d(TAG,"----- numPieceView : '${numPieceView.text.toString()}' -----")
        val numPieceFabricareStr = numPieceView.text.toString()

        if (emptyOrZero(numPieceFabricareStr)){
            // empty string or zero
            _messageToActivity.value = App.resourses!!.getString(R.string.empty_zero_not_allowed)
        }else{
            val fabricare = selectedFabricare.fabricare

            val numPieceFabricare =  numPieceFabricareStr.toInt()

            // i will use this for divide numPiece, subTotalPrice, dryPrice, wetPrice, alterationPrice
            // because if numFabricare is already bigger than 2 , i need to make it down to 1 and i can change the number
            val oldNumPieceFabricare = fabricare!!.numFabricare

            var numfabricare = numPieceFabricare
            var name = fabricare!!.name
            var numPiece = (fabricare!!.numPiece)/oldNumPieceFabricare * numPieceFabricare
            var process = fabricare!!.process
            var basePrice = fabricare!!.basePrice
            var subTotalPrice = (fabricare!!.subTotalPrice)/oldNumPieceFabricare * numPieceFabricare
            var dryPrice = fabricare!!.dryPrice
            var wetPrice = fabricare!!.wetPrice
            var alterationPrice = fabricare!!.alterationPrice
            var fabricareDetails = fabricare!!.fabricareDetails
            var comment = fabricare!!.comment

            when(process){
                name + "_dry_clean_press", name + "_dry_clean" ->{
                    dryPrice = dryPrice/oldNumPieceFabricare*numPieceFabricare
                }
                name+ "_wet_clean_press", name + "_wet_clean" ->{
                    wetPrice = wetPrice/oldNumPieceFabricare*numPieceFabricare
                }

                name + "_alter" ->{
                    alterationPrice = alterationPrice/oldNumPieceFabricare*numPieceFabricare
                }
            }

            val updatedfabricare = Fabricare(
                numfabricare,
                name,
                numPiece,
                process,
                basePrice,
                subTotalPrice,
                dryPrice,
                wetPrice,
                alterationPrice,
                comment,
                fabricareDetails
            )

            updateFabricares(selectedFabricare.index,updatedfabricare)
            updateSelectedFabricare(selectedFabricare.index,updatedfabricare)


            // close dialog and clear edittext
            onClickCancelNumPieceDialog(numPieceView)
        }
    }

    fun onClickHoldBtn(){
        Log.d(TAG,"----- onClickHoldBtn -----")

        val newInvoice = getNewInvoiceOrder()

        val holdedInvoiceOrder = HoldedInvoiceOrder( getCurrentTimeString(),customer, newInvoice,newInvoice !=null)

        HoldedInvoiceOrders.addInvoice(holdedInvoiceOrder)

        // send user to DropPickupActivity
        _moveToDropPickupACT.value = true
    }

    fun emptyOrZero(string:String):Boolean{
        if(string.isEmpty()){
            return true
        }else{
            if(string.toInt() ==0) {
                return true
            }
        }
        return false
    }

    override fun onAddInvoiceOrderCallback(isSuccess: Boolean, newInvoiceId: Long?) {
        if(isSuccess){
            Log.d(TAG,"----- onAddInvoiceOrderCallback INVOICE ORDER added successfully -----")

            // when we make new InvoiceOrder , we don't have id because it is made by firebase
            // we save new InvoiceOrder, so we have the id and we updated InvoiceOrder with id
            _printingContent!!.id = newInvoiceId

            _printing.value = _printingContent!!.makePrintTicket()

            clearVars()

            _moveToDropPickupACT.value = true
        }else{

            clearVars()

            Log.e(TAG,"----- onAddInvoiceOrderCallback INVOICE ORDER failded -----")
        }
    }

    fun onClickCancelCommentDialog(editText: EditText ){
        editText.setText("")
        _closeCommentDialog.value = true
    }

    fun onClickOkCommentDialog(commentView: EditText, selectedFabricare: SeletedFabricare){
        Log.d(TAG,"----- onClickOkCommentDialog : manual value is ${commentView.text}-----")

        val fabricare = selectedFabricare.fabricare

        var numfabricare = fabricare!!.numFabricare
        var name = fabricare!!.name
        var numPiece = fabricare!!.numPiece
        var process = fabricare!!.process
        var basePrice = fabricare!!.basePrice
        var subTotalPrice = fabricare!!.subTotalPrice
        var dryPrice = fabricare!!.dryPrice
        var wetPrice = fabricare!!.wetPrice
        var alterationPrice = fabricare!!.alterationPrice
        var fabricareDetails = fabricare!!.fabricareDetails
        var comment = commentView.text.toString()

        val updatedfabricare = Fabricare(
            numfabricare,
            name,
            numPiece,
            process,
            basePrice,
            subTotalPrice,
            dryPrice,
            wetPrice,
            alterationPrice,
            comment,
            fabricareDetails
        )

        updateFabricares(selectedFabricare.index,updatedfabricare)
        updateSelectedFabricare(selectedFabricare.index,updatedfabricare)

        // close dialog and clear edittext
        onClickCancelCommentDialog(commentView)
    }


    // called from activity_make_invoice.xml data binding
    fun onClickRemoveDetailBtn(){
        Log.d(TAG,"----- onClickRemoveDetailBtn -----")

        // check if there is selectedFabricare, there should be
        if (selectedFabricare.value != null && selectedFabricare.value!!.isThereSelectedOne()){

            val fabricare = selectedFabricare.value!!.fabricare!!
            val fabricareDetails = fabricare.fabricareDetails

            // get number of FabricareDetail
            var numDetails = fabricareDetails.count()

            // if there is no FabricareDetail, we have nothing to remove
            if(numDetails != 0){
                // we will remove last entry FabricareDetail
                val lastDetail= fabricareDetails[numDetails-1]

                // get amount we add when we add FabricareDetail
                var subCharge =
                    if(lastDetail.rate != 0f){
                        fabricare.basePrice*lastDetail.rate!!
                    }else {
                        lastDetail.amount!!
                    }
                Log.d(TAG,"----- subCharge : '${subCharge.toString()}' -----")

                // adjust price depend on category , process
                when(lastDetail.category){
                    "general" -> {
                        if(fabricare.dryPrice != 0.0){
                            Log.d(TAG,"----- fabricare.dryPrice : '${fabricare.dryPrice.toString()}' -----")
                            fabricare.dryPrice= fabricare.dryPrice.minus(subCharge)
                            Log.d(TAG,"----- fabricare.dryPrice : '${fabricare.dryPrice.toString()}' -----")
                        }
                        if(fabricare.wetPrice != 0.0){
                            Log.d(TAG,"----- fabricare.wetPrice : '${fabricare.wetPrice.toString()}' -----")
                            fabricare.wetPrice= fabricare.wetPrice.minus(subCharge)
                            Log.d(TAG,"----- fabricare.wetPrice : '${fabricare.wetPrice.toString()}' -----")
                        }
                    }
                    "alteration" -> {
                        Log.d(TAG,"----- fabricare.alterationPrice : '${fabricare.alterationPrice.toString()}' -----")
                        fabricare.alterationPrice=fabricare.alterationPrice.minus(subCharge)
                        Log.d(TAG,"----- fabricare.alterationPrice : '${fabricare.alterationPrice.toString()}' -----")
                    }
                }

                Log.d(TAG,"----- fabricare.subTotalPrice : '${fabricare.subTotalPrice.toString()}' -----")
                fabricare.subTotalPrice= fabricare.subTotalPrice.minus(subCharge)
                Log.d(TAG,"----- fabricare.subTotalPrice : '${fabricare.subTotalPrice.toString()}' -----")

                fabricareDetails.removeAt(numDetails-1)

            }
            fabricare.fabricareDetails = fabricareDetails

            updateFabricares(selectedFabricare.value!!.index,fabricare)
        }

    }

    fun updateFabricareWithFabricareDetails(selectedFabricare:SeletedFabricare,fabricareDetails: MutableList<FabricareDetail>){
        Log.d(TAG,"----- updateFabricareWithFabricareDetails -----")

        val fabricare = selectedFabricare.fabricare

        var numfabricare = fabricare!!.numFabricare
        var name = fabricare!!.name
        var numPiece = fabricare!!.numPiece
        var process = fabricare!!.process
        var basePrice = fabricare!!.basePrice
        var subTotalPrice = fabricare!!.basePrice

        var dryPrice = 0.0
        var wetPrice = 0.0
        var alterationPrice = 0.0

        when(process){
            name + "_dry_clean_press", name + "_dry_clean" ->{
                dryPrice = basePrice
            }
            name+ "_wet_clean_press", name + "_wet_clean" ->{
                wetPrice = basePrice
            }

            name + "_alter" ->{
                alterationPrice = basePrice
            }
        }


        var fabricareDetails = fabricareDetails

        for(item in fabricareDetails){
            when(item.category){
                "general"-> {
                    when(process){
                        name + "_dry_clean_press", name + "_dry_clean" ->{
                            dryPrice += item.price
                            subTotalPrice += item.price
                        }
                        name+ "_wet_clean_press", name + "_wet_clean" ->{
                            wetPrice += item.price
                            subTotalPrice += item.price
                        }

                        name + "_alter" ->{
                            alterationPrice += item.price
                            subTotalPrice += item.price
                        }
                    }
                }
                "alteration"-> {
                    alterationPrice += item.price
                    subTotalPrice += item.price
                }

            }
        }

        var comment = fabricare!!.comment

        val updatedfabricare = Fabricare(
            numfabricare,
            name,
            numPiece,
            process,
            basePrice,
            subTotalPrice,
            dryPrice,
            wetPrice,
            alterationPrice,
            comment,
            fabricareDetails
        )

        updateFabricares(selectedFabricare.index,updatedfabricare)
        updateSelectedFabricare(selectedFabricare.index,updatedfabricare)
    }



    // called from activity_make_invice.xml data binding
    fun onClickRemoveBtn(){
        Log.d(TAG,"----- onClickRemoveBtn -----")

        // check if there is selectedFabricare, there should be
        if (selectedFabricare.value != null && selectedFabricare.value!!.isThereSelectedOne()){
            if(fabricares.value !=null && fabricares.value!!.count() !=0 ){
                val temp = fabricares.value
                temp!!.removeAt(selectedFabricare.value!!.index)
                _fabricares.value = temp

                if(fabricares.value!!.count() !=0){
                    // we have still have Fabricare in fabricares

                    val lastFabricareIndex= fabricares.value!!.count()-1

                    // update selectedFabricare with last Fabricare in fabricares
                    SeletedFabricare.index = lastFabricareIndex
                    SeletedFabricare.fabricare = fabricares.value!![lastFabricareIndex]
                    _selectedFabricare.value = SeletedFabricare
                }else{
                    _selectedFabricare.value = SeletedFabricare.nothingSelected()
                }
            }
        }
    }

    // called from MakeInvoiceActivity.kt, user choose date from date picker
    override fun onDateSet(datePicker: DatePicker?, year: Int, month: Int, day: Int) {
        Log.d(TAG,"----- datePicker  -----")

        //due date string will be in format 'yyyy-MM-dd'
        val dueDateStr = StringBuilder()
        dueDateStr.append(year.toString())
        dueDateStr.append("-")

        // month number start from 0, january is 0

        dueDateStr.append("%02d".format(month+1))
        dueDateStr.append("-")

        // zero leading ref) https://stackoverflow.com/a/28666381/3151712
        // string format ref) https://stackoverflow.com/a/23088000/3151712
        dueDateStr.append("%02d".format(day))

        _dueDate.value = dueDateStr.toString()
    }


    fun onClickCancelTagDialog(tagEditText:EditText, colorSpinner : Spinner){
        tagEditText.setText("")
        colorSpinner.setSelection(0)

        // tag dialog is following due date picker, so if i cancel tag , i will also cancel due date
        clearVars()

        _closeTagDialog.value = true
    }

    // enter button is clicked tagAlert MakeInvoiceActivity.kt
    fun onClickEnterBtnTagAlert(tagEditText:EditText, colorSpinner : Spinner){

        if(tagEditText.text.isNotEmpty() && colorSpinner.selectedItem.toString().isNotEmpty()){
            Log.d(TAG,"---- user enter tag and tag color -----")
            Log.d(TAG,"---- tag : ${tagEditText.text}, tag color : ${colorSpinner.selectedItem as String} -----")

            _tag.value = tagEditText.text.toString()
            _tagColor.value = colorSpinner.selectedItem.toString()
        }else{
            _messageToActivity.value = App.resourses!!.getString(R.string.empty_not_allowed)
        }
    }

    fun getNewInvoiceOrder():InvoiceOrder{
//            date time format ref) https://stackoverflow.com/a/38220579/3151712
//            date time format ref) https://stackoverflow.com/a/41507429/3151712
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Format date

        // get current datatime string
        val nowString: String = dateFormat.format(Calendar.getInstance().getTime())

        // kotlin time string timestamp ref) https://stackoverflow.com/a/6993420/3151712
        // get due date timestamp from time string which is in "yyyy-MM-dd"
        val dueDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dueTimestamp =
            if(dueDate.value == null){
                null
            }else{
                dueDateFormat.parse(dueDate.value).time
            }

        val newInvoice = InvoiceOrder(
            customer = customer,
            qtyTable = qty.value!!,
            priceStatement = priceStatement.value,
            fabricares = fabricares.value,
            createdAt = nowString,
            createdBy = teamId,
            createdAtTimestamp = System.currentTimeMillis(),
            dueDateTime = dueDate.value,
            dueTimestamp = dueTimestamp,
            tag = tag.value,
            tagColor = tagColor.value
        )

        return newInvoice
    }

    fun getNewInvoiceOrderBrief():InvoiceOrderBrief{
//            date time format ref) https://stackoverflow.com/a/38220579/3151712
//            date time format ref) https://stackoverflow.com/a/41507429/3151712
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss") // Format date

        // get current datatime string
        val nowString: String = dateFormat.format(Calendar.getInstance().getTime())

        // kotlin time string timestamp ref) https://stackoverflow.com/a/6993420/3151712
        // get due date timestamp from time string which is in "yyyy-MM-dd"
        val dueDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val dueTimestamp = dueDateFormat.parse(dueDate.value).time

        val newInvoiceBrief = InvoiceOrderBrief(
            createdAt = nowString,
            createdBy = teamId,
            createdAtTimestamp = System.currentTimeMillis(),
            dueDateTime = dueDate.value,
            dueTimestamp = dueTimestamp,
            priceStatement = priceStatement.value,
            qtyTable = qty.value,
            customerId = customer.id,
            firstName = customer.firstName,
            lastName = customer.lastName,
            phoneNum = customer.phoneNum,
            email = customer.email
        )
        return newInvoiceBrief
    }


    fun makeColorItemList(){
        val colorItemList = mutableListOf<ColorItem>()
        colorItemList.add(ColorItem("White", Color.parseColor("#FFFFFF")))
        colorItemList.add(ColorItem("Silver", Color.parseColor("#C0C0C0")))
        colorItemList.add(ColorItem("Gray", Color.parseColor("#808080")))
        colorItemList.add(ColorItem("Black", Color.parseColor("#000000")))
        colorItemList.add(ColorItem("Salmon", Color.parseColor("#FA8072")))
        colorItemList.add(ColorItem("Red", Color.parseColor("#FF0000")))
        colorItemList.add(ColorItem("Pink", Color.parseColor("#FFC0CB")))
        colorItemList.add(ColorItem("HotPink", Color.parseColor("#FF69B4")))
        colorItemList.add(ColorItem("DeepPink", Color.parseColor("#FF1493")))
        colorItemList.add(ColorItem("Coral", Color.parseColor("#FF7F50")))
        colorItemList.add(ColorItem("Orange", Color.parseColor("#FFA500")))
        colorItemList.add(ColorItem("OrangeRed", Color.parseColor("#FF4500")))
        colorItemList.add(ColorItem("Gold", Color.parseColor("#FFD700")))
        colorItemList.add(ColorItem("Yellow", Color.parseColor("#FFFF00")))
        colorItemList.add(ColorItem("Khaki", Color.parseColor("#F0E68C")))
        colorItemList.add(ColorItem("Lavender", Color.parseColor("#E6E6FA")))
        colorItemList.add(ColorItem("Fuchsia", Color.parseColor("#FF00FF")))
        colorItemList.add(ColorItem("Purple", Color.parseColor("#800080")))
        colorItemList.add(ColorItem("Indigo", Color.parseColor("#4B0082")))
        colorItemList.add(ColorItem("LawnGreen", Color.parseColor("#7CFC00")))
        colorItemList.add(ColorItem("Lime", Color.parseColor("#00FF00")))
        colorItemList.add(ColorItem("Green", Color.parseColor("#008000")))
        colorItemList.add(ColorItem("Olive", Color.parseColor("#808000")))
        colorItemList.add(ColorItem("Aqua", Color.parseColor("#00FFFF")))
        colorItemList.add(ColorItem("Blue", Color.parseColor("#0000FF")))
        colorItemList.add(ColorItem("DarkBlue", Color.parseColor("#00008B")))
        colorItemList.add(ColorItem("Navy", Color.parseColor("#000080")))
        colorItemList.add(ColorItem("Beige", Color.parseColor("#F5F5DC")))
        colorItemList.add(ColorItem("Ivory", Color.parseColor("#FFFFF0")))
        colorItemList.add(ColorItem("Tan", Color.parseColor("#D2B48C")))
        colorItemList.add(ColorItem("Brown", Color.parseColor("#A52A2A")))
        colorItemList.add(ColorItem("Maroon", Color.parseColor("#800000")))

        _colorItems.value = colorItemList
    }

    fun clearVars(){
        _dueDate.value = ""
        _tag.value = ""
        _tagColor.value = ""
        _printingContent  = null
        _printing.value = mutableListOf<PrintingJob>()
    }



    override fun onCleared() {
        super.onCleared()

        valueEvnetListener?.let{
            Repository.removeValueEventOnWholeItems(sharedPrefs,valueEvnetListener!!)
        }

    }
}



enum class DiscountStatement(){
    ENTIRE,
    DRY,
    WET,
    ALTER;

    var rate : Float = 0f
    var amount : Double = 0.0
}

