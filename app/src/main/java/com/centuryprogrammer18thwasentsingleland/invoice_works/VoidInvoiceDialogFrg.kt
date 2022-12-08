package com.centuryprogrammer18thwasentsingleland.invoice_works

import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.VoidedInvoiceDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.repository.RemoveInvoiceOrderCallback
import com.centuryprogrammer18thwasentsingleland.repository.Repository
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentTimeString
import com.centuryprogrammer18thwasentsingleland.utils.getCurrentTimestamp
import kotlinx.android.synthetic.main.manager_firebase_signup_frg.*

//for studying DialogFragment
// ref) two ways DialogFragment https://blog.mindorks.com/implementing-dialog-fragment-in-android
// ref) DialogFragment with Navigation https://stackoverflow.com/a/55256858/3151712
// ref) DialogFragmetn https://stackoverflow.com/a/47514643/3151712
// ref) youtube Android Studio DIalog Fragment Tutorial https://www.youtube.com/watch?v=alV6wxrbULs&t=2s

class VoidInvoiceDialogFrg : DialogFragment(), RemoveInvoiceOrderCallback {
    private val TAG = VoidInvoiceDialogFrg::class.java.simpleName

    private lateinit var args : VoidInvoiceDialogFrgArgs

    private lateinit var binding : VoidedInvoiceDialogFrgBinding

    private lateinit var masterKeyAlias : String
    private lateinit var sharedPrefs : SharedPreferences

    val viewModel : VoidInvoiceDialogFrgVM by viewModels()

    private lateinit var customer : Customer
    private lateinit var invoiceOrder : InvoiceOrder

    private var setFullScreen:Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("Hey", "onCreate")
        var  setFullScreen = false

        args = VoidInvoiceDialogFrgArgs.fromBundle(requireArguments())
        customer = args.customer
        invoiceOrder = args.invoiceOrder

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle(getString(R.string.void_invoice))
        builder.setMessage(getString(R.string.Would_you_like_to_void_invoice,args.invoiceOrder!!.id.toString()))

        binding = DataBindingUtil.inflate(layoutInflater,R.layout.voided_invoice_dialog_frg ,null,false)

        binding.frg = this

        builder.setView(binding.root)

        return builder.create()
    }

    fun onClickRemvoBtn(){
        Log.d(TAG, "***** onClickRemvoBtn *****")

        // update InvoiceOrder with void data
        invoiceOrder.isVoid = true
        invoiceOrder.voidAt = getCurrentTimeString()
        invoiceOrder.voidBy = sharedPrefs.getString("logged_team_id", null)
        invoiceOrder.voidAtTimestamp = getCurrentTimestamp()

        Repository.removeInvoiceOrder(sharedPrefs,customer,invoiceOrder, this@VoidInvoiceDialogFrg)
    }

    fun onClickCancelBtn(){
        Log.d(TAG, "***** onClickCancelBtn *****")

        dismiss()
    }



    override fun onRemoveInvoiceOrderCallback(isSuccess: Boolean) {
        if(isSuccess){
            Log.d(TAG,"***** onRemoveInvoiceOrderCallback | REMOVED SUCCESSFULLY *****")
//            dismiss()

            Log.d(TAG, "***** moveToDropPickupAct *****")

            val intent = Intent(requireActivity(), DropPickupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}