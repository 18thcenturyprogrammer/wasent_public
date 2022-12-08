package com.centuryprogrammer18thwasentsingleland.release

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbManager
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.databinding.PrintReleaseFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class PrintReleaseFrg : Fragment(),IOCallBack {
    private val TAG = PrintReleaseFrg::class.java.simpleName

    private lateinit var binding: PrintReleaseFrgBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.print_release_frg,container,false)

        binding.frg = this

        return binding.root
    }

    fun printing(btn:View){
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

                val releaseContent =
                    when(btn.id){
                        R.id.btnSuedePrintReleaseFrg ->{
                            getString(R.string.suede_and_leather_release)
                        }
                        R.id.btnCurtainPrintReleaseFrg -> {
                            getString(R.string.curtains_and_draperies_release)
                        }
                        R.id.btnRugPrintReleaseFrg -> {
                            getString(R.string.area_rugs_release)
                        }
                        R.id.btnWetCleaningPrintReleaseFrg -> {
                            getString(R.string.wetcleaning_release)
                        }
                        else->{
                            ""
                        }
                    }

                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(releaseContent))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val bottomRelease =
                    StringBuilder().
                    append("______________________________________").
                    append("\r\n").
                    append(getString(R.string.signature)).
                    append("\r\n").
                    append("\r\n").
                    append("\r\n").
                    append("______________________________________").
                    append("\r\n").
                    append(getString(R.string.date)).
                    toString()


                printingJobs.add(GenstarPrint.makeSmallPrintingJob(bottomRelease))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

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