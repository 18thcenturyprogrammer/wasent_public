package com.centuryprogrammer18thwasentsingleland.search

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.SearchingSearchFrgBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity

class SearchingSearchFrg : Fragment() {
    private val TAG = SearchingSearchFrg::class.java.simpleName

    private lateinit var binding: SearchingSearchFrgBinding

    lateinit var masterKeyAlias :String
    lateinit var sharedPrefs : SharedPreferences

    private val viewModel: SearchingSearchFrgVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initEncrypSharedPrefs()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.searching_search_frg,container, false)

        binding.viewModel = viewModel

        viewModel.initSharedPrefs(sharedPrefs)

        val spinnerItems = resources.getStringArray(R.array.searching_search_spinner_items)

        //         spinner item space ref) https://stackoverflow.com/a/54526961/3151712
        binding.spnCategorySearchingSearchFrg.adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,spinnerItems)

        binding.spnCategorySearchingSearchFrg.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when(position){
                    0 -> {
                        Log.d(TAG,"***** Last Name SELECTED *****")
                        viewModel.category = "Last Name"
                    }
                    1 -> {
                        Log.d(TAG,"***** First Name SELECTED *****")
                        viewModel.category = "First Name"
                    }
                    2 -> {
                        Log.d(TAG,"***** Invoice Number SELECTED *****")
                        viewModel.category = "Invoice Number"
                    }


//                    i was trying to include functionality for searching note, but
//                            firebase realtime database doesn't support 'where', 'like' searching, so i postponed
//                        i think if i break note into words and make node for each word, i am able to search note
//
//                    3 -> {
//                        Log.d(TAG,"*****Customer Note SELECTED *****")
//                        viewModel.category = "Customer Note"
//                    }
//                    4 ->{
//                        Log.d(TAG,"***** Drop-off Note SELECTED *****")
//                        viewModel.category = "Drop-off Note"
//                    }
//                    5 ->{
//                        Log.d(TAG,"***** Invoice Note SELECTED *****")
//                        viewModel.category = "Invoice Note"
//                    }
                }
            }
        }

        val adapter = SearchResultApt(this)
        binding.rvSearchingSearchFrg.adapter = adapter
        binding.rvSearchingSearchFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        viewModel.searchResults.observe(viewLifecycleOwner, Observer {
            adapter.submitList(it)
            adapter.notifyDataSetChanged()

            if(it.count() >0){

                // hiding soft input keyboard
                val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
            }
        })
        return binding.root
    }

    fun onClickCardview(clickedSearchResult: SearchResult){
        Log.d(TAG, "***** onClickCardview *****")
        Log.d(TAG, "***** clickedSearchResult.type ${clickedSearchResult.type}*****")
        Log.d(TAG, "***** clickedSearchResult.id ${clickedSearchResult.id}*****")
        Log.d(TAG, "***** clickedSearchResult.content ${clickedSearchResult.content}*****")

        when(clickedSearchResult.type){
            "customer" -> {
//                val bundle = Bundle()
//                bundle.putSerializable("customer", clickedSearchResult.obj as Customer)

                val intent = Intent(requireContext(),DropPickupActivity::class.java)
//                intent.putExtra("customer", bundle)
                intent.putExtra("customer", clickedSearchResult.obj as Customer)

                startActivity(intent)
            }

            "invoice" -> {
                val invoiceOrder = clickedSearchResult.obj as InvoiceOrder
                val customer = invoiceOrder.customer

                val bundle = bundleOf("customer" to customer, "invoiceOrder" to invoiceOrder)
                findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
            }
        }

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