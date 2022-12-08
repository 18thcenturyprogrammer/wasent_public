package com.centuryprogrammer18thwasentsingleland

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.analytics.FirebaseAnalytics


class MainActivity : AppCompatActivity() {

    // for google firebase analytics
    // ref) udemy 121
    private var mFirebaseAnalytics: FirebaseAnalytics? = null

    var counter :Int =0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // for google firebase analytics
        // ref) udemy 121
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        setContentView(R.layout.activity_main)
    }

    private fun saveSharedPreferences(){
        val refSharedPreferences = getPreferences(Context.MODE_PRIVATE)
    }


}
