package com.centuryprogrammer18thwasentsingleland.data

data class CleanerInitSetting(
    var cleanerName : String? = null,
    var cleanerPhoneNum : String? = null,
    var cleanerStreetAddress : String? = null,
    var cleanerCity : String? = null,
    var cleanerState : String? = null,
    var cleanerZipcode : String? = null,
    var managerId : String? = null,
    var managerPassword : String? = null,
    var tax : String? = null,
    var envRate : String? = null,
    var envAmount : String? = null,
    var ownerUid: String? = null,
    var ownerEmail : String? = null,
    var createdAt:String? = null,
    var createdTimestamp: Long? = null
    ){}

data class CleanerStripeAccount(
    var isStripeVerified : Boolean? = null,
    var verifiedStripeAccount : String? = null,
    var feeRate : Float?= null,
    var verifiedAt:String? = null,
    var verifiedTimestamp: Long? = null
){}




//accountType
//isStripeVerified
//stripeAccount
//isValid
//blockedReason
//feeRate
//phoneNum
//streetAddress
//city
//state
//zipcode
