package com.centuryprogrammer18thwasentsingleland.data
import android.os.Parcelable
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.utils.GenstarPrint
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString
import kotlinx.android.parcel.Parcelize
import java.io.Serializable
import java.lang.StringBuilder
import java.text.SimpleDateFormat
import java.util.*


// i have to implement Parcelable, so i can pass InvoiceOrder array between fragment using navigation
// however, i don't need to implement required methods according to https://stackoverflow.com/a/59881330/3151712
// android parcelable navigation array pass parameter ref) https://stackoverflow.com/a/59881330/3151712
@Parcelize
data class InvoiceOrder(
//    @get:Exclude
    var id: Long? = null,
    var customer: Customer? = null,

    //"dry"
    //"wet"
    //"alter"
    //"press"
    //"clean"
    var qtyTable: MutableMap<String,Int>? = null,

    //  "subTotal"
    //  "discount"
    //  "tax"
    //  "env"
    //  "total"
    //  "prepaid"
    //  "balance"
    //  "dryPrice"
    //  "wetPrice"
    //  "alterPrice"
    //
    //   balance will be changed as customer makes payment.
    //   zero balance means customer paid fully for InvoiceOrder. however, customer make partial payment and pick up garments.
    //   that InvoiceOrder never be zero balance even though the transanction is cleared which is related the InvoiceOrder
    // prepaid
    // if balance is $10 and customer pay $5, balance will be $5
    //
    // fullpaid
    // if balance is $10 and customer pay $10, balance will be $0
    //
    // partialpaid
    // if balance is $10 and customer pay $5, balance will be $0
    var priceStatement : MutableMap<String,Double>? = null,

    var fabricares : MutableList<Fabricare>? = null,
    var createdAt: String? = null,
    var createdBy: String? = null,
    var createdAtTimestamp: Long? = null,

    var dueDateTime : String?=null,
    var dueTimestamp:Long? = null,


    // if pickedUpAt is null, not picked up yet
    var pickupId: Long? = null,
    var pickedUpAt: String?=null,
    var pickedUpBy: String?=null,
    var pickedAtTimestamp: Long? = null,


    // if rackLocation is null, not Racked up yet
    var rackLocation: String?=null,
    var rackedAt: String?=null,
    var rackedBy: String?=null,
    var rackedAtTimestamp: Long? = null,

    var note:String?= null,

    var voidAt: String?=null,
    var isVoid:Boolean? =null,
    var voidBy: String?=null,
    var voidAtTimestamp: Long? = null,

    // if orgInvoiceOrderId is null, not adjusted yet
    var orgInvoiceOrderId:Long?=null,

    // if this has team id value, that means this InvoiceOrder is old InvoiceOrder and customer adjusted , made new one
    var adjustedBy: String?=null,

    var tag : String? = null,
    var tagColor : String? = null
): Serializable,Parcelable{

    fun makePrintTicket():MutableList<PrintingJob>{
        val invoiceOrderId =
            StringBuilder().
            append(id.toString()).
            append("\r\n").
            toString()

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

        val shirt =
            StringBuilder().
            append(customer!!.starch).
            append(" , ").
            append(customer!!.shirt).
            append("\r\n").
            toString()

        val tag =
            StringBuilder().
            append(App.resourses!!.getString(R.string.tag)).
            append("  ").
            append(tag).
            append(" : ").
            append(tagColor).
            append("\r\n").
            toString()

        val drop =
            StringBuilder().
            append(App.resourses!!.getString(R.string.drop_off_time)).
            append(" : ").
            append(createdAt).
            append("\r\n").
            toString()

        val sdf = SimpleDateFormat("E")
        val date = Date(dueTimestamp!!)

        val due =
            StringBuilder().
            append(App.resourses!!.getString(R.string.due)).
            append(" : ").
            append(dueDateTime).
            append(" (").
            append(sdf.format(date)).
            append(")").
            append("\r\n").
            toString()


        val fullLine =
            StringBuilder().
            append("-------------------------------------------").
            append("\r\n").
            toString()

        val subTotal =
            StringBuilder().
            append(App.resourses!!.getString(R.string.subtotal)).
            append("   ").
            append(makeTwoPointsDecialString(priceStatement!!["subTotal"]!!)).
            append("\r\n").
            toString()

        val discount =
            StringBuilder().
            append(App.resourses!!.getString(R.string.discount)).
            append("   ").
            append("-").
            append(makeTwoPointsDecialString(priceStatement!!["discount"]!!)).
            append("\r\n").
            toString()

        val total =
            StringBuilder().
            append(App.resourses!!.getString(R.string.only_total)).
            append("   ").
            append(makeTwoPointsDecialString(priceStatement!!["total"]!!)).
            append("\r\n").
            toString()

        val preapaid =
            StringBuilder().
            append(App.resourses!!.getString(R.string.prepaid)).
            append("   ").
            append("-").
            append(makeTwoPointsDecialString(priceStatement!!["prepaid"]!!)).
            append("\r\n").
            toString()

        val balance =
            StringBuilder().
            append(App.resourses!!.getString(R.string.balance)).
            append("   ").
            append(makeTwoPointsDecialString(priceStatement!!["balance"]!!)).
            append("\r\n").
            toString()

        val totalPieces =
            StringBuilder().
            append(App.resourses!!.getString(R.string.only_total)).
            append(" : ").
            append(qtyTable!!["dry"]!!+qtyTable!!["wet"]!!+ qtyTable!!["alter"]!!+qtyTable!!["clean"]!!+qtyTable!!["press"]!!).
            append("\r\n").
            toString()

        val printingJobs = mutableListOf<PrintingJob>()

        printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())

        printingJobs.add(GenstarPrint.makeLargeBoldPrintingJob(invoiceOrderId))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeLargePrintingJob(name))
        printingJobs.add(GenstarPrint.makeSmallPrintingJob(phoneNumEmail))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())
        printingJobs.add(GenstarPrint.makeSmallPrintingJob(shirt))
        printingJobs.add(GenstarPrint.makeLargePrintingJob(tag))

        printingJobs.add(GenstarPrint.makeSmallPrintingJob(drop))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeLargeBoldPrintingJob(due))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeCenterAlignPrintingJob())

        printingJobs.add(GenstarPrint.makeSmallPrintingJob(fullLine))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

        for(item in fabricares!!){

            var process = ""

            //    {basename}_dry_clean_press
            //    {basename}_wet_clean_press
            //    {basename}_dry_clean
            //    {basename}_wet_clean
            //    {basename}_dry_press
            //    {basename}_wet_press
            //    {basename}_alter
            if(item.process.endsWith("_dry_clean_press")){
                process = "Dry C/P"
            }
            if(item.process.endsWith("_wet_clean_press")){
                process = "Wet C/P"
            }
            if(item.process.endsWith("_dry_clean")){
                process = "Dry C/O"
            }
            if(item.process.endsWith("_wet_clean")){
                process = "Wet C/O"
            }
            if(item.process.endsWith("_dry_press")){
                process = "Dry P/O"
            }
            if(item.process.endsWith("_wet_press")){
                process = "Wet P/O"
            }
            if(item.process.endsWith("_alter")){
                process = "A/O"
            }


            val firstLine =
                StringBuilder().
                append(item.numPiece).
                append("  ").
                append(item.name).
                append(" ").
                append(process).
                append("    ").
                append(makeTwoPointsDecialString(item.subTotalPrice)).
                append("\r\n").
                toString()

            printingJobs.add(GenstarPrint.makeSmallPrintingJob(firstLine))

            val secondLineBuilder =
                StringBuilder().
                append("    ")

            for(detail in item.fabricareDetails){
                secondLineBuilder.
                append("[").append(detail.name)

                if(detail.category == "alteration"){
                    secondLineBuilder.append(" ").append(makeTwoPointsDecialString(detail.price))
                }

                secondLineBuilder.append("] ")
            }

            val secondLine =
                secondLineBuilder.
                append("\r\n").
                toString()

            printingJobs.add(GenstarPrint.makeSmallPrintingJob(secondLine))



            if(item.comment.isNotEmpty()){
                val thirdLine =
                    StringBuilder().
                    append("    ").
                    append(item.comment).
                    append("\r\n").
                    toString()

                printingJobs.add(GenstarPrint.makeSmallPrintingJob(thirdLine))
            }

        }

        printingJobs.add(GenstarPrint.makeSmallPrintingJob(fullLine))
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeRightAlignPrintingJob())

        printingJobs.add(GenstarPrint.makeSmallPrintingJob(subTotal))
        printingJobs.add(GenstarPrint.makeSmallPrintingJob(discount))
        printingJobs.add(GenstarPrint.makeSmallPrintingJob(total))
        printingJobs.add(GenstarPrint.makeSmallPrintingJob(preapaid))
        printingJobs.add(GenstarPrint.makeLargePrintingJob(balance))

        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeLeftAlignPrintingJob())

        printingJobs.add(GenstarPrint.makeLargePrintingJob(totalPieces))

        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())
        printingJobs.add(GenstarPrint.makeFeedLinePrintingJob())

        printingJobs.add(GenstarPrint.makeFullCutPrintingJob())

        return printingJobs
    }
}


data class InvoiceOrderBrief(
    // same as InvoiceOrder id
//    @get:Exclude
    var invoiceOrderId: Long? = null,
    var createdAt: String? = null,
    var createdBy: String? = null,
    var createdAtTimestamp: Long? = null,

    var dueDateTime : String?=null,
    var dueTimestamp:Long? = null,

    // if pickedUpAt is null, not picked up yet
    var pickedUpAt: String?=null,
    var pickedUpBy: String?=null,
    var pickedAtTimestamp: Long? = null,


    // if rackLocation is null, not Racked up yet
    var rackLocation: String?=null,
    var rackedAt: String?=null,
    var rackedBy: String?=null,
    var rackedAtTimestamp: Long? = null,

    var note:String?= null,

    var isVoid:Boolean? =null,
    var voidAt: String?=null,
    var voidBy: String?=null,
    var voidAtTimestamp: Long? = null,


    var priceStatement : MutableMap<String,Double>? = null,
    var qtyTable: MutableMap<String,Int>? = null,


    // if orgInvoiceOrderId is null, not adjusted yet
    var orgInvoiceOrderId:Long?=null,

    // if this has team id value, that means this InvoiceOrder is old InvoiceOrder and customer adjusted , made new one
    var adjustedBy: String?=null,

    var customerId: String? = null,
    var firstName:String? = null,
    var lastName:String? = null,
    var phoneNum:String? = null,
    var email:String? = null
):Serializable

data class HoldedInvoiceOrder(
    var holdedAt :String? = null,
    var customer:Customer? = null,
    var invoiceOrder : InvoiceOrder? = null,
    var isEditing : Boolean? = null

):Serializable


// this is also made when user make new invoice
// this is for fast calculation qyt, amount for specific period.
data class InvoiceOrderSale(
    var invoiceOrderId: Long? = null,
    var createdAtTimestamp: Long? = null,
    var rackedAtTimestamp: Long? = null,
    var pickedAtTimestamp: Long? = null,

    //"dry"
    //"wet"
    //"alter"
    //"press"
    //"clean"
    var qtyTable: MutableMap<String,Int>? = null,

    //  "subTotal"
    //  "discount"
    //  "tax"
    //  "env"
    //  "total"
    //  "prepaid"
    //  "balance"
    //  "dryPrice"
    //  "wetPrice"
    //  "alterPrice"
    var priceStatement : MutableMap<String,Double>? = null
):Serializable