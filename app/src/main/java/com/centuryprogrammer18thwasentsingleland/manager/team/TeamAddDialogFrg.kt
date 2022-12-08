package com.centuryprogrammer18thwasentsingleland.manager.team

import android.app.AlertDialog
import android.app.Dialog
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.TeamAddDialogFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class TeamAddDialogFrg : DialogFragment() {
    private val TAG = TeamAddDialogFrg::class.java.simpleName

    lateinit var binding : TeamAddDialogFrgBinding

    val viewModel: TeamAddDialogFrgVM by viewModels()

    lateinit var masterKeyAlias :String

    lateinit var sharedPrefs : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        viewModel.updateSharedPrefs(sharedPrefs)

        viewModel.msgToFrg.observe(this, Observer {
            if(it.isNotEmpty()){
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

        viewModel.closeAddTeamAlert.observe(this, Observer {
            if(it){
                dialog!!.dismiss()

                // if i just dismiss dialog, TeamFrg will not show updated teams,
                // so i use navigation, so reload updates
                val action = TeamAddDialogFrgDirections.actionTeamAddDialogFrgToTeamFrg()
                findNavController().navigate(action)
            }
        })

        binding = DataBindingUtil.inflate(layoutInflater,R.layout.team_add_dialog_frg,null,false)

        // two way data binding team_add_dialog_frg.xml
        binding.setLifecycleOwner(this)

        binding.viewModel = viewModel

        val dialogBuilder = AlertDialog.Builder(requireContext())

        dialogBuilder.setMessage(R.string.add_team_member)
        dialogBuilder.setView(binding.root)
        dialogBuilder.setCancelable(false)

        return dialogBuilder.create()
    }

}