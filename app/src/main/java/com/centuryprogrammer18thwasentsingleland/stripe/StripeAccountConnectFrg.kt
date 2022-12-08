package com.centuryprogrammer18thwasentsingleland.stripe

import android.content.SharedPreferences
import android.net.Uri
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.CreateStripeAccountDialogBinding
import com.centuryprogrammer18thwasentsingleland.databinding.StripeAccountConnectFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase

class StripeAccountConnectFrg : Fragment() {
    private val TAG = StripeAccountConnectFrg::class.java.simpleName

    lateinit var binding : StripeAccountConnectFrgBinding

    //variables will be initialised in the onCreate function
    lateinit var masterKeyAlias: String
    private lateinit var sharedPrefs : SharedPreferences

    private val viewModel:StripeAccountConnectFrgVM by viewModels()

    lateinit var stripeAlert : AlertDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        
        // shared preference encryption ref) https://garageprojects.tech/encryptedsharedpreferences-example/
        // ref) https://medium.com/@Naibeck/android-security-encryptedsharedpreferences-ea239e717e5f
        // ref) https://proandroiddev.com/encrypted-preferences-in-android-af57a89af7c8
        //(1) create or retrieve masterkey from Android keystore
        //masterkey is used to encrypt data encryption keys
        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        //(2) Get instance of EncryptedSharedPreferences class
        // as part of the params we pass the storage name, reference to
        // masterKey, context and the encryption schemes used to
        // encrypt SharedPreferences keys and values respectively.
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        binding = DataBindingUtil.inflate(inflater,R.layout.stripe_account_connect_frg,container,false)

        binding.viewModel = viewModel
        binding.frg = this

        val connectedStripeAccount = sharedPrefs.getString("stripe_account","")

        if(connectedStripeAccount.isNotEmpty()){
            // there is connected stripe account
            Log.d(TAG, "***** connectedStripeAccount value is : ${connectedStripeAccount} *****")

            viewModel.changeStripeAccountId(connectedStripeAccount)
        }

        viewModel.updateSharedPrefs(sharedPrefs)
        viewModel.initOwnerEmailandCleanerName()
        viewModel.initFirebaseFunctions()


        viewModel.stripeUrl.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                // user got the stripe work through url

                val builder = AlertDialog.Builder(requireContext())

                val binding : CreateStripeAccountDialogBinding = DataBindingUtil.inflate(layoutInflater,R.layout.create_stripe_account_dialog,null,false)

                binding.frg = this
                binding.url = it

                builder.setTitle(getString(R.string.connect_with_stripe))
                builder.setMessage(getString(R.string.you_will_go_through_stripe))
                builder.setView(binding.root)

                stripeAlert = builder.create()
                stripeAlert.show()
            }
        })

        viewModel.showUpdateBtnStripe.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "***** showUpdateBtnStripeConnect value changed as ${it} *****")
            binding.btnUpdateStripeAccountConnectFrg.isEnabled = it
            binding.btnConnectStripeAccountConnectFrg.isEnabled = it

        })

        viewModel.showBtnStripeConnect.observe(viewLifecycleOwner, Observer {
            Log.d(TAG, "***** showBtnStripeConnect value changed as ${it} *****")
            binding.btnConnectStripeAccountConnectFrg.isEnabled = it
            binding.btnUpdateStripeAccountConnectFrg.isEnabled = it
        })

        viewModel.stripeAccountId.observe(viewLifecycleOwner, Observer {
            binding.tvIdStripeAccountConnectFrg.text = it
        })

        return binding.root
    }


    // called from create_stripe_account_dialog.xml
    fun onCancelBtnClicked(){
        Log.d(TAG,"----- onCancelBtnClicked -----")

        stripeAlert.dismiss()
    }

    // called from create_stripe_account_dialog.xml
    fun onOkBtnClicked(url: String){
        Log.d(TAG,"----- onCancelBtnClicked -----")

        val tabIntentBuilder = CustomTabsIntent.Builder()
        tabIntentBuilder.setToolbarColor(ContextCompat.getColor(requireContext(), R.color.colorPrimary))

        val tabIntent = tabIntentBuilder.build()

        tabIntent.launchUrl(requireContext(), Uri.parse(url))
    }





    // for debug


//    fun onClickDeleteAcctBtn(){
//        Log.d(TAG,"----- onClickDeleteAcctBtn -----")
//
//        sharedPrefs.edit().remove("stripe_account").apply()
//    }
//
//
//    fun onOnClickShowAccountStripeConnect(){
//        Log.d(TAG,"----- onOnClickShowAccountStripeConnect -----")
//        Log.d(TAG,"----- sharedPrefs.getString(stripe_account) is : ${sharedPrefs.getString("stripe_account","")}-----")
//        binding.tvDebugStripeAccountConnectFrg.text = sharedPrefs.getString("stripe_account","")
//    }






}