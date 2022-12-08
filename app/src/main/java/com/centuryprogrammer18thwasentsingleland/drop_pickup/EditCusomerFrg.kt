package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.databinding.EditCusomerFrgBinding

import com.centuryprogrammer18thwasentsingleland.utils.PhoneNumTextWatcher
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast


class EditCusomerFrg : Fragment() {
        private val TAG = EditCusomerFrg::class.java.simpleName

        lateinit var args : EditCusomerFrgArgs

        //    data binding
        private lateinit var binding : EditCusomerFrgBinding

        // encrypted shared prefereces
        lateinit var masterKeyAlias : String
        lateinit var sharedPrefs : SharedPreferences

        // ViewModel
        private val viewModel : EditCusomerFrgVM by viewModels()

        private lateinit var customer:Customer

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)


//        get the arguments which are passed by safe args
//        ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-start-external-activity/index.html?index=..%2F..android-kotlin-fundamentals#3
            args = EditCusomerFrgArgs.fromBundle(requireArguments())

            customer = args.customer
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {

            (activity as DropPickupActivity).setActionBarTitle(getString(R.string.edit_customer))

            //        this job has to be done in onCreateView in case of using in Fragment
//        reF) https://codelabs.developers.google.com/codelabs/kotlin-android-training-data-binding-basics/index.html?index=..%2F..android-kotlin-fundamentals#2
//        ref) https://stackoverflow.com/a/34719627/3151712
            binding = DataBindingUtil.inflate(inflater,R.layout.edit_cusomer_frg,container,false)

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

            // fill fields with customer data which already i have
            initUI()

            binding.etPhoneNumEditCustomerFrg.addTextChangedListener(PhoneNumTextWatcher(binding.etPhoneNumEditCustomerFrg))

            binding.swShirtEditCustomerFrg.setOnCheckedChangeListener { button, isChecked ->
                if (isChecked){
                    // show what user want how shirt should be done

                    // Toast extension function
                    Toast(requireContext()).showCustomToast(requireContext(), getString(R.string.shirt_in_Box))
                }else{
                    // Toast extension function
                    Toast(requireContext()).showCustomToast(requireContext(), getString(R.string.shirt_on_hanger))
                }
            }

            binding.skbStarchEditCustomerFrg.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
                var seekBarValue : Int = 0
                var seekBarStarch :String =""
                override fun onProgressChanged(seekBar: SeekBar?, value: Int, fromUser: Boolean) {
                    seekBarValue = value
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    seekBarStarch = when (seekBarValue){
                        0 ->  getString(R.string.no_starch)
                        1 ->  getString(R.string.light_starch)
                        2 ->  getString(R.string.medium_starch)
                        3 ->  getString(R.string.heavy_starch)
                        else-> ""
                    }
                    // Toast extension function
                    Toast(requireContext()).showCustomToast(requireContext(), seekBarStarch)
                }
            })

            binding.btnEditCustomer.setOnClickListener {

                val editedCustomer: Customer

                // get data user entered
                with(binding){
                    val firstName = etFistNameEditCustomerFrg.text.toString()
                    val lastName = etLastNameEditCustomerFrg.text.toString()
                    val phoneNum = etPhoneNumEditCustomerFrg.text.toString()
                    val email = etEmailEditCustomerFrg.text.toString()
                    val streetAddress = etStreetEditCustomerFrg.text.toString()
                    val city = etCityEditCustomerFrg.text.toString()
                    val state = etStateEditCustomerFrg.text.toString()
                    val zipcode = etZipEditCustomerFrg.text.toString()


                    val shirt = if (swShirtEditCustomerFrg.isChecked) "Shirt in Box" else "Shirt on Hanger"
                    val starch = when (skbStarchEditCustomerFrg.progress){
                        0 ->  getString(R.string.no_starch)
                        1 ->  getString(R.string.light_starch)
                        2 ->  getString(R.string.medium_starch)
                        3 ->  getString(R.string.heavy_starch)
                        else-> ""
                    }

                    val note = etNoteEditCustomerFrg.text.toString()


                    // make Customer obj with updated data
                    editedCustomer = Customer(customer.id, firstName, lastName, phoneNum, email, streetAddress, city, state, zipcode, shirt, starch, note)

                    Log.d(TAG,"***** editedCustomer obj ***** ${editedCustomer}")
                }

                // validate data
                if (validateCustomer(editedCustomer)){

                    // i will use add func for updating again
                    viewModel.editCustomer(sharedPrefs, editedCustomer)
                }else{
                    // customer validation failed

                    // Toast extension function
                    Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.you_enter_incorrect_data))
                }

            }


            viewModel.editedCustomer.observe(viewLifecycleOwner, Observer { editedCustomer ->
                Log.d(TAG,"***** isCustomerEdited ******")
                Log.d(TAG,"***** viewModel.addedCustomer.value is '${viewModel.editedCustomer.value.toString()}' ******")

                if (editedCustomer != null){
                    Log.d(TAG,"***** customer edited in firebase *****")
                    Log.d(TAG,"***** ViewModel editedCustomer = ${viewModel.editedCustomer.value.toString()} *****")


                    // move to DropPickupActivity
                    moveToDropPickupAct()

                    // clear variables
                    viewModel.cleanUpVars()

                    Log.d(TAG,"***** customer edited in firebase , and cleaned up vars in ViewModel *****")
                    Log.d(TAG,"***** ViewModel editedCustomer = ${viewModel.editedCustomer.value.toString()} *****")
                }
            })

            return binding.root
        }

        fun initUI(){
            with(binding){
                etFistNameEditCustomerFrg.setText(customer.firstName)
                etLastNameEditCustomerFrg.setText(customer.lastName)
                etPhoneNumEditCustomerFrg.setText(customer.phoneNum)
                etEmailEditCustomerFrg.setText(customer.email)
                etStreetEditCustomerFrg.setText(customer.streetAddress)
                etCityEditCustomerFrg.setText(customer.city)
                etStateEditCustomerFrg.setText(customer.state)
                etZipEditCustomerFrg.setText(customer.zipcode)
                etNoteEditCustomerFrg.setText(customer.note)

                swShirtEditCustomerFrg.isChecked = customer.shirt == "Shirt in Box"

                skbStarchEditCustomerFrg.progress = when(customer.starch){
                    getString(R.string.no_starch) -> 0
                    getString(R.string.light_starch) -> 1
                    getString(R.string.medium_starch) -> 2
                    getString(R.string.heavy_starch) -> 3
                    else -> -1
                }
            }
        }


        private fun validateCustomer(customer: Customer):Boolean{
            return customer.firstName != "" && customer.lastName != "" && customer.phoneNum?.length == 14
        }

        fun moveToDropPickupAct(){
            val intent = Intent(requireContext(),DropPickupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

}
