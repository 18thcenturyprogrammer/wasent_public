package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.util.Log
import android.widget.EditText
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceWithPayment
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar

class PaymentPickupFrgVM : ViewModel() {
    private val TAG = PaymentPickupFrgVM::class.java.simpleName

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg :LiveData<String>
        get() = _msgToFrg

    private val _showZeroAmountAlert = MutableLiveData<MutableList<Any>>()
    val showZeroAmountAlert :LiveData<MutableList<Any>>
        get() = _showZeroAmountAlert

    private val _invoiceWithPayment = MutableLiveData<InvoiceWithPayment>()
    val invoiceWithPayment :LiveData<InvoiceWithPayment>
        get() = _invoiceWithPayment

    fun onOkBtnClicked(invoiceWithPayment: InvoiceWithPayment, amountEditText: EditText){
        Log.d(TAG,"----- onOkBtnClicked -----")
        Log.d(TAG,"----- before updated invoiceWithPayment : ${invoiceWithPayment.invoiceOrderId.toString()} ${invoiceWithPayment.typePayment.toString()} ${invoiceWithPayment.isPaid.toString()} --- user entered amoutn is ${amountEditText.text} -----")

        if(amountEditText.text.isNotEmpty()){

            val amount = amountEditText.text.toString().replace("[$,]".toRegex(),"").toDouble()

            if(amount != 0.0){

                if(amount <= invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!!) {

                    when (invoiceWithPayment.typePayment) {
                        "prepaid" -> {

                            val updatedInvoiceWithPayment = InvoiceWithPayment(
                                invoiceWithPayment.invoiceOrderId,
                                invoiceWithPayment.invoiceOrder!!,
                                true,
                                "prepaid",
                                amount
                            )

                            _invoiceWithPayment.value = updatedInvoiceWithPayment

                            Log.d(TAG,"----- after updated invoiceWithPayment : ${invoiceWithPayment.invoiceOrderId.toString()}  ${invoiceWithPayment.typePayment.toString()} ${invoiceWithPayment.amount.toString()} -----")
                            Log.d(TAG,"----- PaymentPickupFrg is observing this -----\n\n\n")
                        }

                        "partialpaid" -> {
                            if(amount != invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!!){

                                val updatedInvoiceWithPayment = InvoiceWithPayment(
                                    invoiceWithPayment.invoiceOrderId,
                                    invoiceWithPayment.invoiceOrder!!,
                                    true,
                                    "partialpaid",
                                    amount
                                )

                                _invoiceWithPayment.value = updatedInvoiceWithPayment
                                Log.d(TAG,"----- after updated invoiceWithPayment : ${invoiceWithPayment.invoiceOrderId.toString()}  ${invoiceWithPayment.typePayment.toString()} ${invoiceWithPayment.amount.toString()} -----")

                            }else{
                                _msgToFrg.value = App.resourses!!.getString(R.string.enter_fullamount_partilal_payment_should_be_lessa_than_balance,"$"+invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!!.toString())
                            }
                        }
                    }

                }else{
                    _msgToFrg.value = App.resourses!!.getString(R.string.you_can_enter_amount_is_smaller_than_balance,
                        makeTwoPointsDecialStringWithDollar(invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!!))
                }


            }else{
                // amount is zero

                when(invoiceWithPayment.typePayment){
                    "prepaid" -> {
                        _msgToFrg.value = App.resourses!!.getString(R.string.empty_zero_not_allowed)
                    }

                    "partialpaid" -> {
                        _showZeroAmountAlert.value = mutableListOf(true,invoiceWithPayment)
                    }
                }
            }

        }else{
            _msgToFrg.value = App.resourses!!.getString(R.string.empty_not_allowed)
        }
    }


    // user click cancel button on payment_pickup_frg.xml
    fun onCancelBtnClicked(invoiceWithPayment: InvoiceWithPayment){
        val old = invoiceWithPayment
        old.isPaid = null
        old.typePayment = null
        old.amount = null

        _invoiceWithPayment.value = old
    }
}