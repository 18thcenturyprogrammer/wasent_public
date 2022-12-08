package com.centuryprogrammer18thwasentsingleland.drop_pickup

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.conveyor.ConveyorAct
import com.centuryprogrammer18thwasentsingleland.data.Customer
import com.centuryprogrammer18thwasentsingleland.databinding.ActivityDropPickupBinding
import com.centuryprogrammer18thwasentsingleland.databinding.ExitDialogDropPickupBinding
import com.centuryprogrammer18thwasentsingleland.databinding.UpchargeDialogMakeInvoiceBinding
import com.centuryprogrammer18thwasentsingleland.inventory.InventoryAct
import com.centuryprogrammer18thwasentsingleland.login.ManagerLoginAct
import com.centuryprogrammer18thwasentsingleland.login.ReSignInAct
import com.centuryprogrammer18thwasentsingleland.manager.ItemsPricesActivity
import com.centuryprogrammer18thwasentsingleland.rack.RackAct
import com.centuryprogrammer18thwasentsingleland.release.ReleaseAct
import com.centuryprogrammer18thwasentsingleland.sales.SalesAct
import com.centuryprogrammer18thwasentsingleland.search.SearchAct
import com.centuryprogrammer18thwasentsingleland.stripe.StripeConnectAct
import com.centuryprogrammer18thwasentsingleland.utils.isManagerSignedIn
import com.centuryprogrammer18thwasentsingleland.utils.isTeamSignedIn
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_drop_pickup.*

class DropPickupActivity:AppCompatActivity() {
    private val TAG = DropPickupActivity::class.java.simpleName

    private lateinit var binding : ActivityDropPickupBinding

    private lateinit var masterKeyAlias: String
    private lateinit var sharedPrefs: SharedPreferences

    private var exitAlert : AlertDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        Log.d(TAG,"===== onCreate  =====")

        super.onCreate(savedInstanceState)

        // check auth.currentUser is null , if currentUser is null, send user to re sign-in
        checkUserLoggedIn()

        var receivedCustomer : Customer? = null

        intent.getSerializableExtra("customer")?.let {
            receivedCustomer = it as Customer

            Log.d(TAG,"===== receivedCustomer.lastName | ${receivedCustomer?.lastName}  =====")
            Log.d(TAG,"===== receivedCustomer.firstName | ${receivedCustomer?.firstName}  =====")
            Log.d(TAG,"===== receivedCustomer.email | ${receivedCustomer?.email}  =====")
        }

        binding = DataBindingUtil.setContentView(this,R.layout.activity_drop_pickup)

//        android navigation component pass starting
//        https://stackoverflow.com/a/50457707
        val navHostFragment = myNavHostFragment as NavHostFragment
        val inflater = navHostFragment.navController.navInflater
        val navGraph = inflater.inflate(R.navigation.drop_pickup_navigation)

        val bundle = Bundle()
        bundle.putSerializable("customer",receivedCustomer)

        navHostFragment.navController.setGraph(navGraph, bundle)

        initEncrypSharedPrefs()

        if(!isTeamSignedIn(sharedPrefs)){
            // unauthorized access
            finish()
        }
    }

    override fun onBackPressed() {
        if(isTaskRoot){
            // this is the last activity in the activity stack, but we could still have something in navigation component back stack

            Log.d(TAG, "***** DropPickupActivity: this is the last activity in the activity stack *****" )
            Log.d(TAG, "***** supportFragmentManager.backStackEntryCount : ${supportFragmentManager.backStackEntryCount.toString()} *****" )

            val navBackStackEntry = findNavController(R.id.myNavHostFragment).currentBackStackEntry

            navBackStackEntry?.destination?.let {
                Log.d(TAG, "***** navBackStackEntry?.destination?.id : ${it.id.toString()} *****" )
                Log.d(TAG, "***** R.id.startFragment : ${R.id.startFragment.toString()} *****" )

                if(it.id == R.id.startFragment){
                    // if navBackStackEntry?.destination?.id is same as R.id.startFragment
                    // that means we are looking at the startFragment now

                    // startFragment is the last fragment in the navigation back stack

                    showAlert()
                }else{
                    // if user move to add team fragment from DropPickupActivity, user get out of app when user press back button because navBackStackEntry?.destination? set as teamFrg
                    // so i check navBackStackEntry?.destination? is teamFrg,and if it is, redirect user to DropPickupActivity
                    if(it.id == R.id.teamFrg){
                        val intent = Intent(this, DropPickupActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                    }else{
                        super.onBackPressed()
                    }
                }
            }
        }else{
            // there is more than one activity in the activity stack

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
            R.id.mnManagerConnectStripe -> moveToStripeConnectAct()
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

        val intent = Intent(this,SearchAct::class.java)
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

    fun moveToStripeConnectAct(){
        Log.d(TAG, "***** moveToStripeConnectAct *****")

        val intent = Intent(this, StripeConnectAct::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
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

    fun setActionBarTitle(subTitle: String?) {
        val title = StringBuilder().append(getString(R.string.app_name)).append(" - ").append(subTitle).toString()
        Log.d(TAG,"***** title is ${title} *****")
        supportActionBar?.setTitle(title)
    }

    fun showAlert(){
        val exitAlertBuilder = AlertDialog.Builder(this@DropPickupActivity)
        exitAlertBuilder.setCancelable(false)

        val exitDialogBinding = DataBindingUtil.inflate<ExitDialogDropPickupBinding>(layoutInflater,R.layout.exit_dialog_drop_pickup,null,false)

        exitDialogBinding.act = this

        exitAlertBuilder.setView(exitDialogBinding.root)
        exitAlert = exitAlertBuilder.create()

        exitAlert?.show()
    }

    // called from exit_dialog_drop_pickup.xml
    fun onClickCancelBtn(){
        Log.d(TAG,"***** onClickCancelBtn *****")
        exitAlert?.dismiss()
    }

    // called from exit_dialog_drop_pickup.xml
    fun onClickExitBtn(){
        // user want to exit

        Log.d(TAG,"***** onClickCancelBtn *****")
        finish()
        exitAlert?.dismiss()
    }

}