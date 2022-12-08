package com.centuryprogrammer18thwasentsingleland.invoice_works



import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceWorksDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import kotlinx.android.synthetic.main.invoice_works_dialog_upgraded_frg.*


class InvoiceWorksDialogFrg : DialogFragment() {
    private val TAG = InvoiceWorksDialogFrg::class.java.simpleName

//    private lateinit var args : InvoiceWorksDialogFrgArgs

    private lateinit var binding : InvoiceWorksDialogFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel: InvoiceWorksDialogFrgVM by viewModels()

    lateinit var customer:Customer
    lateinit var invoiceOrder:InvoiceOrder

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        args = InvoiceWorksDialogFrgArgs.fromBundle(requireArguments())
//        customer = args.customer
//        invoiceOrder = args.invoiceOrder

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

    // dialog fragment window size change ref) https://stackoverflow.com/a/58179977/3151712
    override fun onResume() {
        super.onResume()
        val window = dialog!!.window ?: return
        val params = window.attributes
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT
//        params.height = LinearLayout.LayoutParams.MATCH_PARENT
        params.width = 750
        params.height = 500

        window.attributes = params
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.invoice_works_dialog_frg, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        viewModel.messageOnFragment.observe(this, Observer {
            if (it != ""){
                // there is message to show

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        viewModel.closeDialog.observe(this, Observer {
            if(it){
                // close dialog

                dialog?.dismiss()
            }
        })

        binding = DataBindingUtil.inflate(layoutInflater,R.layout.invoice_works_dialog_frg,null, false)

        binding.viewModel = viewModel

        with(binding){
            tvFirstNameInvoiceWorksDialog.text = customer.firstName
            tvLastNameInvoiceWorksDialog.text = customer.lastName
            tvPhoneInvoiceWorksDialog.text = customer.phoneNum

            tvIdInvoiceWorksDialog.text = invoiceOrder.id.toString()
            tvCreatedAtInvoiceWorksDialog.text = invoiceOrder.createdAt
            tvDueDateTimeInvoiceWorksDialog.text = invoiceOrder.dueDateTime
            tvRackLocationInvoiceWorksDialog.text = invoiceOrder.rackLocation
            tvBalanceInvoiceWorksDialog.text = makeTwoPointsDecialStringWithDollar(invoiceOrder.priceStatement!!["balance"]!!)

            val totalQty = invoiceOrder.qtyTable!!["dry"]!!+invoiceOrder.qtyTable!!["wet"]!!+invoiceOrder.qtyTable!!["alter"]!!+invoiceOrder.qtyTable!!["clean"]!!+invoiceOrder.qtyTable!!["press"]!!

            tvTotalQtyInvoiceWorksDialog.text = totalQty.toString()
        }

        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setView(binding.root)


        return alertDialogBuilder.create()
    }


}