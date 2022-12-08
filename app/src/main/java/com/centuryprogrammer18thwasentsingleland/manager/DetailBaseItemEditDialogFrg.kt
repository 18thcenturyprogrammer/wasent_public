package com.centuryprogrammer18thwasentsingleland.manager

import android.app.Dialog
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.DetailBaseItemAddDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.DetailBaseItemEditDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DetailBaseItemAddDialogFrg
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DetailBaseItemAddDialogFrgArgs
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DetailBaseItemAddDialogFrgVM
import com.centuryprogrammer18thwasentsingleland.utils.SignedCurrencyTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class DetailBaseItemEditDialogFrg : DialogFragment() {
    private val TAG = DetailBaseItemEditDialogFrg::class.java.simpleName

    private lateinit var args : DetailBaseItemEditDialogFrgArgs

    private lateinit var binding : DetailBaseItemEditDialogFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel: DetailBaseItemEditDialogFrgVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = DetailBaseItemEditDialogFrgArgs.fromBundle(requireArguments())

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

        viewModel.setArgs(args)
        viewModel.setSharedPreferences(sharedPrefs)

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        viewModel.messageOnFragment.observe(this, Observer {
            if (it != ""){
                // there is message to show

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        viewModel.closeDialog.observe(this, Observer {
            if(it){
                // close dialog

                dialog?.dismiss()
            }
        })

        binding = DataBindingUtil.inflate(layoutInflater,R.layout.detail_base_item_edit_dialog_frg,null,false)

        // two way data binding
        binding.setLifecycleOwner(this)

        // databinding variables
        binding.frg = this
        binding.viewModel = viewModel


        // DetailItem name ex) small , large, fancy
        binding.tvDetailNameDetailBaseItemEditDialog.text = args.detailBaseItem.name

        // ex) pant dry clean press
        binding.tvProcessDetailBaseItemEditDialog.text = args.detailBaseItem.baseItemProcess.replace("_"," ")
        binding.tvBasePriceDetailBaseItemEditDialog.text = makeTwoPointsDecialStringWithDollar(args.itemPrice)

        // adding textwatcher for signed currency format
        binding.etAmountDetailBaseItemEditDialog.addTextChangedListener(
            SignedCurrencyTextWatcher(binding.etAmountDetailBaseItemEditDialog)
        )

        // if etRatePriceDetailBaseItemEditDialog has focus, the other etAmountDetailBaseItemEditDialog will be zero as default
        binding.etRatePriceDetailBaseItemEditDialog.setOnFocusChangeListener { view, hasFocus ->
            if(hasFocus){
                Log.d(TAG,"***** rate editext got focus *****")

                val editText = view as EditText
                editText.setText("")
                binding.etAmountDetailBaseItemEditDialog.setText(0.0.toString())
            }
        }

        binding.etAmountDetailBaseItemEditDialog.setOnFocusChangeListener { view, hasFocus ->
            if(hasFocus){
                Log.d(TAG,"***** amount editext got focus *****")

                val editText = view as EditText
                editText.setText("")
                binding.etRatePriceDetailBaseItemEditDialog.setText(0.0f.toString())
            }
        }


        // build alert dialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())

        builder.setMessage(R.string.edit_detail_item)
        builder.setView(binding.root)


        return builder.create()
    }

    // user click delete button on detail_base_item_edit_dialog_frg.xml
    fun onClickDelete(){
        Log.d(TAG, "***** onClickDelete *****")

        val pushKey = args.detailBaseItem.name +"_"+args.detailBaseItem.baseItemProcess

        val action = DetailBaseItemEditDialogFrgDirections.actionDetailBaseItemEditDialogFrgToDetailBaseItemRemoveDialogFrg(pushKey)
        findNavController().navigate(action)
    }
}