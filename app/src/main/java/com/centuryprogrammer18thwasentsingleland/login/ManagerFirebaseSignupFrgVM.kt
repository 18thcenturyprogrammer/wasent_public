package com.centuryprogrammer18thwasentsingleland.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R
import com.centuryprogrammer18thwasentsingleland.utils.isValidPassword
import com.centuryprogrammer18thwasentsingleland.utils.validEmailAddress
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth


class ManagerFirebaseSignupFrgVM : ViewModel() {
    private val TAG = ManagerFirebaseSignupFrgVM::class.java.simpleName

    var mAuth = FirebaseAuth.getInstance()


    // one way data binding. receiving data from manager_firebase_signup_frg.xml
    var email: String = ""
    var password1: String = ""
    var password2: String = ""

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    private val _goToInitSetting = MutableLiveData<Boolean>()
    val goToInitSetting : LiveData<Boolean>
        get() = _goToInitSetting

    fun signup(email:String, password: String){
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(object : OnCompleteListener<AuthResult?>{
                override fun onComplete(task: Task<AuthResult?>) {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(TAG, "createUserWithEmail:success");

//                        user =mAuth.getCurrentUser()

                        _goToInitSetting.value = true

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());

                    }
                }
            })
    }


    fun onClickCreateBtn() {
        if(email.isNotEmpty() && password1.isNotEmpty() && password2.isNotEmpty()){
            if(validEmailAddress(email)){
                if(password1.length > 5){
                    if(isValidPassword(password1)){
                        if(password1 == password2){
                            signup(email,password1)
                        }
                        else{
                            _msgToFrg.value = App.resourses!!.getString(R.string.passwords_not_matched)
                        }
                    }else{
                        _msgToFrg.value = App.resourses!!.getString(R.string.password_alpha_numeric_special_character)
                    }
                }else{
                    _msgToFrg.value = App.resourses!!.getString(R.string.password_least_6)
                }
            }else{
                _msgToFrg.value = App.resourses!!.getString(R.string.not_correct_email)
            }
        }else{
            _msgToFrg.value = App.resourses!!.getString(R.string.email_password_cannot_be_empty)
        }
    }
}