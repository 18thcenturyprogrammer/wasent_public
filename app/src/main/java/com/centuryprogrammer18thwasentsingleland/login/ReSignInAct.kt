package com.centuryprogrammer18thwasentsingleland.login

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.ActReSignInBinding
import com.centuryprogrammer18thwasentsingleland.drop_pickup.DropPickupActivity
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class ReSignInAct : AppCompatActivity() {
    private val TAG = ReSignInAct::class.java.simpleName

    lateinit var binding : ActReSignInBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "***** onCreate *****")
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.act_re_sign_in)

        binding.act = this

    }



    fun onClickSignInBtn(email: EditText, password: EditText){
        Log.d(TAG,"***** onClickSignInBtn *****")
        Log.d(TAG,"***** email | ${email.text.toString()} *****")
        Log.d(TAG,"***** password | ${password.text.toString()} *****")


        var auth = FirebaseAuth.getInstance()
        var emailStr = email.text.toString()
        var passwordStr = password.text.toString()

        if(emailStr.isNotEmpty() && passwordStr.isNotEmpty() ){
            auth.signInWithEmailAndPassword(emailStr, passwordStr).addOnCompleteListener(object:
                OnCompleteListener<AuthResult> {
                override fun onComplete(result: Task<AuthResult>) {
                    if(result.isSuccessful){
                        Log.d(TAG,"***** sign-in success *****")

                        Toast(this@ReSignInAct).showCustomToast(this@ReSignInAct,getString(R.string.successfully_signed_in))

                        val intent = Intent(this@ReSignInAct, DropPickupActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                        startActivity(intent)
                    }else{
                        Log.d(TAG,"***** sign-in failed *****")

                        Toast(this@ReSignInAct).showCustomToast(this@ReSignInAct,getString(R.string.sign_in_failed))
                    }
                }
            })
        }else{
            Log.d(TAG,"***** user didn't input email or password *****")

            Toast(this@ReSignInAct).showCustomToast(this@ReSignInAct,getString(R.string.empty_not_allowed))
        }



    }

    fun ifSignOutSendTo(){
        Log.d(TAG,"***** ifSignOutSendTo *****")

        var auth = FirebaseAuth.getInstance()
        var user = auth.currentUser

        if(user == null){
            // send user team login fragment through ManagerLoginAct
            val intent = Intent(this, ManagerLoginAct::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}