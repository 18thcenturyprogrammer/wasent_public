package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.DetailItemFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentBaseitemBinding
import com.centuryprogrammer18thwasentsingleland.repository.DetailItemViewholderLongClickCallback

class DetailItemFrg : Fragment() , DetailItemViewholderLongClickCallback {
    private val TAG = DetailItemFrg::class.java.simpleName

    private lateinit var binding : DetailItemFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    private val viewModel: DetailItemFrgVM by viewModels()

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
        viewModel.addValueEventOnDetailItem()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"***** onCreateView *****")

        binding = DataBindingUtil.inflate(inflater, R.layout.detail_item_frg, container,false )

        binding.viewModel = viewModel

        val adapter = DetailItemAdapter(this)

        binding.rvDetailItemFrg.adapter = adapter
        binding.rvDetailItemFrg.layoutManager = GridLayoutManager(requireContext(), 6)

        viewModel.detailItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(ArrayList(it.values))
            adapter.notifyDataSetChanged()
        })

        binding.btnAddDetailItemFrg.setOnClickListener {
            val action = DetailItemFrgDirections.actionDetailItemFrgToDetailItemAddDialogFrg()
            findNavController().navigate(action)
        }

        return binding.root

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onDetailItemViewholderLongClick(view: View) {
        val detailItemName = view.findViewById<TextView>(R.id.tvNameDetailItemViewholder).text.toString()
        Log.d(TAG,"***** long clicked sending Detail item to dialog fragment value is '${detailItemName}'*****")

        val action = DetailItemFrgDirections.actionDetailItemFrgToDetailItemRemoveFrg(detailItemName)
        findNavController().navigate(action)
    }
}