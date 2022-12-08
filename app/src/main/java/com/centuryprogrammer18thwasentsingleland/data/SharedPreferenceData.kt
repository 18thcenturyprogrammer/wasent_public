package com.centuryprogrammer18thwasentsingleland.data

import java.io.Serializable

data class SharedPreferenceData(
    var cleaner_name: String,
    var owner_email: String,
    var cleaner_phone_num : String,
    var cleaner_street_address : String,
    var cleaner_city : String,
    var cleaner_state : String,
    var cleaner_zipcode : String
) : Serializable {}



