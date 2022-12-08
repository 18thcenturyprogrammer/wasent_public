package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.PickupHistory
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.databinding.DropoffFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.*
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.StringBuilder


class DropoffFrg : Fragment() , IOCallBack {
    private val TAG = DropoffFrg::class.java.simpleName

    lateinit var args : DropoffFrgArgs

    lateinit var binding: DropoffFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel: DropoffFrgVM by viewModels()

    lateinit var customer: Customer
    lateinit var invoiceOrders : MutableList<InvoiceOrder>
    var lastPickupHistory : PickupHistory? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as DropPickupActivity).setActionBarTitle(getString(R.string.dropoff))

        args = DropoffFrgArgs.fromBundle(requireArguments())
        customer = args.customer
        invoiceOrders = args.invoiceOrders.toList().toMutableList()
        lastPickupHistory = args.lastPickupHistory

        //      showing softkeyboard
//        ref) https://stackoverflow.com/a/5617130/3151712
        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)

        viewModel.setCustomer(customer)

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        viewModel.setSharedPrefs(sharedPrefs)

        binding = DataBindingUtil.inflate(inflater,R.layout.dropoff_frg, container,false)

        // two way data binding observable with architecture component ref) https://developer.android.com/topic/libraries/data-binding/architecture#livedata
        binding.setLifecycleOwner(viewLifecycleOwner)

        binding.frg = this
        binding.viewModel = viewModel

        // check if there are InvoiceOrders to be picked up
        if(invoiceOrders.count() != 0 ){

            binding.btnPickupDropoffFrg.isEnabled = true
        }

        addImeOpiontEditText()

        viewModel.msgToFrg.observe(viewLifecycleOwner, Observer {
            if(!it.isEmpty()){

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        viewModel.cancelClicked.observe(viewLifecycleOwner, Observer {
            if(it){
                with(binding){
                    etDryDropoffFrg.isEnabled= true
                    etWetDropoffFrg.isEnabled= true
                    etAlterDropoffFrg.isEnabled= true
                    etHouseholdDropoffFrg.isEnabled= true
                    etCleanOnlyDropoffFrg.isEnabled= true
                    etPressOnlyDropoffFrg.isEnabled= true
                    etExtDropoffFrg.isEnabled= true
                    etRedoDropoffFrg.isEnabled= true
                }
            }
        })


        viewModel.okClicked.observe(viewLifecycleOwner, Observer {
            if(it){
                with(binding){
                    etDryDropoffFrg.isEnabled= false
                    etWetDropoffFrg.isEnabled= false
                    etAlterDropoffFrg.isEnabled= false
                    etHouseholdDropoffFrg.isEnabled= false
                    etCleanOnlyDropoffFrg.isEnabled= false
                    etPressOnlyDropoffFrg.isEnabled= false
                    etExtDropoffFrg.isEnabled= false
                    etRedoDropoffFrg.isEnabled= false
                }
                showDatePicker(requireContext(),viewModel,viewModel)
            }
        })

        viewModel.printing.observe(viewLifecycleOwner, Observer {

            if(it.count() > 0 ){
                val mPos = Pos()
                val mUsb = USBPrinting()

                mPos.Set(mUsb)
                mUsb.SetCallBack(this@DropoffFrg)

                val mUsbManager = requireActivity().getSystemService(Context.USB_SERVICE) as UsbManager

                val deviceList = mUsbManager.getDeviceList()

//            this genstar machine has two usb devices, and /dev/bus/usb/002/005 is printer
//            vendor-id="4070" product-id="33054" check in device_filter.xml
//            val deviceA = deviceList["/dev/bus/usb/002/004"]
                val deviceA = deviceList["/dev/bus/usb/002/005"]

                val mPermissionIntent = PendingIntent.getBroadcast(
                    requireContext(),
                    0,
                    Intent(requireActivity().getApplicationInfo().packageName),
                    0
                )

                if (!mUsbManager.hasPermission(deviceA)) {

                    Log.d(TAG,"***** has NO permission printing *****")

                    mUsbManager.requestPermission(deviceA, mPermissionIntent)

//							 Permission denied
                    Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.permission_denied))
                } else {

                    Log.d(TAG,"***** has permission printing *****")

                    mUsb.Open(mUsbManager,deviceA, requireContext())

                    // android coroutine scope ref) https://developer.android.com/topic/libraries/architecture/coroutines#lifecyclescope
                    viewLifecycleOwner.lifecycleScope.launch {

                        Log.d(TAG,"***** right before GenstarPrint.printTicket *****")
                        Log.d(TAG,"***** printing from VM : ${it.toString()} *****")

                        // makeTopTicket is extension func of Fragment which makes PrintingJob for top part of ticket
                        val topTicket = makeTopTicket(sharedPrefs)

                        // add part of ticket for dropoff
                        topTicket.addAll(it)

                        GenstarPrint.printTicket(mPos,576,true,false,true,1, topTicket )

                        mUsb.Close()

                        // clear vars and enable edittext
                        viewModel.onClickCancel()
                    }
                }
            }
        })

        return binding.root
    }

    fun onClickPickup(){
        Log.d(TAG,"***** onClickPickup *****")

        // send user to PickupAct

        val action = DropoffFrgDirections.actionDropoffFrgToPickupAct(
            customer,
            invoiceOrders.toTypedArray(),
            lastPickupHistory)

        findNavController().navigate(action)
    }

    fun addImeOpiontEditText(){
        // onDone is extension function of EditText which i added
        binding.etDryDropoffFrg.onDone { viewModel.onClickOk() }
        binding.etWetDropoffFrg.onDone { viewModel.onClickOk() }
        binding.etAlterDropoffFrg.onDone { viewModel.onClickOk() }
        binding.etHouseholdDropoffFrg.onDone { viewModel.onClickOk() }
        binding.etCleanOnlyDropoffFrg.onDone { viewModel.onClickOk() }
        binding.etPressOnlyDropoffFrg.onDone { viewModel.onClickOk() }
        binding.etExtDropoffFrg.onDone { viewModel.onClickOk() }
        binding.etRedoDropoffFrg.onDone { viewModel.onClickOk() }

    }

    // implementing IOCallBack
    override fun OnClose() {
        Log.d(TAG,"***** printer IO closed *****")
    }

    override fun OnOpenFailed() {
        Log.d(TAG,"***** printer IO open FAILED *****")
    }

    override fun OnOpen() {
        Log.d(TAG,"***** printer IO opened *****")
    }

}