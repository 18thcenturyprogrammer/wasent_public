package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController

import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentNewCustomerBinding
import com.centuryprogrammer18thwasentsingleland.utils.PhoneNumTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.isNumberStr
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast


class NewCustomerFragment : Fragment() {
    private val TAG = NewCustomerFragment::class.java.canonicalName

    private lateinit var args : NewCustomerFragmentArgs

//    data binding
    private lateinit var binding : FragmentNewCustomerBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    // ViewModel
    private val viewModel : NewCustomerFragmentViewModel by viewModels()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        get the arguments which are passed by safe args
//        ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-start-external-activity/index.html?index=..%2F..android-kotlin-fundamentals#3
        args = NewCustomerFragmentArgs.fromBundle(requireArguments())

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as DropPickupActivity).setActionBarTitle(getString(R.string.new_customer))

        //        this job has to be done in onCreateView in case of using in Fragment
//        reF) https://codelabs.developers.google.com/codelabs/kotlin-android-training-data-binding-basics/index.html?index=..%2F..android-kotlin-fundamentals#2
//        ref) https://stackoverflow.com/a/34719627/3151712
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_new_customer,container,false)

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

        binding.etPhoneNum.addTextChangedListener(PhoneNumTextWatcher(binding.etPhoneNum))

        args.phoneNumOrLastName?.let {
            if (isNumberStr(it)){
                // number string , so phone number

                binding.etPhoneNum.setText(it)
            }else{
                // lastname

                binding.etLastName.setText(it)
            }
        }

        binding.swShirt.setOnCheckedChangeListener { button, isChecked ->
            if (isChecked){
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.shirt_in_Box))
            }else{
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.shirt_on_hanger))
            }
        }

        binding.skbStarch.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            var seekBarValue : Int = 0
            var seekBarStarch :String =""
            override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                seekBarValue = value
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                seekBarStarch = when (seekBarValue){
                    0 ->  getString(R.string.no_starch)
                    1 ->  getString(R.string.light_starch)
                    2 ->  getString(R.string.medium_starch)
                    3 ->  getString(R.string.heavy_starch)
                    else-> ""
                }
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),seekBarStarch)
            }
        })

        binding.btnAddCustomer.setOnClickListener {

            val newCustomer: Customer

            with(binding){
                val firstName = etFirstName.text.toString()
                val lastName = etLastName.text.toString()
                val phoneNum = etPhoneNum.text.toString()
                val email = etEmail.text.toString()
                val streetAddress = etStreetAddress.text.toString()
                val city = etCity.text.toString()
                val state = etState.text.toString()
                val zipcode = etZipcode.text.toString()


                val shirt = if (swShirt.isChecked) "Shirt in Box" else "Shirt on Hanger"
                val starch = when (skbStarch.progress){
                    0 ->  getString(R.string.no_starch)
                    1 ->  getString(R.string.light_starch)
                    2 ->  getString(R.string.medium_starch)
                    3 ->  getString(R.string.heavy_starch)
                    else-> ""
                }

                val note = etNote.text.toString()


                newCustomer = Customer(null, firstName, lastName, phoneNum, email, streetAddress, city, state, zipcode, shirt, starch, note)

                Log.d(TAG,"***** newCustomer obj ***** $newCustomer")
            }

            if (validateCustomer(newCustomer)){
                viewModel.addCustomer(sharedPrefs, newCustomer)
            }else{
                val action
                        = NewCustomerFragmentDirections.actionNewCustomerFragmentToNotifyDialogFragment(getString(R.string.notice),getString(R.string.new_customer_validation_notice),null)
                findNavController().navigate(action)
            }

        }

        viewModel.addedCustomer.observe(viewLifecycleOwner, Observer { addedCustomer ->
            Log.d(TAG,"***** isCustomerAdded ******")
            Log.d(TAG,"***** viewModel.addedCustomer.value is '${viewModel.addedCustomer.value.toString()}' ******")

            if (addedCustomer != null){
                Log.d(TAG,"***** customer added in firebase *****")
                Log.d(TAG,"***** ViewModel addedCustomer = ${viewModel.addedCustomer.value.toString()} *****")


                val action = NewCustomerFragmentDirections.actionNewCustomerFragmentToTaskBoardFragment(viewModel.addedCustomer.value!!)
                findNavController().navigate(action)
                viewModel.cleanUpVars()

                Log.d(TAG,"***** customer added in firebase , and cleaned up vars in ViewModel *****")
                Log.d(TAG,"***** ViewModel addedCustomer = ${viewModel.addedCustomer.value.toString()} *****")
            }
        })

        return binding.root
    }


    private fun validateCustomer(customer:Customer):Boolean{
        return customer.firstName != "" && customer.lastName != "" && customer.phoneNum?.length == 14
    }




}
