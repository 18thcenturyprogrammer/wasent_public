package com.centuryprogrammer18thwasentsingleland.sales

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.PickupListSalesFrgBinding

class PickupListSalesFrg : Fragment() {
    private val TAG = PickupListSalesFrg::class.java.simpleName

    private lateinit var args : PickupListSalesFrgArgs

    private lateinit var binding : PickupListSalesFrgBinding

    private lateinit var masterKeyAlias : String
    private lateinit var sharedPrefs : SharedPreferences

    private val viewModel: PickupListSalesFrgVM by viewModels()

    private lateinit var startDate: MutableList<Int>
    private lateinit var endDate: MutableList<Int>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = PickupListSalesFrgArgs.fromBundle(requireArguments())
        startDate = args.startDate.toList().toMutableList()
        endDate = args.endDate.toList().toMutableList()

        initEncrypSharedPrefs()

        viewModel.initSharedPrefs(sharedPrefs)
        viewModel.initVars(startDate,endDate)
        viewModel.getPickups()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.pickup_list_sales_frg,container,false)

        val adapter = PickupApt()

        binding.rvPickupListSalesFrg.adapter = adapter
        binding.rvPickupListSalesFrg.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)

        viewModel.displayPickups.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()
        })

        return binding.root
    }

    fun initEncrypSharedPrefs(){
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
    }

}