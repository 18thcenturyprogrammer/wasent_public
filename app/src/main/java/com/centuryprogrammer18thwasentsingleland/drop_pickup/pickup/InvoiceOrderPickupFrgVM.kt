package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.InvoiceWithPayment
import com.centuryprogrammer18thwasentsingleland.utils.notifyObserver

class InvoiceOrderPickupFrgVM : ViewModel() {
    private val TAG = InvoiceOrderPickupFrgVM::class.java.simpleName

    // this will be not changed. this is will be used as reference
    var orgInvoiceWithPayments = mutableListOf<InvoiceWithPayment>()

    private val _invoiceWithPayments = MutableLiveData<MutableList<InvoiceWithPayment>>()
    val invoiceWithPayments : LiveData<MutableList<InvoiceWithPayment>>
        get() = _invoiceWithPayments

//    private val _updatedInvoiceWithPayment = MutableLiveData<InvoiceWithPayment>()
//    val updatedInvoiceWithPayment : LiveData<InvoiceWithPayment>
//        get() = _updatedInvoiceWithPayment


    // get data from InvoiceOrderPickupFrg
    fun initInvoiceWithPayments(receivedInvoiceWithPayments: MutableList<InvoiceWithPayment>){

        // copy clone mutablelist value ref) https://stackoverflow.com/a/51770492/3151712
        _invoiceWithPayments.value = receivedInvoiceWithPayments.toMutableList()

        Log.d(TAG, "\n\n\n----- _invoiceWithPayments initialized by fragment (_invoiceWithPayments is containing copy value from activity)-----")
    }

    // get data from InvoiceOrderPickupFrg
    fun initOrgInvoiceWithPayments(receivedInvoiceWithPayments: MutableList<InvoiceWithPayment>){

        // copy clone mutablelist value ref) https://stackoverflow.com/a/51770492/3151712
        orgInvoiceWithPayments = receivedInvoiceWithPayments.toMutableList()

        // for debugging
        Log.d(TAG, "\n\n\n----- orgInvoiceWithPayment initialized by fragment (_invoiceWithPayments is containing copy value from activity)-----")
        for(item in orgInvoiceWithPayments){
            Log.d(TAG, "-----  typePayment :${item.typePayment} | amount :${item.amount.toString()} | id :${item.invoiceOrderId.toString()}-----\n\n\n")
        }
    }

    fun onClickAllFullSelect(){
        Log.d(TAG, "\n----- user click select all full paid button-----")
        Log.d(TAG, "----- _invoiceWithPayments will changed as below-----\n")

        invoiceWithPayments.value?.let {
            for(item in it){

                // list find filter object value ref) https://stackoverflow.com/a/51010611/3151712
                val orgInvoiceWithPayment = orgInvoiceWithPayments.find { it.invoiceOrderId == item.invoiceOrderId }

                item.amount = orgInvoiceWithPayment!!.invoiceOrder!!.priceStatement!!["balance"]
                item.isPaid = true
                item.typePayment = "fullpaid"

                Log.d(TAG, "-----  ${item.invoiceOrderId.toString()} ${item.typePayment}  ${item.amount.toString()} -----")
            }
        }

        Log.d(TAG, "-----  this will affect InvoiceOrderPickupFrg because this is observing -----\n\n\n")
        _invoiceWithPayments.notifyObserver()
    }


    fun onClickALLFullDeselect(){
        Log.d(TAG, "\n----- user click DEselect all full paid button-----")
        Log.d(TAG, "----- _invoiceWithPayments will changed as below-----\n")

        invoiceWithPayments.value?.let {

            for(item in it){

                if(item.typePayment == "fullpaid"){
                    item.amount = null
                    item.isPaid = null
                    item.typePayment = null
                }
                Log.d(TAG, "-----  ${item.invoiceOrderId.toString()} ${item.typePayment}  ${item.amount.toString()} -----")
            }
        }

        Log.d(TAG, "-----  this will affect InvoiceOrderPickupFrg because this is observing -----\n\n\n")
        _invoiceWithPayments.notifyObserver()
    }

    // user clicked 'select all prepaid' button
    fun onClickAllPreSelect(){
        Log.d(TAG, "\n----- user click select all prepaid paid button-----")
        Log.d(TAG, "----- _invoiceWithPayments will changed as below-----\n")

        invoiceWithPayments.value?.let {
            for(item in it){

                val orgInvoiceWithPayment = orgInvoiceWithPayments.find { it.invoiceOrderId == item.invoiceOrderId }

                item.amount = orgInvoiceWithPayment!!.invoiceOrder!!.priceStatement!!["balance"]
                item.isPaid = true
                item.typePayment = "prepaid"
                Log.d(TAG, "-----  ${item.invoiceOrderId.toString()} ${item.typePayment}  ${item.amount.toString()} -----")
            }
        }

        Log.d(TAG, "-----  this will affect InvoiceOrderPickupFrg because this is observing -----\n\n\n")
        _invoiceWithPayments.notifyObserver()
    }


    // user clicked 'unselect all prepaid' button
    fun onClickALLPreDeselect(){
        Log.d(TAG, "\n----- user click DEselect all prepaid paid button-----")
        Log.d(TAG, "----- _invoiceWithPayments will changed as below-----\n")

        invoiceWithPayments.value?.let {
            for(item in it){

                if(item.typePayment == "prepaid"){

                    item.amount = null
                    item.isPaid = null
                    item.typePayment = null
                }
                Log.d(TAG, "-----  ${item.invoiceOrderId.toString()} ${item.typePayment}  ${item.amount.toString()} -----")
            }
        }

        Log.d(TAG, "-----  this will affect InvoiceOrderPickupFrg because this is observing -----\n\n\n")
        _invoiceWithPayments.notifyObserver()
    }


    // there are three type of payment
    // 1. fullpaid
    // 2. prepaid
    // 3. partialpaid
    fun onDeCheckedTypePayment(invoiceWithPayment:InvoiceWithPayment){
        Log.d(TAG,"----- onDeCheckedTypePayment -----")
        Log.d(TAG,"----- invoiceWithPayment.typePayment : '${invoiceWithPayment.typePayment}' -----")

        // back original InvoiceWithPayment
        val orgInvoiceWithPayment = orgInvoiceWithPayments.find { it.invoiceOrderId == invoiceWithPayment.invoiceOrderId }

        Log.d(TAG,"----- orgInvoiceWithPayment : typePayment : '${orgInvoiceWithPayment?.typePayment.toString()}' -----")

        _invoiceWithPayments.value = updateWithModifiedInvoiceWithPayment(orgInvoiceWithPayment!!.copy())
    }

    fun updateWithModifiedInvoiceWithPayment(modifiedInvoiceWithPayment: InvoiceWithPayment ):MutableList<InvoiceWithPayment>{
        val temp = mutableListOf<InvoiceWithPayment>()
        invoiceWithPayments.value?.let {
            if(it.count() >0){

                for(item in it){
                    if(item.invoiceOrderId == modifiedInvoiceWithPayment.invoiceOrderId){
                        temp.add(modifiedInvoiceWithPayment)
                    }else{
                        temp.add(item)
                    }
                }
            }
        }
        return temp
    }

    // get data from InvoiceOrderPickupFrg
    fun updateInvoiceWithPayments(modifiedInvoiceWithPayments: MutableList<InvoiceWithPayment>){

        _invoiceWithPayments.value = modifiedInvoiceWithPayments

    }
}