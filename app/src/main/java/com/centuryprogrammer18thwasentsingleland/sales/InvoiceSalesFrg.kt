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
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceSalesFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentDateString
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.lvrenyang.io.Pos
import com.lvrenyang.io.USBPrinting
import com.lvrenyang.io.base.IOCallBack
import kotlinx.coroutines.launch
import java.lang.StringBuilder

class InvoiceSalesFrg : Fragment(),IOCallBack {
    private val TAG = InvoiceSalesFrg::class.java.simpleName

    private lateinit var binding : InvoiceSalesFrgBinding

    private lateinit var actViewModel : SalesActVM
    private val viewModel: InvoiceSalesFrgVM by viewModels()

    private var startDate :MutableMap<String,Int>? = null
    private var endDate :MutableMap<String,Int>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actViewModel = (activity as SalesAct).viewModel

        actViewModel.invoiceOrderBriefs.observe(this, Observer {
            viewModel.updateInvoiceOrderBriefs(it)
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

        viewModel.isUpdatedInvoiceVars.observe(this, Observer {
            if(it){
                // vars updated , update UI

                with(binding){

                    if(viewModel.totalNumInvoice !=0 || viewModel.totalNumQty !=0){
                        // there is something to print

                        btnPrintInvoiceSalesFrg.isEnabled = true
                    }
                    btnPrintInvoiceSalesFrg.isEnabled = true

                    tvDrycleanPieces.text = viewModel.dryQty.toString()
                    tvDrycleanAmount.text = makeTwoPointsDecialStringWithDollar(viewModel.dryAmount)

                    tvWetcleanPieces.text = viewModel.wetQty.toString()
                    tvWetcleanAmount.text = makeTwoPointsDecialStringWithDollar(viewModel.wetAmount)

                    tvAlterationPieces.text = viewModel.alterQty.toString()
                    tvAlterationAmount.text = makeTwoPointsDecialStringWithDollar(viewModel.alterAmount)

                    tvPressonlyPieces.text = viewModel.pressOnlyQty.toString()

                    tvCleanonlyPieces.text = viewModel.cleanOnlyQty.toString()

                    tvGarmentsTotalPieces.text = viewModel.totalNumQty.toString()
                    tvGarmentsTotalSales.text = makeTwoPointsDecialStringWithDollar(viewModel.totalAmountGarments)

                    tvDiscountAmount.text = "-"+makeTwoPointsDecialStringWithDollar(viewModel.discountAmount)
                    tvTaxAmount.text = makeTwoPointsDecialStringWithDollar(viewModel.taxAmount)
                    tvEnvAmount.text = makeTwoPointsDecialStringWithDollar(viewModel.envAmount)

                    tvTotalInvoices.text = viewModel.totalNumInvoice.toString()

                    tvTotalAmount.text = makeTwoPointsDecialStringWithDollar(viewModel.totalAmountInvoice)
                }
            }
        })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.invoice_sales_frg,container, false)

        binding.frg = this

        return binding.root
    }

    fun printing(){
        Log.d(TAG,"***** InvoiceSalesFrg printing *****")
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
                    append(getString(R.string.invoices_sales)).
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
                    append(getString(R.string.garment_pieces)).
                    append("-----").
                    append(getString(R.string.sales)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(label))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val dryclean =
                    StringBuilder().
                    append(getString(R.string.dryclean)).
                    append(" :  ").
                    append(viewModel.dryQty).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.dryAmount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(dryclean))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val wetclean =
                    StringBuilder().
                    append(getString(R.string.wetclean)).
                    append(" :  ").
                    append(viewModel.wetQty).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.wetAmount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(wetclean))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val alteration =
                    StringBuilder().
                    append(getString(R.string.alteration)).
                    append(" :  ").
                    append(viewModel.alterQty).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.alterAmount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(alteration))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val pressOnly =
                    StringBuilder().
                    append(getString(R.string.press_only)).
                    append(" :  ").
                    append(viewModel.pressOnlyQty).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(pressOnly))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())


                val cleanOnly =
                    StringBuilder().
                    append(getString(R.string.clean_only)).
                    append(" :  ").
                    append(viewModel.cleanOnlyQty).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(cleanOnly))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val total =
                    StringBuilder().
                    append(getString(R.string.only_total)).
                    append(" :  ").
                    append(viewModel.totalNumQty).
                    append("   ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.totalAmountGarments)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(total))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val label2 =
                    StringBuilder().
                    append("-----").
                    append(getString(R.string.amount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(label2))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val discount =
                    StringBuilder().
                    append(getString(R.string.discount)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.discountAmount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(discount))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val tax =
                    StringBuilder().
                    append(getString(R.string.tax)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.taxAmount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(tax))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val env =
                    StringBuilder().
                    append(getString(R.string.env)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.envAmount)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(env))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val totalInvoices =
                    StringBuilder().
                    append(getString(R.string.total_invoices)).
                    append(" :  ").
                    append(viewModel.totalNumInvoice).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(totalInvoices))
                printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

                val totalSales =
                    StringBuilder().
                    append(getString(R.string.total_sales)).
                    append(" :  ").
                    append(makeTwoPointsDecialStringWithDollar(viewModel.totalAmountInvoice)).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(totalSales))
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