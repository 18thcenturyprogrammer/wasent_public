package com.centuryprogrammer18thwasentsingleland.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Payment
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar

class CategorizedPaymentsFrgVM : ViewModel() {
    private val TAG = CategorizedPaymentsFrgVM::class.java.simpleName

    private lateinit var payments : MutableList<Payment>

    private val _displayPayments = MutableLiveData<MutableList<Payment>>()
    val displayPayments : LiveData<MutableList<Payment>>
        get() = _displayPayments

    fun initPayments(receivedPayments: MutableList<Payment>){
        payments = receivedPayments
    }

    fun getDisplayPayments(categoryName:String){
        val temp = mutableListOf<Payment>()

        for(item in payments){
            when(categoryName){
                "prepaid_paid" -> {
                    item.prepaidAmount?.let{amount ->
                        if(amount > 0.0){
                            temp.add(item)
                        }
                    }
                }

                "prepaid_pickup" -> {
                    item.pastPrepaidAmount?.let{ amount ->
                        if(amount > 0.0){
                            temp.add(item)
                        }
                    }
                }

                "credit_pickup" -> {
                    item.partialpaidAmount?.let{amount ->
                        if(amount > 0.0){
                            temp.add(item)
                        }
                    }
                }

                "credit_payback" -> {
                    item.creditpaidAmount?.let{amount ->
                        if(amount > 0.0){
                            temp.add(item)
                        }
                    }
                }

                "all_payments" -> {
                    temp.add(item)
                }
            }
        }
        _displayPayments.value = temp
    }

}