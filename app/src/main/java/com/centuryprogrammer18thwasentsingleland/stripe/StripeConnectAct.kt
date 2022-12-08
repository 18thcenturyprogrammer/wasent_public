package com.centuryprogrammer18thwasentsingleland.stripe

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.ActivityStripeConnectBinding

class StripeConnectAct : AppCompatActivity() {
    private val TAG = StripeConnectAct::class.java.simpleName

    lateinit var binding: ActivityStripeConnectBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this,R.layout.activity_stripe_connect)

        val navController = findNavController(R.id.navHostStripeConnect)

        navController.setGraph(navController.graph)
    }
}