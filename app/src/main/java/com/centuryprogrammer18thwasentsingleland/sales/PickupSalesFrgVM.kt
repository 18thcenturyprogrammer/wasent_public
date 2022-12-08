package com.centuryprogrammer18thwasentsingleland.sales

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.data.Payment

class PickupSalesFrgVM : ViewModel() {
    private val TAG = PickupSalesFrgVM::class.java.simpleName

    private var payments = mutableListOf<Payment>()

    private val _isUpdatedPaymentsVars =  MutableLiveData<Boolean>()
    val isUpdatedPaymentsVars : LiveData<Boolean>
        get() = _isUpdatedPaymentsVars

    var totalNumPayments = 0
    var totalAmountPayments = 0.0

    var fullpaidNumPayments=0
    var fullpaidAmountPayments =0.0

    var prepaidNumPayments = 0
    var prepaidAmountPayments = 0.0

    var prepaidPickupNumPayments = 0
    var prepaidPickupAmountPayments = 0.0

    var partialpaidNumPayments = 0
    var partialpaidAmountPayments = 0.0

    var creditNumPayments = 0
    var creditAmountPayments = 0.0

    var creditPaybackNumPayments = 0
    var creditPaybackAmountPayments = 0.0


    var totalSecondAmountPayments = 0.0

    var tax = 0.0
    var env = 0.0
    var discount = 0.0
    var dryPrice = 0.0
    var wetPrice = 0.0
    var alterPrice = 0.0



    fun updatePayments(receivedPayments: MutableList<Payment>){
        payments = receivedPayments
        updateVarsPayments()
    }

    fun updateVarsPayments(){
        // real amount which we received in the period time
        // this will include prepaid-paid, credit-payback because we received real money
        // this will not include prepaid pickup, credit pickup
        totalNumPayments = 0
        totalAmountPayments = 0.0

        fullpaidNumPayments=0
        fullpaidAmountPayments =0.0

        prepaidNumPayments = 0
        prepaidAmountPayments = 0.0

        prepaidPickupNumPayments = 0
        prepaidPickupAmountPayments = 0.0

        partialpaidNumPayments = 0
        partialpaidAmountPayments = 0.0

        creditNumPayments = 0
        creditAmountPayments = 0.0

        creditPaybackNumPayments = 0
        creditPaybackAmountPayments = 0.0

        totalSecondAmountPayments = 0.0

        tax = 0.0
        env = 0.0
        discount = 0.0
        dryPrice = 0.0
        wetPrice = 0.0
        alterPrice = 0.0



        for(item in payments){
            item.fullPaidAmount?.let { amount ->
                if(amount > 0.0 ){
                    totalNumPayments += 1
                    totalAmountPayments += amount

                    fullpaidNumPayments += 1
                    fullpaidAmountPayments += amount
                }
            }

            item.prepaidAmount?.let {amount ->
                // i include prepaid in totalNumPayments and totalAmountPayments because we received money from customer

                if(amount > 0.0){
                    totalNumPayments += 1
                    totalAmountPayments += amount

                    prepaidNumPayments += 1
                    prepaidAmountPayments += amount
                }
            }

            item.pastPrepaidAmount?.let {amount ->
                if(amount > 0.0){
                    if(item.fullPaidAmount != null && item.fullPaidAmount!! > 0.0 && amount > 0.0){
                        prepaidPickupNumPayments += 1
                        prepaidPickupAmountPayments += amount
                    }

                    if(item.partialpaidAmount != null && item.partialpaidAmount!! > 0.0 && amount > 0.0){
                        prepaidPickupNumPayments += 1
                        prepaidPickupAmountPayments += amount
                    }
                }
            }

            item.partialpaidAmount?.let {amount ->
                // i include prepaid in totalNumPayments and totalAmountPayments because we received money from customer

                if(amount > 0.0){
                    totalNumPayments += 1
                    totalAmountPayments += amount

                    partialpaidNumPayments += 1
                    partialpaidAmountPayments += amount
                }
            }

            item.creditAmount?.let {amount ->

                if(amount > 0.0){
                    creditNumPayments += 1
                    creditAmountPayments += amount
                }
            }

            item.creditpaidAmount?.let {amount ->
                // i include prepaid in totalNumPayments and totalAmountPayments because we received money from customer

                if(amount > 0.0){
                    totalNumPayments += 1
                    totalAmountPayments += amount

                    creditPaybackNumPayments += 1
                    creditPaybackAmountPayments += amount
                }
            }

            item.tax?.let {amount->
                if(amount > 0.0){
                    totalSecondAmountPayments += amount
                    tax +=amount
                }
            }

            item.env?.let {amount->
                if(amount > 0.0){
                    totalSecondAmountPayments += amount
                    env +=amount
                }

            }

            item.discount?.let {amount->
                if(amount > 0.0){
                    totalSecondAmountPayments += amount
                    discount +=amount
                }

            }

            item.dryPrice?.let {amount->
                if(amount > 0.0){
                    totalSecondAmountPayments += amount
                    dryPrice +=amount
                }

            }

            item.wetPrice?.let {amount->
                if(amount > 0.0){
                    totalSecondAmountPayments += amount
                    wetPrice +=amount
                }

            }

            item.alterPrice?.let {amount->
                if(amount > 0.0){
                    totalSecondAmountPayments += amount
                    alterPrice +=amount
                }
            }

        }
        _isUpdatedPaymentsVars.value = true
    }
}