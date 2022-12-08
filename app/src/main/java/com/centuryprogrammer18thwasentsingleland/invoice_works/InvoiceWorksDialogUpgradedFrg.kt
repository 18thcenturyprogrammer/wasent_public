package com.centuryprogrammer18thwasentsingleland.invoice_works


import android.os.Bundle

import com.centuryprogrammer18thwasentsingleland.R

import android.app.Dialog
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbManager
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.widget.Scroller

import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceWorksDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceWorksDialogUpgradedFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch


class InvoiceWorksDialogUpgradedFrg : DialogFragment(), IOCallBack {
    private val TAG = InvoiceWorksDialogUpgradedFrg::class.java.simpleName

    private lateinit var args : InvoiceWorksDialogUpgradedFrgArgs

    private lateinit var binding : InvoiceWorksDialogUpgradedFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel: InvoiceWorksDialogUpgradedFrgVM by viewModels()

    lateinit var customer:Customer
    lateinit var invoiceOrder:InvoiceOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG,"***** onCreate *****")

//        args = InvoiceWorksDialogUpgradedFrgArgs.fromBundle(requireArguments())
//        customer = args.customer
//        invoiceOrder = args.invoiceOrder

        customer = arguments?.getSerializable("customer") as Customer
        invoiceOrder = arguments?.getSerializable("invoiceOrder") as InvoiceOrder

        // shared preference encryption ref) https://garageprojects.tech/encryptedsharedpreferences-example/
        // ref) https://medium.com/@Naibeck/android-security-encryptedsharedpreferences-ea239e717e5f
        // ref) https://proandroiddev.com/encrypted-preferences-in-android-af57a89af7c8
        //(1) create or retrieve masterkey from Android keystore
        //masterkey is used to encrypt data encryption keys
        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        //(2) Get instance of EncryptedSharedPreferences class
        // as part of the params we pass the storage name, reference to
        // masterKey, context and the encryption schemes used to
        // encrypt SharedPreferences keys and values respectively.
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    // dialog fragment window size change ref) https://stackoverflow.com/a/58179977/3151712
    override fun onResume() {
        super.onResume()

        val window = dialog!!.window ?: return
        val params = window.attributes
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT
//        params.height = LinearLayout.LayoutParams.MATCH_PARENT
        params.width = 800
        params.height = 660

        window.attributes = params
    }

//    override fun onCreateView(
//        inflater: LayoutInflater, container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        return inflater.inflate(R.layout.invoice_works_dialog_upgraded_frg, container, false)
//    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel.messageOnFragment.observe(this, Observer {
            if (it != ""){
                // there is message to show

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        viewModel.closeDialog.observe(this, Observer {
            if(it){
                // close dialog

                dialog?.dismiss()
            }
        })

        binding = DataBindingUtil.inflate(layoutInflater,R.layout.invoice_works_dialog_upgraded_frg,null, false)

        binding.frg = this
        binding.viewModel = viewModel


        // if invoice was picked up or edited or voided, user can not edit , void
        // one invoice adjusted, so one invoice is made then user can not adjust
        if(invoiceOrder.pickedUpAt != null || invoiceOrder.voidAt != null || invoiceOrder.orgInvoiceOrderId != null || invoiceOrder.adjustedBy != null){
            binding.btnEditInvoiceWorkDialogFrg.isEnabled = false
            binding.btnVoidInvoiceWorkDialogFrg.isEnabled = false
            binding.btnRackInvoiceWorkDialogFrg.isEnabled = false
        }


        with(binding){
            tvInvoiceIdInvoiceWorksDialog.text = invoiceOrder.id.toString()
            tvFirstNameInvoiceWorksDialog.text = customer.firstName
            tvLastNameInvoiceWorksDialog.text = customer.lastName
            tvPhoneInvoiceWorksDialog.text = customer.phoneNum
            tvEmailnvoiceWorksDialog.text = customer.email


            tvCreatedAtInvoiceWorksDialog.text = invoiceOrder.createdAt
            tvCreatedByInvoiceWorksDialog.text = invoiceOrder.createdBy?.replace("team_id_","")

            tvDueAtInvoiceWorksDialog.text = invoiceOrder.dueDateTime
            tvTagInvoiceWorksDialog.text = invoiceOrder.tag+"/"+invoiceOrder.tagColor

            tvRackedAtInvoiceWorksDialog.text = invoiceOrder.rackLocation
            tvRackedByInvoiceWorksDialog.text = invoiceOrder.rackedAt?.let {
                StringBuilder(it).append(" by ").append(invoiceOrder.rackedBy?.replace("team_id_",""))
            }

            tvPickedUpAtInvoiceWorksDialog.text = invoiceOrder.pickedUpAt
            tvPickedUpByInvoiceWorksDialog.text = invoiceOrder.pickedUpBy?.replace("team_id_","")

            tvNoteInvoiceWorksDialog.movementMethod = ScrollingMovementMethod()
            tvNoteInvoiceWorksDialog.text = invoiceOrder.note

            tvVoidedAtInvoiceWorksDialog.text = invoiceOrder.voidAt
            tvVoidedByInvoiceWorksDialog.text = invoiceOrder.voidBy?.replace("team_id_","")

            tvOriginalIdInvoiceWorksDialog.text = invoiceOrder.orgInvoiceOrderId?.toString()

            var adjustedBy = ""
            invoiceOrder.adjustedBy?.let {
                adjustedBy= getString(R.string.adjusted_by)+" "+it.replace("team_id_","")
            }

            tvEditedByInvoiceWorksDialog.text = adjustedBy


            tvSubtotalInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["subTotal"]!!)
            tvDiscountInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["discount"]!!)
            tvTaxInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["tax"]!!)



            val totalQty = invoiceOrder.qtyTable!!["dry"]!!+invoiceOrder.qtyTable!!["wet"]!!+invoiceOrder.qtyTable!!["alter"]!!+invoiceOrder.qtyTable!!["clean"]!!+invoiceOrder.qtyTable!!["press"]!!

            Log.d(TAG,"***** totalQty : ${totalQty.toString()} *****")

            tvTotalQtyInvoiceWorksDialog.text = totalQty.toString()

            tvEnvInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["env"]!!)
            tvTotalInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["total"]!!)
            tvPrepaidInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["prepaid"]!!)
            tvBalanceInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["balance"]!!)

            val adapter = FabricareInvoiceWorksApt()
            cvFabricaresInvoiceWorkDialogFrg.adapter = adapter
            cvFabricaresInvoiceWorkDialogFrg.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL,false)
            adapter.submitList(invoiceOrder.fabricares)
            adapter.notifyDataSetChanged()
        }

        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setView(binding.root)
        alertDialogBuilder.setCancelable(false)


        return alertDialogBuilder.create()
    }

    fun onClickReprintBtn(){
        Log.d(TAG,"***** onClickReprintBtn *****")

        val mPos = Pos()
        val mUsb = USBPrinting()

        mPos.Set(mUsb)

        mUsb.SetCallBack(this)

        val mUsbManager = requireContext().getSystemService(Context.USB_SERVICE) as UsbManager

        val deviceList = mUsbManager.getDeviceList()

//            this genstar machine has two usb devices, and /dev/bus/usb/002/005 is printer
//            vendor-id="4070" product-id="33054" check in device_filter.xml
//            val deviceA = deviceList["/dev/bus/usb/002/004"]
        val deviceA = deviceList["/dev/bus/usb/002/005"]

        val mPermissionIntent = PendingIntent.getBroadcast(
            requireContext(),
            0,
            Intent(requireContext().getApplicationInfo().packageName),
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
            lifecycleScope.launch {

                Log.d(TAG,"***** right before GenstarPrint.printTicket *****")
                Log.d(TAG,"***** printing invoiceOrder : ${invoiceOrder.toString()} *****")

                // makeTopTicket is extension func of Fragment which makes PrintingJob for top part of ticket
//                        val topTicket = makeTopTicket(sharedPrefs)

                // add part of ticket for dropoff
//                        topTicket.addAll(it)

                GenstarPrint.printTicket(mPos,576,true,false,true,1, invoiceOrder.makePrintTicket() )

                mUsb.Close()
            }
        }
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






    fun onClickEditBtn(){
        Log.d(TAG,"***** onClickEditBtn *****")

        val action = InvoiceWorksDialogUpgradedFrgDirections.actionInvoiceWorksDialogUpgradedFrgToMakeInvoiceActivity(customer,invoiceOrder)
        findNavController().navigate(action)
    }

    fun onClickVoidBtn(){
        Log.d(TAG,"***** onClickVoidBtn *****")

        val action = InvoiceWorksDialogUpgradedFrgDirections.actionInvoiceWorksDialogUpgradedFrgToVoidInvoiceDialogFrg(customer,invoiceOrder)
        findNavController().navigate(action)
    }

    fun onClickRackBtn(){
        Log.d(TAG,"***** onClickRackBtn *****")

        val action = InvoiceWorksDialogUpgradedFrgDirections.actionInvoiceWorksDialogUpgradedFrgToRackAct()
        findNavController().navigate(action)
    }
}