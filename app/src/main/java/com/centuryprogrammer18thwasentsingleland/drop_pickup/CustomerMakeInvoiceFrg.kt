package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FrgCustomerMakeInvoiceBinding

class CustomerMakeInvoiceFrg : Fragment() {
    private val TAG = CustomerMakeInvoiceFrg::class.java.simpleName

    private lateinit var binding : FrgCustomerMakeInvoiceBinding

    private val viewModel: CustomerMakeInvoiceFrgVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.frg_customer_make_invoice, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

}