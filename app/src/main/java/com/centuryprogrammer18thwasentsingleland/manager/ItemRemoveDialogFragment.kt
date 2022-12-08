package com.centuryprogrammer18thwasentsingleland.manager

import android.app.Dialog

import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys

import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentItemRemoveDialogBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast


class ItemRemoveDialogFragment : DialogFragment() {
    private val TAG = ItemRemoveDialogFragment::class.java.simpleName

    private lateinit var args : ItemRemoveDialogFragmentArgs
    private lateinit var binding :FragmentItemRemoveDialogBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    private val viewModel : ItemRemoveDialogFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // getting args from previous fragment
        args = ItemRemoveDialogFragmentArgs.fromBundle(requireArguments())
        viewModel.itemName = args.itemName

        // getting shared preferences
        // i think this should be here than onCreate because user could go other activity and change SharedPreferences and
//        that should be affected. if i put these in the onCreate, when user come back from other fragment they can use new SharedPreferences data
//        however, i need sharedPrefs here because when i talk to firebase i need sharedPrefs.
//        eventually, i decided i will send user out of program when they change sharedPrefs

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


        viewModel.setSharedPreferences(sharedPrefs)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val layoutInflater = LayoutInflater.from(requireContext())
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_item_remove_dialog,null, false)
        binding.viewModel = viewModel

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(R.string.remove_item)
        val message : String = args.itemName+" : "+getString(R.string.do_you_want_remove_item)
        builder.setMessage(message)
        builder.setView(binding.root)

        viewModel.closeDialog.observe(this, Observer {
            if(it){
                dialog!!.dismiss()
            }
        })

        viewModel.isDeletedItem.observe(this, Observer {
            if (it){
                // deleting item is succeed

                Log.d(TAG,"===== livedata says successfully item deleted, so show toast =====")
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.successfully_item_deleted))

                viewModel.changeCloseDialogTo(true)
                viewModel.itemName = null
            }else{
                Log.d(TAG,"===== livedata says failed item deleted, so show toast =====")
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.failed_item_deleted))

            }
        })

        return builder.create()
    }
}