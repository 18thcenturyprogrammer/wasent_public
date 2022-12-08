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
import com.centuryprogrammer18thwasentsingleland.data.PickupHistory
import com.centuryprogrammer18thwasentsingleland.databinding.FragmentTaskBoardBinding
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar


// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [TaskBoardFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class TaskBoardFragment : Fragment() {
    private val TAG = TaskBoardFragment::class.java.simpleName

    private lateinit var args : TaskBoardFragmentArgs

    private lateinit var binding : FragmentTaskBoardBinding

    // encrypted shared preferences
    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    private val viewModel : TaskBoardFragmentVM by viewModels()

    lateinit var customer : Customer

    lateinit var invoiceOrders : MutableList<InvoiceOrder>

    var lastPickupHistory : PickupHistory? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"***** onCreate *****")
        super.onCreate(savedInstanceState)

        args = TaskBoardFragmentArgs.fromBundle(requireArguments())
        customer = args.customer

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

//        android soft keyboard hide programmatically ref) https://developer.android.com/reference/android/view/inputmethod/InputMethodManager.html#HIDE_NOT_ALWAYS
//        hide softkeyboard
//        ref) https://stackoverflow.com/a/5617130/3151712
//        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS,0)

        viewModel.setSharedPrefs(sharedPrefs)
        viewModel.setCustomer(args.customer)
        viewModel.getInvoiceOrderForCustomer()

    }

    override fun onCreateView(

        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG,"***** onCreateView *****")

        (activity as DropPickupActivity).setActionBarTitle(getString(R.string.task_board))

        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_task_board,container,false)
        binding.frg = this
        binding.viewModel = viewModel

        // filled textview with customer data
        with(binding){
            tvFirstNameTaskBoardFrg.text = args.customer.firstName
            tvLastNameTaskBoardFrg.text = args.customer.lastName
            tvPhoneTaskBoardFrg.text = args.customer.phoneNum
            tvEmailTaskBoardFrg.text =args.customer.email
            tvShirtTaskBoardFrg.text = args.customer.shirt
            tvStarchTaskBoardFrg.text = args.customer.starch
            tvNoteTaskBoardFrg.text = args.customer.note
        }


        val adapter = InvoiceOrderTaskBoardApt(viewModel,this)

        binding.rvHistoryTaskBoardFrg.adapter = adapter
        binding.rvHistoryTaskBoardFrg.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.VERTICAL,false)

        viewModel.invoiceOrders.observe(viewLifecycleOwner, Observer {
            if(it != null){
                Log.d(TAG,"***** invoiceOrders updated and has values *****")
                Log.d(TAG,"***** invoiceOrders value : '${it.toString()}' *****")

                adapter.submitList(it)
                adapter.notifyDataSetChanged()

                invoiceOrders = it
            }
        })

        viewModel.lastPickHistory.observe(viewLifecycleOwner, Observer {
            lastPickupHistory = it

            if(it != null && it.newBalance != null){
                Log.d(TAG,"***** lastPickHistory updated and has values *****")
                Log.d(TAG,"***** lastPickHistory value : '${it.toString()}' *****")

                binding.tvLastPickupHistoryTaskBoardFrg.text = getString(R.string.unpaid_balance_past) +" "+ makeTwoPointsDecialStringWithDollar(it.newBalance!!)
            }
        })

        return binding.root
    }

    fun onBtnDropoffClicked() {
        Log.d(TAG,"***** onBtnDropoffClicked *****")

        val action = TaskBoardFragmentDirections.actionTaskBoardFragmentToDropoffFrg(
            customer,
            invoiceOrders.toTypedArray(),
            lastPickupHistory)
        findNavController().navigate(action)
    }

    fun onBtnInvoiceClicked(){
        Log.d(TAG,"***** onBtnInvoiceClicked *****")

        val action = TaskBoardFragmentDirections.actionTaskBoardFragmentToMakeInvoiceActivity(customer,null)
        findNavController().navigate(action)
    }

    // user clicked one of InvoiceOrderBrief , send user InvoiceWorkFrg
    fun onInovoiceOrderClicked(invoiceOrder: InvoiceOrder){
        Log.d(TAG,"***** onInovoiceOrderClicked *****")

        val bundle = bundleOf("customer" to customer, "invoiceOrder" to invoiceOrder)
        findNavController().setGraph(R.navigation.invoice_works_upgraded_nav,bundle)
    }


    fun onBtnPickupClicked(){
        Log.d(TAG,"***** onBtnPickupClicked *****")

        val action = TaskBoardFragmentDirections.actionTaskBoardFragmentToPickupAct(
            customer,
            invoiceOrders.toTypedArray(),
            lastPickupHistory
        )
        findNavController().navigate(action)
    }

    fun onBtnEditClicked(){
        Log.d(TAG,"***** onBtnEditClicked *****")

        val action = TaskBoardFragmentDirections.actionTaskBoardFragmentToEditCusomerFrg(customer)
        findNavController().navigate(action)
    }

    fun onBtnHistoryClicked(){
        Log.d(TAG,"***** onBtnHistoryClicked *****")

        val action = TaskBoardFragmentDirections.actionTaskBoardFragmentToUserInvoiceHistoryFrg(customer)
        findNavController().navigate(action)
    }

}