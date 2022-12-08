package com.centuryprogrammer18thwasentsingleland.testjacob

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.databinding.ActivityFirebaseLoggedInCheckTestBinding
import com.centuryprogrammer18thwasentsingleland.login.ReSignInAct
import com.centuryprogrammer18thwasentsingleland.utils.showCustomToast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class FirebaseLoggedInCheckTest : AppCompatActivity() {
    private val TAG = FirebaseLoggedInCheckTest::class.java.simpleName

    lateinit var binding : ActivityFirebaseLoggedInCheckTestBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        Log.d(TAG, "***** onCreate *****")
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_firebase_logged_in_check_test)

        binding.act = this

    }

    fun onClickShowUser(){
        Log.d(TAG,"***** onClickShowUser *****")
        var user = FirebaseAuth.getInstance().currentUser

        Log.d(TAG,"***** user | ${user} *****")
        Log.d(TAG,"***** user.email | ${user?.email} *****")
        Log.d(TAG,"***** user.uid | ${user?.uid} *****")
        binding.tvUserFirebaseLoggedInCheckTest.text = user.toString()
        binding.tvUserEmailFirebaseLoggedInCheckTest.text = user?.email.toString()
        binding.tvUserUidFirebaseLoggedInCheckTest.text = user?.uid.toString()
    }

    fun logOutUser(){
        Log.d(TAG,"***** logOutUser *****")
        var auth = FirebaseAuth.getInstance()
        var user = auth.currentUser
        auth.signOut()


        Log.d(TAG,"***** user | ${auth.currentUser} *****")
        Log.d(TAG,"***** user.email | ${auth.currentUser?.email} *****")
        Log.d(TAG,"***** user.uid | ${auth.currentUser?.uid} *****")

        binding.tvUserFirebaseLoggedInCheckTest.text = auth.currentUser.toString()
        binding.tvUserEmailFirebaseLoggedInCheckTest.text = auth.currentUser?.email.toString()
        binding.tvUserUidFirebaseLoggedInCheckTest.text = auth.currentUser?.uid.toString()
    }

    fun onClickLoginBtn(email:EditText,password:EditText){
        var auth = FirebaseAuth.getInstance()
        var email = email.text.toString()
        var password = password.text.toString()
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(object: OnCompleteListener<AuthResult>{
            override fun onComplete(result: Task<AuthResult>) {
                if(result.isSuccessful){
                    binding.tvUserFirebaseLoggedInCheckTest.text = auth.currentUser.toString()
                    binding.tvUserEmailFirebaseLoggedInCheckTest.text = auth.currentUser?.email.toString()
                    binding.tvUserUidFirebaseLoggedInCheckTest.text = auth.currentUser?.uid.toString()

                    Toast(this@FirebaseLoggedInCheckTest).showCustomToast(this@FirebaseLoggedInCheckTest,"loggin success")
                }else{
                    Toast(this@FirebaseLoggedInCheckTest).showCustomToast(this@FirebaseLoggedInCheckTest,"loggin FAILED")

                }
            }
        })

    }

    fun ifSignOutSendTo(){
        Log.d(TAG,"***** ifSignOutSendTo *****")

        var auth = FirebaseAuth.getInstance()
        var user = auth.currentUser

        if(user == null){
            // send user team login fragment through ManagerLoginAct
            val intent = Intent(this, ReSignInAct::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}