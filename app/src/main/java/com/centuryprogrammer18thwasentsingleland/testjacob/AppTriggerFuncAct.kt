package com.centuryprogrammer18thwasentsingleland.testjacob

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.ActivityAppTriggerFuncBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class AppTriggerFuncAct : AppCompatActivity() {
    private val TAG = AppTriggerFuncAct::class.java.simpleName

    lateinit var binding : ActivityAppTriggerFuncBinding
    lateinit var functions: FirebaseFunctions

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        functions = Firebase.functions

        binding = DataBindingUtil.setContentView(this,R.layout.activity_app_trigger_func)

        binding.btnGoTestAppTriggerFuncAct.setOnClickListener {
            triggerFirebaseFunc()
        }
    }

    fun triggerFirebaseFunc(){

        val data = hashMapOf(
            "ownerEmail" to "113aa@b.com",
            "cleanerName" to "dura cleaners"
        )
        functions.getHttpsCallable("checkStripeAccountStatus").call(data).continueWith { task ->
            // This continuation runs on either success or failure, but if the task
            // has failed then result will throw an Exception which will be
            // propagated down.

            Log.d(TAG,"***** returning value  *****")
            Log.d(TAG,"***** task.result?.data.toString() is : ${task.result?.data.toString()}  *****")

            binding.tvGoTestAppTriggerFuncAct.text = task.result?.data.toString()

            var resultStr = task.result?.data as HashMap<String,String>
            Log.d(TAG, "***** resultStr is : ${resultStr.getValue("accountId")} *****")
        }
    }
}