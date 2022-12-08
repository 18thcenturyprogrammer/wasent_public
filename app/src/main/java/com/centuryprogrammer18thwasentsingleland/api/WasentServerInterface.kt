package com.centuryprogrammer18thwasentsingleland.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface WasentServerInterface {

    // coroutine with retrofit receiving data as Response
    // ref) https://youtu.be/S-10lLA0nbk?t=237

//    @GET("stripe_create_account")
//    suspend fun getCreateAccount(): Response<StripeCreateAccountResponse>


    // post json body ref) https://stackoverflow.com/a/28712275
    // normally json post use class, but i can just HashMap instead
    @POST("stripe_create_account")
    suspend fun getCreateAccount(@Body body: HashMap<String, String>): Response<StripeCreateAccountResponse>

}