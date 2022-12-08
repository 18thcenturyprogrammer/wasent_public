package com.centuryprogrammer18thwasentsingleland.inventory

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
import android.widget.AdapterView
import android.widget.ArrayAdapter
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
import com.centuryprogrammer18thwasentsingleland.databinding.DashboardInventoryFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.*
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch
import java.lang.StringBuilder


class DashboardInventoryFrg : Fragment(), InvoiceInventoryFrgCallback, IOCallBack {
    private val TAG = DashboardInventoryFrg::class.java.simpleName

    lateinit var binding : DashboardInventoryFrgBinding

    private lateinit var masterKeyAlias: String
    private lateinit var sharedPrefs: SharedPreferences

    val viewModel: DashboardInventoryFrgVM by viewModels()

    private lateinit var adapter : InvoiceInventoryApt

    private lateinit var invoiceOrdersForPrint : MutableList<InvoiceOrder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEncrypSharedPrefs()
        viewModel.updateSharedPrefs(sharedPrefs)
        viewModel.getInventoryInvoiceOrders()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.dashboard_inventory_frg,container,false)

        binding.frg = this

        val spinnerItems = resources.getStringArray(R.array.dashboard_inventory_spinner_items)

        // spinner item space ref) https://stackoverflow.com/a/54526961/3151712
        binding.spnDashboardInventoryFrg.adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,spinnerItems)

        binding.spnDashboardInventoryFrg.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> {
                        Log.d(TAG,"***** created at, invoice id SELECTED *****")
                        adapter.onUpdatedHighLighted("createdAt")
                        viewModel.sortByCreatedAtInvoiceId()

                    }
                    1 -> {
                        Log.d(TAG,"***** customer name SELECTED *****")
                        adapter.onUpdatedHighLighted("customerName")
                        viewModel.sortCustomerName()
                    }
                    2 -> {
                        Log.d(TAG,"***** rack location SELECTED *****")
                        adapter.onUpdatedHighLighted("rackLocation")
                        viewModel.sortRackLocation()
                    }
                }
            }
        }


        adapter = InvoiceInventoryApt(viewModel,this)
        binding.rvDashboardInventoryFrg.adapter = adapter
        binding.rvDashboardInventoryFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        viewModel.inventoryInvoiceOrders.observe(viewLifecycleOwner, Observer {
            if(it != null && it.count() > 0){
                // there is inventory InvoiceOrder

                // save sorted inventoryInvoiceOrders for printing job
                invoiceOrdersForPrint= it

                // enabled print button
                binding.btnPrintDashboardInventoryFrg.isEnabled = true

                adapter.submitList(it)
                adapter.notifyDataSetChanged()
            }
        })

        return binding.root
    }

    override fun onInovoiceOrderClicked(invoiceOrder: InvoiceOrder) {
        Log.d(TAG,"***** onInovoiceOrderClicked *****")

        val bundle = bundleOf("customer" to invoiceOrder.customer!!, "invoiceOrder" to invoiceOrder)
        findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
    }

    fun onClickPrintBtn(){
        printing()
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
                Log.d(TAG,"***** invoiceOrdersForPrint.count : ${invoiceOrdersForPrint.count().toString()} *****")

                // makeTopTicket is extension func of Fragment which makes PrintingJob for top part of ticket
//                        val topTicket = makeTopTicket(sharedPrefs)

                // add part of ticket for dropoff
//                        topTicket.addAll(it)

                val printingJobs = mutableListOf<PrintingJob>()

                val title =
                    StringBuilder().
                    append(getCurrentDateString()).
                    append(" ").
                    append(getString(R.string.inventory)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())
                printingJobs.add(GenstarPrint.makeLargePrintingJob(title))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

                val label =
                    StringBuilder().
                    append(getString(R.string.invoice_id)).
                    append("---").
                    append(getString(R.string.created_at)).
                    append("---").
                    append(getString(R.string.due_date)).
                    append("\r\n").
                    append(getString(R.string.rack)).
                    append("---").
                    append(getString(R.string.balance)).
                    append("---").
                    append(getString(R.string.qty)).
                    append("\r\n").
                    append(getString(R.string.name)).
                    append("---").
                    append(getString(R.string.phone_number)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(label))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                for(item in invoiceOrdersForPrint){

                    val str =
                        StringBuilder().
                        append(item.id).
                        append("  ").
                        append(item.createdAt).
                        append("  ").
                        append(item.dueDateTime).
                        append("\r\n").
                        append(item.rackLocation?:getString(R.string.not_yet)).
                        append("  ").
                        append(makeTwoPointsDecialStringWithDollar(item.priceStatement!!["balance"]!!)).
                        append("  ").
                        append(item.qtyTable!!["dry"]!!+
                                item.qtyTable!!["wet"]!!+
                                item.qtyTable!!["alter"]!!+
                                item.qtyTable!!["press"]!!+
                                item.qtyTable!!["clean"]!!).
                        append("\r\n").
                        append(item.customer!!.lastName).
                        append(",").
                        append(item.customer!!.firstName).
                        append("  ").
                        append(item.customer!!.phoneNum).
                        append("\r\n").
                                toString()

                    printingJobs.add(GenstarPrint.makeSmallPrintingJob(str))
                    printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                }

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