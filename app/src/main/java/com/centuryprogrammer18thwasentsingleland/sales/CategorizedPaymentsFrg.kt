package com.centuryprogrammer18thwasentsingleland.sales

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbManager
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.Payment
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.databinding.CategorizedPaymentsFrgBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup.PickupActVM
import com.centuryprogrammer18thwasentsingleland.repository.InvoiceOrdersByIdCallback
import com.centuryprogrammer18thwasentsingleland.repository.PickupByIdCallback
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class CategorizedPaymentsFrg : Fragment(), InvoiceOrdersByIdCallback,IOCallBack {
    private val TAG = CategorizedPaymentsFrg::class.java.simpleName

    private lateinit var args : CategorizedPaymentsFrgArgs

    private lateinit var binding : CategorizedPaymentsFrgBinding

    private lateinit var masterKeyAlias : String
    private lateinit var sharedPrefs : SharedPreferences

    private lateinit var actViewModel : SalesActVM
    private val viewModel: CategorizedPaymentsFrgVM by viewModels()

    private lateinit var categoryName :String

    private var startDate :MutableMap<String,Int>? = null
    private var endDate :MutableMap<String,Int>? = null

    private var contentsForPrint : MutableList<Payment>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "***** onCreate *****")

        args = CategorizedPaymentsFrgArgs.fromBundle(requireArguments())

        initEncrypSharedPrefs()

        actViewModel = (requireActivity() as SalesAct).viewModel

        categoryName = args.categoryName
        Log.d(TAG, "***** categoryName : ${categoryName} *****")

        setActionBarTitle(categoryName)

        actViewModel.payments.value?.let {
            // for debugging
            Log.d(TAG, "***** actViewModel.payments is observed, there is changed actViewModel.payments or going through for fragment loading *****")
            for(item in it){
                Log.d(TAG, "***** payment:  ${item.invoiceOrderId} ${item.totalPaidAmount} ${item.paymentMaidAt} ${item.paymentMaidBy} *****")
            }

            if(it.count() > 0){
                viewModel.initPayments(it)
            }
        }

        actViewModel.startDate.value?.let {
            if(it.count() > 0){
                startDate = it
            }
        }

        actViewModel.entDate.value?.let {
            if(it.count() > 0){
                endDate = it
            }
        }

        viewModel.getDisplayPayments(categoryName)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.categorized_payments_frg,container,false)

        binding.frg = this

        val adapter = PaymentsApt(sharedPrefs,this,categoryName)

        binding.rvPaymentsCategorizedPaymentsFrg.adapter = adapter
        binding.rvPaymentsCategorizedPaymentsFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        viewModel.displayPayments.observe(viewLifecycleOwner, Observer {

            // for debugging
            Log.d(TAG, "***** categorized payments *****")
            for(item in it){
                Log.d(TAG, "***** payment:  ${item.invoiceOrderId} ${item.totalPaidAmount} ${item.paymentMaidAt} ${item.paymentMaidBy} *****")
            }

            if(it.count() > 0){
                // we have something we can print out

                binding.btnPrintCategorizedPaymentsFrg.isEnabled = true
            }

            contentsForPrint = it
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    // this is called from PaymentsApt.kt
    fun onPickupBtnClicked(pickupId:Long) {
        Log.d(TAG, "----- onPickupBtnClicked -----")

        val bundle = Bundle()
        bundle.putLong("pickupId", pickupId)
        findNavController().setGraph(R.navigation.pickup_dialog_nav,bundle)
    }

    // this is called after Repository.getInvoiceOrdersById()
    override fun onInvoiceOrdersByIdCallback(snapshot: DataSnapshot?, error: DatabaseError?) {
        Log.d(TAG, "----- onInvoiceOrdersByIdCallback -----")

        error?.let {
            Log.e(TAG, "----- error : ${error.message}-----")
        }

        // if there is no item , snapshot will be null
        snapshot?.let {
            val foundInvoiceOrder = snapshot.getValue(InvoiceOrder::class.java)

            // if i cant' find one , foundInvoiceOrder will be null
            Log.d(TAG,"----- foundInvoiceOrder : ${foundInvoiceOrder.toString()}-----")

            if(foundInvoiceOrder == null){
                Toast(requireActivity()).showCustomToast(requireContext(),getString(R.string.not_found_matched_invoice_id))
            }else{
                val bundle = Bundle()
                bundle.putSerializable("customer",foundInvoiceOrder.customer)
                bundle.putSerializable("invoiceOrder", foundInvoiceOrder)

                findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
            }
        }
    }

    fun initEncrypSharedPrefs(){
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

    fun setActionBarTitle(categoryName:String){

        val title = when(categoryName){
            "prepaid_paid" -> {
                getString(R.string.prepaid_dash_paid)
            }

            "prepaid_pickup" -> {
                getString(R.string.prepaid_dash_pickup)
            }

            "credit_pickup" -> {
                getString(R.string.credit_pickup)
            }

            "credit_payback" -> {
                getString(R.string.credit_payback)
            }

            "all_payments" -> {
                getString(R.string.payments)
            }

            else -> {""}
        }

        (activity as SalesAct).setActionBarTitle(title)
    }

    fun printing(){
        Log.d(TAG,"***** CategorizedPaymentsFrg printing *****")
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

                Log.d(TAG,"***** right before GenstarPrint.printTicket Invoice Sales*****")

                // makeTopTicket is extension func of Fragment which makes PrintingJob for top part of ticket
//                        val topTicket = makeTopTicket(sharedPrefs)

                // add part of ticket for dropoff
//                        topTicket.addAll(it)

                val printingJobs = mutableListOf<PrintingJob>()

                val dateStr =
                    StringBuilder().
                    append(startDate!!["year"]!!).
                    append("-").
                    append(startDate!!["month"]!!+1).
                    append("-").
                    append(startDate!!["day"]).
                    append(" ~ ").
                    append(endDate!!["year"]!!).
                    append("-").
                    append(endDate!!["month"]!!+1).
                    append("-").
                    append(endDate!!["day"]).
                    toString()

                val title =
                    StringBuilder().
                    append(dateStr).
                    append("\r\n").
                    append(categoryName.replace("_","-").capitalize()).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())
                printingJobs.add(GenstarPrint.makeLargePrintingJob(title))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

                val label =
                    StringBuilder().
                    append(getString(R.string.invoice_price)).
                    append("-----").
                    append(getString(R.string.paid)).
                    append("-----").
                    append(getString(R.string.credit)).
                    append("\r\n").
                    append(getString(R.string.paid_at)).
                    append("---").
                    append(getString(R.string.processed_by)).
                    append("---").
                    append(getString(R.string.pickup_id)).
                    append("---").
                    append(getString(R.string.invoice_id)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(label))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                for(item in contentsForPrint!!){

                    val invoicePrice = makeTwoPointsDecialStringWithDollar(item.invoiceOrderBalance!!)

                    val paid =
                        when(categoryName){
                            "prepaid_paid" -> {
                                makeTwoPointsDecialStringWithDollar(item.prepaidAmount!!)
                            }

                            "prepaid_pickup" -> {
                                makeTwoPointsDecialStringWithDollar(item.pastPrepaidAmount!!)
                            }

                            "credit_pickup" -> {
                                makeTwoPointsDecialStringWithDollar(item.partialpaidAmount!!)
                            }

                            "credit_payback" -> {
                                makeTwoPointsDecialStringWithDollar(item.creditpaidAmount!!)
                            }

                            "all_payments" -> {
                                var paidAmount = 0.0

                                item.prepaidAmount?.let{
                                    paidAmount = it
                                }

                                item.pastPrepaidAmount?.let{
                                    paidAmount = it
                                }

                                item.partialpaidAmount?.let{
                                    paidAmount = it
                                }

                                item.creditpaidAmount?.let{
                                    paidAmount = it
                                }

                                makeTwoPointsDecialStringWithDollar(paidAmount)
                            }

                            else -> {
                                ""
                            }
                        }

                    val credit =
                        when(categoryName){
                            "prepaid_paid" -> {
                                makeTwoPointsDecialStringWithDollar(0.0)
                            }

                            "prepaid_pickup" -> {
                                makeTwoPointsDecialStringWithDollar(0.0)
                            }

                            "credit_pickup" -> {
                                makeTwoPointsDecialStringWithDollar(item.creditAmount!!)
                            }

                            "credit_payback" -> {
                                makeTwoPointsDecialStringWithDollar(0.0)
                            }

                            "all_payments" -> {
                                var creditAmount = 0.0

                                item.partialpaidAmount?.let{
                                    creditAmount = item.creditAmount!!
                                }

                                makeTwoPointsDecialStringWithDollar(creditAmount)
                            }

                            else -> {
                                ""
                            }
                        }


                    val firstLine =
                        StringBuilder().
                        append(invoicePrice).
                        append("   ").
                        append(paid).
                        append("   ").
                        append(credit).
                        append("\r\n").
                        toString()

                    printingJobs.add(GenstarPrint.makeSmallPrintingJob(firstLine))

                    val secondLine =
                        StringBuilder().
                        append(item.paymentMaidAt).
                        append("   ").
                        append(item.paymentMaidBy).
                        append("   ").
                        append(item.pickupId).
                        append("   ").
                        append(item.invoiceOrderId).
                        append("\r\n").
                        toString()

                    printingJobs.add(GenstarPrint.makeSmallPrintingJob(secondLine))
                    printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                }

                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                printingJobs.add(GenstarPrint.makeFullCutPrintingJob())

                GenstarPrint.printTicket(mPos,576,true,false,true,1, printingJobs )

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

}