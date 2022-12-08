package com.centuryprogrammer18thwasentsingleland.login

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
import androidx.navigation.fragment.findNavController
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.ManagerFirebaseLoginFrgBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class ManagerFirebaseLoginFrg : Fragment() {
    private val TAG = ManagerFirebaseLoginFrg::class.java.simpleName

    private lateinit var binding: ManagerFirebaseLoginFrgBinding

    private val viewModel: ManagerFirebaseLoginFrgVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

//        (activity as ManagerLoginAct).setActionBarTitle(getString(R.string.manager_login))
        
        binding = DataBindingUtil.inflate(inflater,R.layout.manager_firebase_login_frg, container, false)
        binding.frg = this
        binding.viewModel = viewModel


        viewModel.msgToFrg.observe(viewLifecycleOwner, Observer {
            if(it.isNotEmpty()){
                // Toast extension function
                Toast(requireContext()).showCustomToast(requireContext(),it)
            }
        })

        viewModel.goToInitSetting.observe(viewLifecycleOwner, Observer {
            if(it){
                val action = ManagerFirebaseLoginFrgDirections.actionManagerFirebaseLoginFrgToInitSettingFrg()
                findNavController().navigate(action)
            }
        })

        return binding.root
    }

    fun onClickCreateBtn() {
        Log.d(TAG,"***** onClickCreateBtn *****")

        val action = ManagerFirebaseLoginFrgDirections.actionManagerFirebaseLoginFrgToManagerFirebaseSignupFrg()
        findNavController().navigate(action)

    }
}