package com.centuryprogrammer18thwasentsingleland.sales

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.usb.UsbManager
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
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.databinding.CategorizedInvoicesFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class CategorizedInvoicesFrg : Fragment(),IOCallBack {
    private val TAG = CategorizedInvoicesFrg::class.java.simpleName

    private lateinit var args :CategorizedInvoicesFrgArgs

    private lateinit var binding : CategorizedInvoicesFrgBinding

    private lateinit var masterKeyAlias : String
    private lateinit var sharedPrefs : SharedPreferences

    private val viewModel: CategorizedInvoicesFrgVM by viewModels()

    private lateinit var categoryName: String
    private lateinit var startDate: MutableList<Int>
    private lateinit var endDate: MutableList<Int>

    private lateinit var contentsForPrint : MutableList<InvoiceOrder>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "***** onCreate *****")

        args = CategorizedInvoicesFrgArgs.fromBundle(requireArguments())

        categoryName = args.categoryName
        startDate = args.startDate.toList().toMutableList()
        endDate = args.endDate.toList().toMutableList()

        Log.d(TAG, "***** categoryName : ${categoryName} *****")
        Log.d(TAG, "***** startDate : ${startDate.toString()} *****")
        Log.d(TAG, "***** endDate : ${endDate.toString()} *****")

        initEncrypSharedPrefs()
        viewModel.initSharedPrefs(sharedPrefs)
        viewModel.initVars(categoryName,startDate,endDate)
        viewModel.getInvoiceOrders(categoryName)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.categorized_invoices_frg, container, false)

        binding.frg = this

        val adapter = CategorizedInvoiceOrderApt(sharedPrefs,viewModel, categoryName)

        binding.rvInvoicesCategorizedInvoicesFrg.adapter = adapter
        binding.rvInvoicesCategorizedInvoicesFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        with(binding){
            when(categoryName){
                "all_invoices" ->{
                    lbVoidedAtCategorizedInvoicesFrg.visibility = View.INVISIBLE
                    lbVoidedByCategorizedInvoicesFrg.visibility = View.INVISIBLE

                    lbOrgInvoiceIdCategorizedInvoicesFrg.visibility = View.GONE
                    lbBtnOrgInvoiceCategorizedInvoicesFrg.visibility = View.GONE
                }

                "voided" ->{
                    lbVoidedAtCategorizedInvoicesFrg.visibility = View.VISIBLE
                    lbVoidedByCategorizedInvoicesFrg.visibility = View.VISIBLE

                    lbOrgInvoiceIdCategorizedInvoicesFrg.visibility = View.GONE
                    lbBtnOrgInvoiceCategorizedInvoicesFrg.visibility = View.GONE
                }

                "adjusted" -> {
                    lbVoidedAtCategorizedInvoicesFrg.visibility = View.GONE
                    lbVoidedByCategorizedInvoicesFrg.visibility = View.GONE

                    lbOrgInvoiceIdCategorizedInvoicesFrg.visibility = View.VISIBLE
                    lbBtnOrgInvoiceCategorizedInvoicesFrg.visibility = View.VISIBLE
                }
            }
        }

        viewModel.invoiceOrders.observe(viewLifecycleOwner, Observer {

            if(it.count() > 0){
                binding.btnPrintCategorizedInvoicesFrg.isEnabled = true
                contentsForPrint = it
            }

            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        viewModel.msgToFrg.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        viewModel.foundOrginvoiceOrder.observe(viewLifecycleOwner, Observer {
            val bundle = bundleOf("customer" to it.customer, "invoiceOrder" to it)
            findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
        })

        return binding.root
    }



    fun showVoidedAdjustedInvoice(clickedInvoiceOrder : InvoiceOrder){
        val bundle = bundleOf("customer" to clickedInvoiceOrder.customer, "invoiceOrder" to clickedInvoiceOrder)
        findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
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

    fun printing(){
        Log.d(TAG,"***** CategorizedInvoicesFrg printing *****")
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
                    append(startDate[0]).
                    append("-").
                    append(startDate[1]+1).
                    append("-").
                    append(startDate[2]).
                    append(" ~ ").
                    append(endDate[0]).
                    append("-").
                    append(endDate[1]+1).
                    append("-").
                    append(endDate[2]).
                    toString()

                val title =
                    StringBuilder().
                    append(dateStr).
                    append("\r\n").
                    append(categoryName.replace("_"," ").capitalize()).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())
                printingJobs.add(GenstarPrint.makeLargePrintingJob(title))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())


                val label=
                    when(categoryName){
                        "all_invoices"-> {
                            StringBuilder().
                            append(getString(R.string.invoice_id)).
                            append("-----").
                            append(getString(R.string.name)).
                            append("-----").
                            append(getString(R.string.phone_number)).
                            append("\r\n").
                            append(getString(R.string.created_at)).
                            append("--").
                            append(getString(R.string.by)).
                            append("--").
                            append(getString(R.string.due_date)).
                            append("\r\n").
                            append(getString(R.string.only_total)).
                            append("\r\n").
                            toString()
                        }

                        "voided"-> {
                                StringBuilder().
                                append(getString(R.string.invoice_id)).
                                append("-----").
                                append(getString(R.string.name)).
                                append("-----").
                                append(getString(R.string.phone_number)).
                                append("\r\n").
                                append(getString(R.string.created_at)).
                                append("---").
                                append(getString(R.string.by)).
                                append("---").
                                append(getString(R.string.due_date)).
                                append("\r\n").
                                append(getString(R.string.only_total)).
                                append("---").
                                append(getString(R.string.voided_at)).
                                append("---").
                                append(getString(R.string.by)).
                                append("\r\n").
                                toString()
                        }

                        "adjusted"-> {
                            StringBuilder().
                            append(getString(R.string.invoice_id)).
                            append("-----").
                            append(getString(R.string.name)).
                            append("-----").
                            append(getString(R.string.phone_number)).
                            append("\r\n").
                            append(getString(R.string.created_at)).
                            append("----").
                            append(getString(R.string.by)).
                            append("----").
                            append(getString(R.string.due_date)).
                            append("\r\n").
                            append(getString(R.string.only_total)).
                            append("----").
                            append(getString(R.string.org_invoice_id)).
                            append("\r\n").
                            toString()
                        }
                        else -> {
                            ""
                        }
                }

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(label))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                for(item in contentsForPrint!!){

                    val nameAndPhone =
                        StringBuilder().
                        append(item.customer!!.firstName).
                        append(" ").
                        append(item.customer!!.lastName).
                        append("   ").
                        append(item.customer!!.phoneNum).
                        toString()

                    val itemsStrBuilder = StringBuilder()

                    val firstLine =
                        StringBuilder().
                        append(item.id).
                        append("   ").
                        append(nameAndPhone).
                        append("\r\n").
                        toString()

                    printingJobs.add(GenstarPrint.makeSmallPrintingJob(firstLine))

                    val secondLine=
                        when(categoryName){
                            "all_invoices"-> {
                                StringBuilder().
                                append(item.createdAt).
                                append("  ").
                                append(item.createdBy).
                                append("  ").
                                append(item.dueDateTime).
                                append("\r\n").
                                append(makeTwoPointsDecialStringWithDollar(item.priceStatement!!["total"]!!)).
                                append("\r\n").
                                toString()
                            }

                            "voided"-> {
                                StringBuilder().
                                append(item.createdAt).
                                append("   ").
                                append(item.createdBy).
                                append("   ").
                                append(item.dueDateTime).
                                append("\r\n").
                                append(makeTwoPointsDecialStringWithDollar(item.priceStatement!!["total"]!!)).
                                append("   ").
                                append(item.voidAt).
                                append("   ").
                                append(item.voidBy).
                                append("\r\n").
                                toString()
                            }

                            "adjusted"-> {
                                StringBuilder().
                                append(item.createdAt).
                                append("   ").
                                append(item.createdBy).
                                append("   ").
                                append(item.dueDateTime).
                                append("\r\n").
                                append(makeTwoPointsDecialStringWithDollar(item.priceStatement!!["total"]!!)).
                                append("   ").
                                append(item.orgInvoiceOrderId).
                                append("\r\n").
                                toString()
                            }
                            else -> {
                                ""
                            }
                        }

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