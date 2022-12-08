package com.centuryprogrammer18thwasentsingleland.stripe

import android.content.SharedPreferences
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.api.WasentServerInterface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.logging.SocketHandler
import kotlin.coroutines.CoroutineContext

class StripeAccountConnectFrgVM : ViewModel() {
    private val TAG = StripeAccountConnectFrgVM::class.java.simpleName

    lateinit var sharedPrefs: SharedPreferences

    // call firebase functions from app ref) https://firebase.google.com/docs/functions/callable#kotlin+ktx
    lateinit var functions : FirebaseFunctions

    lateinit var ownerEmail : String
    lateinit var cleanerName : String

    private val  _stripeAccountId = MutableLiveData<String>()
    val stripeAccountId: LiveData<String>
        get() = _stripeAccountId

    private val _msgToFrg= MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _stripeUrl = MutableLiveData<String>()
    val stripeUrl : LiveData<String>
        get() = _stripeUrl

    private val _showBtnStripeConnect = MutableLiveData<Boolean>()
    val showBtnStripeConnect : LiveData<Boolean>
        get() = _showBtnStripeConnect

    private val _showUpdateBtnStripe = MutableLiveData<Boolean>()
    val showUpdateBtnStripe : LiveData<Boolean>
        get() = _showUpdateBtnStripe

    fun initFirebaseFunctions(){
        functions = Firebase.functions
    }

    fun updateSharedPrefs(receivedSSharedPrefs:SharedPreferences){
        sharedPrefs = receivedSSharedPrefs
    }

    fun initOwnerEmailandCleanerName(){
        ownerEmail = FirebaseAuth.getInstance().currentUser!!.email!!
        cleanerName = sharedPrefs.getString("cleaner_name","")
    }

    fun onOnClickStripeConnect(){
        Log.d(TAG,"----- onOnClickStripeConnect -----")

        // disable button,so user cannot click again
        _showBtnStripeConnect.value = false

        getCreateAccountLink()
    }

    fun onOnClickUpdateStripe(){
        Log.d(TAG,"----- onOnClickUpdateStripeConnect -----")

        // disable button,so user cannot click again
        _showUpdateBtnStripe.value = false

        val data = hashMapOf(
            "cleanerName" to cleanerName
        )

        // call firebase functions from app ref) https://firebase.google.com/docs/functions/callable#kotlin+ktx
        functions.getHttpsCallable("checkStripeAccountStatus").call(data).continueWith { task ->
            // This continuation runs on either success or failure, but if the task
            // has failed then result will throw an Exception which will be
            // propagated down.

            Log.d(TAG,"***** returning value  *****")
            Log.d(TAG,"***** task.result?.data.toString() is : ${task.result?.data.toString()}  *****")

            var resultStr = task.result?.data as HashMap<String,String>

            Log.d(TAG, "***** resultStr is : ${resultStr.getValue("accountId")} *****")

            val updatedAccountId = resultStr.getValue("accountId")

            _stripeAccountId.value = updatedAccountId

            sharedPrefs.edit().putString("stripe_account", updatedAccountId).apply()

            Log.d(TAG,"----- sharedPrefs  value updated saved -----")

            Log.d(TAG,"----- show buttons -----")
            _showUpdateBtnStripe.value = true
        }
    }


    // using retrofit ref) https://youtu.be/xIHz0C5qt-Q
    // ref)https://youtu.be/S-10lLA0nbk

    // get stripe workthrough url from my wasent server
    fun getCreateAccountLink(){
        val retrofit = Retrofit.Builder()
            .baseUrl(App.wasentServerUrl)
            .addConverterFactory(GsonConverterFactory.create(GsonBuilder().create()))
            .build()
            .create(WasentServerInterface::class.java)

        viewModelScope.launch(Dispatchers.Default){
            val response = retrofit.getCreateAccount(hashMapOf("ownerEmail" to ownerEmail, "cleanerName" to cleanerName))

            if(response.isSuccessful){
                Log.d(TAG,"----- create account retrofit call SUCCESS -----")

                // ref) https://stripe.com/docs/connect/standard-accounts
                // url for user will click and  go
                val url = response.body()!!.url

                Log.d(TAG,"----- returned url from server is | ${url} -----")

                withContext(Dispatchers.Main){
                    _showBtnStripeConnect.value = true
                    _stripeUrl.value = url
                }
            }else{
                Log.d(TAG,"----- create account retrofit call FAILED -----")

                withContext(Dispatchers.Main){
                    _showBtnStripeConnect.value = true
                    _msgToFrg.value = App.resourses!!.getString(R.string.problem_init_connect_stripe)
                }
            }
        }
    }

    fun changeStripeAccountId(accountId: String){
        _stripeAccountId.value = accountId
    }

}