package com.centuryprogrammer18thwasentsingleland.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.centuryprogrammer18thwasentsingleland.App
import com.centuryprogrammer18thwasentsingleland.R

class TeamLoginFrgVM : ViewModel() {
    private val TAG = TeamLoginFrgVM::class.java.simpleName

    // both way data binding
    var password : String = ""

    private val _signinPassword = MutableLiveData<String>()
    val signinPassword : LiveData<String>
        get() = _signinPassword

    private val _msgToFrg = MutableLiveData<String>()
    val msgToFrg : LiveData<String>
        get() = _msgToFrg

    fun onClickSigninBtn(){
        Log.d(TAG,"----- onClickSigninBtn -----")
        Log.d(TAG,"----- password : '${password}' -----")

        if (!password.isEmpty()){
            if(password.length >3){
                _signinPassword.value = password
            }else{
                _msgToFrg.value = App.resourses!!.getString(R.string.team_pass_least_4_chars)
            }
        }else{
            _msgToFrg.value = App.resourses!!.getString(R.string.empty_not_allowed)
        }
        _signinPassword.value = password
    }
}