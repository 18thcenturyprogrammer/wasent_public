package com.centuryprogrammer18thwasentsingleland.rack

import android.content.Context.INPUT_METHOD_SERVICE
import android.content.SharedPreferences
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.DoingRackFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast


class DoingRackFrg : Fragment() {
    private val TAG = DoingRackFrg::class.java.simpleName

    lateinit var binding : DoingRackFrgBinding

    lateinit var masterKeyAlias :String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel: DoingRackFrgVM by viewModels()

    private var imm : InputMethodManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.doing_rack_frg, container,false)

        binding.frg = this
        binding.viewModel = viewModel

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

        viewModel.setSharedPrefs(sharedPrefs)

        val adapater = InvoiceOrderRackApt()

        binding.rvInvoiceOrderDoingRackFrg.adapter = adapater
        binding.rvInvoiceOrderDoingRackFrg.layoutManager = GridLayoutManager(requireContext(),3,GridLayoutManager.VERTICAL,false)


        imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager

        viewModel.foundInvoiceOrder.observe(viewLifecycleOwner, Observer {
            Log.d(TAG,"***** foundInvoiceOrder : '${it.toString()}' *****")

            if(it != null){

                with(binding){
                    tvFirstNameDoingRackFrg.text = it.customer?.firstName
                    tvLastNameDoingRackFrg.text = it.customer?.lastName
                    tvPhoneDoingRackFrg.text = it.customer?.phoneNum
                    tvEmailDoingRackFrg.text = it.customer?.email
                    tvNoteDoingRackFrg.text = it.customer?.note

                    // textview scroll scrollable ref) https://stackoverflow.com/a/56355714/3151712
                    tvNoteDoingRackFrg.movementMethod = ScrollingMovementMethod()

                    tvCreateAtDoingRackFrg.text = getString(R.string.created_at_colon)+" "+it.createdAt
                    tvDueDateTimeDoingRackFrg.text = getString(R.string.due_colon)+" "+it.dueDateTime
                    tvInvoiceNoteDoingRackFrg.text = it.note

                    // textview scroll scrollable ref) https://stackoverflow.com/a/56355714/3151712
                    tvInvoiceNoteDoingRackFrg.movementMethod = ScrollingMovementMethod()
                }

            }else{
                showBasicUI()
            }
        })

        viewModel.rackedInvoiceOrders.observe(viewLifecycleOwner, Observer {
            if(it.count() !=0){
                adapater.submitList(it)
                adapater.notifyDataSetChanged()
            }
        })

        viewModel.isfinishedDoneProcess.observe(viewLifecycleOwner, Observer {
            if(it){
                showBasicUI()

                viewModel.clearVars()
            }
        })

        viewModel.msgToFrg.observe(viewLifecycleOwner, Observer {
            if(!it.isEmpty()){
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        return binding.root
    }

    fun onClickCancel(){
        Log.d(TAG, "----- onClickCancel -----")

        viewModel.clearVars()
    }

    fun onClickOk(){
        Log.d(TAG, "----- onClickOk -----")

        val invoiceId = binding.etInvoiceIdDoingRackFrg.text.toString()

        viewModel.getInvoiceById(sharedPrefs, invoiceId)

        okBtnClickedUI()
    }

    fun onClickDone(){
        Log.d(TAG, "----- onClickDone -----")

        val rackLocation = binding.etRackLocationDoingRackFrg.text.toString()

        if(!rackLocation.isEmpty()){

            viewModel.updateRackLocation(rackLocation)

        }else{
            // Toast extension function
            Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.empty_not_allowed))
        }

    }

    fun showBasicUI(){
        binding.etInvoiceIdDoingRackFrg.setText("")
        binding.etInvoiceIdDoingRackFrg.isEnabled = true
        binding.etInvoiceIdDoingRackFrg.requestFocus()

        // programmatically focus edittext show keyboard ref) https://stackoverflow.com/a/63374178/3151712
        imm?.showSoftInput(binding.etInvoiceIdDoingRackFrg, InputMethodManager.SHOW_IMPLICIT)

        binding.etRackLocationDoingRackFrg.setText("")
        binding.etRackLocationDoingRackFrg.isEnabled = false

        binding.btnOkDoingRackFrg.isEnabled = true
        binding.btnOkDoingRackFrg.visibility = View.VISIBLE

        binding.btnDoneDoingRackFrg.isEnabled = false
        binding.btnDoneDoingRackFrg.visibility = View.GONE

        binding.tvFirstNameDoingRackFrg.text = ""
        binding.tvLastNameDoingRackFrg.text = ""
        binding.tvPhoneDoingRackFrg.text = ""
        binding.tvEmailDoingRackFrg.text = ""
        binding.tvNoteDoingRackFrg.text = ""

        binding.tvCreateAtDoingRackFrg.text = ""
        binding.tvDueDateTimeDoingRackFrg.text=""
        binding.tvInvoiceNoteDoingRackFrg.text = ""
    }

    fun okBtnClickedUI(){
        binding.etInvoiceIdDoingRackFrg.isEnabled = false

        binding.etRackLocationDoingRackFrg.isEnabled = true
        binding.etRackLocationDoingRackFrg.requestFocus()

        // programmatically focus edittext show keyboard ref) https://stackoverflow.com/a/63374178/3151712
        imm?.showSoftInput(binding.etRackLocationDoingRackFrg, InputMethodManager.SHOW_IMPLICIT)

        binding.btnOkDoingRackFrg.isEnabled = false
        binding.btnOkDoingRackFrg.visibility = View.GONE

        binding.btnDoneDoingRackFrg.isEnabled = true
        binding.btnDoneDoingRackFrg.visibility = View.VISIBLE
    }


}