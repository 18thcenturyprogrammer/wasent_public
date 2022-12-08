package com.centuryprogrammer18thwasentsingleland.login

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.CreateStripeAccountDialogBinding
import com.centuryprogrammer18thwasentsingleland.databinding.InitSettingFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.CurrencyTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.PhoneNumTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast


class InitSettingFrg : Fragment() {
    private val TAG = InitSettingFrg::class.java.simpleName

    private lateinit var binding : InitSettingFrgBinding

    //variables will be initialised in the onCreate function
    lateinit var masterKeyAlias: String
    private lateinit var sharedPrefs : SharedPreferences

    val viewModel: InitSettingFrgVM by viewModels()

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

        viewModel.getCurrentUser()
        viewModel.setSharedPrefs(sharedPrefs)
        viewModel.loadSettings(sharedPrefs)


        binding = DataBindingUtil.inflate(inflater,R.layout.init_setting_frg, container, false)
        binding.viewModel = viewModel

        binding.etCleanerPhoneInitSettingFrg.addTextChangedListener(
            PhoneNumTextWatcher(binding.etCleanerPhoneInitSettingFrg)
        )

        binding.etEnvAmountInitSettingFrg.addTextChangedListener(
            CurrencyTextWatcher(binding.etEnvAmountInitSettingFrg)
        )

        viewModel.enableSaveBtn.observe(viewLifecycleOwner, Observer {
            binding.btnSaveInitSettingFrg.isEnabled = it
        })

        viewModel.msgToFrg.observe(viewLifecycleOwner, Observer {
            if(!it.isEmpty()){

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)

            }
        })

        viewModel.setFocusOn.observe(viewLifecycleOwner, Observer {
            val imm  = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

            imm.showSoftInput(it, InputMethodManager.SHOW_IMPLICIT)
        })

        viewModel.moveToManagerLoginAct.observe(viewLifecycleOwner, Observer {
           if(it){
               val intent = Intent(requireContext(), ManagerLoginAct::class.java)
               intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
               startActivity(intent)
           }
        })

        return binding.root
    }

}