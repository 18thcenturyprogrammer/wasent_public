package com.centuryprogrammer18thwasentsingleland.data

import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*


data class Dropoff(
    var customer: Customer? = null,
    var qtyTable: MutableMap<String,Int>? = null,

    var createdAt: String? = null,
    var createdBy: String? = null,
    var createdAtTimestamp: Long? = null,

    var dueDateTime : String?=null,
    var dueTimestamp:Long? = null,

    var note:String?= null
){
    fun makePrintTicket():MutableList<PrintingJob>{
        val name =
            StringBuilder().
            append(customer!!.firstName).
            append(" ").
            append(customer!!.lastName).
            append("\r\n").
            toString()

        val phoneNumEmail =
            StringBuilder().
            append(customer!!.phoneNum).
            append("  ").
            append(customer!!.email).
            append("\r\n").
            toString()

        val created =
            StringBuilder().
            append(App.resourses!!.getString(R.string.drop_off_time)).
            append(" : ").
            append(createdAt).
            append("\r\n").
            toString()

        val sdf = SimpleDateFormat("yyyy-MM-dd(E)")
        val date = Date(dueTimestamp!!)

        val due =
            StringBuilder().
            append(App.resourses!!.
            getString(R.string.due)).
            append(" : ").
            append(sdf.format(date)).
            append("\r\n").
            toString()

        val qty =
            StringBuilder().
            append(App.resourses!!.getString(R.string.only_total)).
            append(" : ").
            append(qtyTable!!["dry"]!!+qtyTable!!["wet"]!!+ qtyTable!!["alter"]!!+qtyTable!!["clean_only"]!!+qtyTable!!["press_only"]!!+qtyTable!!["household"]!!+qtyTable!!["redo"]!!+qtyTable!!["ext"]!!).
            append(" ").
            append(App.resourses!!.getString(R.string.pieces)).
            append("\r\n").
            toString()

        val printingJobs = mutableListOf<PrintingJob>()


        printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

        printingJobs.add(GenstarPrint.makeLargeBoldPrintingJob(name))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeSmallBoldPrintingJob(phoneNumEmail))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeSmallPrintingJob(created))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeLargePrintingJob(due))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeSmallPrintingJob(qty))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeFullCutPrintingJob())

        return printingJobs
    }
}