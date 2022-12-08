package com.centuryprogrammer18thwasentsingleland.login

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.TeamLoginFrgBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.utils.onDone
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class TeamLoginFrg : Fragment() {
    private val TAG = TeamLoginFrg::class.java.simpleName

    private lateinit var binding: TeamLoginFrgBinding

    private lateinit var sharedPrefs : SharedPreferences

    val viewModel: TeamLoginFrgVM by viewModels()

    private lateinit var masterKeyAlias: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as ManagerLoginAct).setActionBarTitle(getString(R.string.team_login))

        binding = DataBindingUtil.inflate(inflater,R.layout.team_login_frg, container, false)
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

        // imeoption done click ref) https://stackoverflow.com/a/59758037/3151712
        binding.etPassworTeamLoginFrg.onDone {
            viewModel.onClickSigninBtn()
        }

        viewModel.signinPassword.observe(viewLifecycleOwner, Observer {
            Log.d(TAG,"===== signinPassword :  '${it}' =====")
            var foundTeamPass =""
            var foundTeamId =""

            if(sharedPrefs.all.values.contains("team_pass_"+it)){
                foundTeamPass = "team_pass_"+it
            }


            if(!foundTeamPass.isEmpty()){
                // shared preference get key by value ref) https://stackoverflow.com/a/12713418/3151712
                for(entry in sharedPrefs.all.entries){
                    if(foundTeamPass.equals(entry.value)){
                        foundTeamId = entry.key
                    }
                }
            }

            Log.d(TAG,"===== foundTeamId :  '${foundTeamId}' =====")

            if(foundTeamId.isEmpty()){

                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.incorrect_password))

                viewModel.password = ""

                // clear field
                binding.etPassworTeamLoginFrg.setText("")
            }else{
                Log.d(TAG,"===== goDropPickActivity  =====")

                sharedPrefs.edit().putString("logged_team_id",foundTeamId.replace("team_id_","")).apply()
                goDropPickActivity()
            }
        })

        val loggedTeamId= sharedPrefs.getString("logged_team_id", null)

        loggedTeamId?.let {
            // there is logged team id, send user to main page

            goDropPickActivity()
        }

        return binding.root
    }

    fun goDropPickActivity(){
        val intent = Intent(requireContext(), DropPickupActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NO_ANIMATION
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)

        startActivity(intent)
    }
}