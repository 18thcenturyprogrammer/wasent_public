package com.centuryprogrammer18thwasentsingleland.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrderBrief

class InvoiceSalesFrgVM : ViewModel() {
    private val TAG = InvoiceSalesFrgVM::class.java.simpleName

    private var invoiceOrderBriefs = mutableListOf<InvoiceOrderBrief>()

    private val _isUpdatedInvoiceVars =  MutableLiveData<Boolean>()
    val isUpdatedInvoiceVars : LiveData<Boolean>
        get() = _isUpdatedInvoiceVars

    var totalNumInvoice = 0

    var dryQty = 0
    var wetQty = 0
    var alterQty = 0
    var cleanOnlyQty = 0
    var pressOnlyQty = 0

    var totalNumQty = 0

    var dryAmount = 0.0
    var wetAmount = 0.0
    var alterAmount = 0.0
    var discountAmount = 0.0
    var taxAmount = 0.0
    var envAmount = 0.0

    var totalAmountGarments = 0.0
    var totalAmountInvoice = 0.0

    fun updateInvoiceOrderBriefs(receivedInvoiceOrderBriefs: MutableList<InvoiceOrderBrief>){
        invoiceOrderBriefs = receivedInvoiceOrderBriefs
        updateVarsWithInvoiceOrderBriefs()
    }

    fun updateVarsWithInvoiceOrderBriefs(){
        totalNumInvoice = 0

        dryQty = 0
        wetQty = 0
        alterQty = 0
        cleanOnlyQty = 0
        pressOnlyQty = 0

        totalNumQty = 0

        dryAmount = 0.0
        wetAmount = 0.0
        alterAmount = 0.0
        discountAmount = 0.0
        taxAmount = 0.0
        envAmount = 0.0

        totalAmountGarments = 0.0
        totalAmountInvoice = 0.0

        for(item in invoiceOrderBriefs){

            // exclude adjusted old InvoiceOrder
            if(item.adjustedBy == null){

                // InvoiceOrder which is not adjusted

                totalNumInvoice += 1
                dryQty += item.qtyTable["dry"]!!
                wetQty += item.qtyTable["wet"]!!
                alterQty += item.qtyTable["alter"]!!
                cleanOnlyQty += item.qtyTable["clean"]!!
                pressOnlyQty += item.qtyTable["press"]!!

                totalNumQty += item.qtyTable!!["dry"]!!+item.qtyTable!!["wet"]!!+item.qtyTable!!["alter"]!!+item.qtyTable!!["clean"]!!+item.qtyTable!!["press"]!!

                dryAmount += item.priceStatement["dryPrice"]!!
                wetAmount += item.priceStatement["wetPrice"]!!
                alterAmount += item.priceStatement["alterPrice"]!!
                discountAmount += item.priceStatement["discount"]!!
                taxAmount += item.priceStatement["tax"]!!
                envAmount += item.priceStatement["env"]!!

                totalAmountGarments += item.priceStatement["subTotal"]!!
                totalAmountInvoice += item.priceStatement["total"]!!
            }
        }

        _isUpdatedInvoiceVars.value = true
    }
}