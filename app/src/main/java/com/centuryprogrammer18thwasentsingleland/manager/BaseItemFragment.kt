package com.centuryprogrammer18thwasentsingleland.manager

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentBaseitemBinding
import com.centuryprogrammer18thwasentsingleland.utils.BaseItemFrgAdapterInterface
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast


class BaseItemFragment : Fragment(), BaseItemFrgAdapterInterface {
    private val TAG = BaseItemFragment::class.java.simpleName

    private lateinit var binding : FragmentBaseitemBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    private val viewModel: BaseItemFragmentViewModel by viewModels()

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

        viewModel.setSharedPreferences(sharedPrefs)
        viewModel.getAllBaseItemsPartialBaseItems()

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_baseitem, container,false )

        binding.viewModel = viewModel

        val adapter = BaseItemAdapter(this, viewModel)

        binding.rvBaseItemFrg.adapter = adapter
        binding.rvBaseItemFrg.layoutManager = LinearLayoutManager(requireContext())

        viewModel.allBaseItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(ArrayList(it.values))
            adapter.notifyDataSetChanged()
        })

        viewModel.isUpdatedOk.observe(viewLifecycleOwner, Observer {
            if (it){
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.successfully_saved))
                viewModel.defaultIsUpdatedOk()
            }
        })

        return binding.root
    }

    // this is called from BaseItemAdapter when user click Edit button or ok button, so i need to show or hide soft keyboard
    override fun onShowSoftKeyboard(show: Boolean, editText: EditText) {
        Log.d(TAG,"***** user clicked edit button or ok button  *****")

        if(show){
            Log.d(TAG,"***** user clicked edit button  *****")

            // show soft keyboard programmatically ref) https://stackoverflow.com/a/8078341/3151712
            // android doc ref) https://developer.android.com/reference/android/view/inputmethod/InputMethodManager#summary
            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

        }else{
            Log.d(TAG,"***** user clicked OK button  *****")

            // hide soft keyboard programmatically ref) https://stackoverflow.com/questions/13593069/androidhide-keyboard-after-button-click
            val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        }

    }
}