package com.centuryprogrammer18thwasentsingleland.sales

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
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
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.PrintingJob
import com.centuryprogrammer18thwasentsingleland.databinding.PickupSalesFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.android.synthetic.main.pickup_sales_frg.*
import kotlinx.android.synthetic.main.pickup_sales_frg.view.*
import kotlinx.coroutines.launch
import java.lang.StringBuilder


class PickupSalesFrg : Fragment(),IOCallBack {
    private val TAG = PickupSalesFrg::class.java.simpleName

    private lateinit var binding : PickupSalesFrgBinding

    private lateinit var actViewModel : SalesActVM
    private val viewModel: PickupSalesFrgVM by viewModels()

    private var startDate :MutableMap<String,Int>? = null
    private var endDate :MutableMap<String,Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actViewModel = (activity as SalesAct).viewModel

        actViewModel.payments.observe(this, Observer {
            viewModel.updatePayments(it)
        })

        actViewModel.startDate.observe(this, Observer {
            if(it.count() != 0){
                startDate = it
            }
        })

        actViewModel.entDate.observe(this, Observer {
            if(it.count() != 0){
                endDate = it
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.pickup_sales_frg,container,false)

        binding.frg = this

        viewModel.isUpdatedPaymentsVars.observe(viewLifecycleOwner, Observer {
            if(it){
                with(binding){

                    if(viewModel.totalNumPayments > 0 ){
                        // there is something to print

                        btnPrintPickupSales.isEnabled = true
                    }

                    tvFullpaidNumPickupSales.text = viewModel.fullpaidNumPayments.toString()
                    tvFullpaidAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.fullpaidAmountPayments)

                    tvPrepaidNumPickupSales.text = viewModel.prepaidNumPayments.toString()
                    tvPrepaidAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.prepaidAmountPayments)

                    tvPrepaidPickupNumPickupSales.text = viewModel.prepaidPickupNumPayments.toString()
                    tvPrepaidPickupAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.prepaidPickupAmountPayments)

                    tvPartialpaidNumPickupSales.text = viewModel.partialpaidNumPayments.toString()
                    tvPartialpaidAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.partialpaidAmountPayments)

                    tvCreditSaleNumPickupSales.text = viewModel.creditNumPayments.toString()
                    tvCreditSaleAmountickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.creditAmountPayments)

                    tvCreditPaybackNumPickupSales.text = viewModel.creditPaybackNumPayments.toString()
                    tvCreditPaybackAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.creditPaybackAmountPayments)

                    tvTotalNumPaymentsPickupSales.text = viewModel.totalNumPayments.toString()
                    tvTotalAmountPaymentsPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.totalAmountPayments)

                    tvTaxAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.tax)
                    tvEnvAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.env)
                    tvDiscountAmountPickupSales.text = "- "+makeTwoPointsDecialStringWithDollar(viewModel.discount)
                    tvDrycleanAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.dryPrice)
                    tvWetcleanAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.wetPrice)
                    tvAlterationAmountPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.alterPrice)

                    tvTotalSecondPickupSales.text = makeTwoPointsDecialStringWithDollar(viewModel.totalSecondAmountPayments)
                }
            }
        })

        return binding.root
    }

    fun printing(){
        Log.d(TAG,"***** PickupSalesFrg printing *****")
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
                    append(getString(R.string.pickup_sales)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())
                printingJobs.add(GenstarPrint.makeLargePrintingJob(title))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
                printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

                val label =
                    StringBuilder().
                    append("-----").
                    append(getString(R.string.num_payments)).
                    append("-----").
                    append(getString(R.string.sales)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(label))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val fullpaid =
                    StringBuilder().
                    append(getString(R.string.fullpaid)).
                    append(" :  ").
                    append(viewModel.fullpaidNumPayments).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.fullpaidAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(fullpaid))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val prepaidPaid =
                    StringBuilder().
                    append(getString(R.string.prepaid_paid)).
                    append(" :  ").
                    append(viewModel.prepaidNumPayments).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.prepaidAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(prepaidPaid))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val prepaidPickup =
                    StringBuilder().
                    append(getString(R.string.prepaid_pickup)).
                    append(" :  ").
                    append(viewModel.prepaidPickupNumPayments).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.prepaidPickupAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(prepaidPickup))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val partialPaid =
                    StringBuilder().
                    append(getString(R.string.partialpaid)).
                    append(" :  ").
                    append(viewModel.partialpaidNumPayments).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.partialpaidAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(partialPaid))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val creditPickup =
                    StringBuilder().
                    append(getString(R.string.credit_pickup)).
                    append(" :  ").
                    append(viewModel.creditNumPayments).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.creditAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(creditPickup))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val creditPayback =
                    StringBuilder().
                    append(getString(R.string.credit_payback)).
                    append(" :  ").
                    append(viewModel.creditPaybackNumPayments).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.creditPaybackAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(creditPayback))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val total =
                    StringBuilder().
                    append(getString(R.string.only_total)).
                    append(" :  ").
                    append(viewModel.totalNumPayments).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.totalAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(total))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val label2 =
                    StringBuilder().
                    append("-----").
                    append(getString(R.string.sales)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(label2))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val tax =
                    StringBuilder().
                    append(getString(R.string.tax)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.tax)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(tax))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())


                val env =
                    StringBuilder().
                    append(getString(R.string.env)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.env)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(env))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val discount =
                    StringBuilder().
                    append(getString(R.string.discount)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.discount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(discount))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val dryclean =
                    StringBuilder().
                    append(getString(R.string.dryclean)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.dryPrice)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(dryclean))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val wetclean =
                    StringBuilder().
                    append(getString(R.string.wetclean)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.wetPrice)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(wetclean))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val alteration =
                    StringBuilder().
                    append(getString(R.string.alteration)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.alterPrice)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(alteration))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val total2 =
                    StringBuilder().
                    append(getString(R.string.only_total)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.totalSecondAmountPayments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(total2))
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