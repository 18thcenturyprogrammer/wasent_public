package com.centuryprogrammer18thwasentsingleland.rack

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.inputmethod.InputMethodManager
import androidx.navigation.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.conveyor.ConveyorAct
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.inventory.InventoryAct
import com.centuryprogrammer18thwasentsingleland.login.ManagerLoginAct
import com.centuryprogrammer18thwasentsingleland.login.ReSignInAct
import com.centuryprogrammer18thwasentsingleland.manager.ItemsPricesActivity
import com.centuryprogrammer18thwasentsingleland.release.ReleaseAct
import com.centuryprogrammer18thwasentsingleland.sales.SalesAct
import com.centuryprogrammer18thwasentsingleland.search.SearchAct
import com.centuryprogrammer18thwasentsingleland.utils.isManagerSignedIn
import com.centuryprogrammer18thwasentsingleland.utils.isTeamSignedIn
import com.google.firebase.auth.FirebaseAuth

class RackAct : AppCompatActivity() {
    private val TAG = RackAct::class.java.simpleName

    lateinit var masterKeyAlias :String
    lateinit var sharedPrefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // check auth.currentUser is null , if currentUser is null, send user to re sign-in
        checkUserLoggedIn()

        setActionBarTitle(getString(R.string.rack))

        setContentView(R.layout.act_rack)

        initEncrypSharedPrefs()

        if(!isTeamSignedIn(sharedPrefs)){
            // unauthorized access
            finish()
        }

        //      showing softkeyboard
//        ref) https://stackoverflow.com/a/5617130/3151712
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)


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
        //R.id.myNavHostFragment need to be change, but i won't use this menu anyway
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
}