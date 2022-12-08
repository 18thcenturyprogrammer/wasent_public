package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.UserHistoryMakeInvoiceFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.UserInvoiceHistoryFrgCallback


// this fragment will be shown in MakeInvoiceActivity
// this is similar with UserInvoiceHistoryFrg which will search user invoices.
// this fragment will show last 3 months invoices

// this is similar with UserInvoiceHistoryFrg, so i will use UserInvoiceHistoryApt and callbacks (UserInvoiceHistoryFrgCallback, UserInvoiceHistoryViewModelCallback)
class UserHistoryMakeInvoiceFrg : Fragment(), UserInvoiceHistoryFrgCallback {
    private val TAG = UserHistoryMakeInvoiceFrg::class.java.simpleName

    lateinit var args : UserHistoryMakeInvoiceFrgArgs

    lateinit var binding : UserHistoryMakeInvoiceFrgBinding

    lateinit var masterKeyAlias :String
    lateinit var sharedPrefs: SharedPreferences

    private val viewModel: UserHistoryMakeInvoiceFrgVM by viewModels()

    lateinit var customer:Customer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = UserHistoryMakeInvoiceFrgArgs.fromBundle(requireArguments())
        customer = args.customer

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )


        viewModel.updateSharedPrefs(sharedPrefs)
        viewModel.updateCusomer(customer)

        viewModel.getInvoicesLastThreeMons()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.user_history_make_invoice_frg, container, false)

        binding.frg = this

        val adapter = UserInvoiceHistoryApt(viewModel,this)
        binding.rvInvoiceUserHistoryMakeInvoiceFrg.adapter = adapter
        binding.rvInvoiceUserHistoryMakeInvoiceFrg.layoutManager = LinearLayoutManager(requireContext(),
            LinearLayoutManager.VERTICAL, false)

        viewModel.invoiceOrders.observe(viewLifecycleOwner, Observer {
            if(!it.isEmpty()){
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

        return binding.root
    }

    // this is similar with UserInvoiceHistoryFrg, so i will use UserInvoiceHistoryApt and callbacks (UserInvoiceHistoryFrgCallback, UserInvoiceHistoryViewModelCallback)
    // called from UserInvoiceHistoryApt
    override fun onInovoiceOrderClicked(invoiceOrder: InvoiceOrder) {
        Log.d(TAG, "***** onInovoiceOrderClicked *****")

        val navController = findNavController()
        Log.d(TAG, "***** navController : ${navController.toString()} *****")

        val bundle = bundleOf("customer" to invoiceOrder.customer!!, "invoiceOrder" to invoiceOrder)
        findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
//        findNavController().navigate(R.id.invoiceWorksDialogUpgradedFrg,bundle)
    }

    fun onClickUserInfosBtn(){
        Log.d(TAG, "***** onClickUserInfosBtn *****")

        findNavController().navigateUp()
    }
}