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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Dropoff
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.databinding.CategorizedPaymentsFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.DropOffSalesFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class DropOffSalesFrg : Fragment(),IOCallBack {
    private val TAG = DropOffSalesFrg::class.java.simpleName

    private lateinit var args : DropOffSalesFrgArgs

    private lateinit var binding : DropOffSalesFrgBinding

    private lateinit var masterKeyAlias : String
    private lateinit var sharedPrefs : SharedPreferences

    private val viewModel: DropOffSalesFrgVM by viewModels()

    private lateinit var startDate: MutableList<Int>
    private lateinit var endDate: MutableList<Int>
    private lateinit var contentsForPrint: MutableList<Dropoff>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = DropOffSalesFrgArgs.fromBundle(requireArguments())
        startDate = args.startDate.toList().toMutableList()
        endDate = args.endDate.toList().toMutableList()

        initEncrypSharedPrefs()

        viewModel.initSharedPrefs(sharedPrefs)
        viewModel.initVars(startDate, endDate)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as SalesAct).setActionBarTitle(getString(R.string.dropoff))

        binding = DataBindingUtil.inflate(inflater, R.layout.drop_off_sales_frg,container,false)

        binding.frg = this

        val adapter = DropoffApt()
        binding.rvDropoffSalesFrg.adapter = adapter
        binding.rvDropoffSalesFrg.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewModel.getDropoffsBetween()

        viewModel.displayDropoffs.observe(viewLifecycleOwner, Observer {

            if(it.count() > 0 ){
                binding.btnPrintDropoffSalesFrg.isEnabled = true
                contentsForPrint = it
            }

            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        return binding.root
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
        Log.d(TAG,"***** DropOffSalesFrg printing *****")
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
                    append(getString(R.string.dropoff)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())
                printingJobs.add(GenstarPrint.makeLargePrintingJob(title))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

                val label =
                    StringBuilder().
                    append(getString(R.string.name)).
                    append("-----").
                    append(getString(R.string.phone_number)).
                    append("-----").
                    append(getString(R.string.items)).
                    append("\r\n").
                    append(getString(R.string.created_at)).
                    append("---").
                    append(getString(R.string.by)).
                    append("---").
                    append(getString(R.string.due_date)).
                    append("\r\n").
                    toString()

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

                    if(item.qtyTable!!["dry"]!! >0 ){
                        itemsStrBuilder.append("Dry[ "+ item.qtyTable!!["dry"]!!.toString()+" ]")
                    }

                    if(item.qtyTable!!["wet"]!! >0 ){
                        itemsStrBuilder.append("Wet[ "+ item.qtyTable!!["wet"]!!.toString()+" ]")
                    }

                    if(item.qtyTable!!["alter"]!! >0 ){
                        itemsStrBuilder.append("Alter[ "+ item.qtyTable!!["alter"]!!.toString()+" ]")
                    }

                    if(item.qtyTable!!["household"]!! >0 ){
                        itemsStrBuilder.append("Household[ "+ item.qtyTable!!["household"]!!.toString()+" ]")
                    }

                    if(item.qtyTable!!["clean_only"]!! >0 ){
                        itemsStrBuilder.append("Clean only[ "+ item.qtyTable!!["clean_only"]!!.toString()+" ]")
                    }

                    if(item.qtyTable!!["press_only"]!! >0 ){
                        itemsStrBuilder.append("Press only[ "+ item.qtyTable!!["press_only"]!!.toString()+" ]")
                    }

                    if(item.qtyTable!!["ext"]!! >0 ){
                        itemsStrBuilder.append("Ext[ "+ item.qtyTable!!["ext"]!!.toString()+" ]")
                    }

                    if(item.qtyTable!!["redo"]!! >0 ){
                        itemsStrBuilder.append("Redo[ "+ item.qtyTable!!["redo"]!!.toString()+" ]")
                    }

                    itemsStrBuilder.append("   ")


                    val firstLine =
                        StringBuilder().
                        append(nameAndPhone).
                        append("   ").
                        append(itemsStrBuilder.toString()).
                        append("   ").
                        append("\r\n").
                        toString()

                    printingJobs.add(GenstarPrint.makeSmallPrintingJob(firstLine))

                    val secondLine =
                        StringBuilder().
                        append(item.createdAt).
                        append("   ").
                        append(item.createdBy).
                        append("   ").
                        append(item.dueDateTime).
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