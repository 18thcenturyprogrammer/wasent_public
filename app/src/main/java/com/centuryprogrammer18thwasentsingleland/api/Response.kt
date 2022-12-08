package com.centuryprogrammer18thwasentsingleland.api

import com.google.gson.annotations.SerializedName

// this is what i got form server
//{"object":"account_link","created":1607150758,"expires_at":1607151058,"url":"https://connect.stripe.com/express/onboarding/3LjToisBfoJb"}

// using retrofit ref) https://youtu.be/xIHz0C5qt-Q

data class StripeCreateAccountResponse(

    // when i communicate with retrofit i can change serialized name which will be json key
    // retrofit different name serializedName ref) https://stackoverflow.com/a/52928517
    @SerializedName("object")
    var type : String,
    var created : Int,
    var expires_at : Int,
    var url : String
){}
