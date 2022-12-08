package com.centuryprogrammer18thwasentsingleland.data

import java.io.Serializable

data class Customer(
//    @get:Exclude
    var id: String? = null,
    var firstName:String? = null,
    var lastName:String? = null,
    var phoneNum:String? = null,
    var email:String? = null,
    var streetAddress:String? = null,
    var city:String? = null,
    var state:String? = null,
    var zipcode:String? = null,
    var shirt:String? = null,
    var starch:String? = null,
    var note:String? = null
): Serializable
