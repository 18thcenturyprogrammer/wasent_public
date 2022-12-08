package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.content.SharedPreferences
import android.util.Log
import android.widget.CompoundButton
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.*
import com.centuryprogrammer18thwasentsingleland.repository.PickupProcess
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.*
import kotlin.math.log


@Suppress("UNREACHABLE_CODE")
class PickupActVM : ViewModel(), PickupProcess {
    private val TAG = PickupActVM::class.java.simpleName

    var lastPickupHistory: PickupHistory? = null

    // connected to 'act_pickup.xml' etPaidPickupAct by data binding
    // notice this is String type because data binding
    // two way data binding ref) https://developer.android.com/topic/libraries/data-binding/architecture#livedata
    var paidAmount = MutableLiveData<String>("0.0")

    lateinit var customer : Customer

    lateinit var sharedPrefs: SharedPreferences

    var orgInvoiceWithPayments = mutableListOf<InvoiceWithPayment>()

    private val _invoiceWithPayments = MutableLiveData<MutableList<InvoiceWithPayment>>()
    val invoiceWithPayments : LiveData<MutableList<InvoiceWithPayment>>
        get() = _invoiceWithPayments

    private var payments = mutableListOf<Payment>()

    // the expected amount which customer pays
    private val _balance = MutableLiveData<Double>(0.0)
    val balance : LiveData<Double>
        get() = _balance

    private val _credit = MutableLiveData<Double>(0.0)
    val credit : LiveData<Double>
        get() = _credit

    private val _qty = MutableLiveData<Int>(0)
    val qty : LiveData<Int>
        get() = _qty

    private val _payMethod = MutableLiveData<String>("")
    val payMethod : LiveData<String>
        get() = _payMethod

    private val _checkNum = MutableLiveData<String>("")
    val checkNum : LiveData<String>
        get() = _checkNum

    private val _closeCheckDialog = MutableLiveData<Boolean>()
    val closeCheckDialog : LiveData<Boolean>
        get() = _closeCheckDialog

    private val _openFinishDialog = MutableLiveData<Double>()
    val openFinishDialog : LiveData<Double>
        get() = _openFinishDialog

    private val _msgToAct = MutableLiveData<String>()
    val msgToAct : LiveData<String>
        get() = _msgToAct

    private val _unpaidIncluded = MutableLiveData<Boolean>(false)
    val unpaidIncluded : LiveData<Boolean>
        get() = _unpaidIncluded

    // the amount which cashier need to give to customer
    private var change :Double = 0.0

    fun initCustomer(receivedCustomer: Customer){
        customer = receivedCustomer
    }

    fun initSharedPrefs(receivedPrefs: SharedPreferences){
        sharedPrefs = receivedPrefs
    }

    fun initLastPickupHistory(receivedPickupHistory : PickupHistory?){
        lastPickupHistory = receivedPickupHistory
    }

    // initialize orgInvoiceWithPayments, _invoiceWithPayments
    fun initInvoiceWithPayments(receivedInvoiceWithPayments : MutableList<InvoiceWithPayment>){

        // kotlin copy by reference is default behavior
        // i need value copy
        // copy clone mutablelist value ref) https://stackoverflow.com/a/51770492/3151712
        orgInvoiceWithPayments = receivedInvoiceWithPayments.toMutableList()
        _invoiceWithPayments.value = receivedInvoiceWithPayments
    }

    // one InvoiceWithPayment is changed
    // (could be 1. changed as user want pay something, 2. changed as user deselect to not to pay )
    // update _invoiceWithPayments
    // update _payment
    // update _balance
    // update _credit
    // update _qty
    fun updateInvoiceWithPayment(invoiceWithPayment: InvoiceWithPayment){
        val old = invoiceWithPayments.value
        val temp = mutableListOf<InvoiceWithPayment>()

        for (item in old!!){
            if(item.invoiceOrderId == invoiceWithPayment.invoiceOrderId){

                // save modified InvoiceWithPayment
                temp.add(invoiceWithPayment)
            }else{

                temp.add(item)
            }
        }

        _invoiceWithPayments.value = temp

        // for debugging
        _invoiceWithPayments.value?.let {
            Log.d(TAG,"----- updateInvoiceWithPayment -----")
            for(item in it){
                Log.d(TAG,"----- id : ${item.invoiceOrderId} , amount : ${item.amount} ,  isPaid : ${item.isPaid.toString()}-----\n")
            }
        }

        updatePayments(invoiceWithPayment)
        updateBalanceAndCredit()
        updateQty()
    }

    // this is called when user select invoice as paying or unselect invoice as not paying
    fun updatePayments(invoiceWithPayment: InvoiceWithPayment){

        if(invoiceWithPayment.isPaid == null){
            // invoiceWithPayment is dechecked InvoiceOrder , i need to remove it

            val tempPayments = mutableListOf<Payment>()

            // user unchecked InvoiceWithPayment, so i saved only checked InvoiceWithPayment in tempPayments
            for (item in payments){
                if(item.invoiceOrderId != invoiceWithPayment.invoiceOrderId){

                    // save only user want to pay
                    tempPayments.add(item)
                }
            }
            payments = tempPayments

            Log.d(TAG,"----- payment payments.count() : ${payments.count().toString()} -----")

        }else{
            // invoiceWithPayment is checked InvoiceOrder , i need to make Payment

            if(payments.count() == 0 ){
                // this is the first payment (user checked radio button and this is the first selected one)

                val newPayment = Payment()
                newPayment.invoiceOrderId = invoiceWithPayment.invoiceOrderId

                newPayment.invoiceOrderBalance = invoiceWithPayment.invoiceOrder!!.priceStatement!!["total"]!!

                newPayment.totalPaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!+invoiceWithPayment.amount!!

                when(invoiceWithPayment.typePayment){
                    "fullpaid" ->{
                        // fullpaid selected (user checked fullpaid radio button and this is the first selected one)

                        newPayment.fullPaidAmount = invoiceWithPayment.amount
                        newPayment.pastPrepaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!
                        newPayment.tax = invoiceWithPayment.invoiceOrder!!.priceStatement!!["tax"]!!
                        newPayment.env = invoiceWithPayment.invoiceOrder!!.priceStatement!!["env"]!!
                        newPayment.discount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["discount"]!!
                        newPayment.dryPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["dryPrice"]!!
                        newPayment.wetPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["wetPrice"]!!
                        newPayment.alterPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["alterPrice"]!!
                    }
                    "prepaid" ->{
                        // prepaid selected (user checked prepaid radio button and this is the first one)

                        newPayment.prepaidAmount = invoiceWithPayment.amount

                        // i don't do about these tax, env, discount, dryPrice, wetPrice, alterPrice
                        // because if i do something about these here, it will be double counted because it will be saved in fullpaid or partialpaid later
                    }
                    "partialpaid" ->{
                        // partialpaid selected (user checked partialpaid radio button and this is the first selected one)

                        newPayment.pastPrepaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!
                        newPayment.partialpaidAmount = invoiceWithPayment.amount

                        // amount user didn't pay when user pick up, which is needed to pay later
                        newPayment.creditAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!! - invoiceWithPayment.amount!!
                        newPayment.tax = invoiceWithPayment.invoiceOrder!!.priceStatement!!["tax"]!!
                        newPayment.env = invoiceWithPayment.invoiceOrder!!.priceStatement!!["env"]!!
                        newPayment.discount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["discount"]!!
                        newPayment.dryPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["dryPrice"]!!
                        newPayment.wetPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["wetPrice"]!!
                        newPayment.alterPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["alterPrice"]!!
                    }
                }
                payments.add(newPayment)

                Log.d(TAG,"-----first payment payments.count() : ${payments.count().toString()} -----")

            }else{
                // user marked radio button as checked and there were selected other InvoiceWithPayment already

                val tempPayments = mutableListOf<Payment>()
                var isUpdatedFlag = false

                for (item in payments){
                    if(item.invoiceOrderId == invoiceWithPayment.invoiceOrderId){
                        // there was same invoiceOrder

                        isUpdatedFlag = true

                        val updatedPayment = Payment()
                        updatedPayment.invoiceOrderId = invoiceWithPayment.invoiceOrderId

                        updatedPayment.invoiceOrderBalance = invoiceWithPayment.invoiceOrder!!.priceStatement!!["total"]!!

                        updatedPayment.totalPaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!+invoiceWithPayment.amount!!

                        when(invoiceWithPayment.typePayment){
                            "fullpaid" ->{
                                // fullpaid selected (user checked fullpaid radio button and there were selected other InvoiceWithPayment already and there was same InvoiceWithPayment user choose)

                                updatedPayment.fullPaidAmount = invoiceWithPayment.amount
                                updatedPayment.pastPrepaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!
                                updatedPayment.tax = invoiceWithPayment.invoiceOrder!!.priceStatement!!["tax"]!!
                                updatedPayment.env = invoiceWithPayment.invoiceOrder!!.priceStatement!!["env"]!!
                                updatedPayment.discount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["discount"]!!
                                updatedPayment.dryPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["dryPrice"]!!
                                updatedPayment.wetPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["wetPrice"]!!
                                updatedPayment.alterPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["alterPrice"]!!
                            }
                            "prepaid" ->{
                                // prepaid selected (user checked prepaid radio button and there were selected other InvoiceWithPayment already and there was same InvoiceWithPayment user choose)

                                updatedPayment.prepaidAmount = invoiceWithPayment.amount

                                // i don't do about these tax, env, discount, dryPrice, wetPrice, alterPrice
                                // because if i do something about these here, it will be double counted because it will be saved in fullpaid or partialpaid later
                            }
                            "partialpaid" ->{
                                // partialpaid selected (user checked partialpaid radio button and there were selected other InvoiceWithPayment already and there was same InvoiceWithPayment user choose)

                                updatedPayment.pastPrepaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!
                                updatedPayment.partialpaidAmount = invoiceWithPayment.amount

                                // amount user didn't pay when user pick up, which is needed to pay later
                                updatedPayment.creditAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!! - invoiceWithPayment.amount!!
                                updatedPayment.tax = invoiceWithPayment.invoiceOrder!!.priceStatement!!["tax"]!!
                                updatedPayment.env = invoiceWithPayment.invoiceOrder!!.priceStatement!!["env"]!!
                                updatedPayment.discount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["discount"]!!
                                updatedPayment.dryPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["dryPrice"]!!
                                updatedPayment.wetPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["wetPrice"]!!
                                updatedPayment.alterPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["alterPrice"]!!
                            }
                        }

                        // save modified Payment
                        tempPayments.add(updatedPayment)
                    }else{
                        tempPayments.add(item)
                    }
                }

                if(!isUpdatedFlag){
                    // user radio button selected
                    // (user marked radio button as checked and there were selected other InvoiceWithPayment already and
                    // there was NOT same InvoiceWithPayment user chose already)


                    val newPayment = Payment()
                    newPayment.invoiceOrderId = invoiceWithPayment.invoiceOrderId

                    newPayment.invoiceOrderBalance = invoiceWithPayment.invoiceOrder!!.priceStatement!!["total"]!!

                    newPayment.totalPaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!+invoiceWithPayment.amount!!

                    when(invoiceWithPayment.typePayment){
                        "fullpaid" ->{
                            newPayment.fullPaidAmount = invoiceWithPayment.amount
                            newPayment.pastPrepaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!
                            newPayment.tax = invoiceWithPayment.invoiceOrder!!.priceStatement!!["tax"]!!
                            newPayment.env = invoiceWithPayment.invoiceOrder!!.priceStatement!!["env"]!!
                            newPayment.discount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["discount"]!!
                            newPayment.dryPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["dryPrice"]!!
                            newPayment.wetPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["wetPrice"]!!
                            newPayment.alterPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["alterPrice"]!!
                        }
                        "prepaid" ->{
                            newPayment.prepaidAmount = invoiceWithPayment.amount
                        }
                        "partialpaid" ->{
                            newPayment.pastPrepaidAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!!
                            newPayment.partialpaidAmount = invoiceWithPayment.amount
                            newPayment.creditAmount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!! - invoiceWithPayment.amount!!
                            newPayment.tax = invoiceWithPayment.invoiceOrder!!.priceStatement!!["tax"]!!
                            newPayment.env = invoiceWithPayment.invoiceOrder!!.priceStatement!!["env"]!!
                            newPayment.discount = invoiceWithPayment.invoiceOrder!!.priceStatement!!["discount"]!!
                            newPayment.dryPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["dryPrice"]!!
                            newPayment.wetPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["wetPrice"]!!
                            newPayment.alterPrice = invoiceWithPayment.invoiceOrder!!.priceStatement!!["alterPrice"]!!
                        }
                    }
                    tempPayments.add(newPayment)
                }
                payments = tempPayments

                Log.d(TAG,"----- payments.count() : ${payments.count().toString()} -----")
            }

        }
    }

    // go through pyaments and calcualte Balance and Credit( the amount which customer didn't pay and will go to next order)
    fun updateBalanceAndCredit(){
        var balance = 0.0
        var credit = 0.0
        for(payment in payments){
            Log.d(TAG,"----- payment.invoiceOrderId : ${payment.invoiceOrderId.toString()} -----")
            Log.d(TAG,"----- payment.invoiceOrderBalance : ${payment.invoiceOrderBalance.toString()} -----")

            payment.fullPaidAmount?.let {
                balance += it
            }

            payment.prepaidAmount?.let {
                balance += it
            }

            payment.partialpaidAmount?.let {
                balance += it
            }

            Log.d(TAG,"----- balance : ${balance.toString()} -----")

            payment.creditAmount?.let{
                credit += it
            }

            Log.d(TAG,"----- credit : ${credit.toString()} -----")
        }
        _balance.value = balance
        _credit.value = credit
    }

    fun updateQty(){
        var qty = 0

        invoiceWithPayments.value?.let {
            for(item in it){
                if(item.isPaid !=null && item.isPaid!!){
                    val itemQty = item.invoiceOrder!!.qtyTable!!["dry"]!! +
                            item.invoiceOrder!!.qtyTable!!["wet"]!! +
                            item.invoiceOrder!!.qtyTable!!["alter"]!! +
                            item.invoiceOrder!!.qtyTable!!["press"]!!+
                            item.invoiceOrder!!.qtyTable!!["clean"]!!

                    qty += itemQty
                }
            }
        }
        _qty.value = qty
    }


    fun onCashBtnClicked(){
        Log.d(TAG,"----- onCashBtnClicked -----")
        var temp = "0.0"
        balance.value?.let {
            temp = makeTwoPointsDecialString(it)
        }

        // connected to 'act_pickup.xml' etPaidPickupAct by data binding, set pay amount as balance
        // notice this is String type because data binding
        paidAmount.value= temp

        _payMethod.value= "cash"
        _checkNum.value = ""
    }

    // called from check_dialog.pickup.xml when user click ok button
    fun onCheckBtnClicked(checkNumView:EditText){
        Log.d(TAG,"----- onCheckBtnClicked -----")
        Log.d(TAG,"----- check num : '${checkNumView.text}' -----")

        val checkNum = checkNumView.text

        if (checkNum.isNotEmpty()){
            var temp = "0.0"
            balance.value?.let {
                temp = makeTwoPointsDecialString(it)
            }

            // connected to 'act_pickup.xml' etPaidPickupAct by data binding, set pay amount as balance
            // notice this is String type because data binding
            paidAmount.value = temp

            _payMethod.value = "check"
            _checkNum.value = checkNumView.text.toString()
            _closeCheckDialog.value = true
        }else{
            _msgToAct.value = App.resourses!!.getString(R.string.empty_not_allowed)
        }
    }

    // called from check_dialog_pickup.xml when user click cancel
    fun onCancelBtnClicked(){
        Log.d(TAG,"----- check_dialog_pickup.xml onCancelBtnClicked -----")

        _closeCheckDialog.value = true
    }

    fun onCardBtnClicked(){
        var temp = "0.0"
        balance.value?.let {
            temp = makeTwoPointsDecialString(it)
        }

        // connected to 'act_pickup.xml' etPaidPickupAct by data binding, set pay amount as balance
        // notice this is String type because data binding
        paidAmount.value= temp

        _payMethod.value= "card"
        _checkNum.value = ""
    }


    // notice i used CompoundButton not ToggleButton
    // data binding toggle button compound button ref) https://stackoverflow.com/a/27911988/3151712
    // called from data binding ''act_pickup.xml''

    // this is called user change toggle button for including unpaid or not
    fun onChangedUnpaidToggle(tbtn: CompoundButton, isSelected:Boolean){
        if(isSelected){
            // user unpaid toggle set as include

            _unpaidIncluded.value = true
            _balance.value?.let {
                _balance.value = it + lastPickupHistory!!.newBalance!!
            }

        }else{
            // user unpaid toggle set as exclude

            _unpaidIncluded.value = false
            _balance.value?.let {
                _balance.value = it - lastPickupHistory!!.newBalance!!
            }
        }

    }

    fun onClickDoneBtn(){
        Log.d(TAG,"----- user clicked done button ----- ")
        Log.d(TAG,"----- paid amount user entered is '${paidAmount}' ----- ")

        paidAmount.value?.let {paidAmount ->
            if(paidAmount.replace("[$,]".toRegex(),"").toDouble() < balance.value!!){
                Log.d(TAG,"----- user smaller amount than balance ----- ")
                Log.d(TAG,"----- paidAmount : '${paidAmount}' ----- balance : '${balance.value.toString()}' -----")

                _msgToAct.value = App.resourses!!.getString(R.string.payment_should_be_larger_than_balance)
            }else{
                processPickup()
            }
        }
    }


    fun processPickup(){
        Log.d(TAG,"----- processPickup -----")

        val currentTimeStr = getCurrentTimeString()
        val currentTimestamp = getCurrentTimestamp()
        val teamId = sharedPrefs.getString("logged_team_id",null)

        var paidAmountStr = paidAmount.value!!.replace("[$,]".toRegex(),"")
        var payMethod = payMethod.value
        var checkNum = checkNum.value

        var pickUpAt = currentTimeStr
        var pickedUpBy = teamId
        var pickUpAtTimestamp = currentTimestamp

        if(unpaidIncluded.value!! && (invoiceWithPayments.value == null || invoiceWithPayments.value!!.count() == 0) ){
            // customer wants to pay only unpaid which customer didn't pay
            // i called this creditpaid

            // user want to pay only unpaid amount which is from the last
            // so i will make empty obj
            var selectedInvoiceWithPayments = mutableListOf<InvoiceWithPayment>()

            // make Payment obj for payment of unpaid amount past
            val newPayment = Payment()

            newPayment.paymentMaidAt = currentTimeStr
            newPayment.paymentMaidBy = teamId
            newPayment.paymentMaidAtAtTimestamp = currentTimestamp
            newPayment.creditpaidAmount = balance.value
            newPayment.totalPaidAmount = balance.value
            payments.add(newPayment)

            val newPickup = Pickup()
            newPickup.balance = balance.value

            // if balance is $10 and customer pay with $20. realPaidAmount is $20
            newPickup.realPaidAmount = paidAmountStr.toDouble()
            newPickup.payMethod = payMethod
            newPickup.pickUpAt = pickUpAt
            newPickup.pickedUpBy = pickedUpBy
            newPickup.pickUpAtTimestamp = pickUpAtTimestamp

            val newPickupHistory = PickupHistory()

            newPickupHistory.customerId = customer.id
            newPickupHistory.unpaidBalance = lastPickupHistory!!.newBalance
            newPickupHistory.paidAmount = balance.value
            newPickupHistory.newBalance = lastPickupHistory!!.newBalance!! - balance.value!!
            newPickupHistory.pickUpAt = currentTimeStr
            newPickupHistory.pickedUpBy = teamId
            newPickupHistory.pickUpAtTimestamp = currentTimestamp

            Repository.makePickups(sharedPrefs,customer,payments,newPickup,newPickupHistory,selectedInvoiceWithPayments,this)

            change = paidAmount.value!!.replace("[$,]".toRegex(),"").toDouble() - balance.value!!

        }else{
            if(_invoiceWithPayments.value != null && _invoiceWithPayments.value!!.count() > 0){

                // collect InvoiceWithPayment only user selected
                var selectedInvoiceWithPayments = mutableListOf<InvoiceWithPayment>()

                // filtering for getting only selected
                selectedInvoiceWithPayments = _invoiceWithPayments.value!!.filter {
                    it.isPaid == true
                }.toMutableList()

                // if there is NO selected one, i will do nothing
                if(selectedInvoiceWithPayments.count() > 0 ){

                    //Get list of some property values of Object from list of Object
                    // ref) https://medium.com/@hayi/kotlin-get-list-of-some-property-values-of-object-from-list-of-object-8da9419c2e77
                    val invoiceIds = selectedInvoiceWithPayments.listOfField(InvoiceWithPayment::invoiceOrderId)


                    // mutableMapOf<Long,MutableMap<String,Double>>
                    // mutableMapOf<{InvoiceOrder.id}},PriceStatement>
                    // so I tried to use Long, but firebase not allowed Long for key, so i changed it to string
                    val priceStatements = mutableMapOf<String,MutableMap<String,Double>>()

                    // if customer pay part of the balance, the rest of them will be paid later
                    // the rest is called creditAmount
                    var creditAmount = 0.0


                    for (selectedInvoiceWithPayment in selectedInvoiceWithPayments){

                        when(selectedInvoiceWithPayment.typePayment){
                            "prepaid" -> {
                                // i don't make priceStatement as below because priceStatement is needed for only customer picks up real garments
                                // when user prepaid , they only make payment . not picking up garments

                                selectedInvoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"] =
                                    selectedInvoiceWithPayment.invoiceOrder!!.priceStatement!!["prepaid"]!! + selectedInvoiceWithPayment.amount!!

                                // prepaid
                                // if balance is $10 and customer pay $5, balance will be $5
                                selectedInvoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"] =
                                    selectedInvoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!! - selectedInvoiceWithPayment.amount!!
                            }
                            else ->{

                                val priceStatement = selectedInvoiceWithPayment.invoiceOrder!!.priceStatement!!
                                priceStatements.put(selectedInvoiceWithPayment.invoiceOrderId!!.toString(),priceStatement)

                                // update pick up time and team id
                                selectedInvoiceWithPayment.invoiceOrder!!.pickedUpAt = currentTimeStr
                                selectedInvoiceWithPayment.invoiceOrder!!.pickedUpBy = teamId
                                selectedInvoiceWithPayment.invoiceOrder!!.pickedAtTimestamp = currentTimestamp

                                if(selectedInvoiceWithPayment.typePayment == "partialpaid"){
                                    creditAmount += selectedInvoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!!-selectedInvoiceWithPayment.amount!!
                                }

                                // fullpaid
                                // if balance is $10 and customer pay $10, balance will be $0
                                //
                                // partialpaid
                                // if balance is $10 and customer pay $5, balance will be $0
                                selectedInvoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"] = 0.0
                            }
                        }
                    }

                    for(item in payments){
                        item.paymentMaidAt = pickUpAt
                        item.paymentMaidBy = pickedUpBy
                        item.paymentMaidAtAtTimestamp = pickUpAtTimestamp
                    }

                    val pickup = Pickup(
                                    null,
                                    invoiceIds,
                                    priceStatements,
                                    balance.value!!,
                                    paidAmount.value!!.replace("[$,]".toRegex(),"").toDouble(),
                                    payMethod,
                                    checkNum,
                                    pickUpAt,
                                    pickedUpBy,
                                    pickUpAtTimestamp
                                )


                    val pickupHistory =  if(unpaidIncluded.value!!){
                        // user include unpaid amount of last time

                             // make Payment obj for payment of unpaid amount past
                            val newCreditPayment = Payment()

                            newCreditPayment.paymentMaidAt = currentTimeStr
                            newCreditPayment.paymentMaidBy = teamId
                            newCreditPayment.paymentMaidAtAtTimestamp = currentTimestamp
                            newCreditPayment.creditpaidAmount = lastPickupHistory!!.newBalance
                            newCreditPayment.totalPaidAmount = lastPickupHistory!!.newBalance
                            payments.add(newCreditPayment)

                            PickupHistory(
                                null,
                                customer.id,
                                invoiceIds,
                                lastPickupHistory!!.newBalance,
                                balance.value,
                                paidAmount.value!!.replace("[$,]".toRegex(),"").toDouble(),
                                creditAmount,
                                pickUpAt,
                                pickedUpBy,
                                pickUpAtTimestamp
                            )
                        }else{
                            // user didn't include unpaid amount

                            if(lastPickupHistory != null && lastPickupHistory!!.newBalance != null){
                                // there is lastPickupHistory

                                PickupHistory(
                                    null,
                                    customer.id,
                                    invoiceIds,
                                    lastPickupHistory!!.newBalance,
                                    balance.value,
                                    paidAmount.value!!.replace("[$,]".toRegex(),"").toDouble(),
                                    lastPickupHistory!!.newBalance!! +creditAmount,
                                    pickUpAt,
                                    pickedUpBy,
                                    pickUpAtTimestamp
                                )
                            }else{
                                // there was no PickupHistory, this is the first one

                                PickupHistory(
                                    null,
                                    customer.id,
                                    invoiceIds,
                                    0.0,
                                    balance.value,
                                    paidAmount.value!!.replace("[$,]".toRegex(),"").toDouble(),
                                    creditAmount,
                                    pickUpAt,
                                    pickedUpBy,
                                    pickUpAtTimestamp
                                )
                            }
                        }
                    Repository.makePickups(sharedPrefs,customer,payments,pickup,pickupHistory,selectedInvoiceWithPayments,this)

                    change = paidAmount.value!!.replace("[$,]".toRegex(),"").toDouble() - balance.value!!
                }else{
                    // user didn't choose any InvoiceWithPayment

                    if(unpaidIncluded.value!!){
                        // user want to pay only unpaid amount which is from the last
                        // so i will make empty obj
                        var selectedInvoiceWithPayments = mutableListOf<InvoiceWithPayment>()

                        // make Payment obj for payment of unpaid amount past
                        val newPayment = Payment()

                        newPayment.paymentMaidAt = currentTimeStr
                        newPayment.paymentMaidBy = teamId
                        newPayment.paymentMaidAtAtTimestamp = currentTimestamp

                        // amount which customer is paying last unpaid before
                        newPayment.creditpaidAmount = balance.value

                        // accumulated paid amount so far
                        newPayment.totalPaidAmount = balance.value
                        payments.add(newPayment)

                        val newPickup = Pickup()
                        newPickup.balance = balance.value

                        // if balance is $10 and customer pay with $20. realPaidAmount is $20
                        newPickup.realPaidAmount = paidAmountStr.toDouble()
                        newPickup.payMethod = payMethod
                        newPickup.pickUpAt = pickUpAt
                        newPickup.pickedUpBy = pickedUpBy
                        newPickup.pickUpAtTimestamp = pickUpAtTimestamp

                        val newPickupHistory = PickupHistory()

                        newPickupHistory.customerId = customer.id
                        newPickupHistory.unpaidBalance = lastPickupHistory!!.newBalance
                        newPickupHistory.paidAmount = balance.value
                        newPickupHistory.newBalance = lastPickupHistory!!.newBalance!! - balance.value!!
                        newPickupHistory.pickUpAt = currentTimeStr
                        newPickupHistory.pickedUpBy = teamId
                        newPickupHistory.pickUpAtTimestamp = currentTimestamp

                        Repository.makePickups(sharedPrefs,customer,payments,newPickup,newPickupHistory,selectedInvoiceWithPayments,this)

                        change = paidAmount.value!!.replace("[$,]".toRegex(),"").toDouble() - balance.value!!
                    }
                }
            }
        }
    }

    // callback . this is called after Repository.makePickups
    override fun onPickupProcess(isSuccess: Boolean) {
        if(isSuccess){
            // pickup process was finished successfully

            Log.d(TAG, "----- pickup process was finished successfully -----")

            _openFinishDialog.value = change
        }else{
            // something wrong while finishing up pick up process

            Log.e(TAG, "----- something wrong while finishing up pick up process -----")
        }
    }
}