package com.centuryprogrammer18thwasentsingleland.data

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable


// priceStatements --- mutableMapOf<Long,MutableMap<String,Double>>
// mutableMapOf<{InvoiceOrder.id}},PriceStatement>
// so I tried to use Long, but firebase not allowed Long for key, so i changed it to string
// when user pay unpaid balance which is from previous, Pickup also will be maid
data class Pickup(
    var id: Long? = null,

    // if this is null, it is creditpaid which customer paid unpaid amount before
    var invoiceOrderIds : MutableList<Long>? = null,
    var priceStatements : MutableMap<String,MutableMap<String,Double>>? = null,
    var balance : Double? = null,

    // if balance is $10, customer paid $6 and he put $4 as credit
    // then realPaidAmount is $6
    // if balance is $10, customer paid $15, then realPaidAmount is $15
    var realPaidAmount : Double? = null,

    // cash, check, card
    var payMethod : String? = null,
    var checkNum : String? = null,

    var pickUpAt : String? = null,
    var pickedUpBy: String? = null,
    var pickUpAtTimestamp: Long? = null

):Serializable{}

// when user pickup #2 invoice
// unpaidBalance is amount user didn't pay up to #1 invoice
// newPrice is amount #2 invoice
// paidAmount is amount user paid at this moment
// newBalance is come up after all calculation, and which will be pass to next picking up
data class PickupHistory(
    var pickupId: Long? = null,
    var customerId: String? = null,
    var invoiceOrderIds : MutableList<Long>? = null,

    // accumulated from past
    var unpaidBalance : Double? = null,

    // total amount of invoice order
    var newPrice: Double? = null,

    // amount which user paid now
    var paidAmount: Double? = null,

    // amount will be passed to next order
    var newBalance: Double?= null,

    var pickUpAt : String? = null,
    var pickedUpBy: String? = null,
    var pickUpAtTimestamp: Long? = null
):Serializable{}


data class Payment(
    var pickupId: Long? = null,
    var invoiceOrderId: Long? = null,

    // invoiceOrder amount which customer need to pay originally (very first price which customer pays nothing)
    var invoiceOrderBalance: Double? = null,

    var paymentMaidAt : String? = null,
    var paymentMaidBy: String? = null,
    var paymentMaidAtAtTimestamp: Long? = null,

    // accumulated paid amount so far
    // ex) user prepaid $10 before and user paid $5 this time
    // then totalPaidAmount is 15.00
    var totalPaidAmount : Double? = null,

    // will be saved data when full paid , partial paid
    var tax : Double? = null,
    // will be saved data when full paid , partial paid
    var env : Double? = null,
    // will be saved data when full paid , partial paid
    var discount : Double? = null,
    // will be saved data when full paid , partial paid
    var dryPrice : Double? = null,
    // will be saved data when full paid , partial paid
    var wetPrice : Double? = null,
    // will be saved data when full paid , partial paid
    var alterPrice : Double? = null,

// user paid full, making payment will happened only when user is picking up
    var fullPaidAmount : Double? = null,

    // prepaid process is consisted with two steps
    // one step is user making payment
    // another step is user picking up
    var prepaidAmount : Double? = null,

    var pastPrepaidAmount : Double? = null,


    // user paid partial , making payment will happened only when user is picking up
    var partialpaidAmount : Double? = null,

    // amount user didn't pay when user pick up, which is needed to pay later
    var creditAmount : Double? = null,

    // amount which customer payback later
    var creditpaidAmount : Double? = null

){}


////////////////////////////////////////////////////////////////////////////////////////////////////
// Payment is maid for each InvoiceOrder basically, but it can be maid for payment for unpaid amount
//
//

////////////////////////////////////////////////////////////////////////////////////////////////////
//
// example)
// invoice 1 $10
// user paid $10 when user is picking up
//
//            data class payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 1.0,
//
//                var invoiceOrderBalance: Long? = 10.0,
//
//                var paymentMaidAt : String? = {2000-11-22 09:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = 10.0,
//
//                // will be saved data when full paid , partial paid
//                var tax : Double? = {tax},
//                var env : Double? = {env},
//                var discount : Double? = {discount},
//                var dryPrice : Double? = {dryPrice},
//                var wetPrice : Double? = {wetPrice},
//                var alterPrice : Double? = {alterPrice},
//
//                var totalPaidAmount : Double? = 10.0,
//            ){}






////////////////////////////////////////////////////////////////////////////////////////////////////
// example)
// invoice 1 $10
// user prepaid $6 , and user paid $4 when user is picking up
//
//            data class payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 1,
//
//                var invoiceOrderBalance: Long? = 10.0,
//
//                var prepaidAmount : Double? = 6.0,
//                var pastPrepaidAmount : Double? = null,
//
//                var paymentMaidAt : String? = {2000-11-22 09:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var totalPaidAmount : Double? = 6.0
//            ){}
//
//
//            data class Payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 1,
//
//                var invoiceOrderBalance: Double? = 10.0,
//
//                var paymentMaidAt : String? = {2000-11-24 11:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = 4.0,
//
//                var prepaidAmount : Double? = null,
//
//                var pastPrepaidAmount : Double? = 6.0,
//
//                var partialpaidAmount : Double? = null,
//
//                // will be saved data when full paid , partial paid
//                var tax : Double? = {tax},
//                var env : Double? = {env},
//                var discount : Double? = {discount},
//                var dryPrice : Double? = {dryPrice},
//                var wetPrice : Double? = {wetPrice},
//                var alterPrice : Double? = {alterPrice},
//                var creditAmount : Double? = null,
//
//                var creditpaidAmount : Double? = null,
//
//                var totalPaidAmount : Double? = 10.0
//            ){}



////////////////////////////////////////////////////////////////////////////////////////////////////
// example)
// invoice 2 $10
// user prepaid $2 , and user prepaid $3 and user paid $5 when user is picking up
//
//            data class Payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 2,
//
//                var invoiceOrderBalance: Double? = 10.0,
//
//                var paymentMaidAt : String? = {2000-11-22 09:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = null,
//
//                var prepaidAmount : Double? = 2.0,
//
//                var pastPrepaidAmount : Double? = null,
//
//                var partialpaidAmount : Double? = null,
//
//                var creditAmount : Double? = null,
//
//                var creditpaidAmount : Double? = null,
//
//                var totalPaidAmount : Double? = 2.0
//            ){}
//
//            data class Payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 2,
//
//                var invoiceOrderBalance: Double? = 10.0,
//
//                var paymentMaidAt : String? = {2000-11-26 11:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = null,
//
//                var prepaidAmount : Double? = 3.0,
//
//                var pastPrepaidAmount : Double? = 2.0,
//
//                var partialpaidAmount : Double? = null,
//
//                var creditAmount : Double? = null,
//
//                var creditpaidAmount : Double? = null,
//
//                var totalPaidAmount : Double? = 5.0
//            ){}
//
//            data class Payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 2,
//
//                var invoiceOrderBalance: Double? = 10.0,
//
//                var paymentMaidAt : String? = {2000-12-28 14:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = 5.0,
//
//                var prepaidAmount : Double? = null,
//
//                var pastPrepaidAmount : Double? = 5.0,
//
//                var partialpaidAmount : Double? = null,
//
//                var creditAmount : Double? = null,
//
//                var creditpaidAmount : Double? = null,
//
//                // will be saved data when full paid , partial paid
//                var tax : Double? = {tax},
//                var env : Double? = {env},
//                var discount : Double? = {discount},
//                var dryPrice : Double? = {dryPrice},
//                var wetPrice : Double? = {wetPrice},
//                var alterPrice : Double? = {alterPrice},
//
//                var totalPaidAmount : Double? = 10.0
//            ){}

////////////////////////////////////////////////////////////////////////////////////////////////////
// example)
// invoice 100003 $10
// user prepaid $2 , and user paid $3 when user is picking up, $5 will be passed to next order
//
//            data class Payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 100003,
//
//                var invoiceOrderBalance: Double? = 10.0,
//
//                var paymentMaidAt : String? = {2000-11-22 09:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = null,
//
//                var prepaidAmount : Double? = 2.0,
//
//                var pastPrepaidAmount : Double? = null,
//
//                var partialpaidAmount : Double? = null,
//
//                var creditAmount : Double? = null,
//
//                var creditpaidAmount : Double? = null,
//
//                var totalPaidAmount : Double? = 2.0
//            ){}
//
//
//            data class Payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 100003,
//
//                var invoiceOrderBalance: Double? = 10.0,
//
//                var paymentMaidAt : String? = {2000-11-30 16:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = null,
//
//                var prepaidAmount : Double? = null,
//
//                var pastPrepaidAmount : Double? = 2.0,
//
//                var partialpaidAmount : Double? = 3.0,
//
//                var creditAmount : Double? = 5.0,
//
//                var creditpaidAmount : Double? = null,
//
//                // will be saved data when full paid , partial paid
//                var tax : Double? = {tax},
//                var env : Double? = {env},
//                var discount : Double? = {discount},
//                var dryPrice : Double? = {dryPrice},
//                var wetPrice : Double? = {wetPrice},
//                var alterPrice : Double? = {alterPrice},
//
//                var totalPaidAmount : Double? = 10.0
//            ){}

////////////////////////////////////////////////////////////////////////////////////////////////////
// Payment is maid for each InvoiceOrder basically, but it can be maid for payment for unpaid amount
// example)
// invoice 100004 $10 , customer didn't pay $7
// user prepaid $2 , and $7 which customer didn't pay
//            data class Payment(
//                var pickupId: Long? = {id number},
//                var invoiceOrderId: Long? = 100004,
//
//                var invoiceOrderBalance: Double? = 10.0,
//
//                var paymentMaidAt : String? = {2000-11-30 16:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = null,
//
//                var prepaidAmount : Double? = 2.0,
//
//                var pastPrepaidAmount : Double? = null,
//
//                var partialpaidAmount : Double? = null,
//
//                var creditAmount : Double? = null,
//
//                var creditpaidAmount : Double? = null,
//
//                var totalPaidAmount : Double? = 2.0
//            ){}
//
//            data class Payment(
//                var pickupId: Long? = null,
//                var invoiceOrderId: Long? = null,
//
//                var invoiceOrderBalance: Double? = null,
//
//                var paymentMaidAt : String? = {2000-11-30 16:13:53},
//                var paymentMaidBy: String? = {team_id},
//                var paymentMaidAtAtTimestamp: Long? = {timestamp},
//
//                var fullPaidAmount : Double? = null,
//
//                var prepaidAmount : Double? = null,
//
//                var pastPrepaidAmount : Double? = null,
//
//                var partialpaidAmount : Double? = null,
//
//                var creditAmount : Double? = null,
//
//                var creditpaidAmount : Double? = 7.0,
//
//                var totalPaidAmount : Double? = 7.0
//            ){}




// i have to implement Parcelable, so i can pass InvoiceOrder array between fragment using navigation
// however, i don't need to implement required methods according to https://stackoverflow.com/a/59881330/3151712
// android parcelable navigation array pass parameter ref) https://stackoverflow.com/a/59881330/3151712
@Parcelize
data class InvoiceWithPayment(
    var invoiceOrderId: Long? = null,
    var invoiceOrder: InvoiceOrder? = null,
    var isPaid: Boolean? = null,
    var typePayment: String? = null,

    // amount which user decided to pay
    var amount: Double? = null
):Serializable, Parcelable {}