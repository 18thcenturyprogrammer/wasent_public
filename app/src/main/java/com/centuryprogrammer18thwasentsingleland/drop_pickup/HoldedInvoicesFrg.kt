package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.HoldedInvoicesFrgBinding
import com.centuryprogrammer18thwasentsingleland.login.ManagerLoginAct
import com.centuryprogrammer18thwasentsingleland.singletons.HoldedInvoiceOrders

class HoldedInvoicesFrg : Fragment() {
    private val TAG = HoldedInvoicesFrg::class.java.simpleName

    private lateinit var binding : HoldedInvoicesFrgBinding

    private lateinit var masterKeyAlias : String
    private lateinit var sharedPrefs : SharedPreferences

    val viewModel: HoldedInvoicesFrgVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as DropPickupActivity).setActionBarTitle(getString(R.string.holded_invoices))

        binding = DataBindingUtil.inflate(inflater, R.layout.holded_invoices_frg,container,false)

        val adapter = HoldedInvoiceOrderApt(this)
        binding.rvHoldedInvoicesFrg.adapter = adapter
        binding.rvHoldedInvoicesFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        adapter.submitList(HoldedInvoiceOrders.getHoldedInvoiceOrders())
        adapter.notifyDataSetChanged()

        return binding.root
    }

    fun onCardViewClicked(clickedPosition : Int){
        Log.d(TAG,"***** onCardViewClicked *****")
        Log.d(TAG,"***** clickedPosition : '${clickedPosition.toString()}' *****")

        val holdedInvoiceOrder = HoldedInvoiceOrders.getHoldedInvoiceByIndex(clickedPosition)
        HoldedInvoiceOrders.removeHoldedInvoiceByIndex(clickedPosition)

        val action = HoldedInvoicesFrgDirections.actionHoldedInvoicesFrgToMakeInvoiceActivity(holdedInvoiceOrder.customer!!, holdedInvoiceOrder.invoiceOrder)
        findNavController().navigate(action)
    }
}