package com.centuryprogrammer18thwasentsingleland.manager


import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentItemBinding
import com.centuryprogrammer18thwasentsingleland.repository.ItemViewholderLongClickCallback

class ItemFragment : Fragment(), ItemViewholderLongClickCallback {
    private val TAG = ItemFragment::class.java.simpleName

    private lateinit var binding : FragmentItemBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    private val viewModel : ItemFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG,"***** onCreate() called *****")

        // i think this should be here than onCreate because user could go other activity and change SharedPreferences and
//        that should be affected. if i put these in the onCreate, when user come back from other fragment they can use new SharedPreferences data
//        however, i need sharedPrefs here because when i talk to firebase i need sharedPrefs.if i put addChildEventOnItem in onCreateView,
//        whenever user rotate the device we will call addChildEventOnItem again and will get data again
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

        viewModel.addValueEventOnItem()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_item,container,false)

        val adapter = ItemAdapter(this)
        binding.rvItem.adapter = adapter
        binding.rvItem.layoutManager = GridLayoutManager(requireContext(), 6, GridLayoutManager.VERTICAL, false)

        viewModel.liveItems.observe(viewLifecycleOwner, Observer {
            // liveItems has changed
            Log.d(TAG,"*****liveItems has changed *****")
            Log.d(TAG,"*****liveItems size is ${viewModel.liveItems.value?.size.toString()} *****")

            adapter.submitList(it.values.toList())
            adapter.notifyDataSetChanged()
        })

        binding.btnGoItemAddDialogFragment.setOnClickListener {
            val action = ItemFragmentDirections.actionItemFragmentToItemAddDialogFragment()
            findNavController().navigate(action)
        }

        return binding.root
    }

    override fun onItemViewholderLongClick(view: View) {
//        user long clicked on Item viewholder, move to item remove dialog fragment

//        Toast.makeText(requireContext(), "user long clicked view item name is '${view.findViewById<TextView>(R.id.tvItemNameViewholder).text.toString()}'", Toast.LENGTH_LONG).show()

        val itemName = view.findViewById<TextView>(R.id.tvItemNameViewholder).text.toString()

        Log.d(TAG,"***** long clicked sending itemName to dialog fragment value is '${itemName}'*****")
        val action = ItemFragmentDirections.actionItemFragmentToItemRemoveDialogFragment(itemName)

        findNavController().navigate(action)
    }
}