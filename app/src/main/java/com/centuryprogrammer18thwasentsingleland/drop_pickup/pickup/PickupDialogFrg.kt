package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.app.Dialog
import android.content.SharedPreferences
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.InvoiceWorksDialogUpgradedFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.PickupDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.invoice_works.FabricareInvoiceWorksApt
import com.centuryprogrammer18thwasentsingleland.invoice_works.InvoiceWorksDialogUpgradedFrg
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialString
import com.centuryprogrammer18thwasentsingleland.utils.makeTwoPointsDecialStringWithDollar
import kotlinx.android.synthetic.main.dropoff_frg.*
import java.lang.StringBuilder

class PickupDialogFrg : DialogFragment() {
    private val TAG = PickupDialogFrg::class.java.simpleName

    private lateinit var args : PickupDialogFrgArgs

    private lateinit var binding : PickupDialogFrgBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    private val viewModel: PickupDialogFrgVM by viewModels()

    private var pickupId : Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        args = PickupDialogFrgArgs.fromBundle(requireArguments())

        pickupId = args.pickupId

        initEncrypSharedPrefs()
        viewModel.initSharedPrefs(sharedPrefs)
        viewModel.initPickupId(pickupId!!)
        viewModel.getPickup()
    }

    // dialog fragment window size change ref) https://stackoverflow.com/a/58179977/3151712
    override fun onResume() {
        super.onResume()

        val window = dialog!!.window ?: return
        val params = window.attributes
//        params.width = LinearLayout.LayoutParams.MATCH_PARENT
//        params.height = LinearLayout.LayoutParams.MATCH_PARENT
        params.width = 800
        params.height = 510

        window.attributes = params
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = DataBindingUtil.inflate(layoutInflater,R.layout.pickup_dialog_frg,null, false)

        val alertDialogBuilder = AlertDialog.Builder(requireContext())

        alertDialogBuilder.setView(binding.root)
        alertDialogBuilder.setCancelable(false)

        viewModel.foundPickup.observe(this, Observer {
            with(binding){

                var itemStr = ""

                for(id in it.invoiceOrderIds!!){
                    val strBuilder = StringBuilder()

                    strBuilder.append(getString(R.string.invoice_id_colon)).append(id).append("\n")

                    it.priceStatements?.let {
                        val dryPrice = makeTwoPointsDecialString(it[id.toString()]!!["dryPrice"]!!)
                        val wetPrice = makeTwoPointsDecialString(it[id.toString()]!!["wetPrice"]!!)
                        val alterPrice = makeTwoPointsDecialString(it[id.toString()]!!["alterPrice"]!!)
                        val subTotal = makeTwoPointsDecialString(it[id.toString()]!!["subTotal"]!!)
                        val tax = makeTwoPointsDecialString(it[id.toString()]!!["tax"]!!)
                        val env = makeTwoPointsDecialString(it[id.toString()]!!["env"]!!)
                        val discount = makeTwoPointsDecialString(it[id.toString()]!!["discount"]!!)
                        val total = makeTwoPointsDecialString(it[id.toString()]!!["total"]!!)
                        val prepaid = makeTwoPointsDecialString(it[id.toString()]!!["prepaid"]!!)
                        val balance = makeTwoPointsDecialString(it[id.toString()]!!["balance"]!!)


                        strBuilder.append("\t\t").append(getString(R.string.dry_colon_dollar)).append(dryPrice)
                        strBuilder.append("\t\t").append(getString(R.string.wet_colon_dollar)).append(wetPrice)
                        strBuilder.append("\t\t").append(getString(R.string.alter_colon_dollar)).append(alterPrice)
                        strBuilder.append("\t\t").append(getString(R.string.subtotal_colon_dollar)).append(subTotal).append("\n")
                        strBuilder.append("\t\t").append(getString(R.string.tax_colon_dollar)).append(tax)
                        strBuilder.append("\t\t").append(getString(R.string.env_colon_dollar)).append(env)
                        strBuilder.append("\t\t").append(getString(R.string.discount_colon_minus_dollar)).append(discount)
                        strBuilder.append("\t\t").append(getString(R.string.total_colon_dollar)).append(total).append("\n")
                        strBuilder.append("\t\t").append(getString(R.string.prepaid_colon_dollar)).append(prepaid)
                        strBuilder.append("\t\t").append(getString(R.string.balance_colon_dollar)).append(balance).append("\n\n\n")
                    }
                    itemStr += strBuilder.toString()
                }

                // textview scroll scrollable ref) https://stackoverflow.com/a/56355714/3151712
                tvIdAndPriceTablePickupDialogFrg.movementMethod= ScrollingMovementMethod()
                tvIdAndPriceTablePickupDialogFrg.text = itemStr

                tvBalancePickupDialogFrg.text = makeTwoPointsDecialStringWithDollar(it.balance!!)
                tvPaidAmountPickupDialogFrg.text = makeTwoPointsDecialStringWithDollar(it.realPaidAmount!!)
                tvMethodPickupDialogFrg.text = it.payMethod!!
                tvCheckNumPickupDialogFrg.text = it.checkNum
                tvPickupAtPickupDialogFrg.text = it.pickUpAt
                tvPickupByPickupDialogFrg.text = it.pickedUpBy
            }
        })

        return alertDialogBuilder.create()
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