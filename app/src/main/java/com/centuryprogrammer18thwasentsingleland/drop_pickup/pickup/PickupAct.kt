package com.centuryprogrammer18thwasentsingleland.drop_pickup.pickup

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.navArgs
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.conveyor.ConveyorAct
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.data.InvoiceOrder
import com.centuryprogrammer18thwasentsingleland.data.InvoiceWithPayment
import com.centuryprogrammer18thwasentsingleland.data.PickupHistory
import com.centuryprogrammer18thwasentsingleland.databinding.ActPickupBinding
import com.centuryprogrammer18thwasentsingleland.databinding.CheckDialogPickupBinding
import com.centuryprogrammer18thwasentsingleland.databinding.FinishDialogPickupBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.inventory.InventoryAct
import com.centuryprogrammer18thwasentsingleland.login.ManagerLoginAct
import com.centuryprogrammer18thwasentsingleland.login.ReSignInAct
import com.centuryprogrammer18thwasentsingleland.manager.ItemsPricesActivity
import com.centuryprogrammer18thwasentsingleland.rack.RackAct
import com.centuryprogrammer18thwasentsingleland.release.ReleaseAct
import com.centuryprogrammer18thwasentsingleland.sales.SalesAct
import com.centuryprogrammer18thwasentsingleland.search.SearchAct
import com.centuryprogrammer18thwasentsingleland.utils.*
import com.google.firebase.auth.FirebaseAuth

class PickupAct : AppCompatActivity() {
    private val TAG = PickupAct::class.java.simpleName

    //   safe args ref) https://developer.android.com/guide/navigation/navigation-pass-data#Safe-args
    val args: PickupActArgs by navArgs()

    lateinit var binding: ActPickupBinding

    lateinit var masterKeyAlias : String
    lateinit var sharedPrefs : SharedPreferences

    val viewModel : PickupActVM by viewModels()

    lateinit var customer: Customer
    lateinit var invoiceOrders : MutableList<InvoiceOrder>
    var lastPickupHistory : PickupHistory? = null
    var invoiceWithPayments = mutableListOf<InvoiceWithPayment>()

    private var checkAlert : AlertDialog? = null
    private var finishAlert :AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check auth.currentUser is null , if currentUser is null, send user to re sign-in
        checkUserLoggedIn()

        setActionBarTitle(getString(R.string.pick_up))

        Log.d(TAG,"***** onCreate *****")

        // showing softkeyboard
        // android keyboard soft ref) https://stackoverflow.com/a/5617130/3151712
//        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)

        // get args and variables init
        customer = args.customer
        invoiceOrders = args.invoiceOrders.toList().toMutableList()
        lastPickupHistory = args.lastPickupHistroy
        invoiceWithPayments = convertInvoiceOrdersIntoInvoiceWithPayments(invoiceOrders)

        initEncrypSharedPrefs()

        if(!isTeamSignedIn(sharedPrefs)){
            // unauthorized access
            finish()
        }

        viewModel.initCustomer(customer)
        viewModel.initSharedPrefs(sharedPrefs)
        viewModel.initLastPickupHistory(lastPickupHistory)
        viewModel.initInvoiceWithPayments(invoiceWithPayments)

        binding = DataBindingUtil.setContentView(this, R.layout.act_pickup)


        // setting up UserMakeInvoiceFrg. this should be called after DataBindingUtil.setContentView
        // and setting up InvoiceOrderPickupFrg
        setFragment()

        // init data binding variables
        binding.viewModel = viewModel
        binding.act = this

        // two way data binding observable with architecture component ref) https://developer.android.com/topic/libraries/data-binding/architecture#livedata
        binding.setLifecycleOwner(this)

        // init views
        with(binding){
            etPaidPickupAct.addTextChangedListener(
                CurrencyTextWatcher(etPaidPickupAct)
            )

            lastPickupHistory?.newBalance?.let {
                // last there is PickupHistory.newBalance

                if(it != 0.0){
                    // there was some credit or unpaid amount

                    // show unpaid amount on view
                    tvUnpaidPickupAct.text = makeTwoPointsDecialStringWithDollar(it)

                    // enable toggle button so user can include unpaid amount if user want

                    tbtnUnpaidPickupAct.isEnabled = true
                }
            }
        }

        viewModel.invoiceWithPayments.observe(this, Observer {
            Log.d(TAG, "\n\n\n***** invoiceWithPayments is updated and detected while PickupAct is observing invoiceWithPayments in ViewModel *****")
            for(item in it){
                Log.d(TAG,"***** '${item.invoiceOrderId.toString()}' '${item.typePayment.toString()}' '${item.amount.toString()}' '${item.isPaid.toString()}' *****")
            }

            invoiceWithPayments = it

            // check invoiceWithPayments and if there is checked invoicewithpayment
            // make done button enabled

            if (it !=null && it.count() >0 ){
                for(item in it){
                    if(item.isPaid !=null && item.isPaid!!){
                        // there is more than one InvoiceWithOrder user chose, so user can make payment

                        Log.d(TAG,"***** there is user selected InvoiceWithPaymet, so show Done Button *****\n\n\n")
                        binding.btnDonePickupAct.isEnabled = true
                        break
                    }
                }
            }
        })

        viewModel.balance.observe(this, Observer {
            binding.tvBalancePickupAct.text = makeTwoPointsDecialStringWithDollar(it)

            // whenever i get new balance, refresh etPaidPickupAct
            binding.etPaidPickupAct.setText(makeTwoPointsDecialString(0.0))
        })

        viewModel.credit.observe(this, Observer {
            binding.tvCreditPickupAct.text = makeTwoPointsDecialStringWithDollar(it)
        })

        viewModel.qty.observe(this, Observer {
            binding.tvQtyPickupAct.text =  getString(R.string.qty)+" "+it.toString()
        })


        viewModel.msgToAct.observe(this, Observer {
            if(it.isNotEmpty()){
                // Toast extension function
                Toast(this).showCustomToast(this,it)
            }
        })

        viewModel.closeCheckDialog.observe(this, Observer {
            if(it){
                // close dialog
                checkAlert!!.dismiss()
            }
        })

        viewModel.unpaidIncluded.observe(this, Observer {
            if(it){
                binding.btnDonePickupAct.isEnabled = true
            }
        })

        // very last step. every pick up process is done. i will show change and let user close pick up process
        viewModel.openFinishDialog.observe(this, Observer {change ->

            // change is the amount which cashier need to give to customer
            openFinishDialog(change)
        })
    }



///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
// option menu

    override fun onCreateOptionsMenu(menu: Menu?): Boolean{
        Log.d(TAG,"===== onCreateOptionsMenu  =====")

//        ref https://www.journaldev.com/9357/android-actionbar-example-tutorial

        if(isManagerSignedIn(sharedPrefs)){
            // manager signed in ,so make action bar for manager
            menuInflater.inflate(R.menu.manager_option_menu,menu)
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

        val intent = Intent(this,DropPickupActivity::class.java)
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
        val navCont = findNavController(R.id.myNavHostFragment)
        navCont.setGraph(R.navigation.team_nav)
    }

    fun moveToRackAct(){
        Log.d(TAG, "***** moveToRackAct *****")

        val intent = Intent(this,RackAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    fun moveToItemsPricesAct(){
        Log.d(TAG, "***** moveToItemsPricesAct *****")

        val intent = Intent(this,ItemsPricesActivity::class.java)
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


///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////


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

    // set action bar titel
    fun setActionBarTitle(subTitle: String?) {
        val title = StringBuilder().append(getString(R.string.app_name)).append(" - ").append(subTitle).toString()
        Log.d(TAG,"***** title is ${title} *****")
        supportActionBar?.setTitle(title)
    }


    // very last step. every pick up process is done. i will show change and let user close pick up process
    // change is the amount cashier need to give to customer
    fun openFinishDialog(change:Double){
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setMessage(R.string.pickup_process_successfully)

        val inflater = LayoutInflater.from(this)
        val dialogBinding: FinishDialogPickupBinding = DataBindingUtil.inflate(inflater, R.layout.finish_dialog_pickup,null,false)

        dialogBinding.act = this

        // display change
        dialogBinding.tvChangeFinishDialogPickup.text = makeTwoPointsDecialStringWithDollar(change)

        alertBuilder.setView(dialogBinding.root)

        // outside touch will not close dialog
        alertBuilder.setCancelable(false)
        finishAlert = alertBuilder.create()
        finishAlert!!.show()
    }

    // user want to pay with check , let them enter check num
    fun onCheckBtnClicked(){
        val alertBuilder = AlertDialog.Builder(this)
        alertBuilder.setMessage(R.string.enter_check_num)

        val inflater = LayoutInflater.from(this)
        val dialogBinding: CheckDialogPickupBinding = DataBindingUtil.inflate(inflater, R.layout.check_dialog_pickup,null,false)

        dialogBinding.act = this
        dialogBinding.viewModel = viewModel

        alertBuilder.setView(dialogBinding.root)

        // outside touch will not close dialog
        alertBuilder.setCancelable(false)
        checkAlert = alertBuilder.create()
        checkAlert!!.show()
    }


    // called from 'finish_dialog_pickup.xml' when user click ok button
    fun onClickOkfinish(){
        // everything is done , send user to DropPickupActivity

        val intent = Intent(this, DropPickupActivity::class.java)
        startActivity(intent)
    }

    // setting UserMakeInvoiceFrg. this should be called after DataBindingUtil.setContentView
    // show user infos , user history
    fun setFragment(){

        // programmatically navigation pass parameter ref) https://stackoverflow.com/a/56448153/3151712
        // ref) https://stackoverflow.com/a/54613997/3151712
        // getting basic concept , but don't follow this ref) https://stackoverflow.com/a/53922244/3151712
        val navCont = findNavController(R.id.toUserPickupFrg)
        val bundle = Bundle()
        bundle.putSerializable("customer",args.customer)

        // i could use user_make_invoice_nav here too, so i didn't make another wheel
        navCont.setGraph(R.navigation.user_make_invoice_nav,bundle)

        val navContInvoice = findNavController(R.id.toInvoiceOrderPickupFrg)
        val bundleInvoice = Bundle()
        bundleInvoice.putSerializable("invoiceWithPayments",invoiceWithPayments.toList().toTypedArray())

        // i could use user_make_invoice_nav here too, so i didn't make another wheel
        navContInvoice.setGraph(R.navigation.invoice_order_pickup_nav,bundleInvoice)
    }

    fun convertInvoiceOrdersIntoInvoiceWithPayments(invoiceOrders : MutableList<InvoiceOrder>): MutableList<InvoiceWithPayment>{
        val invoiceWithPayments = mutableListOf<InvoiceWithPayment>()
        for(item in invoiceOrders){
            invoiceWithPayments.add(InvoiceWithPayment(item.id,item))
        }
        return invoiceWithPayments
    }

//    android leak alert dialog ref) https://stackoverflow.com/a/2850597/3151712
    override fun onDestroy() {
        super.onDestroy()
        checkAlert?.dismiss()
        finishAlert?.dismiss()
    }
}

