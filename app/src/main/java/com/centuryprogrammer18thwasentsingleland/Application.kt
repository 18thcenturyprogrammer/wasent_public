package com.centuryprogrammer18thwasentsingleland

import android.app.Application
import android.content.res.Resources
import com.stripe.android.PaymentConfiguration


// i couldn't use resource string ,so i changed Application class
// ref) https://stackoverflow.com/a/51279662/3151712

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
        resourses = resources

        // save stripe publishable key
        // stripe publishable key application ref) https://stripe.com/docs/connect/collect-then-transfer-guide#android-setup-client-side
        // need update
        PaymentConfiguration.init(
            applicationContext,
            "pk_test_51HsFV3EpgfvwVVPooLcsLcx1bfM0t8vcG2aIw7bwuTyNQJwWKrb1jBfPoTw2Y11zmyxqeGmyNUaWQUTALZ1OnTaJ00ayAWwcil"
        )


    }

    companion object {
        var instance: App? = null
            private set
        var resourses: Resources? = null
            private set

        val wasentServerUrl= "https://us-central1-wasent-6c0e1.cloudfunctions.net/wasent_web/"

    }
}