package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceWithPayment
import com.centuryprogrammer18thwasentsingleland.databinding.PaymentPickupFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.UpchargeDialogMakeInvoiceBinding
import com.centuryprogrammer18thwasentsingleland.utils.*

class PaymentPickupFrg : Fragment() {
    private val TAG = PaymentPickupFrg::class.java.simpleName

    private val args : PaymentPickupFrgArgs by navArgs()

    private lateinit var binding:PaymentPickupFrgBinding

    private lateinit var actViewModel: PickupActVM

    val viewModel: PaymentPickupFrgVM by viewModels()

    // invoiceWithPayment which is selected in PaymentPickupFrg
    private lateinit var invoiceWithPayment: InvoiceWithPayment

    // invoiceWithPayments which are all InvoiceWithPayments for customer in PaymentPickupFrg
    private lateinit var invoiceWithPayments: Array<InvoiceWithPayment>

    private var zeroAmountAlert : AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "***** PaymentPickupFrg onCreate *****")

        actViewModel = (activity as PickupAct).viewModel

        invoiceWithPayment = args.invoiceWithPayment
        invoiceWithPayments = args.invoiceWithPayments

        Log.d(TAG, "***** right after after received arg invoiceWithPayment : ${invoiceWithPayment.invoiceOrderId.toString()} ${invoiceWithPayment.typePayment.toString()} ${invoiceWithPayment.amount.toString()} *****")

        for(item in invoiceWithPayments){
            Log.d(TAG, "***** invoiceWithPayments : ${item.invoiceOrderId.toString()} ${item.typePayment.toString()} ${item.amount.toString()} *****")
        }

        viewModel.msgToFrg.observe(this, Observer {
            if(it.isNotEmpty()){
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        // data structure of showZeroAmountAlert
        // showZeroAmountAlert[0] is Boolean , showZeroAmountAlert[1] is InvoiceWithPayment
        viewModel.showZeroAmountAlert.observe(this, Observer {
            if(it[0] as Boolean){
                // show zero amount alert

                showZeroAmountAlert(it[1] as InvoiceWithPayment)
            }else{
                // hide zero amount alert

                zeroAmountAlert?.dismiss()
            }
        })

        // invoiceWithPayment which is selected in PaymentPickupFrg
        viewModel.invoiceWithPayment.observe(this, Observer {
            // there was change on invoiceWithPayment, let's update invoiceWithPayment in activity viewmodel

            Log.d(TAG,"***** invoiceWithPayment in PaymentPickupFrgVM was changed PaymentPickupFrg is observing, probably change amount *****")
            Log.d(TAG, "***** invoiceWithPayment : ${it.invoiceOrderId.toString()} ${it.typePayment.toString()} ${it.amount.toString()} *****")
            Log.d(TAG,"***** i will merge changed invoiceWithPayment into invoiceWithPayments *****")

            val temp = mutableListOf<InvoiceWithPayment>()
            for(item in invoiceWithPayments){
                if(item.invoiceOrderId == it.invoiceOrderId){
                    temp.add(it)
                }else{
                    temp.add(item)
                }
            }

            for(item in temp){
                Log.d(TAG, "***** ${item.invoiceOrderId.toString()} ${item.typePayment.toString()} ${item.amount.toString()} *****")
            }


            Log.d(TAG, "***** updated invoiceWithPayments above is sending to InvoiceOrderPickupFrg *****")

            // this is for passing data back to previouse fragment when popupto is happened
            // navigation pass data popupto popup ref) https://stackoverflow.com/a/60757744/3151712
            // navigation pass data popupto popup android docs ref) https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result
            setNavigationResult(temp, "result")

            // move to previous fragment which is PaymentPickupFrg
            findNavController().navigateUp()

        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.payment_pickup_frg, container, false)

        binding.invoice = invoiceWithPayment
        binding.frg = this
        binding.viewModel = viewModel

        with(binding){

            tvBalancePaymentPickupFrg.setText(makeTwoPointsDecialStringWithDollar(invoiceWithPayment!!.invoiceOrder!!.priceStatement!!["balance"]!!))

            // check what user choose at InvoiceOrderPickup
            when(invoiceWithPayment.typePayment){

                "prepaid" ->{
                    lbTitlePaymentPickupFrg.setText(R.string.prepaid)
                }

                "partialpaid" ->{
                    lbTitlePaymentPickupFrg.setText(R.string.partialpaid)
                }
            }
            lbDetailPaymentPickupFrg.setText(R.string.enter_customer_want_to_pay)

            // adding textwatcher for signed currency format
            etAmountPaymentPickupFrg.addTextChangedListener(
                CurrencyTextWatcher(etAmountPaymentPickupFrg)
            )
        }

        return binding.root
    }

    fun showZeroAmountAlert(receivedInvoiceWithPayment: InvoiceWithPayment){

        val amount = makeTwoPointsDecialStringWithDollar(receivedInvoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]!!)

        val zeroAmountAlertBuilder = AlertDialog.Builder(requireContext())
        val message = getString(R.string.enter_zero_amount_which_will_be_added_to_next, amount)
        zeroAmountAlertBuilder.setMessage(message)
        zeroAmountAlertBuilder.setCancelable(false)

        zeroAmountAlertBuilder.setPositiveButton(R.string.ok,object : DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, which: Int) {
                Log.d(TAG,"/n/n***** zero amount alert ok button onClick ******")

                val temp = mutableListOf<InvoiceWithPayment>()

                receivedInvoiceWithPayment.isPaid = true
                receivedInvoiceWithPayment.typePayment = "partialpaid"
                receivedInvoiceWithPayment.amount = 0.0

                for(item in invoiceWithPayments){
                    if(item.invoiceOrderId == receivedInvoiceWithPayment.invoiceOrderId){
                        temp.add(receivedInvoiceWithPayment)
                    }else{
                        temp.add(item)
                    }
                }

                Log.d(TAG,"/n/n***** zero amount invoiceWithPaymet will be merged into invoiceWithPayments. The result will be below ******")
                for(item in invoiceWithPayments){
                    Log.d(TAG, "***** ${item.invoiceOrderId.toString()} ${item.typePayment.toString()} ${item.amount.toString()} *****")
                }


                // temp data will be sent to InvoicePickupFrg.kt
                // this is for passing data back to previouse fragment when popupto is happened
                // navigation pass data popupto popup ref) https://stackoverflow.com/a/60757744/3151712
                // navigation pass data popupto popup android docs ref) https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result
                setNavigationResult(temp,"result")

                findNavController().navigateUp()
            }
        })

        zeroAmountAlertBuilder.setNegativeButton(R.string.cancel, object: DialogInterface.OnClickListener {
            override fun onClick(dialog: DialogInterface, which:Int) {
                Log.d(TAG,"***** zero amount alert cancel button onClick ******")
                zeroAmountAlert?.dismiss()
            }
        })

        zeroAmountAlert = zeroAmountAlertBuilder.create()

        zeroAmountAlert?.show()
    }


}