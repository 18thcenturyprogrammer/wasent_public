package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceWithPayment
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceOrderPickupFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.getNavigationResult

class InvoiceOrderPickupFrg : Fragment() {
    private val TAG = InvoiceOrderPickupFrg::class.java.simpleName

    private lateinit var args : InvoiceOrderPickupFrgArgs
    private lateinit var binding : InvoiceOrderPickupFrgBinding

    private lateinit var actViewModel: PickupActVM
    private val viewModel : InvoiceOrderPickupFrgVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "\n\n\n ***** InvoiceOrderPickupFrg onCreate *****\n")

        args = InvoiceOrderPickupFrgArgs.fromBundle(requireArguments())

        Log.d(TAG,"***** received args.invoiceWithPayments from PickupAct *****")
        for(item in args.invoiceWithPayments){
            Log.d(TAG,"***** ${item.invoiceOrderId.toString()} ***** ${item.typePayment.toString()} ***** ${item.amount.toString()}*****/n")
        }

        actViewModel = (activity as PickupAct).viewModel


        viewModel.initOrgInvoiceWithPayments(args.invoiceWithPayments.toList().toMutableList())
        viewModel.initInvoiceWithPayments(args.invoiceWithPayments.toList().toMutableList())

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.invoice_order_pickup_frg,container,false)

        binding.viewModel = viewModel
        binding.frg = this

        val adapter = InvoiceWithPaymentPickupApt(viewModel, this)
        binding.rvInvoiceOrderPickupFrg.adapter = adapter
        binding.rvInvoiceOrderPickupFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)


        // observing invoiceWithPayments and if there is changed one , update it
        actViewModel.invoiceWithPayments.observe(viewLifecycleOwner, Observer {
            Log.d(TAG,"***** we found actViewModel invoiceWithPayments updated while observing*****")

            // for debugging
            for(item in it){
                Log.d(TAG,"***** ${item.invoiceOrderId.toString()} ${item.amount.toString()} ${item.isPaid.toString()} *****\n")
            }

            adapter.submitList(it.toList())
            adapter.notifyDataSetChanged()

        })

        // if invoiceWithPayments is changed, i will update invoiceWithPayment in activity ViewModel one by one
        viewModel.invoiceWithPayments.observe(viewLifecycleOwner, Observer {
            it?.let {
                Log.d(TAG,"\n***** we found invoiceWithPayments in   InvoiceOrderPickupFrgVM   updated while observing*****")

                for(item in it){
                    Log.d(TAG,"***** ${item.invoiceOrderId.toString()} ${item.amount.toString()} ${item.isPaid.toString()} is sent to PickupAct updateInvoiceWithPayment *****\n")
                    actViewModel.updateInvoiceWithPayment(item)
                }
            }
        })

        // this is for passing data back to previous fragment when popupto is happened
        // navigation pass data popupto popup ref) https://stackoverflow.com/a/60757744/3151712
        // navigation pass data popupto popup android docs ref) https://developer.android.com/guide/navigation/navigation-programmatic#returning_a_result

        // receiving data back from PaymentPickupFrg
        getNavigationResult("result")?.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "\n\n\n***** i got result from PaymentPickupFrg to InvoiceOrderPickupFrg *****")
            Log.d(TAG, "***** InvoiceWithPayments*****")

            for(item in it){
                Log.d(TAG, "***** ${item.invoiceOrderId.toString()} ${item.typePayment.toString()} ${item.amount.toString()}*****\n\n\n")
            }

            viewModel.updateInvoiceWithPayments(it)
        })

        return binding.root
    }



    // there are three type of payment
    // 1. fullpaid
    // 2. prepaid
    // 3. partialpaid

    // user changed as radio button checked. this is called from InvoiceWithPaymentPickupApt
    fun onCheckedTypePayment(invoiceWithPayment:InvoiceWithPayment){
        Log.d(TAG,"\n***** radio button checked    onCheckedTypePayment *****")
        Log.d(TAG,"***** '${invoiceWithPayment.invoiceOrderId.toString()}' '${invoiceWithPayment.typePayment}' '${invoiceWithPayment.amount.toString()}'*****\n\n")

        when(invoiceWithPayment.typePayment){
            "fullpaid" -> {
                Log.d(TAG,"***** user fullpaid radio button checked so i will make InvoiceWithPayment*****")

                val updatedInvoiceWithPayment = InvoiceWithPayment(
                    invoiceWithPayment.invoiceOrderId,
                    invoiceWithPayment.invoiceOrder!!,
                    true,
                    "fullpaid",
                    invoiceWithPayment.invoiceOrder!!.priceStatement!!["balance"]
                )

                Log.d(TAG,"***** updatedInvoiceWithPayment *****")
                Log.d(TAG,"***** '${updatedInvoiceWithPayment.invoiceOrderId.toString()}' '${updatedInvoiceWithPayment.typePayment}' '${updatedInvoiceWithPayment.amount.toString()}'*****\n\n")

                viewModel.updateInvoiceWithPayments(updateWithModifiedInvoiceWithPayment(updatedInvoiceWithPayment))
            }
            "prepaid" -> {
                Log.d(TAG,"***** user prepaid radio button checked so i will make InvoiceWithPayment*****")

                val updatedInvoiceWithPayment = InvoiceWithPayment(
                    invoiceWithPayment.invoiceOrderId,
                    invoiceWithPayment.invoiceOrder!!,
                    true,
                    "prepaid",
                    null
                )

                Log.d(TAG,"***** updatedInvoiceWithPayment *****")
                Log.d(TAG,"***** right before sending to PaymentPickupFrg '${updatedInvoiceWithPayment.invoiceOrderId.toString()}' '${updatedInvoiceWithPayment.typePayment}' '${updatedInvoiceWithPayment.amount.toString()}'*****\n\n")

                val action = InvoiceOrderPickupFrgDirections.actionInvoiceOrderPickupFrgToPaymentPickupFrg(updatedInvoiceWithPayment, viewModel.invoiceWithPayments.value!!.toTypedArray())
                findNavController().navigate(action)
            }

            "partialpaid" -> {
                Log.d(TAG,"***** user partialpaid radio button checked so i will make InvoiceWithPayment*****")

                val updatedInvoiceWithPayment = InvoiceWithPayment(
                    invoiceWithPayment.invoiceOrderId,
                    invoiceWithPayment.invoiceOrder!!,
                    true,
                    "partialpaid",
                    null
                )


                Log.d(TAG,"***** updatedInvoiceWithPayment *****")
                Log.d(TAG,"***** right before sending to PaymentPickupFrg '${updatedInvoiceWithPayment.invoiceOrderId.toString()}' '${updatedInvoiceWithPayment.typePayment}' '${updatedInvoiceWithPayment.amount.toString()}'*****\n\n")


                val action = InvoiceOrderPickupFrgDirections.actionInvoiceOrderPickupFrgToPaymentPickupFrg(updatedInvoiceWithPayment,viewModel.invoiceWithPayments.value!!.toTypedArray())
                findNavController().navigate(action)
            }

        }
    }

    fun updateWithModifiedInvoiceWithPayment(modifiedInvoiceWithPayment: InvoiceWithPayment ):MutableList<InvoiceWithPayment>{
        val temp = mutableListOf<InvoiceWithPayment>()
        viewModel.invoiceWithPayments.value?.let {
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
}