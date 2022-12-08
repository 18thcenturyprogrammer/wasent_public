package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentStartBinding
import com.centuryprogrammer18thwasentsingleland.utils.onDone
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast


class StartFragment : Fragment() {
    private val TAG = StartFragment::class.java.simpleName

    private lateinit var args : StartFragmentArgs

    // data binding
    private lateinit var binding : FragmentStartBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    // shared activity ViewModel
    private val actViewModel : DropPickupActivityViewModel by activityViewModels()

    private val viewModel : StartFragmentVM by viewModels()

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

        args = StartFragmentArgs.fromBundle(requireArguments())

        val receivedCustomer = args.customer

        receivedCustomer?.let {
            // there was customer argument which is passed from other activity or fragment

            Log.d(TAG,"===== it.lastName | ${it.lastName}  =====")
            Log.d(TAG,"===== it.firstName | ${it.firstName}  =====")
            Log.d(TAG,"===== it.email | ${it.email}  =====")

            viewModel.initCustomer(it)
        }

        viewModel.sharedPrefs = sharedPrefs

        viewModel.messageToFrg.observe(this, Observer {
            if(it.isNotEmpty()){

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        viewModel.foundInvoiceOrder.observe(this, Observer {
            if(it.id != null){
                Log.d(TAG,"***** found matched invoice order , send user to  *****")
            }
        })

        // goNewCustomerFragment key is searchWord String which is phone number
        // goNewCustomerFragment value is Boolean flag for requesting for sending user to NewCustomerFragment.kt
        viewModel.goNewCustomerFragment.observe(this, Observer {
            if(it.isNotEmpty() && it.values.first()){
                val action = StartFragmentDirections.actionStartFragmentToNewCustomerFragment(it.keys.first())
                correctNavCont(findNavController()).navigate(action)
            }
        })

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (activity as DropPickupActivity).setActionBarTitle(getString(R.string.home))

        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_NOT_ALWAYS)


//        this job has to be done in onCreateView in case of using in Fragment
//        reF) https://codelabs.developers.google.com/codelabs/kotlin-android-training-data-binding-basics/index.html?index=..%2F..android-kotlin-fundamentals#2
//        ref) https://stackoverflow.com/a/34719627/3151712
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_start,container,false)

        binding.frg = this
        binding.viewModel= viewModel


        val adapter = SearchedCustomerStartApt(viewModel,this)
        binding.rvMathchedCustomersStartFrg.adapter = adapter
        binding.rvMathchedCustomersStartFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)


        viewModel.foundCustomers.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                val numMatched = it.count()

                if(numMatched == 1){
                    goTaskBoardFragment(it.first())
                    viewModel.clearVars()
                }else{
                    adapter.submitList(it)
                    adapter.notifyDataSetChanged()

                    //      show hide softkeyboard
                    //      ref) https://stackoverflow.com/a/5617130/3151712
                    val inputMethodManager  = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
                }
            }
        })

        // imeoption click searh button ref) ref) https://stackoverflow.com/a/59758037/3151712
        binding.etNamePhoneInvoiceStartFrg.onDone {
            viewModel.onClickSearchBtn()
        }

        binding.btnGoNewCustomer.setOnClickListener {button ->

//            pass arguments by safe args
//          ref) https://codelabs.developers.google.com/codelabs/kotlin-android-training-start-external-activity/index.html?index=..%2F..android-kotlin-fundamentals#3

            val phoneNum = ""
            val action = StartFragmentDirections.actionStartFragmentToNewCustomerFragment(phoneNum)
            correctNavCont(findNavController()).navigate(action)

        }

        return binding.root
    }

    fun onClickHoldedBtn(){
        Log.d(TAG, "***** onClickHoldedBtn *****")

        val action = StartFragmentDirections.actionStartFragmentToHoldedInvoicesFrg()
        correctNavCont(findNavController()).navigate(action)
    }

    // user clicked InvoiceOrder . from SearchedCustomerStartApt
    fun goTaskBoardFragment(customer: Customer){
        val action = StartFragmentDirections.actionStartFragmentToTaskBoardFragment(customer)
        correctNavCont(findNavController()).navigate(action)
    }

    fun correctNavCont(navCont:NavController):NavController{
        if(navCont.currentDestination?.id == R.id.startFragment){
            return navCont
        }else{
            // programmatically navigation pass parameter ref) https://stackoverflow.com/a/56448153/3151712
            // ref) https://stackoverflow.com/a/54613997/3151712
            // getting basic concept , but don't follow this ref) https://stackoverflow.com/a/53922244/3151712

            navCont.setGraph(R.navigation.drop_pickup_navigation)

            return navCont
        }
    }
}