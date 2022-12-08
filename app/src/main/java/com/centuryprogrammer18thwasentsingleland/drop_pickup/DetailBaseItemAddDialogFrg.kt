package com.centuryprogrammer18thwasentsingleland.drop_pickup



import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.DetailBaseItemAddDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.SignedCurrencyTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class DetailBaseItemAddDialogFrg : DialogFragment() {
    private val TAG = DetailBaseItemAddDialogFrg::class.java.simpleName

    private lateinit var args : DetailBaseItemAddDialogFrgArgs

    private lateinit var binding : DetailBaseItemAddDialogFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel: DetailBaseItemAddDialogFrgVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = DetailBaseItemAddDialogFrgArgs.fromBundle(requireArguments())

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

        binding = DataBindingUtil.inflate(layoutInflater,R.layout.detail_base_item_add_dialog_frg,null,false)

        binding.viewModel = viewModel

        // DetailItem name ex) small , large, fancy
        binding.tvDetailNameDetailBaseItemAddDialog.text = args.mergedDetailItem.name

        // ex) pant_dry_clean_press
        binding.tvProcessDetailBaseItemAddDialog.text = args.mergedDetailItem.selectedFabricare!!.fabricare!!.process.replace("_"," ")
        binding.tvBasePriceDetailBaseItemAddDialog.text = makeTwoPointsDecialStringWithDollar(args.mergedDetailItem.selectedFabricare!!.fabricare!!.basePrice)

        // adding textwatcher for signed currency format
        binding.etAmountDetailBaseItemAddDialog.addTextChangedListener(
            SignedCurrencyTextWatcher(binding.etAmountDetailBaseItemAddDialog)
        )

        // if etRatePriceDetailBaseItemAddDialog has focus, the other etAmountDetailBaseItemAddDialog will be zero as default
        binding.etRatePriceDetailBaseItemAddDialog.setOnFocusChangeListener { view, hasFocus ->
            if(hasFocus){
                Log.d(TAG,"***** rate editext got focus *****")
                val editText = view as EditText
                editText.setText("")
                binding.etAmountDetailBaseItemAddDialog.setText(0.0.toString())
            }
        }

        binding.etAmountDetailBaseItemAddDialog.setOnFocusChangeListener { view, hasFocus ->
            if(hasFocus){
                Log.d(TAG,"***** amount editext got focus *****")
                val editText = view as EditText
                editText.setText("")
                binding.etRatePriceDetailBaseItemAddDialog.setText(0.0f.toString())
            }
        }


        // build alert dialog
        val builder = androidx.appcompat.app.AlertDialog.Builder(requireContext())

        builder.setMessage(R.string.add_detail_item)
        builder.setView(binding.root)


        return builder.create()
    }
}