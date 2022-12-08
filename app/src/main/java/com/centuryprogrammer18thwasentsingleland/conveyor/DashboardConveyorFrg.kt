package com.centuryprogrammer18thwasentsingleland.conveyor

import android.app.DatePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.databinding.DashboardConveyorFrgBinding
import com.centuryprogrammer18thwasentsingleland.databinding.DashboardInventoryFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.InvoiceConveyorFrgCallback
import java.lang.StringBuilder
import java.util.*

class DashboardConveyorFrg : Fragment(), InvoiceConveyorFrgCallback {

    private val TAG = DashboardConveyorFrg::class.java.simpleName

    lateinit var binding : DashboardConveyorFrgBinding

    private lateinit var masterKeyAlias: String
    private lateinit var sharedPrefs: SharedPreferences

    val viewModel: DashboardConveyorFrgVM by viewModels()

    private lateinit var adapter : InvoiceConveyorApt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initEncrypSharedPrefs()

        viewModel.updateSharedPrefs(sharedPrefs)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.dashboard_conveyor_frg,container,false)
        binding.frg = this
        binding.viewModel = viewModel

        // two way data binding
        binding.setLifecycleOwner(viewLifecycleOwner)

        with(binding) {
            etRackLocationDashboardConveyorFrg.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    btnSearchDashboardConveyorFrg.isEnabled = true

                    btnStartDateDashboardConveyorFrg.isEnabled = false
                    btnEndDateDashboardConveyorFrg.isEnabled = false
                    btnThreeMonsDashboardConveyorFrg.isEnabled = false
                    btnSixMonsDashboardConveyorFrg.isEnabled =false
                    btnApplyDashboardConveyorFrg.isEnabled = false
                    spnDashboardConveyorFrg.isEnabled = false

                    viewModel?.clearInvoiceOrders()
                }
            }
        }

            val spinnerItems = resources.getStringArray(R.array.dashboard_conveyor_spinner_items)

//         spinner item space ref) https://stackoverflow.com/a/54526961/3151712
            binding.spnDashboardConveyorFrg.adapter = ArrayAdapter(requireContext(),android.R.layout.simple_spinner_dropdown_item,spinnerItems)

            binding.spnDashboardConveyorFrg.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
                override fun onNothingSelected(parent: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }

                override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                    when(position){
                        0 -> {
                            Log.d(TAG,"***** Created at, Invoice Id SELECTED *****")
                            adapter.onUpdatedHighLighted("createdAt")
                            viewModel.sortByCreatedAtInvoiceId()

                        }
                        1 -> {
                            Log.d(TAG,"***** Customer Name SELECTED *****")
                            adapter.onUpdatedHighLighted("customerName")
                            viewModel.sortByCustomerName()
                        }
                        2 -> {
                            Log.d(TAG,"***** Due Date SELECTED *****")
                            adapter.onUpdatedHighLighted("dueDate")
                            viewModel.sortByDueDate()
                        }
                        3 -> {
                            Log.d(TAG,"***** Picked up at SELECTED *****")
                            adapter.onUpdatedHighLighted("pickedUpAt")
                            viewModel.sortByPickedUpAt()
                        }
                    }
                }
            }


            adapter = InvoiceConveyorApt(viewModel,this)
            binding.rvDashboardConveyorFrg.adapter = adapter
            binding.rvDashboardConveyorFrg.layoutManager = LinearLayoutManager(requireContext(),
                LinearLayoutManager.VERTICAL,false)

            viewModel.conveyorInvoiceOrders.observe(viewLifecycleOwner, Observer {
                if(it != null && it.count() > 0){
                    // we got InvoiceOrders from firebase, so change UI , so user can apply filter

                    with(binding){

                        // remove focus edittext ref) https://stackoverflow.com/a/6120141/3151712
                        etRackLocationDashboardConveyorFrg.clearFocus()
                        val inputMethodManager = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        inputMethodManager.toggleSoftInput(InputMethodManager.HIDE_NOT_ALWAYS,0)


                        btnSearchDashboardConveyorFrg.isEnabled = false

                        btnStartDateDashboardConveyorFrg.isEnabled = true
                        btnEndDateDashboardConveyorFrg.isEnabled = true
                        btnThreeMonsDashboardConveyorFrg.isEnabled = true
                        btnSixMonsDashboardConveyorFrg.isEnabled =true
                        spnDashboardConveyorFrg.isEnabled = true
                    }
                }
            })

            viewModel.displayConveyorInvoiceOrders.observe(viewLifecycleOwner, Observer {
                if(it != null){
                    adapter.submitList(it)
                    adapter.notifyDataSetChanged()
                }
            })

            viewModel.startDate.observe(viewLifecycleOwner, Observer {
                if(!it.isEmpty()){
                    // month start from zero , so plus 1
                    binding.tvStartDateDashboardConveyorFrg.text =
                        StringBuilder(it["month"]!!.plus(1)
                            .toString())
                            .append("-")
                            .append(it["day"].toString())
                            .append("-")
                            .append(it["year"].toString())

                    viewModel.entDate.value?.let {
                        if(!it.isEmpty()){
                            // start date and end date entered both, show search button
                            binding.btnApplyDashboardConveyorFrg.isEnabled = true
                        }
                    }
                }
            })

        viewModel.entDate.observe(viewLifecycleOwner, Observer {
            if(!it.isEmpty()){
                // month start from zero , so plus 1
                binding.tvEndDateDashboardConveyorFrg.text =
                    StringBuilder(it["month"]!!.plus(1)
                        .toString())
                        .append("-")
                        .append(it["day"].toString())
                        .append("-")
                        .append(it["year"].toString())

                viewModel.entDate.value?.let {
                    if(!it.isEmpty()){
                        // start date and end date entered both, show search button
                        binding.btnApplyDashboardConveyorFrg.isEnabled = true
                    }
                }
            }
        })

            return binding.root
    }


    fun onClickStartBtn(){
        Log.d(TAG,"***** onClickStartBtn *****")

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
        cal.add(Calendar.YEAR,-2)
        dialog.datePicker.minDate = cal.timeInMillis

        // i will share onDateSet(), so set tag
        dialog.datePicker.setTag("start")

        dialog.show()
    }

    fun onClickEndBtn(){
        Log.d(TAG,"***** onClickEndBtn *****")

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
        cal.add(Calendar.YEAR,-2)
        dialog.datePicker.minDate = cal.timeInMillis



        // i will share onDateSet(), so set tag
        dialog.datePicker.setTag("end")

        dialog.show()
    }


    override fun onInovoiceOrderClicked(invoiceOrder: InvoiceOrder) {
        TODO("Not yet implemented")
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