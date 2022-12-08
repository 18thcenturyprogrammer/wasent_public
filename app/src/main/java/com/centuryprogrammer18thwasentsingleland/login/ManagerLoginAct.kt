package com.centuryprogrammer18thwasentsingleland.login

import android.content.SharedPreferences
import android.os.Bundle

import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.ActLoginBinding


class ManagerLoginAct : AppCompatActivity() {
    private val TAG = ManagerLoginAct::class.java.simpleName

    private lateinit var binding : ActLoginBinding

    //variables will be initialised in the onCreate function
    lateinit var masterKeyAlias: String
    private lateinit var sharedPrefs : SharedPreferences

    private val viewModel:ManagerLoginActVM by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setActionBarTitle(getString(R.string.authentication))

        binding = DataBindingUtil.setContentView(this,R.layout.act_login)
        binding.viewModel = viewModel

        initEncrypSharedPrefs()

        val managerId = sharedPrefs.getString("manager_id",null)
        Log.d(TAG,"===== managerId : '${managerId}' =====")

        if(managerId == null){
            // manager is empty , this app installed and not initialized app

            // programmatically navigation pass parameter ref) https://stackoverflow.com/a/56448153/3151712
            // ref) https://stackoverflow.com/a/54613997/3151712
            // getting basic concept , but don't follow this ref) https://stackoverflow.com/a/53922244/3151712
            val navCont = findNavController(R.id.frgLoginAct)
            navCont.setGraph(R.navigation.manager_firebase_login_nav)
        }else{
            // there is manager id

            Log.d(TAG,"===== there is manager id =====")
            Log.d(TAG,"===== managerId : '${managerId}' =====")

            // programmatically navigation pass parameter ref) https://stackoverflow.com/a/56448153/3151712
            // ref) https://stackoverflow.com/a/54613997/3151712
            // getting basic concept , but don't follow this ref) https://stackoverflow.com/a/53922244/3151712
            val navCont = findNavController(R.id.frgLoginAct)
            navCont.setGraph(R.navigation.team_login_nav)
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