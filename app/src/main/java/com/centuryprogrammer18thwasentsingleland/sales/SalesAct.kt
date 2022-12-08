package com.centuryprogrammer18thwasentsingleland.sales

import android.app.DatePickerDialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.conveyor.ConveyorAct
import com.centuryprogrammer18thwasentsingleland.databinding.ActSalesBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.inventory.InventoryAct
import com.centuryprogrammer18thwasentsingleland.login.ManagerLoginAct
import com.centuryprogrammer18thwasentsingleland.login.ReSignInAct
import com.centuryprogrammer18thwasentsingleland.manager.ItemsPricesActivity
import com.centuryprogrammer18thwasentsingleland.rack.RackAct
import com.centuryprogrammer18thwasentsingleland.release.ReleaseAct
import com.centuryprogrammer18thwasentsingleland.search.SearchAct
import com.centuryprogrammer18thwasentsingleland.utils.isManagerSignedIn
import com.centuryprogrammer18thwasentsingleland.utils.isTeamSignedIn
import com.google.firebase.auth.FirebaseAuth
import java.util.*

class SalesAct : AppCompatActivity() {
    private val TAG = SalesAct::class.java.simpleName

    lateinit var binding : ActSalesBinding

    lateinit var masterKeyAlias :String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel : SalesActVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check auth.currentUser is null , if currentUser is null, send user to re sign-in
        checkUserLoggedIn()

        setActionBarTitle(getString(R.string.sales))

        binding = DataBindingUtil.setContentView(this,R.layout.act_sales)
        binding.act = this
        binding.viewModel = viewModel


        initEncrypSharedPrefs()
        if(!isTeamSignedIn(sharedPrefs)){
            // unauthorized access
            finish()
        }

        viewModel.updateSharedPrefs(sharedPrefs)

        viewModel.startDate.observe(this, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                // month start from zero , so plus 1
                binding.tvStartDateSalesAct.text =
                    java.lang.StringBuilder(
                        it["month"]!!.plus(1)
                            .toString()
                    )
                        .append("-")
                        .append(it["day"].toString())
                        .append("-")
                        .append(it["year"].toString())

                viewModel.entDate.value?.let {
                    if(!it.isEmpty()){
                        // start date and end date entered both, show search button
                        binding.btnSearchSalesAct.isEnabled = true
                        binding.btnPickupListSalesAct.isEnabled = true
                        binding.btnVoidListSalesAct.isEnabled = true
                        binding.btnAdjustListSalesAct.isEnabled = true
                        binding.btnDropoffListSalesAct.isEnabled = true
                    }else{
                        binding.btnSearchSalesAct.isEnabled = false
                        binding.btnPickupListSalesAct.isEnabled = false
                        binding.btnVoidListSalesAct.isEnabled = false
                        binding.btnAdjustListSalesAct.isEnabled = false
                        binding.btnDropoffListSalesAct.isEnabled = false
                    }
                }
            }
        })

        viewModel.entDate.observe(this, androidx.lifecycle.Observer {
            if(!it.isEmpty()){
                // month start from zero , so plus 1
                binding.tvEndDateSalesAct.text =
                    java.lang.StringBuilder(
                        it["month"]!!.plus(1)
                            .toString()
                    )
                        .append("-")
                        .append(it["day"].toString())
                        .append("-")
                        .append(it["year"].toString())

                viewModel.entDate.value?.let {
                    if(!it.isEmpty()){
                        // start date and end date entered both, show search button
                        binding.btnSearchSalesAct.isEnabled = true
                        binding.btnPickupListSalesAct.isEnabled = true
                        binding.btnVoidListSalesAct.isEnabled = true
                        binding.btnAdjustListSalesAct.isEnabled = true
                        binding.btnDropoffListSalesAct.isEnabled = true
                    }else{
                        binding.btnSearchSalesAct.isEnabled = false
                        binding.btnPickupListSalesAct.isEnabled = false
                        binding.btnVoidListSalesAct.isEnabled = false
                        binding.btnAdjustListSalesAct.isEnabled = false
                        binding.btnDropoffListSalesAct.isEnabled = false
                    }
                }
            }
        })

        viewModel.invoiceOrderBriefs.observe(this, androidx.lifecycle.Observer { invoiceOrderBriefs ->
            binding.btnInvoiceSalesSalesAct.isEnabled =
                invoiceOrderBriefs != null && invoiceOrderBriefs.count() > 0

            binding.btnInvoiceListSalesAct.isEnabled =
                invoiceOrderBriefs != null && invoiceOrderBriefs.count() > 0

            if(invoiceOrderBriefs != null && invoiceOrderBriefs.count() > 0){
                val navCont = findNavController(R.id.salesActnavHostfrg)
                navCont.setGraph(R.navigation.invoices_sales_nav)
            }
        })

        viewModel.payments.observe(this, androidx.lifecycle.Observer { payments ->
            if(payments != null && payments.count() > 0){
                with(binding){
                    btnPickupSalesSalesAct.isEnabled = true
                    btnPrepaidPaidListSalesAct.isEnabled = true
                    btnPrepaidPickupListSalesAct.isEnabled = true
                    btnCreditPickupListSalesAct.isEnabled = true
                    btnCreditPaybackListSalesAct.isEnabled = true
                    btnPaymentListSalesAct.isEnabled = true
                }

                val navCont = findNavController(R.id.salesActnavHostfrg)
                navCont.setGraph(R.navigation.pickups_sales_nav)

            }else{
                with(binding){

                    // pickup, prepaid-paid, prepaid-pickup , credit-pickup, credit-payback , payment can be found using payments in VM

                    btnPickupSalesSalesAct.isEnabled = false
                    btnPrepaidPaidListSalesAct.isEnabled = false
                    btnPrepaidPickupListSalesAct.isEnabled = false
                    btnCreditPickupListSalesAct.isEnabled = false
                    btnCreditPaybackListSalesAct.isEnabled = false
                    btnPaymentListSalesAct.isEnabled = false
                }
            }
        })

        viewModel.checkedRadioBtnId.observe(this, androidx.lifecycle.Observer {
            Log.d(TAG,"***** radio button checked changed *****")
            Log.d(TAG,"***** checked radio button id : ${it.toString()} *****")

            when(it){
                R.id.rbtnInvoiceSalesAct -> {
                    Log.d(TAG,"***** invoices radio button checked changed *****")

                    // check if radio button status changed by user or program
                    if(binding.rbtnInvoiceSalesAct.isPressed){
                        // by user , not program

                        Log.d(TAG,"***** by user *****")
                        Log.d(TAG,"***** invoices radio button isChecked : ${binding.rbtnInvoiceSalesAct.isChecked.toString()} +++++")

                        if(binding.rbtnInvoiceSalesAct.isChecked){

                        }else{
                            Log.d(TAG,"***** invoice is NOT checked, i will deleted invoiceOrderBriefs *****")
                            viewModel.clearInvoiceOrderBriefs()
                        }
                    }
                }

                R.id.rbtnPickupSalesAct -> {
                    Log.d(TAG,"***** invoices radio button checked changed *****")

                    // check if radio button status changed by user or program
                    if(binding.rbtnPickupSalesAct.isPressed){
                        // by user , not program

                        Log.d(TAG,"***** by user *****")
                        Log.d(TAG,"***** pickup radio button isChecked : ${binding.rbtnPickupSalesAct.isChecked.toString()} +++++")

                        if(binding.rbtnPickupSalesAct.isChecked){

                        }else{
                            Log.d(TAG,"***** pickup is NOT checked, i will deleted payments *****")
                            viewModel.clearPayments()
                        }
                    }
                }

                -1 -> {
                    Log.d(TAG,"***** not any radio button checked *****")
                }
            }

        })
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
        val dialog = DatePickerDialog(this, viewModel, year, month, day)

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
        val dialog = DatePickerDialog(this, viewModel, year, month, day)

        // user can select only past date or today
        dialog.datePicker.maxDate = cal.timeInMillis

        // user can select only past date or today
        cal.add(Calendar.YEAR,-3)
        dialog.datePicker.minDate = cal.timeInMillis



        // i will share onDateSet(), so set tag
        dialog.datePicker.setTag("end")

        dialog.show()

    }

    fun onInvoiceBtnClicked(){
        Log.d(TAG, "***** onInvoiceBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)
        navCont.setGraph(R.navigation.invoices_sales_nav)
    }

    fun onInvoiceListBtnClicked(){
        Log.d(TAG, "***** onInvoiceListBtnClicked *****")

        val bundle = Bundle()
        bundle.putString("categoryName","all_invoices")

        val startDateArray = arrayOf(viewModel.startDate.value!!["year"]!!, viewModel.startDate.value!!["month"]!!, viewModel.startDate.value!!["day"]!!)
        bundle.putIntArray("startDate",startDateArray.toList().toIntArray())

        val endDateArray = arrayOf(viewModel.entDate.value!!["year"]!!, viewModel.entDate.value!!["month"]!!, viewModel.entDate.value!!["day"]!!)
        bundle.putIntArray("endDate",endDateArray.toList().toIntArray())

        val navCont = findNavController(R.id.salesActnavHostfrg)
        navCont.setGraph(R.navigation.categorized_invoices_nav, bundle)
    }

    fun onVoidedBtnClicked(){
        Log.d(TAG, "***** onInvoiceBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)

        val bundle = Bundle()
        bundle.putString("categoryName","voided")

        val startDateArray = arrayOf(viewModel.startDate.value!!["year"]!!, viewModel.startDate.value!!["month"]!!, viewModel.startDate.value!!["day"]!!)
        bundle.putIntArray("startDate",startDateArray.toList().toIntArray())

        val endDateArray = arrayOf(viewModel.entDate.value!!["year"]!!, viewModel.entDate.value!!["month"]!!, viewModel.entDate.value!!["day"]!!)
        bundle.putIntArray("endDate",endDateArray.toList().toIntArray())

        navCont.setGraph(R.navigation.categorized_invoices_nav, bundle)
    }

    fun onAdjustedBtnClicked(){
        Log.d(TAG, "***** onAdjustedBtnClicked *****")

        val bundle = Bundle()
        bundle.putString("categoryName","adjusted")

        val startDateArray = arrayOf(viewModel.startDate.value!!["year"]!!, viewModel.startDate.value!!["month"]!!, viewModel.startDate.value!!["day"]!!)
        bundle.putIntArray("startDate",startDateArray.toList().toIntArray())

        val endDateArray = arrayOf(viewModel.entDate.value!!["year"]!!, viewModel.entDate.value!!["month"]!!, viewModel.entDate.value!!["day"]!!)
        bundle.putIntArray("endDate",endDateArray.toList().toIntArray())

        val navCont = findNavController(R.id.salesActnavHostfrg)
        navCont.setGraph(R.navigation.categorized_invoices_nav, bundle)
    }

    fun onDropoffsBtnClicked(){
        Log.d(TAG, "***** onDropoffsBtnClicked *****")

        val bundle = Bundle()

        val startDateArray = arrayOf(viewModel.startDate.value!!["year"]!!, viewModel.startDate.value!!["month"]!!, viewModel.startDate.value!!["day"]!!)
        bundle.putIntArray("startDate",startDateArray.toList().toIntArray())

        val endDateArray = arrayOf(viewModel.entDate.value!!["year"]!!, viewModel.entDate.value!!["month"]!!, viewModel.entDate.value!!["day"]!!)
        bundle.putIntArray("endDate",endDateArray.toList().toIntArray())

        val navCont = findNavController(R.id.salesActnavHostfrg)
        navCont.setGraph(R.navigation.dropoff_sales_nav, bundle)
    }

    fun onPaymentsBtnClicked(){
        Log.d(TAG, "***** onPaymentsBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)

        val bundle = Bundle()
        bundle.putString("categoryName","all_payments")

        navCont.setGraph(R.navigation.categorized_payment_nav,bundle)
    }

    fun onPickupBtnClicked(){
        Log.d(TAG, "***** onPickupBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)
        navCont.setGraph(R.navigation.pickups_sales_nav)
    }

    fun onPickupListBtnClicked(){
        Log.d(TAG, "***** onPickupListBtnClicked *****")

        val bundle = Bundle()

        val startDateArray = arrayOf(viewModel.startDate.value!!["year"]!!, viewModel.startDate.value!!["month"]!!, viewModel.startDate.value!!["day"]!!)
        bundle.putIntArray("startDate",startDateArray.toList().toIntArray())

        val endDateArray = arrayOf(viewModel.entDate.value!!["year"]!!, viewModel.entDate.value!!["month"]!!, viewModel.entDate.value!!["day"]!!)
        bundle.putIntArray("endDate",endDateArray.toList().toIntArray())

        val navCont = findNavController(R.id.salesActnavHostfrg)
        navCont.setGraph(R.navigation.pickup_list_sales_nav, bundle)
    }

    fun onPrepaidPaidBtnClicked(){
        Log.d(TAG, "***** onPrepaidPaidBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)

        val bundle = Bundle()
        bundle.putString("categoryName","prepaid_paid")

        navCont.setGraph(R.navigation.categorized_payment_nav,bundle)
    }

    fun onPrepaidPickupBtnClicked(){
        Log.d(TAG, "***** onPrepaidPickupBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)

        val bundle = Bundle()
        bundle.putString("categoryName","prepaid_pickup")

        navCont.setGraph(R.navigation.categorized_payment_nav,bundle)
    }

    fun onCreditPickupBtnClicked(){
        Log.d(TAG, "***** onCreditPickupBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)

        val bundle = Bundle()
        bundle.putString("categoryName","credit_pickup")

        navCont.setGraph(R.navigation.categorized_payment_nav,bundle)
    }

    fun onCreditPaybackBtnClicked(){
        Log.d(TAG, "***** onCreditPaybackBtnClicked *****")

        val navCont = findNavController(R.id.salesActnavHostfrg)

        val bundle = Bundle()
        bundle.putString("categoryName","credit_payback")

        navCont.setGraph(R.navigation.categorized_payment_nav,bundle)
    }




    // if this is the last activity , sent user to DropPickupActivity
    override fun onBackPressed() {
        Log.d(TAG,"***** onBackPressed  *****")

        if(isTaskRoot){
            Log.d(TAG,"***** This is last activity in the activity stack  *****")
            // This is last activity

            val intent = Intent(this, DropPickupActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }else{
            // there are more activity in the stack

            super.onBackPressed()
        }
    }

///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// option menu

    override fun onCreateOptionsMenu(menu: Menu?): Boolean{
        Log.d(TAG,"===== onCreateOptionsMenu  =====")

//        ref https://www.journaldev.com/9357/android-actionbar-example-tutorial

        if(isManagerSignedIn(sharedPrefs)){
            // manager signed in ,so make action bar for manager
            menuInflater.inflate(R.menu.manager_option_small_menu,menu)
        }else{
            // normal action bar for team

            menuInflater.inflate(R.menu.team_option_men,menu)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        when(item?.itemId){

            R.id.mnManagerHome, R.id.mnTeamHome -> moveToDropPickupAct()
            R.id.mnManagerSearch, R.id.mnTeamSearch -> moveToSearchAct()
            R.id.mnManagerAddTeam -> addTeam()
            R.id.mnManagerRack, R.id.mnTeamRack -> moveToRackAct()
            R.id.mnManagerItemsPrices -> moveToItemsPricesAct()
            R.id.mnManagerInventory, R.id.mnTeamInventory -> moveToInventoryAct()
            R.id.mnManagerConveyor, R.id.mnTeamConveyor -> moveToConveyorAct()
            R.id.mnManagerSales -> moveToSalesAct()
            R.id.mnManagerRelease, R.id.mnTeamRelease-> moveToReleaseAct()
            R.id.mnManagerOptionSetting -> moveToInitSettingFrg()
            R.id.mnManagerSignout, R.id.mnTeamSignout  -> signout()

        }

        return super.onOptionsItemSelected(item)
    }

    fun moveToDropPickupAct(){
        Log.d(TAG, "***** moveToDropPickupAct *****")

        val intent = Intent(this, DropPickupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToSearchAct(){
        Log.d(TAG, "***** moveToSearchAct *****")

        val intent = Intent(this, SearchAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun addTeam(){
        //R.id.myNavHostFragment need to be change, but i won't use this menu anyway
        val navCont = findNavController(R.id.myNavHostFragment)
        navCont.setGraph(R.navigation.team_nav)
    }

    fun moveToRackAct(){
        Log.d(TAG, "***** moveToRackAct *****")

        val intent = Intent(this, RackAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToItemsPricesAct(){
        Log.d(TAG, "***** moveToItemsPricesAct *****")

        val intent = Intent(this, ItemsPricesActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToInventoryAct(){
        Log.d(TAG, "***** moveToInventoryAct *****")

        val intent = Intent(this, InventoryAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToConveyorAct(){
        Log.d(TAG, "***** moveToConveyorAct *****")

        val intent = Intent(this, ConveyorAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToSalesAct(){
        Log.d(TAG, "***** moveToSalesAct *****")

        val intent = Intent(this, SalesAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToReleaseAct(){
        Log.d(TAG, "***** moveToReleaseAct *****")

        val intent = Intent(this, ReleaseAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToInitSettingFrg(){
        //R.id.myNavHostFragment need to be change, but i won't use this menu anyway
        val navCont = findNavController(R.id.myNavHostFragment)
        navCont.setGraph(R.navigation.manager_firebase_login_nav)
    }

    fun signout(){
        // clean logged team id from shared preferences
        sharedPrefs.edit().putString("logged_team_id",null).apply()

        // send user team login fragment through ManagerLoginAct
        val intent = Intent(this, ManagerLoginAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun checkUserLoggedIn(){
        var auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null){
            // user status is logged out, user need to logged in , send user ReSignin

            val intent = Intent(this, ReSignInAct::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
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
            this,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun setActionBarTitle(subTitle: String?) {
        val title = StringBuilder().append(getString(R.string.app_name)).append(" - ").append(subTitle).toString()
        Log.d(TAG,"***** title is ${title} *****")
        supportActionBar?.setTitle(title)
    }
}