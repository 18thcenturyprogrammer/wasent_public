package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.UserInvoiceHistoryFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.UserInvoiceHistoryFrgCallback
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import java.lang.StringBuilder
import java.util.*

class UserInvoiceHistoryFrg : Fragment() , UserInvoiceHistoryFrgCallback {
    private val TAG = UserInvoiceHistoryFrg::class.java.simpleName

    lateinit var args : UserInvoiceHistoryFrgArgs

    lateinit var binding : UserInvoiceHistoryFrgBinding

    private lateinit var masterKeyAlias : String
    lateinit var sharedPrefs: SharedPreferences

    val viewModel: UserInvoiceHistoryFrgVM by viewModels()

    lateinit var customer : Customer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = UserInvoiceHistoryFrgArgs.fromBundle(requireArguments())

        customer = args.customer

        viewModel.updateCustomer(customer)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as DropPickupActivity).setActionBarTitle(getString(R.string.customer_invoices_history))

        binding = DataBindingUtil.inflate(inflater,R.layout.user_invoice_history_frg, container ,false)
        binding.frg = this
        binding.viewModel = viewModel

        with(binding){
            tvFirstNameUserInvoiceHistoryFrg.text = customer.firstName
            tvLastNameUserInvoiceHistoryFrg.text = customer.lastName
            tvPhoneNumUserInvoiceHistoryFrg.text = customer.phoneNum
        }

        val adapter = UserInvoiceHistoryApt(viewModel,this)
        binding.rvInvoicesUserInvoiceHistoryFrg.adapter = adapter
        binding.rvInvoicesUserInvoiceHistoryFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL, false)

        viewModel.invoiceOrders.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        viewModel.updateSharedPrefs(sharedPrefs)

        viewModel.startDate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                // month start from zero , so plus 1
                binding.tvStartDateUserInvoiceHistoryFrg.text =
                    StringBuilder(it["month"]!!.plus(1)
                        .toString())
                        .append("-")
                        .append(it["day"].toString())
                        .append("-")
                        .append(it["year"].toString())

                viewModel.entDate.value?.let {
                    if(!it.isEmpty()){
                        // start date and end date entered both, show search button
                        binding.btnSearchUserInvoiceHistoryFrg.isEnabled = true
                    }
                }
            }
        })

        viewModel.entDate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                // month start from zero , so plus 1
                binding.tvEndDateUserInvoiceHistoryFrg.text =
                    StringBuilder(it["month"]!!.plus(1)
                        .toString())
                        .append("-")
                        .append(it["day"].toString())
                        .append("-")
                        .append(it["year"].toString())

                viewModel.startDate.value?.let {
                    if(!it.isEmpty()){
                        // start date and end date entered both, show search button
                        binding.btnSearchUserInvoiceHistoryFrg.isEnabled = true
                    }
                }
            }
        })

        viewModel.msgToFrg.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        return binding.root
    }

    fun onClickStartBtn(){
        Log.d(TAG,"***** onClickStartBtn *****")

        // get today date
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
//            val hour: Int = cal.get(Calendar.HOUR)
//            val minutes: Int = cal.get(Calendar.HOUR)
//            val ampm: Int = cal.get(Calendar.AM_PM)

        // date picker for setting up due date
        // date picker ref) https://stackoverflow.com/a/61865742/3151712
        // select date event will be handled by viewModel
        val dialog = DatePickerDialog(requireContext(), viewModel, year, month, day)

        // i will share onDateSet(), so set tag
        dialog.datePicker.setTag("start")

        dialog.show()
    }

    fun onClickEndBtn(){
        Log.d(TAG,"***** onClickEndBtn *****")

        // get today date
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
//            val hour: Int = cal.get(Calendar.HOUR)
//            val minutes: Int = cal.get(Calendar.HOUR)
//            val ampm: Int = cal.get(Calendar.AM_PM)

        // date picker for setting up due date
        // date picker ref) https://stackoverflow.com/a/61865742/3151712
        // select date event will be handled by viewModel
        val dialog = DatePickerDialog(requireContext(), viewModel, year, month, day)

        // i will share onDateSet(), so set tag
        dialog.datePicker.setTag("end")

        dialog.show()
    }

    override fun onInovoiceOrderClicked(clickedInvoiceOrder : InvoiceOrder){
//        val bundle = bundleOf("customer" to customer, "invoiceOrder" to clickedInvoiceOrder)
//        findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
    }

}