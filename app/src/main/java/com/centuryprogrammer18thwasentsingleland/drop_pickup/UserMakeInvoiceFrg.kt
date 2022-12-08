package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.databinding.UserMakeInvoiceFrgBinding

class UserMakeInvoiceFrg : Fragment() {
    private val TAG = UserMakeInvoiceFrg::class.java.simpleName

    private lateinit var args : UserMakeInvoiceFrgArgs

    private lateinit var binding : UserMakeInvoiceFrgBinding

    private lateinit var sharedPrefs : SharedPreferences

    private val viewModel: UserMakeInvoiceFrgVM by viewModels()

    private lateinit var customer : Customer


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = UserMakeInvoiceFrgArgs.fromBundle(requireArguments())

        customer = args.customer

        Log.d(TAG,"***** customer : '${customer.toString()}' *****")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.user_make_invoice_frg,container,false)

        binding.viewModel = viewModel
        binding.frg = this

        binding.tvLastNameUserMakeInvoiceFrg.text = customer.lastName
        binding.tvFirstNameUserMakeInvoiceFrg.text = customer.firstName
        binding.tvPhoneUserMakeInvoiceFrg.text = customer.phoneNum
        binding.tvEmailUserMakeInvoiceFrg.text = customer.email
        binding.tvShirtUserMakeInvoiceFrg.text = customer.shirt
        binding.tvStarchUserMakeInvoiceFrg.text = customer.starch

        // textview scroll scrollable ref) https://stackoverflow.com/a/56355714/3151712
        binding.tvNoteUserMakeInvoiceFrg.movementMethod= ScrollingMovementMethod()
        binding.tvNoteUserMakeInvoiceFrg.text = customer.note



        return binding.root
    }

    // user clicked user history button
    fun onClickGoHistory(){
        Log.d(TAG,"***** onClickGoHistory *****")
        Log.d(TAG,"***** customer : ${customer.toString()} ******")
        val navController = findNavController()
        Log.d(TAG, "***** navController.currentDestination?.id : ${navController.currentDestination?.id.toString()} *****")

        // i need to send user to UserHistoryMakeInvoiceFrg
        val action = UserMakeInvoiceFrgDirections.actionUserMakeInvoiceFrgToUserHistoryMakeInvoiceFrg(customer)

        // strangely after user click InvoiceOrder in the user history list, automatically user sent to user infos fragment (UserMakeInvoiceFrg)
        // after that if user click clicked user history button, error occurred because navController.currentDestination?.id has null
        // i had to fix as below

        if(navController.currentDestination?.id == R.id.userMakeInvoiceFrg){
            navController.navigate(action)
        }else{
            // programmatically navigation pass parameter ref) https://stackoverflow.com/a/56448153/3151712
            // ref) https://stackoverflow.com/a/54613997/3151712
            // getting basic concept , but don't follow this ref) https://stackoverflow.com/a/53922244/3151712

            // NavController set as UserMakeInvoiceFrg
            val bundle = Bundle()
            bundle.putSerializable("customer",customer)
            navController.setGraph(R.navigation.user_make_invoice_nav,bundle)

            // send user to UserHistoryMakeInvoiceFrg
            navController.navigate(action)
        }
    }



}