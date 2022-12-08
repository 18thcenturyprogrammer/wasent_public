package com.centuryprogrammer18thwasentsingleland.manager

import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentItemAddDialogBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class ItemAddDialogFragment : DialogFragment() {
    private val TAG = ItemAddDialogFragment::class.java.simpleName

    private lateinit var binding : FragmentItemAddDialogBinding

    lateinit var masterKeyAlias : String
    private var sharedPrefs : SharedPreferences? = null

    private val viewModel: ItemAddDialogFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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



        viewModel.setSharedPreferences(sharedPrefs!!)

        viewModel.attachValueEventListeners()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.fragment_item_add_dialog,null, false)

        binding.viewModel = viewModel

        val etName = binding.etItemName
        val etNumPiece = binding.etItemNumPiece

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(R.string.add_item)
        builder.setView(binding.root)

        viewModel.isAllLoaded.observe(this, Observer {
            // if data is all loaded , button is enabled

            Log.d(TAG,"***** isAllLoaded value is '${it.toString()}' *****")
            if (it == 3){
                binding.btnItemAddOk.isEnabled = true
            }

        })

        viewModel.closeDialog.observe(this, Observer { close->
            if(close){
                dialog?.dismiss()
                viewModel.changeCloseDialogTo(false)
            }
        })

        viewModel.messageOnFragment.observe(this, Observer {
            if (it.isNotEmpty()){
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)

                viewModel.emptyMessageOnFragment()
            }
        })

        viewModel.isAddedItem.observe(this, Observer {
            if(!it){
                // failed to add basic item

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.failed_to_add_basic_item))

            }else{
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.add_item_success))

                viewModel.clearVars()
            }
            viewModel.changeCloseDialogTo(true)
            viewModel.changeCloseDialogTo(false)
        })

        return builder.create()
    }

}