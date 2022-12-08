package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FabricareAddDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.CurrencyTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class FabricareAddDialogFrg : DialogFragment() {
    private val TAG = FabricareAddDialogFrg::class.java.simpleName

    private lateinit var binding : FabricareAddDialogFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    private lateinit var actViewModel : MakeInvoiceActVM
    private val viewModel: FabricareAddDialogFrgVM by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        actViewModel = (activity as MakeInvoiceActivity).viewModel

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

        viewModel.setActViewMode(actViewModel)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fabricare_add_dialog_frg, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel.closeDialog.observe(this, Observer {
            if(it){
                dialog!!.dismiss()
            }
        })

        viewModel.messageOnFragment.observe(this, Observer {message->
            if(message != ""){
                // there is something to show

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),message)
            }
        })

        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fabricare_add_dialog_frg,null,false)

        binding.actViewModel = actViewModel
        binding.viewModel= viewModel

        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())

        // adding textwatcher for signed currency format
        binding.etPriceFabricareAddDialogFrg.addTextChangedListener(
            CurrencyTextWatcher(binding.etPriceFabricareAddDialogFrg)
        )











        // for user select one of radio button which in base item [dryclean , wetclean , alteration only ]
        binding.rbtngBaseFabricareAddDialogFrg.setOnCheckedChangeListener { radioGroup, btnId ->

            when (btnId) {
                R.id.rbtnDryFabricareAddDialogFrg -> {
                    binding.rbtngPartialBaseFabricareAddDialogFrg.visibility = View.VISIBLE
                }

                R.id.rbtnWetFabricareAddDialogFrg -> {
                    binding.rbtngPartialBaseFabricareAddDialogFrg.visibility = View.VISIBLE
                }
                R.id.rbtnAlterFabricareAddDialogFrg -> {

                    // there is no partial base item for alteration only
                    binding.rbtngPartialBaseFabricareAddDialogFrg.visibility = View.INVISIBLE
                    binding.rbtngPartialBaseFabricareAddDialogFrg.clearCheck()
                }

            }
        }












        builder.setView(binding.root)

        return builder.create()
    }


}