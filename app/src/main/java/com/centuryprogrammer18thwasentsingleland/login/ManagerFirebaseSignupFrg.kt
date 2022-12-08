package com.centuryprogrammer18thwasentsingleland.login

import android.os.Bundle
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
import com.centuryprogrammer18thwasentsingleland.databinding.ManagerFirebaseSignupFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast

class ManagerFirebaseSignupFrg : Fragment() {
    private val TAG = ManagerFirebaseSignupFrg::class.java.simpleName

    private lateinit var binding: ManagerFirebaseSignupFrgBinding

    val viewModel: ManagerFirebaseSignupFrgVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as ManagerLoginAct).setActionBarTitle(getString(R.string.manager_signup))

        binding = DataBindingUtil.inflate(inflater, R.layout.manager_firebase_signup_frg, container, false)

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
                val action = ManagerFirebaseSignupFrgDirections.actionManagerFirebaseSignupFrgToInitSettingFrg()
                findNavController().navigate(action)
            }
        })

        return binding.root
    }





}