package com.centuryprogrammer18thwasentsingleland.sales

import android.app.DatePickerDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.DashboardSalesFrgBinding
import java.lang.StringBuilder
import java.util.*

class DashboardSalesFrg : Fragment() {
    private val TAG = DashboardSalesFrg::class.java.simpleName

    lateinit var binding: DashboardSalesFrgBinding

    lateinit var masterKeyAlias :String
    lateinit var sharedPrefs : SharedPreferences

    lateinit var actViewModel : SalesActVM

    val viewModel: DashboardSalesFrgVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initEncrypSharedPrefs()

        actViewModel = (activity as SalesAct).viewModel

        viewModel.updateSharedPrefs(sharedPrefs)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater, R.layout.dashboard_sales_frg,container, false)

        // two way data binding
        binding.setLifecycleOwner(viewLifecycleOwner)

        binding.frg = this
        binding.viewModel = viewModel



        viewModel.startDate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                // month start from zero , so plus 1
                binding.tvStartDateDashboardSalesFrg.text =
                    StringBuilder(it["month"]!!.plus(1)
                        .toString())
                        .append("-")
                        .append(it["day"].toString())
                        .append("-")
                        .append(it["year"].toString())

                viewModel.entDate.value?.let {
                    if(!it.isEmpty()){
                        // start date and end date entered both, show search button
                        binding.btnSearchDashboardSalesFrg.isEnabled = true
                    }
                }
            }
        })

        viewModel.entDate.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                // month start from zero , so plus 1
                binding.tvEndDateDashboardSalesFrg.text =
                    StringBuilder(it["month"]!!.plus(1)
                        .toString())
                        .append("-")
                        .append(it["day"].toString())
                        .append("-")
                        .append(it["year"].toString())

                viewModel.entDate.value?.let {
                    if(!it.isEmpty()){
                        // start date and end date entered both, show search button
                        binding.btnSearchDashboardSalesFrg.isEnabled = true
                    }
                }
            }
        })

        viewModel.receivedInvoiceOrderBriefs.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            actViewModel.updateInvoiceOrderBriefs(it)
        })

        viewModel.isUpdatedInvoiceVars.observe(viewLifecycleOwner, androidx.lifecycle.Observer {
            Log.d(TAG, "***** invoice vars updated *****")

            binding.btnInvoiceListDashboardSalesFrg.isEnabled =
                actViewModel.invoiceOrderBriefs.value != null && actViewModel.invoiceOrderBriefs.value!!.count() > 0
        })

        viewModel.isUpdatedPickupVars.observe(viewLifecycleOwner, androidx.lifecycle.Observer {

            if(actViewModel.payments.value != null && actViewModel.payments.value!!.count() > 0){
                with(binding){
                    btnPrepaidPaidListDashboardSalesFrg.isEnabled = true
                    btnPrepaidPickupListDashboardSalesFrg.isEnabled = true
                    btnCreditPickupListDashboardSalesFrg.isEnabled = true
                    btnCreditPaybackListDashboardSalesFrg.isEnabled = true
                    btnPaymentListDashboardSalesFrg.isEnabled = true
                }
            }else{
                with(binding){
                    btnPrepaidPaidListDashboardSalesFrg.isEnabled = false
                    btnPrepaidPickupListDashboardSalesFrg.isEnabled = false
                    btnCreditPickupListDashboardSalesFrg.isEnabled = false
                    btnCreditPaybackListDashboardSalesFrg.isEnabled = false
                    btnPaymentListDashboardSalesFrg.isEnabled = false
                }
            }
        })


        return binding.root
    }

    fun onClickStartBtn(){
        Log.d(TAG, "***** onClickStartBtn *****")

        // get today date
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
//            val hour: Int = cal.get(Calendar.HOUR)
//            val minutes: Int = cal.get(Calendar.HOUR)
//            val ampm: Int = cal.get(Calendar.AM_PM)

        // date picker for setting up due date
        // date picker ref) https://stackoverflow.com/a/61865742/3151712
        // select date event will be handled by viewModel
        val dialog = DatePickerDialog(requireContext(), viewModel, year, month, day)

        // user can select only past date or today
        dialog.datePicker.maxDate = cal.timeInMillis

        // user can select only past date or today
        cal.add(Calendar.YEAR,-3)
        dialog.datePicker.minDate = cal.timeInMillis

        // i will share onDateSet(), so set tag
        dialog.datePicker.setTag("start")

        dialog.show()

    }

    fun onClickEndBtn(){
        Log.d(TAG, "***** onClickEndBtn *****")

        // get today date
        val cal: Calendar = Calendar.getInstance()
        val year: Int = cal.get(Calendar.YEAR)
        val month: Int = cal.get(Calendar.MONTH)
        val day: Int = cal.get(Calendar.DAY_OF_MONTH)
//            val hour: Int = cal.get(Calendar.HOUR)
//            val minutes: Int = cal.get(Calendar.HOUR)
//            val ampm: Int = cal.get(Calendar.AM_PM)

        // date picker for setting up due date
        // date picker ref) https://stackoverflow.com/a/61865742/3151712
        // select date event will be handled by viewModel
        val dialog = DatePickerDialog(requireContext(), viewModel, year, month, day)

        // user can select only past date or today
        dialog.datePicker.maxDate = cal.timeInMillis

        // user can select only past date or today
        cal.add(Calendar.YEAR,-3)
        dialog.datePicker.minDate = cal.timeInMillis



        // i will share onDateSet(), so set tag
        dialog.datePicker.setTag("end")

        dialog.show()

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