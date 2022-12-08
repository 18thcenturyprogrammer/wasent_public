package com.centuryprogrammer18thwasentsingleland.manager

import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.DetailBaseItem
import com.centuryprogrammer18thwasentsingleland.databinding.DetailBaseItemFrgBinding
import com.centuryprogrammer18thwasentsingleland.repository.DetailBaseItemViewholderLongClickCallback
import com.centuryprogrammer18thwasentsingleland.repository.DetailItemViewholderLongClickCallback
import java.lang.StringBuilder

class DetailBaseItemFrg : Fragment(), DetailBaseItemViewholderLongClickCallback {
    private val TAG = DetailBaseItemFrg::class.java.simpleName

    private lateinit var binding : DetailBaseItemFrgBinding

    // for encrypted SharedPreferences
    lateinit var masterKeyAlias: String
    lateinit var sharedPrefs: SharedPreferences

    private val viewModel: DetailBaseItemFrgVM by viewModels()

    private var clickedDetailBaseItem : DetailBaseItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initEncrypSharedPrefs()

        viewModel.initSharedPrefs(sharedPrefs)

        // get all DetailBaseItem 
        viewModel.getDetailBaseItems()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.detail_base_item_frg,container,false)

        val adapter = DetailBaseItemApt(this)

        binding.rvDetailBaseItemFrg.adapter = adapter
        binding.rvDetailBaseItemFrg.layoutManager = GridLayoutManager(requireContext(),6)

        viewModel.detailBaseItems.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it.values.toMutableList())
            adapter.notifyDataSetChanged()
        })

        viewModel.foundItemPrice.observe(viewLifecycleOwner, Observer {
            if(it != null){

                Log.d(TAG,"***** BaseItem or PartialBaseItem price | ${it.toString()} *****")

                val action = DetailBaseItemFrgDirections.actionDetailBaseItemFrgToDetailBaseItemEditDialogFrg2(clickedDetailBaseItem!!,it)
                findNavController().navigate(action)

                clickedDetailBaseItem = null
                viewModel.setNullFoundItemPrice()
            }
        })

        return binding.root
    }

    override fun onDetailBaseItemViewholderLongClick(detailBaseItem: DetailBaseItem) {
        Log.d(TAG,"***** onDetailBaseItemViewholderLongClick *****")

        var pushKey = ""
        if(detailBaseItem.baseItemProcess.endsWith("_dry_clean_press")){
            pushKey = detailBaseItem.baseItemProcess.replace("_dry_clean_press", "_dryclean_press")

            Log.d(TAG,"***** detailBaseItem.name | ${detailBaseItem.name} *****")
            Log.d(TAG,"***** detailBaseItem.baseItemProcess | ${detailBaseItem.baseItemProcess} *****")
            Log.d(TAG,"***** pushKey to find BaseItem | ${pushKey} *****")
            viewModel.getBaseItemByPushKey(pushKey)
        }

        if(detailBaseItem.baseItemProcess.endsWith("_wet_clean_press")){
            pushKey = detailBaseItem.baseItemProcess.replace("_wet_clean_press", "_wetclean_press")

            Log.d(TAG,"***** detailBaseItem.name | ${detailBaseItem.name} *****")
            Log.d(TAG,"***** detailBaseItem.baseItemProcess | ${detailBaseItem.baseItemProcess} *****")
            Log.d(TAG,"***** pushKey to find BaseItem | ${pushKey} *****")
            viewModel.getBaseItemByPushKey(pushKey)
        }

        if(detailBaseItem.baseItemProcess.endsWith("_dry_clean")){
            pushKey = detailBaseItem.baseItemProcess.replace("_dry_clean", "_clean_only")

            Log.d(TAG,"***** detailBaseItem.name | ${detailBaseItem.name} *****")
            Log.d(TAG,"***** detailBaseItem.baseItemProcess | ${detailBaseItem.baseItemProcess} *****")
            Log.d(TAG,"***** pushKey to find PartialBaseItem | ${pushKey} *****")
            viewModel.getPartialBaseItemByPushKey(pushKey, "dryclean")
        }

        if(detailBaseItem.baseItemProcess.endsWith("_wet_clean")){
            pushKey = detailBaseItem.baseItemProcess.replace("_wet_clean", "_clean_only")

            Log.d(TAG,"***** detailBaseItem.name | ${detailBaseItem.name} *****")
            Log.d(TAG,"***** detailBaseItem.baseItemProcess | ${detailBaseItem.baseItemProcess} *****")
            Log.d(TAG,"***** pushKey to find PartialBaseItem | ${pushKey} *****")
            viewModel.getPartialBaseItemByPushKey(pushKey, "wetclean")
        }

        if(detailBaseItem.baseItemProcess.endsWith("_dry_press")){
            pushKey = detailBaseItem.baseItemProcess.replace("_dry_press", "_press_only")

            Log.d(TAG,"***** detailBaseItem.name | ${detailBaseItem.name} *****")
            Log.d(TAG,"***** detailBaseItem.baseItemProcess | ${detailBaseItem.baseItemProcess} *****")
            Log.d(TAG,"***** pushKey to find PartialBaseItem | ${pushKey} *****")
            viewModel.getPartialBaseItemByPushKey(pushKey, "dryclean")
        }

        if(detailBaseItem.baseItemProcess.endsWith("_wet_press")){
            pushKey = detailBaseItem.baseItemProcess.replace("_wet_press", "_press_only")

            Log.d(TAG,"***** detailBaseItem.name | ${detailBaseItem.name} *****")
            Log.d(TAG,"***** detailBaseItem.baseItemProcess | ${detailBaseItem.baseItemProcess} *****")
            Log.d(TAG,"***** pushKey to find PartialBaseItem | ${pushKey} *****")
            viewModel.getPartialBaseItemByPushKey(pushKey, "wetclean")
        }

        if(detailBaseItem.baseItemProcess.endsWith("_alter")){
            pushKey = detailBaseItem.baseItemProcess.replace("_alter", "_alteration_only")

            Log.d(TAG,"***** detailBaseItem.name | ${detailBaseItem.name} *****")
            Log.d(TAG,"***** detailBaseItem.baseItemProcess | ${detailBaseItem.baseItemProcess} *****")
            Log.d(TAG,"***** pushKey to find BaseItem | ${pushKey} *****")
            viewModel.getBaseItemByPushKey(pushKey)
        }

        clickedDetailBaseItem = detailBaseItem
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
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