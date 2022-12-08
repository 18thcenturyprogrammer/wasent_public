package com.centuryprogrammer18thwasentsingleland.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.utils.validEmailAddress
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class ManagerFirebaseLoginFrgVM : ViewModel() {
    private val TAG = ManagerFirebaseLoginFrgVM::class.java.simpleName

    private var mAuth: FirebaseAuth? = FirebaseAuth.getInstance()

    private var currentUser : FirebaseUser? = null

    // one way data binding. receiving data from manager_firebase_login_frg.xml
    var email : String = ""
    var password : String = ""

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _goToInitSetting = MutableLiveData<Boolean>()
    val goToInitSetting : LiveData<Boolean>
        get() = _goToInitSetting

    fun getCurrentUser(){
        // Check if user is signed in (non-null) and update UI accordingly.
        currentUser = mAuth!!.currentUser
    }

    fun signin(email:String, password:String){
        Log.d(TAG,"----- signin called -----")
        mAuth?.signInWithEmailAndPassword(email, password)
            ?.addOnCompleteListener(object : OnCompleteListener<AuthResult> {
                override fun onComplete(task: Task<AuthResult>) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "signInWithEmail:success");

                        _goToInitSetting.value = true

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "signInWithEmail:failure", task.getException());

                        _msgToFrg.value = App.resourses!!.getString(R.string.sign_in_failed)
                    }
                }
            })
    }

    fun onClickOkBtn(){
        Log.d(TAG,"----- onClickOkBtn -----")
        Log.d(TAG,"----- email : '${email}' ----- password : '${password}' -----")

        if(email.isNotEmpty() && password.isNotEmpty() && validEmailAddress(email)){
            signin(email, password)
        }else{
            Log.d(TAG,"----- entered email or password are not correct -----")

            _msgToFrg.value = App.resourses!!.getString(R.string.invalid_input)
        }
    }

}