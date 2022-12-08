package com.centuryprogrammer18thwasentsingleland.manager.team

import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.data.Team
import com.centuryprogrammer18thwasentsingleland.databinding.TeamFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class TeamFrg : Fragment() {
    private val TAG = TeamFrg::class.java.simpleName

    private lateinit var binding : TeamFrgBinding

    private lateinit var masterKeyAlias :String
    private lateinit var sharedPrefs: SharedPreferences

    val viewModel: TeamFrgVM by viewModels()
    val teams = mutableListOf<Team>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        setActionBarTitle(getString(R.string.team))

        Log.d(TAG,"***** onCreateView *****")
        binding = DataBindingUtil.inflate(inflater,R.layout.team_frg, container, false)
        binding.frg = this

        masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
        sharedPrefs = EncryptedSharedPreferences.create(
            "default_shared",
            masterKeyAlias,
            requireContext(),
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )

        viewModel.updateSharedPrefs(sharedPrefs)
        viewModel.loadTeams()

        val adapter = TeamApt(viewModel)
        binding.rvTeamFrg.adapter = adapter
        binding.rvTeamFrg.layoutManager = GridLayoutManager(requireContext(),2)

        viewModel.teams.observe(viewLifecycleOwner, Observer {
            if(it.count() != 0){
                adapter.submitList(it)
                adapter.notifyDataSetChanged()
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

    override fun onResume() {
        super.onResume()

        Log.d(TAG,"***** onResume *****")

//        viewModel.loadTeams()
    }


    fun onClickAddTeam(){
        Log.d(TAG,"***** onClickAddTeam, move to TeamAddDialogFrg *****")

        val action = TeamFrgDirections.actionTeamFrgToTeamAddDialogFrg()
        findNavController().navigate(action)
    }

    // change action bar title ref) https://stackoverflow.com/a/59978985/3151712
    fun setActionBarTitle(subTitle: String?) {
        val title = StringBuilder().append(getString(R.string.app_name)).append(" - ").append(subTitle).toString()
        Log.d(TAG,"***** title is ${title} *****")
        (activity as? AppCompatActivity)!!.supportActionBar?.title = title
    }


}