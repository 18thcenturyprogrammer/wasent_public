package com.centuryprogrammer18thwasentsingleland.stripe

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.FirebaseLoginForStripeConnectFrgBinding
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class FirebaseLoginForStripeConnectFrg : Fragment() {
    private val TAG = FirebaseLoginForStripeConnectFrg::class.java.simpleName

    lateinit var binding : FirebaseLoginForStripeConnectFrgBinding

    private val viewModel : FirebaseLoginForStripeConnectFrgVM by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = DataBindingUtil.inflate(inflater,R.layout.firebase_login_for_stripe_connect_frg, container, false)
        binding.frg = this

        return binding.root
    }

    fun onClickLoginBtn(email:EditText,password:EditText){

        Log.d(TAG,"----- onClickLoginBtn -----")

        var auth = FirebaseAuth.getInstance()
        var emailStr = email.text.toString()
        var passwordStr = password.text.toString()

        if(emailStr.isNotEmpty() && passwordStr.isNotEmpty()){
            auth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(object:
                OnCompleteListener<AuthResult> {
                override fun onComplete(result: Task<AuthResult>) {
                    if(result.isSuccessful){
                        Log.d(TAG,"----- firebase login SUCCESS -----")

                        clearOutEditText()

                        val action = FirebaseLoginForStripeConnectFrgDirections.actionFirebaseLoginForStripeConnectFrgToStripeAccountConnectFrg()
                        findNavController().navigate(action)
                    }else{
                        Log.d(TAG,"----- firebase login FAILED -----")
                        Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.login_failed))
                    }
                }
            })
        }else{
            Toast(requireContext()).showCustomToast(requireContext(),getString(R.string.empty_not_allowed))
        }
    }

    fun clearOutEditText(){
        with(binding){
            Log.d(TAG,"----- clear out the EditText email, password -----")
            etEmailFirebaseLoginForStripeConnectFrg.setText("")
            etPasswordFirebaseLoginForStripeConnectFrg.setText("")
        }
    }

}